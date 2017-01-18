package ch.sebastianhaeni.pancake.processor;

import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.dto.WorkPacket;
import ch.sebastianhaeni.pancake.model.Node;
import ch.sebastianhaeni.pancake.util.Partition;
import mpi.MPI;
import mpi.Request;

import java.util.ArrayDeque;

import static ch.sebastianhaeni.pancake.ParallelSolver.EMPTY_BUFFER;

public abstract class Controller implements IProcessor {

    private static final int INITIAL_WORK_DEPTH = 1000;

    protected final ArrayDeque<Integer> idleWorkers = new ArrayDeque<>(MPI.COMM_WORLD.Size() - 1);
    protected final ArrayDeque<Node> nodes = new ArrayDeque<>();
    protected final int[] workers;
    protected final int workerCount;
    protected final int[] initialState;

    protected int[][] workerData;
    protected Request[] workerListeners;
    protected Request[] workerWorkingListeners;

    private int candidateBound;
    protected int bound = -1;
    protected int lastIncrease = -1;

    protected Controller(int[] initialState, int workerCount) {
        this.initialState = initialState;
        this.workerCount = workerCount;
        this.workers = new int[workerCount];
        this.workerData = new int[workerCount][2];
        this.workerListeners = new Request[workerCount];
        this.workerWorkingListeners = new Request[workerCount];

        for (int i = 0; i < workerCount; i++) {
            this.workers[i] = i + 1;
        }
    }

    @Override
    public void run() {
        initializeListeners();
        try {
            work();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected abstract void work() throws InterruptedException;

    private void initializeListeners() {
        for (int worker : workers) {
            initWorkerListener(worker);
            initWorkingListener(worker);
        }
    }

    protected void initWorkerListener(int worker) {
        workerData[worker - 1] = new int[2];
        workerListeners[worker - 1] = MPI.COMM_WORLD.Irecv(workerData[worker - 1], 0, 2, MPI.INT, worker, Tags.IDLE.tag());
    }

    protected void initWorkingListener(int worker) {
        workerWorkingListeners[worker - 1] = MPI.COMM_WORLD.Irecv(EMPTY_BUFFER, 0, 0, MPI.INT, worker, Tags.WORKING.tag());
    }

    protected void clearListeners() {
        Object[] packetBuf = { new WorkPacket(0, 0) };
        for (int worker : workers) {
            MPI.COMM_WORLD.Send(EMPTY_BUFFER, 0, 0, MPI.INT, worker, Tags.KILL.tag());
            MPI.COMM_WORLD.Isend(packetBuf, 0, 1, MPI.OBJECT, worker, Tags.WORK.tag());
        }
    }

    protected boolean solve() {
        Node root = new Node(initialState);
        root = root.augment();

        nodes.push(root);
        nodes.peek().nextNodes();

        initialWork();

        if (nodes.peek().getGap() == 0) {
            return true;
        }

        System.out.printf("Bound: %d\n", bound);

        Partition partition = new Partition(nodes, workerCount);
        WorkPacket packet = new WorkPacket(bound, candidateBound);
        WorkPacket[] packetBuf = new WorkPacket[1];
        packetBuf[0] = packet;

        for (int i = 0; i < workerCount; i++) {
            packet.setNodes(partition.get(i));
            MPI.COMM_WORLD.Isend(packetBuf, 0, 1, MPI.OBJECT, workers[i], Tags.WORK.tag());
        }

        nodes.clear();

        return false;
    }

    private void initialWork() {
        if (bound < 0) {
            bound = nodes.peek().getGap();
        }
        candidateBound = Integer.MAX_VALUE;

        int i = 0;
        while (nodes.peek().getGap() > 0 && i < INITIAL_WORK_DEPTH) {
            i++;
            int stateBound = nodes.peek().getGap() + nodes.peek().getDepth();
            if (stateBound > bound) {
                if (stateBound < candidateBound) {
                    candidateBound = stateBound;
                }
                nodes.pop();
            } else if (nodes.peek().getChildren().isEmpty()) {
                if (nodes.peek().getDepth() == 0) {
                    bound = candidateBound;
                    candidateBound = Integer.MAX_VALUE;
                    nodes.peek().nextNodes();
                } else {
                    nodes.pop();
                }
            } else {
                nodes.push(nodes.peek().getChildren().pop());
                nodes.peek().nextNodes();
            }
        }
    }

}
