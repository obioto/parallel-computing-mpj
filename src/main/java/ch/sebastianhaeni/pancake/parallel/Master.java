package ch.sebastianhaeni.pancake.parallel;

import ch.sebastianhaeni.pancake.Node;
import ch.sebastianhaeni.pancake.SearchResult;
import mpi.MPI;
import mpi.Request;
import mpi.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

class Master implements IProcessor {

    private static final Logger LOG = LogManager.getLogger("Master");

    private final int[] workers;
    private final int[] initialState;

    Master(int[] initialState, int workerCount) {
        this.initialState = initialState;
        this.workers = new int[workerCount];

        for (int i = 0; i < workerCount; i++) {
            this.workers[i] = i + 1;
        }
    }

    public void run() {
        Node root = new Node(initialState);

        LOG.info("Solving a pancake pile in parallel of height {}.", root.getState().length);

        long start = System.currentTimeMillis();
        Node solution = solve(root);
        long end = System.currentTimeMillis();

        LOG.info("Took {}ms", end - start);

        if (solution == null) {
            LOG.error("No solution found");
            return;
        }

        LOG.info("Found solution after {} flips.", solution.getDepth());

        StringBuilder sb = new StringBuilder();
        Node current = solution;
        while (current.getParent() != null) {
            sb.insert(0, Arrays.toString(current.getParent().getState()) + "\n");
            current = current.getParent();
        }

        System.out.println(sb.toString());
    }

    private Node solve(Node root) {
        if (root.isSolution()) {
            return root;
        }

        Node solutionNode = null;
        int bound = root.getOptimisticDistanceToSolution();
        int maxBound = bound * 10;

        while (solutionNode == null) {
            SearchResult result = search(root, bound);

            if (result.getSolutionNode() != null) {
                solutionNode = result.getSolutionNode();
            }

            if (result.getBound() >= maxBound) {
                return null;
            }

            bound = result.getBound();
        }

        for (int worker : workers) {
            MPI.COMM_WORLD.Isend(new int[0], 0, 0, MPI.INT, worker, ParallelSolver.Tags.FINISH);
        }

        return solutionNode;
    }

    private SearchResult search(Node node, int bound) {
        int newBound = node.getDepth() + node.getOptimisticDistanceToSolution();

        if (newBound > bound) {
            return new SearchResult(newBound);
        }

        if (node.isSolution()) {
            return new SearchResult(node);
        }

        int min = Integer.MAX_VALUE;
        List<Node> successors = node.nextNodes();

        int currentWorker = 0;
        for (Node successor : successors) {
            WorkPacket packet = new WorkPacket(successor, bound);
            int destination = workers[currentWorker % workers.length];
            MPI.COMM_WORLD.Isend(new WorkPacket[]{packet}, 0, 1, MPI.OBJECT, destination, ParallelSolver.Tags.WORK);
            currentWorker++;
        }

        SearchResult[] result = new SearchResult[1];
        Request[] requests = new Request[successors.size()];
        currentWorker = 0;

        for (int i = 0; i < successors.size(); i++) {
            int destination = workers[currentWorker % workers.length];
            requests[i] = MPI.COMM_WORLD.Irecv(result, 0, 1, MPI.OBJECT, destination, ParallelSolver.Tags.RESULT);
            currentWorker++;
        }

        int answers = 0;
        boolean[] handledRequests = new boolean[requests.length];
        while (answers < requests.length) {
            for (int i = 0; i < requests.length; i++) {
                Request request = requests[i];
                Status status;

                if (handledRequests[i] || (status = request.Test()) == null) {
                    continue;
                }

                answers++;
                handledRequests[i] = true;
                LOG.debug("got result from Slave({})", status.source);
                if (result[0].getSolutionNode() != null) {
                    return result[0];
                }

                if (result[0].getBound() < min) {
                    min = result[0].getBound();
                }
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return new SearchResult(min);
    }
}
