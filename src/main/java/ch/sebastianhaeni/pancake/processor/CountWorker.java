package ch.sebastianhaeni.pancake.processor;

import mpi.MPI;

import static ch.sebastianhaeni.pancake.ParallelSolver.CONTROLLER_RANK;

public class CountWorker extends Worker {

    private int count;

    @Override
    void work() {
        while (!stack.isEmpty()) {
            count();
        }

        int[] result = new int[1];
        result[0] = count;
        MPI.COMM_WORLD.Reduce(result, 0, new int[1], 0, 1, MPI.INT, MPI.SUM, CONTROLLER_RANK);
    }

    private void count() {
        candidateBound = Integer.MAX_VALUE;

        while (!stack.isEmpty()) {
            if (stack.peek().getGap() == 0) {
                stack.pop();
                count++;
                continue;
            }

            int stateBound = stack.peek().getGap() + stack.peek().getDepth();
            if (stateBound > bound) {
                if (stateBound < candidateBound && count == 0) {
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
