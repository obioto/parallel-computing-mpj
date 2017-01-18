package ch.sebastianhaeni.pancake.processor.solve;

import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.processor.Worker;
import mpi.MPI;
import mpi.Status;

import java.util.ArrayDeque;

import static ch.sebastianhaeni.pancake.ParallelSolver.CONTROLLER_RANK;

public class SolveWorker extends Worker {

    @Override
    protected void work() {
        while (!nodes.isEmpty() && nodes.peek().getGap() > 0) {
            if (solve()) {
                break;
            }
        }

        ArrayDeque[] result = new ArrayDeque[1];
        result[0] = nodes;
        MPI.COMM_WORLD.Isend(result, 0, 1, MPI.OBJECT, CONTROLLER_RANK, Tags.RESULT.tag());
    }

    private boolean solve() {
        candidateBound = Integer.MAX_VALUE;

        while (nodes.peek().getGap() > 0) {
            int stateBound = nodes.peek().getGap() + nodes.peek().getDepth();
            if (stateBound > bound) {
                if (stateBound < candidateBound) {
                    candidateBound = stateBound;
                }
                nodes.pop();
            } else if (nodes.peek().getChildren().isEmpty()) {
                if (nodes.peek().getDepth() == 0) {
                    if (candidateBound == Integer.MAX_VALUE) {
                        System.out.println(nodes);
                        candidateBound = bound;
                    }
                    requestWork(0);
                    nodes.peek().nextNodes();
                } else {
                    nodes.pop();
                }
            } else {
                nodes.push(nodes.peek().getChildren().pop());
                nodes.peek().nextNodes();
            }

            Status response;
            if ((response = splitCommand.Test()) != null) {
                System.out.format("Worker %d sending to %d\n", MPI.COMM_WORLD.Rank(), response.source);
                splitAndSend(response.source);
                listenToSplit();
            }

            if (killCommand.Test() != null) {
                return true;
            }
        }

        if (nodes.isEmpty()) {
            requestWork(0);
        }

        return false;
    }

}
