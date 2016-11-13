package ch.sebastianhaeni.pancake.processor;

import ch.sebastianhaeni.pancake.ParallelSolver;
import ch.sebastianhaeni.pancake.dto.Node;
import ch.sebastianhaeni.pancake.dto.SearchResult;
import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.dto.WorkPacket;
import mpi.MPI;
import mpi.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.util.List;

import static ch.sebastianhaeni.pancake.ParallelSolver.SLEEP_MILLIS;

public class Worker implements IProcessor {

    private Logger LOG;
    private Request splitCommand;
    private long idleTime = 0;
    private Request killCommand;
    private int computeCount;

    public Worker(int rank) {
        LOG = LogManager.getLogger("Worker(" + rank + ")");
    }

    public void run() {
        killCommand = MPI.COMM_WORLD.Irecv(ParallelSolver.EMPTY_BUFFER, 0, 0, MPI.INT, ParallelSolver.CONTROLLER_RANK, Tags.KILL.tag());
        listenToSplitCommand();

        long startRun = System.currentTimeMillis();

        while (killCommand.Test() == null) {
            WorkPacket work = getWork();

            if (work == null) return;

            Node node = work.getNode();
            int bound = work.getBound();

            SearchResult result = compute(node, bound);
            sendResult(result);
        }

        long endRun = System.currentTimeMillis();
        long passedTime = endRun - startRun;
        DecimalFormat df = new DecimalFormat("###.##");

        String timeUse = df.format((1f - ((float) idleTime / passedTime)) * 100);

        LOG.info("Done. Computed {} nodes. Efficiency: {}%", computeCount, timeUse);
    }

    private WorkPacket getWork() {
        WorkPacket[] work = new WorkPacket[1];

        MPI.COMM_WORLD.Isend(ParallelSolver.EMPTY_BUFFER, 0, 0, MPI.INT, ParallelSolver.CONTROLLER_RANK, Tags.IDLE.tag());

        long start = System.currentTimeMillis();
        Request workRequest = MPI.COMM_WORLD.Irecv(work, 0, 1, MPI.OBJECT, ParallelSolver.CONTROLLER_RANK, Tags.WORK.tag());
        while (workRequest.Test() == null) {
            if (killCommand.Test() != null) {
                return null;
            }
            try {
                Thread.sleep(SLEEP_MILLIS);
            } catch (InterruptedException e) {
                LOG.error("Interrupted", e);
            }
        }
        long end = System.currentTimeMillis();

        long currentIdleTime = end - start;
        idleTime += currentIdleTime;
        if (currentIdleTime > 5) {
            LOG.warn("waited for {}ms", currentIdleTime);
        }
        return work[0];
    }

    private void listenToSplitCommand() {
        splitCommand = MPI.COMM_WORLD.Irecv(ParallelSolver.EMPTY_BUFFER, 0, 0, MPI.INT, ParallelSolver.CONTROLLER_RANK, Tags.SPLIT.tag());
    }

    private SearchResult compute(Node node, int bound) {
        computeCount++;
        int newBound = node.getDepth() + node.getOptimisticDistanceToSolution();

        if (newBound > bound) {
            return new SearchResult(newBound);
        }

        if (node.isSolution()) {
            return new SearchResult(node);
        }

        List<Node> successors = node.nextNodes();

        if (splitCommand != null && splitCommand.Test() != null) {
            splitWork(bound, successors);
        }

        int min = Integer.MAX_VALUE;
        for (int i = 0; i < successors.size(); i++) {

            if (splitCommand != null && splitCommand.Test() != null) {
                successors = successors.subList(i, successors.size());
                splitWork(bound, successors);
                i = 0;
            }
            
            if (killCommand.Test() != null) {
                return null;
            }

            Node successor = successors.get(i);
            SearchResult result = compute(successor, bound);
            if (result == null) {
                return null;
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

    private void splitWork(int bound, List<Node> successors) {
        listenToSplitCommand();

        int halfSize = successors.size() / 2;
        WorkPacket[] splitBuffer = new WorkPacket[halfSize];
        for (int i = 0; i < halfSize; i++) {
            splitBuffer[i] = new WorkPacket(successors.get(0), bound);
            successors.remove(0);
        }

        int[] lengthBuf = new int[1];
        lengthBuf[0] = splitBuffer.length;
        MPI.COMM_WORLD.Send(lengthBuf, 0, 1, MPI.INT, ParallelSolver.CONTROLLER_RANK, Tags.EXCESS_LENGTH.tag());
        MPI.COMM_WORLD.Isend(splitBuffer, 0, splitBuffer.length, MPI.OBJECT, ParallelSolver.CONTROLLER_RANK, Tags.EXCESS.tag());
    }

    private void sendResult(SearchResult searchResult) {
        if (searchResult == null) {
            // it's null, guess we got the kill command
            return;
        }
        MPI.COMM_WORLD.Isend(new SearchResult[]{searchResult}, 0, 1, MPI.OBJECT, ParallelSolver.CONTROLLER_RANK, Tags.RESULT.tag());
    }
}
