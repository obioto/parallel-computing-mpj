package ch.sebastianhaeni.pancake.processor.solve;

import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.model.Node;
import ch.sebastianhaeni.pancake.processor.Controller;
import mpi.MPI;
import mpi.Request;

import java.util.ArrayDeque;

import static ch.sebastianhaeni.pancake.util.Output.showSolution;

public class SolveController extends Controller {

    public SolveController(int[] initialState, int workerCount) {
        super(initialState, workerCount);
    }

    @Override
    protected void work() throws InterruptedException {
        System.out.printf("Solving a pancake pile in parallel of height %s.\n", initialState.length);

        // Start solving
        long start = System.currentTimeMillis();

        if (solve()) {
            long end = System.currentTimeMillis();
            finishSolve(nodes, end - start);
            return;
        }

        ArrayDeque<Node>[] solution = new ArrayDeque[1];

        Request resultCommand = MPI.COMM_WORLD.Irecv(solution, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, Tags.RESULT.tag());
        while (resultCommand.Test() == null) {
            for (int worker : workers) {
                if (workerListeners[worker - 1].Test() != null) {
                    handleIdle(worker, workerData[worker - 1]);
                    initWorkerListener(worker);
                }
            }
            Thread.sleep(20);
        }

        long end = System.currentTimeMillis();
        // End solving

        finishSolve(solution[0], end - start);
    }

    private void handleIdle(int source, int[] result) throws InterruptedException {
        if (idleWorkers.contains(source)) {
            return;
        }
        idleWorkers.add(source);

        if (result[0] > bound) {
            bound = result[0];
            System.out.format("Got bound %d from %d\n", bound, source);
        }

        if (idleWorkers.size() == workerCount) {
            Thread.sleep(100);

            idleWorkers.clear();

            if (lastIncrease == bound) {
                return;
            }
            lastIncrease = bound;
            solve();
        }
    }

    private void finishSolve(ArrayDeque<Node> solution, long millis) {
        showSolution(solution, millis);
        clearListeners();
    }

}
