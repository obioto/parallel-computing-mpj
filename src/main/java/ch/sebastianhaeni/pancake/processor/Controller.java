package ch.sebastianhaeni.pancake.processor;

import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.dto.WorkPacket;
import ch.sebastianhaeni.pancake.model.Node;
import ch.sebastianhaeni.pancake.util.Partition;
import ch.sebastianhaeni.pancake.util.Status;
import mpi.MPI;

import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import static ch.sebastianhaeni.pancake.ParallelSolver.EMPTY_BUFFER;

public abstract class Controller implements IProcessor {

    private static final int INITIAL_WORK_DEPTH = 1000;

    final LinkedBlockingQueue<Integer> idleWorkers = new LinkedBlockingQueue<>(MPI.COMM_WORLD.Size() - 1);
    final Stack<Node> stack = new Stack<>();
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
        Object[] packetBuf = new Object[]{new WorkPacket(0, 0)};
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

        stack.push(root);
        stack.peek().nextNodes();

        initialWork();

        if (stack.peek().getGap() == 0) {
            return true;
        }

        System.out.printf("Bound: %d\n", bound);

        Partition partition = new Partition(stack, workerCount);
        WorkPacket packet = new WorkPacket(bound, candidateBound);
        WorkPacket[] packetBuf = new WorkPacket[1];
        packetBuf[0] = packet;

        for (int i = 0; i < workerCount; i++) {
            packet.setStack(partition.get(i));
            MPI.COMM_WORLD.Isend(packetBuf, 0, 1, MPI.OBJECT, workers[i], Tags.WORK.tag());
        }

        stack.clear();

        return false;
    }

    private void initialWork() {
        if (bound < 0) {
            bound = stack.peek().getGap();
        }
        candidateBound = Integer.MAX_VALUE;

        int i = 0;
        while (stack.peek().getGap() > 0 && i < INITIAL_WORK_DEPTH) {
            i++;
            int stateBound = stack.peek().getGap() + stack.peek().getDepth();
            if (stateBound > bound) {
                if (stateBound < candidateBound) {
                    candidateBound = stateBound;
                }
                stack.pop();
            } else if (stack.peek().getChildren().empty()) {
                if (stack.peek().getDepth() == 0) {
                    bound = candidateBound;
                    candidateBound = Integer.MAX_VALUE;
                    stack.peek().nextNodes();
                } else {
                    stack.pop();
                }
            } else {
                stack.push(stack.peek().getChildren().pop());
                stack.peek().nextNodes();
            }
        }
    }

}
