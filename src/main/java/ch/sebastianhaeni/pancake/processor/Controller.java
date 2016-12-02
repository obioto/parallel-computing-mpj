package ch.sebastianhaeni.pancake.processor;

import java.util.Arrays;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.primitives.Ints;

import ch.sebastianhaeni.pancake.dto.Node;
import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.dto.WorkPacket;
import ch.sebastianhaeni.pancake.util.IntListener;
import ch.sebastianhaeni.pancake.util.Partition;
import ch.sebastianhaeni.pancake.util.Status;
import mpi.MPI;

public class Controller implements IProcessor {

    private static final Logger LOG = LogManager.getLogger("Controller");
    private static final int INITIAL_WORK_DEPTH = 20;

    private final LinkedBlockingQueue<Integer> idleWorkers = new LinkedBlockingQueue<>(MPI.COMM_WORLD.Size() - 1);
    private final Stack<Node> stack = new Stack<>();
    private final int[] workers;
    private final int workerCount;
    private final int[] initialState;
    private final Status status = new Status();

    private int candidateBound;
    private int bound;

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
        LOG.info("Solving a pancake pile in parallel of height {}.", initialState.length);

        // Start solving
        long start = System.currentTimeMillis();

        solve();

        //noinspection unchecked
        Stack<Node>[] solution = new Stack[1];
        MPI.COMM_WORLD.Recv(solution, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, Tags.RESULT.tag());
        status.done();

        long end = System.currentTimeMillis();
        // End solving

        displaySolution(solution[0], end - start);
    }

    private void solve() {
        Node root = new Node(initialState);
        root.augment();

        stack.push(root);
        stack.peek().nextNodes();

        initialWork();

        LOG.info("Done some initial work with stack size of {}", stack.size());

        Partition partition = new Partition(stack, workerCount);
        WorkPacket packet = new WorkPacket(bound, candidateBound);
        WorkPacket[] packetBuf = new WorkPacket[1];
        packetBuf[0] = packet;

        for (int i = 0; i < workerCount; i++) {
            packet.setStack(partition.get(i));
            LOG.info("sending stack of size {} to {}", packet.getStack().size(), workers[i]);
            MPI.COMM_WORLD.Isend(packetBuf, 0, 1, MPI.OBJECT, workers[i], Tags.WORK.tag());
        }
    }

    private void initializeListeners() {
        IntListener.create(Tags.IDLE, this::handleIdleWorker, status);
        IntListener.create(Tags.WORKING, (source, result) -> idleWorkers.remove(source), status);
    }

    private void handleIdleWorker(int source, int result) {
        idleWorkers.add(source);

        if (idleWorkers.size() == workerCount) {
            increaseBound();
            return;
        }

        candidateBound = result < candidateBound ? result : candidateBound;

        int friendWorker = workers[(Ints.indexOf(workers, source) + 1) % workerCount];

        if (!idleWorkers.contains(friendWorker)) {
            int[] receiver = new int[1];
            receiver[0] = source;
            MPI.COMM_WORLD.Isend(receiver, 0, 1, MPI.INT, friendWorker, Tags.SPLIT.tag());
        }
    }

    private void increaseBound() {
        LOG.info("Increasing bound to {}", candidateBound);
        solve();
    }

    private void displaySolution(Stack<Node> solution, long millis) {
        long second = (millis / 1000) % 60;
        long minute = (millis / (1000 * 60)) % 60;
        long hour = (millis / (1000 * 60 * 60)) % 24;

        String time = String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);

        LOG.info("Took {}", time);
        LOG.info("Found solution after {} flips.", solution.size());

        StringBuilder sb = new StringBuilder();

        while (!solution.isEmpty()) {
            sb.insert(0, Arrays.toString(solution.pop().getState()) + '\n');
        }

        System.out.println(sb.toString());
    }

    private void initialWork() {
        bound = stack.peek().getDistance();
        candidateBound = Integer.MAX_VALUE;

        int i = 0;
        while (stack.peek().getDistance() != 0 && i < INITIAL_WORK_DEPTH) {
            i++;
            if (stack.peek().getDistance() + stack.peek().getDepth() > bound) {
                int stateBound = stack.peek().getDepth() + stack.peek().getDistance();
                candidateBound = stateBound < candidateBound ? stateBound : candidateBound;
                stack.pop();
            } else if (stack.peek().getChildren().empty()) {
                if (stack.peek().getDepth() == 0) {
                    bound = candidateBound;
                    LOG.info("Searching with bound {}", candidateBound);
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
