package ch.sebastianhaeni.pancake.parallel;

import mpi.MPI;


public class ParallelSolver {
    static final int MASTER_RANK = 0;

    //    private static int[] INITIAL_STATE = new int[]{2, 1, 5, 4, 3};
//    private static int[] INITIAL_STATE = new int[]{5, 2, 7, 10, 13, 16, 14, 6, 8, 18, 15, 11, 1, 12, 3, 4, 9, 17};
    private static int[] INITIAL_STATE = new int[]{17, 2, 5, 13, 3, 7, 8, 4, 20, 11, 9, 18, 6, 14, 1, 10, 16, 15, 12, 19};
//    private static int[] INITIAL_STATE = new int[]{39,47,13,46,11,43,49,14,7,57,59,42,53,37,55,20,28,33,5,27,34,40,19,52,10,35,8,31,48,30,22,45,12,24,32,36,38,17,50,54,41,60,26,4,2,9,58,23,44,1,6,15,16,29,51,18,21,3,56,25});

    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        IProcessor processor;

        if (rank == MASTER_RANK) {
            processor = new Master(INITIAL_STATE, size - 1);
        } else {
            processor = new Slave(rank);
        }

        processor.run();

        MPI.Finalize();
    }

    static class Tags {
        static final int WORK = 1;
        static final int RESULT = 2;
        static final int FINISH = 3;
        static final int IDLE = 4;
        static final int SPLIT = 5;
        static final int EXCESS = 6;
    }
}
