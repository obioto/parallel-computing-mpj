package ch.sebastianhaeni.pancake.processor.solve;

import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.processor.Worker;
import mpi.MPI;

import java.util.ArrayDeque;

import static ch.sebastianhaeni.pancake.ParallelSolver.CONTROLLER_RANK;

public class SolveWorker extends Worker {

    @Override
    protected void work() {
        if (nodes == null) {
            return;
        }
        while (!nodes.isEmpty() && nodes.peek().getGap() > 0) {
            if (solve()) {
                break;
            }
        }

        ArrayDeque[] result = new ArrayDeque[1];
        result[0] = nodes;
        System.out.format("Worker %d found solution with bound %d\n", MPI.COMM_WORLD.Rank(), bound);
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
                    requestWork(0);
                    nodes.peek().nextNodes();
                } else {
                    nodes.pop();
                }
            } else {
                nodes.push(nodes.peek().getChildren().pop());
                nodes.peek().nextNodes();
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
