package ch.sebastianhaeni.pancake.processor;

import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.model.Node;
import mpi.MPI;

import java.util.Stack;

import static ch.sebastianhaeni.pancake.util.Output.showSolution;

public class SolveController extends Controller {

    public SolveController(int[] initialState, int workerCount) {
        super(initialState, workerCount);
    }

    @Override
    void work() {
        System.out.printf("Solving a pancake pile in parallel of height %s.\n", initialState.length);

        // Start solving
        long start = System.currentTimeMillis();

        if (solve()) {
            long end = System.currentTimeMillis();
            finishSolve(stack, end - start);
            return;
        }

        Stack<Node>[] solution = new Stack[1];
        MPI.COMM_WORLD.Recv(solution, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, Tags.RESULT.tag());
        status.done();

        long end = System.currentTimeMillis();
        // End solving

        finishSolve(solution[0], end - start);
    }

    private void finishSolve(Stack<Node> solution, long millis) {
        showSolution(solution, millis);
        clearListeners();
    }

}
