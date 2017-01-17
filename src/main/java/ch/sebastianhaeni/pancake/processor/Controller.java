package ch.sebastianhaeni.pancake.processor;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.dto.WorkPacket;
import ch.sebastianhaeni.pancake.model.Node;
import ch.sebastianhaeni.pancake.util.Partition;
import ch.sebastianhaeni.pancake.util.Status;
import mpi.MPI;

import static ch.sebastianhaeni.pancake.ParallelSolver.EMPTY_BUFFER;

public abstract class Controller implements IProcessor {

    private static final int INITIAL_WORK_DEPTH = 1000;

    final LinkedBlockingQueue<Integer> idleWorkers = new LinkedBlockingQueue<>(MPI.COMM_WORLD.Size() - 1);
    final LinkedList<Node> nodes = new LinkedList<>();
    final int[] workers;
    final int workerCount;
    final int[] initialState;
    final Status status = new Status();

    private int candidateBound;
    int bound = -1;
    int lastIncrease = -1;

    Controller(int[] initialState, int workerCount) {
        this.initialState = initialState;
        this.workerCount = workerCount;
        this.workers = new int[workerCount];

        for (int i = 0; i < workerCount; i++) {
            this.workers[i] = i + 1;
        }
    }

    @Override
    public void run() {
        initializeListeners();
        work();
    }

    abstract void work();

    abstract void handleIdle(int source, int[] result);

    abstract void initializeListeners();

    void clearListeners() {
        Object[] packetBuf = { new WorkPacket(0, 0) };
        try {
            for (int worker : workers) {
                MPI.COMM_WORLD.Send(EMPTY_BUFFER, 0, 0, MPI.INT, worker, Tags.KILL.tag());
                Thread.sleep(100); // just for good measure
                MPI.COMM_WORLD.Isend(packetBuf, 0, 1, MPI.OBJECT, worker, Tags.WORK.tag());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    boolean solve() {
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
