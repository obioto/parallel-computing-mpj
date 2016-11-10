package ch.sebastianhaeni.pancake;

import mpi.MPI;
import mpi.Request;
import mpi.Status;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("Duplicates")
public class ParallelSolver {
    private static final int MASTER_RANK = 0;
    static int[] workers = new int[]{1, 2, 3, 4};

    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();

        if (rank == MASTER_RANK) {
            runMaster();
        } else {
            runSlave(rank);
        }

        MPI.Finalize();
    }

    private static void runSlave(int rank) {
        Request finishCommand = MPI.COMM_WORLD.Irecv(new int[0], 0, 0, MPI.INT, MASTER_RANK, Tags.FINISH);

        outer:
        while (finishCommand.Test() == null) {
            WorkPacket[] work = new WorkPacket[1];
            Request workRequest = MPI.COMM_WORLD.Irecv(work, 0, 1, MPI.OBJECT, MASTER_RANK, Tags.WORK);

            while (workRequest.Test() == null) {
                if (finishCommand.Test() != null) {
                    break outer;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

//            System.out.format("%d just got some work from master\n", rank);

            Node node = work[0].getNode();
            int bound = work[0].getBound();

            SearchResult result = compute(node, bound);
            sendResult(result);
        }

//        System.out.format("%d is done for the day\n", rank);
    }

    private static SearchResult compute(Node node, int bound) {
        int newBound = node.getFlipCount() + node.getOptimisticDistanceToSolution();

        if (newBound > bound) {
            return new SearchResult(newBound);
        }

        if (node.isSolution()) {
            return new SearchResult(node);
        }

        int min = Integer.MAX_VALUE;
        List<Node> successors = node.nextNodes();

        for (Node successor : successors) {
            SearchResult result = compute(successor, bound);

            if (result.solutionNode != null) {
                return result;
            }

            if (result.bound < min) {
                min = result.bound;
            }
        }

        return new SearchResult(min);
    }

    private static void sendResult(SearchResult searchResult) {
        MPI.COMM_WORLD.Isend(new SearchResult[]{searchResult}, 0, 1, MPI.OBJECT, MASTER_RANK, Tags.RESULT);
    }

    private static void runMaster() {
//        Node root = new Node(new int[]{39,47,13,46,11,43,49,14,7,57,59,42,53,37,55,20,28,33,5,27,34,40,19,52,10,35,8,31,48,30,22,45,12,24,32,36,38,17,50,54,41,60,26,4,2,9,58,23,44,1,6,15,16,29,51,18,21,3,56,25});
        Node root = new Node(new int[]{17, 2, 5, 13, 3, 7, 8, 4, 20, 11, 9, 18, 6, 14, 1, 10, 16, 15, 12, 19});
//        Node root = new Node(new int[]{5, 2, 7, 10, 13, 16, 14, 6, 8, 18, 15, 11, 1, 12, 3, 4, 9, 17});
//        Node root = new Node(new int[]{2, 1, 5, 4, 3});

        System.out.format("Solving a pancake pile in parallel of height %d.\n", root.getPancakes().length);

        long start = System.currentTimeMillis();
        Node solution = solve(root);
        long end = System.currentTimeMillis();

        System.out.format("%dms passed\n", (end - start));

        if (solution == null) {
            System.out.println("No solution found");
            return;
        }
        System.out.format("Found solution after %d flips.\n", solution.getFlipCount());

        StringBuilder sb = new StringBuilder();
        Node current = solution;
        while (current.getParent() != null) {
            sb.insert(0, Arrays.toString(current.getParent().getPancakes()) + "\n");
            current = current.getParent();
        }

        System.out.println(sb.toString());
    }

    private static Node solve(Node root) {
        if (root.isSolution()) {
            return root;
        }

        Node solutionNode = null;
        int bound = root.getOptimisticDistanceToSolution();
        int maxBound = bound * 10;

        while (solutionNode == null) {
            SearchResult result = search(root, bound);

            if (result.solutionNode != null) {
                solutionNode = result.solutionNode;
            }

            if (result.bound >= maxBound) {
                return null;
            }

            bound = result.bound;
        }

        for (int worker : workers) {
            MPI.COMM_WORLD.Isend(new int[0], 0, 0, MPI.INT, worker, Tags.FINISH);
        }

        return solutionNode;
    }

    private static SearchResult search(Node node, int bound) {
        int newBound = node.getFlipCount() + node.getOptimisticDistanceToSolution();

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
            MPI.COMM_WORLD.Isend(new WorkPacket[]{packet}, 0, 1, MPI.OBJECT, destination, Tags.WORK);
            currentWorker++;
        }

        SearchResult[] result = new SearchResult[1];
        Request[] requests = new Request[successors.size()];
        currentWorker = 0;

        for (int i = 0; i < successors.size(); i++) {
            int destination = workers[currentWorker % workers.length];
            requests[i] = MPI.COMM_WORLD.Irecv(result, 0, 1, MPI.OBJECT, destination, Tags.RESULT);
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
//                System.out.format("Master got result from %d\n", status.source);
                if (result[0].solutionNode != null) {
                    return result[0];
                }

                if (result[0].bound < min) {
                    min = result[0].bound;
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

    private static class Tags {
        static final int WORK = 1;
        static final int RESULT = 2;
        static final int FINISH = 3;
    }
}
