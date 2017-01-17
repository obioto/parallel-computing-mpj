package ch.sebastianhaeni.pancake.processor;

import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.util.IntListener;
import mpi.MPI;
import mpi.Status;

import static ch.sebastianhaeni.pancake.ParallelSolver.CONTROLLER_RANK;

public class CountWorker extends Worker {

    private int count;

    @Override
    void work() {
        (new Thread(new IntListener(Tags.GATHER, (source, result) -> {
            status.done();
            int[] countResult = new int[1];
            countResult[0] = count;
            MPI.COMM_WORLD.Reduce(countResult, 0, new int[1], 0, 1, MPI.INT, MPI.SUM, CONTROLLER_RANK);
        }, status, CONTROLLER_RANK, 1))).start();

        while (!nodes.isEmpty()) {
            count();
        }

        int[] result = new int[1];
        result[0] = count;
        MPI.COMM_WORLD.Reduce(result, 0, new int[1], 0, 1, MPI.INT, MPI.SUM, CONTROLLER_RANK);
    }

    private void count() {
        candidateBound = Integer.MAX_VALUE;

        while (!nodes.isEmpty()) {
            if (nodes.peek().getGap() == 0) {
                nodes.pop();
                count++;
                continue;
            }

            int stateBound = nodes.peek().getGap() + nodes.peek().getDepth();
            if (stateBound > bound) {
                if (stateBound < candidateBound && count == 0) {
                    candidateBound = stateBound;
                }
                nodes.pop();
            } else if (nodes.peek().getChildren().isEmpty()) {
                if (nodes.peek().getDepth() == 0) {
                    requestWork(count);
                } else {
                    nodes.pop();
                }
            } else {
                nodes.push(nodes.peek().getChildren().pop());
                nodes.peek().nextNodes();
            }

            Status response;
            if ((response = splitCommand.Test()) != null) {
                splitAndSend(response.source);
                listenToSplit();
            }
        }

        if (nodes.isEmpty() && !status.isDone()) {
            requestWork(count);
        }
    }
}
