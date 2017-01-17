package ch.sebastianhaeni.pancake.processor;

import java.util.LinkedList;

import ch.sebastianhaeni.pancake.dto.Tags;
import mpi.MPI;
import mpi.Status;

import static ch.sebastianhaeni.pancake.ParallelSolver.CONTROLLER_RANK;

public class SolveWorker extends Worker {

    @Override
    void work() {
        while (!nodes.isEmpty() && nodes.peek().getGap() > 0 && !status.isDone()) {
            solve();
        }

        if (status.isDone()) {
            return;
        }

        LinkedList[] result = new LinkedList[1];
        result[0] = nodes;
        MPI.COMM_WORLD.Isend(result, 0, 1, MPI.OBJECT, CONTROLLER_RANK, Tags.RESULT.tag());
    }

    private void solve() {
        candidateBound = Integer.MAX_VALUE;

        while (!nodes.isEmpty() && nodes.peek().getGap() > 0 && !status.isDone()) {
            int stateBound = nodes.peek().getGap() + nodes.peek().getDepth();
            if (stateBound > bound) {
                if (stateBound < candidateBound) {
                    candidateBound = stateBound;
                }
                nodes.pop();
            } else if (nodes.peek().getChildren().isEmpty()) {
                if (nodes.peek().getDepth() == 0) {
                    requestWork(0);
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
            requestWork(0);
        }
    }

}
