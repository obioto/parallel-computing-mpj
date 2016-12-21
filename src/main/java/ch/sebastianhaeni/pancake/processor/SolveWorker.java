package ch.sebastianhaeni.pancake.processor;

import ch.sebastianhaeni.pancake.dto.Tags;
import mpi.MPI;

import java.util.Stack;

import static ch.sebastianhaeni.pancake.ParallelSolver.CONTROLLER_RANK;

public class SolveWorker extends Worker {

    @Override
    void work() {
        while (!stack.isEmpty() && stack.peek().getGap() > 0 && !status.isDone()) {
            solve();
        }

        if (status.isDone()) {
            return;
        }

        Stack[] result = new Stack[1];
        result[0] = stack;
        MPI.COMM_WORLD.Isend(result, 0, 1, MPI.OBJECT, CONTROLLER_RANK, Tags.RESULT.tag());
    }

    private void solve() {
        candidateBound = Integer.MAX_VALUE;

        while (!stack.isEmpty() && stack.peek().getGap() > 0 && !status.isDone()) {
            int stateBound = stack.peek().getGap() + stack.peek().getDepth();
            if (stateBound > bound) {
                if (stateBound < candidateBound) {
                    candidateBound = stateBound;
                }
                stack.pop();
            } else if (stack.peek().getChildren().empty()) {
                if (stack.peek().getDepth() == 0) {
                    requestWork();
                } else {
                    stack.pop();
                }
            } else {
                stack.push(stack.peek().getChildren().pop());
                stack.peek().nextNodes();
            }

            mpi.Status response;
            if ((response = splitCommand.Test()) != null) {
                splitAndSend(response.source);
                listenToSplit();
            }
        }

        if (stack.isEmpty() && !status.isDone()) {
            requestWork();
        }
    }

}
