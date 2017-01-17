package ch.sebastianhaeni.pancake;

import ch.sebastianhaeni.pancake.processor.*;
import ch.sebastianhaeni.pancake.util.Generator;
import ch.sebastianhaeni.pancake.util.Mode;
import mpi.MPI;

public final class ParallelSolver {

    // Change mode here!
    private static final Mode CURRENT_MODE = Mode.SOLVE;

    // Start with alternating sequence
    //private static final int[] INITIAL_STATE = Generator.alternate(16);
    private static final int[] INITIAL_STATE = Generator.alternate(14);

    // Start with random sequence
    // private static final int[] INITIAL_STATE = Generator.random(25));

    // Start with predetermined sequence
    // private static final int[] INITIAL_STATE = new int[]{2, 1, 3, 4});


    public static final int CONTROLLER_RANK = 0;
    public static final int[] EMPTY_BUFFER = new int[0];

    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        IProcessor processor;

        switch (CURRENT_MODE) {
            case COUNT:
                if (rank == CONTROLLER_RANK) {
                    processor = new CountController(INITIAL_STATE, size - 1);
                } else {
                    processor = new CountWorker();
                }
                break;
            case SOLVE:
                if (rank == CONTROLLER_RANK) {
                    processor = new SolveController(INITIAL_STATE, size - 1);
                } else {
                    processor = new SolveWorker();
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported mode");
        }

        processor.run();

        MPI.Finalize();
    }

}
