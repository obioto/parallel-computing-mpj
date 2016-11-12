package ch.sebastianhaeni.pancake.parallel;

import ch.sebastianhaeni.pancake.Node;
import ch.sebastianhaeni.pancake.SearchResult;
import mpi.MPI;
import mpi.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

class Slave implements IProcessor {

    private Logger LOG;
    private Request splitCommand;

    Slave(int rank) {
        LOG = LogManager.getLogger("Slave(" + rank + ")");
    }

    public void run() {
        Request finishCommand = MPI.COMM_WORLD.Irecv(new int[0], 0, 0, MPI.INT, ParallelSolver.MASTER_RANK, ParallelSolver.Tags.FINISH);
        listenToSplitCommand();

        outer:
        while (finishCommand.Test() == null) {
            WorkPacket[] work = new WorkPacket[1];

            Request workRequest = MPI.COMM_WORLD.Irecv(work, 0, 1, MPI.OBJECT, ParallelSolver.MASTER_RANK, ParallelSolver.Tags.WORK);

            long start = System.currentTimeMillis();

            boolean idleSent = false;
            while (workRequest.Test() == null) {
                if (!idleSent) {
                    MPI.COMM_WORLD.Isend(new int[0], 0, 1, MPI.INT, ParallelSolver.MASTER_RANK, ParallelSolver.Tags.IDLE);
                    idleSent = true;
                }
                if (finishCommand.Test() != null) {
                    break outer;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            long end = System.currentTimeMillis();
            LOG.debug("got some work from master, waited for {}ms", end - start);

            Node node = work[0].getNode();
            int bound = work[0].getBound();

            SearchResult result = compute(node, bound);
            sendResult(result);
        }

        LOG.info("done for the day");
    }

    private void listenToSplitCommand() {
        splitCommand = MPI.COMM_WORLD.Irecv(new int[0], 0, 0, MPI.INT, ParallelSolver.MASTER_RANK, ParallelSolver.Tags.SPLIT);
    }

    private SearchResult compute(Node node, int bound) {
        int newBound = node.getDepth() + node.getOptimisticDistanceToSolution();

        if (newBound > bound) {
            return new SearchResult(newBound);
        }

        if (node.isSolution()) {
            return new SearchResult(node);
        }

        int min = Integer.MAX_VALUE;
        List<Node> successors = node.nextNodes();

        if (splitCommand != null && splitCommand.Test() != null) {

            WorkPacket[] split = new WorkPacket[successors.size() / 2];
            for (int i = 0; i < successors.size() / 2; i++) {
                split[i] = new WorkPacket(successors.get(0), bound);
                successors.remove(0);
            }

            LOG.debug("Sending split work {}", Arrays.toString(split));
            MPI.COMM_WORLD.Isend(split, 0, split.length, MPI.OBJECT, ParallelSolver.MASTER_RANK, ParallelSolver.Tags.EXCESS);
        }

        for (Node successor : successors) {
            SearchResult result = compute(successor, bound);

            if (result.getSolutionNode() != null) {
                return result;
            }

            if (result.getBound() < min) {
                min = result.getBound();
            }
        }

        return new SearchResult(min);
    }

    private void sendResult(SearchResult searchResult) {
        MPI.COMM_WORLD.Isend(new SearchResult[]{searchResult}, 0, 1, MPI.OBJECT, ParallelSolver.MASTER_RANK, ParallelSolver.Tags.RESULT);
    }
}
