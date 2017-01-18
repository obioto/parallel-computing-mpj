package ch.sebastianhaeni.pancake.processor.count;

import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.processor.Worker;
import mpi.MPI;
import mpi.Request;
import mpi.Status;

import static ch.sebastianhaeni.pancake.ParallelSolver.CONTROLLER_RANK;
import static ch.sebastianhaeni.pancake.ParallelSolver.EMPTY_BUFFER;

public class CountWorker extends Worker {

    private int count;
    private Request gatherCommand;

    @Override
    protected void work() {
        gatherCommand = MPI.COMM_WORLD.Irecv(EMPTY_BUFFER, 0, 0, MPI.INT, CONTROLLER_RANK, Tags.GATHER.tag());

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

            if (gatherCommand.Test() != null) {
                int[] countResult = new int[1];
                countResult[0] = count;
                MPI.COMM_WORLD.Reduce(countResult, 0, new int[1], 0, 1, MPI.INT, MPI.SUM, CONTROLLER_RANK);
                return;
            }
            if (killCommand.Test() != null) {
                return;
            }
        }

        if (nodes.isEmpty()) {
            requestWork(count);
        }
    }
}
