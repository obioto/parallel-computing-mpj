package ch.sebastianhaeni.pancake.processor;

import mpi.MPI;

import static ch.sebastianhaeni.pancake.ParallelSolver.CONTROLLER_RANK;
import static ch.sebastianhaeni.pancake.util.Output.showCount;

public class CountController extends Controller {

    public CountController(int[] initialState, int workerCount) {
        super(initialState, workerCount);
    }

    @Override
    void work() {
        System.out.format("Counting solutions for a pancake pile of height %d.\n", initialState.length);

        // Start counting
        long start = System.currentTimeMillis();

        if (solve()) {
            long end = System.currentTimeMillis();
            finishCount(1, end - start);
            return;
        }

        int[] countBuf = new int[1];
        MPI.COMM_WORLD.Reduce(new int[]{0}, 0, countBuf, 0, 1, MPI.INT, MPI.SUM, CONTROLLER_RANK);
        status.done();

        long end = System.currentTimeMillis();
        // End counting

        finishCount(countBuf[0], end - start);
    }

    private void finishCount(int count, long millis) {
        showCount(initialState, count, millis);
        clearListeners();
    }

}
