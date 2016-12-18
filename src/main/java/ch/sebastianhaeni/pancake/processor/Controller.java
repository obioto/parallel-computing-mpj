package ch.sebastianhaeni.pancake.processor;

import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.dto.WorkPacket;
import ch.sebastianhaeni.pancake.model.Node;
import ch.sebastianhaeni.pancake.util.IntListener;
import ch.sebastianhaeni.pancake.util.Partition;
import ch.sebastianhaeni.pancake.util.Status;
import mpi.MPI;

import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import static ch.sebastianhaeni.pancake.ParallelSolver.EMPTY_BUFFER;
import static ch.sebastianhaeni.pancake.util.Output.showSolution;

public class Controller implements IProcessor {

    private static final int INITIAL_WORK_DEPTH = 1000;

    private final LinkedBlockingQueue<Integer> idleWorkers = new LinkedBlockingQueue<>(MPI.COMM_WORLD.Size() - 1);
    private final Stack<Node> stack = new Stack<>();
    private final int[] workers;
    private final int workerCount;
    private final int[] initialState;
    private final Status status = new Status();

    private int candidateBound;
    private int bound = -1;
    private int lastIncrease = -1;

    public Controller(int[] initialState, int workerCount) {
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
        System.out.printf("Solving a pancake pile in parallel of height %s.\n", initialState.length);

        // Start solving
        long start = System.currentTimeMillis();

        if (solve()) {
            long end = System.currentTimeMillis();
            finish(stack, end - start);
            return;
        }

        Stack<Node>[] solution = new Stack[1];
        MPI.COMM_WORLD.Recv(solution, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, Tags.RESULT.tag());
        status.done();

        long end = System.currentTimeMillis();
        // End solving

        finish(solution[0], end - start);
    }

    private boolean solve() {
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

    private void initializeListeners() {
        for (int worker : workers) {
            (new Thread(new IntListener(Tags.IDLE, this::handleIdle, status, worker))).start();
        }
    }

    private void handleIdle(int source, int result) {
        idleWorkers.add(source);

        if (result > bound) {
            bound = result;
        }

        if (idleWorkers.size() == workerCount) {
            idleWorkers.clear();

            if (lastIncrease == bound) {
                return;
            }
            lastIncrease = bound;
            (new Thread(this::solve)).start();
        }
    }

    private void finish(Stack<Node> solution, long millis) {
        showSolution(solution, millis);

        clearListeners();
    }

    private void clearListeners() {
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
