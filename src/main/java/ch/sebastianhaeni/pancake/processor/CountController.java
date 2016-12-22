package ch.sebastianhaeni.pancake.processor;

import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.util.IntListener;
import mpi.MPI;

import static ch.sebastianhaeni.pancake.ParallelSolver.CONTROLLER_RANK;
import static ch.sebastianhaeni.pancake.ParallelSolver.EMPTY_BUFFER;
import static ch.sebastianhaeni.pancake.util.Output.showCount;

public class CountController extends Controller {

    private boolean foundSolution;

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

    @Override
    void initializeListeners() {
        for (int worker : workers) {
            (new Thread(new IntListener(Tags.IDLE, this::handleIdle, status, worker, 2))).start();
        }
    }

    @Override
    void handleIdle(int source, int[] result) {

        if (result[1] > 0 && !foundSolution) {
            // found a solution, we won't increase the bound anymore
            foundSolution = true;
            System.out.println("Found the first solution. Not increasing bound anymore.");
        }

        if (idleWorkers.contains(source)) {
            return;
        }
        idleWorkers.add(source);

        if (result[0] > bound) {
            bound = result[0];
        }

        if (idleWorkers.size() == workerCount) {
            idleWorkers.clear();

            if (foundSolution) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (idleWorkers.size() != workerCount) {
                    return;
                }
                for (int worker : workers) {
                    MPI.COMM_WORLD.Isend(EMPTY_BUFFER, 0, 0, MPI.INT, worker, Tags.GATHER.tag());
                }
                return;
            }

            if (lastIncrease == bound) {
                return;
            }
            lastIncrease = bound;
            (new Thread(this::solve)).start();
        }
    }

    private void finishCount(int count, long millis) {
        showCount(initialState, count, millis);
        clearListeners();
    }

}
