package ch.sebastianhaeni.pancake.processor;

import ch.sebastianhaeni.pancake.ParallelSolver;
import ch.sebastianhaeni.pancake.dto.Node;
import ch.sebastianhaeni.pancake.dto.SearchResult;
import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.dto.WorkPacket;
import ch.sebastianhaeni.pancake.listener.ExcessListener;
import ch.sebastianhaeni.pancake.listener.IdleListener;
import ch.sebastianhaeni.pancake.listener.PancakeListener;
import ch.sebastianhaeni.pancake.listener.ResultListener;
import mpi.MPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Controller implements IProcessor {

    private static final Logger LOG = LogManager.getLogger("Controller");

    private final int[] workers;
    private final int[] initialState;
    private final LinkedBlockingQueue<Integer> idleWorkers = new LinkedBlockingQueue<>(MPI.COMM_WORLD.Size() - 1);
    private final LinkedBlockingQueue<WorkPacket> outstandingWork = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<SearchResult> results = new LinkedBlockingQueue<>();
    private final ArrayList<PancakeListener> listeners = new ArrayList<>();
    private Distributor distributor;

    public Controller(int[] initialState, int workerCount) {
        this.initialState = initialState;
        this.workers = new int[workerCount];

        for (int i = 0; i < workerCount; i++) {
            this.workers[i] = i + 1;
        }

        this.distributor = new Distributor(idleWorkers, outstandingWork, workers);
    }

    public void run() {
        initializeListeners();

        Node root = new Node(initialState);

        LOG.info("Solving a pancake pile in parallel of height {}.", root.getState().length);

        Node solution = null;
        long start = System.currentTimeMillis();
        try {
            solution = solve(root);
        } catch (InterruptedException e) {
            LOG.error("Exception while solving", e);
        }
        long end = System.currentTimeMillis();

        cleanUp();

        displaySolution(solution, end - start);
    }

    private void cleanUp() {
    	outstandingWork.clear();
        distributor.stopDistributing();

        for (int worker : workers) {
            MPI.COMM_WORLD.Isend(ParallelSolver.EMPTY_BUFFER, 0, 0, MPI.INT, worker, Tags.KILL.tag());
        }
    }

    private void initializeListeners() {
        for (int worker : workers) {
            IdleListener idleListener = new IdleListener(worker, idleWorkers);
            ResultListener resultListener = new ResultListener(worker, results);
            ExcessListener excessListener = new ExcessListener(worker, outstandingWork);

            idleListener.start();
            resultListener.start();
            excessListener.start();

            listeners.add(idleListener);
            listeners.add(resultListener);
            listeners.add(excessListener);
        }
    }

    private void displaySolution(Node solution, long millis) {
        long second = (millis / 1000) % 60;
        long minute = (millis / (1000 * 60)) % 60;
        long hour = (millis / (1000 * 60 * 60)) % 24;

        String time = String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);
        LOG.info("Took {}", time);

        if (solution == null) {
            LOG.error("No solution found");
            return;
        }

        LOG.info("Found solution after {} flips.", solution.getDepth());

        listeners.forEach(PancakeListener::stopListening);

        StringBuilder sb = new StringBuilder();
        Node current = solution;
        Node previous = null;

        while (current != null) {
            StringBuilder currentSb = new StringBuilder();
            int[] state = current.getState();
            for (int i = 0; i < state.length; i++) {
                int pancake = state[i];
                currentSb.append(String.format("%02d", pancake));
                if (previous != null && previous.getFlipPosition() - 1 == i) {
                    currentSb.append("|");
                } else {
                    currentSb.append(" ");
                }
            }

            sb.insert(0, currentSb.toString() + "\n");
            previous = current;
            current = current.getParent();
        }

        System.out.println(sb.toString());
    }

    private Node solve(Node root) throws InterruptedException {
        if (root.isSolution()) {
            return root;
        }

        Node solutionNode = null;
        int bound = root.getOptimisticDistanceToSolution();
        int maxBound = bound * 2;

        while (solutionNode == null) {
            LOG.info("Searching with bound {}", bound);
            SearchResult result = search(root, bound);

            if (result.getSolutionNode() != null) {
                solutionNode = result.getSolutionNode();
            }

            if (result.getBound() >= maxBound) {
                LOG.error("Max bound of {} reached by {}", maxBound, result.getBound());
                return null;
            }

            if (bound >= result.getBound()) {
                bound++;
            } else {
                bound = result.getBound();
            }
        }

        return solutionNode;
    }

    private SearchResult search(Node node, int bound) throws InterruptedException {
        int newBound = node.getDepth() + node.getOptimisticDistanceToSolution();

        if (newBound > bound) {
            return new SearchResult(newBound);
        }

        if (node.isSolution()) {
            return new SearchResult(node);
        }

        List<Node> successors = node.nextNodes();

        outstandingWork.addAll(successors.stream()
                .map(successor -> new WorkPacket(successor, bound))
                .collect(Collectors.toList()));

        if (!distributor.isStarted()) {
            distributor.start();
        }

        SearchResult result;
        int min = Integer.MAX_VALUE;

        while ((result = results.poll(1, TimeUnit.SECONDS)) != null
                || min == Integer.MAX_VALUE
                || (workers.length - idleWorkers.size() > 0)) {

            if (result == null) {
                continue;
            }
            if (result.getSolutionNode() != null) {
                return result;
            }
            if (result.getBound() < min) {
                min = result.getBound();
            }
        }

        return new SearchResult(min);
    }
}
