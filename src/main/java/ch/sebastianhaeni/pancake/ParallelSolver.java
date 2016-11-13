package ch.sebastianhaeni.pancake;

import ch.sebastianhaeni.pancake.processor.IProcessor;
import ch.sebastianhaeni.pancake.processor.Controller;
import ch.sebastianhaeni.pancake.processor.Worker;
import mpi.MPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ParallelSolver {
    private static final Logger LOG = LogManager.getLogger("ParallelSolver");

    public static final int CONTROLLER_RANK = 0;
    public static final int SLEEP_MILLIS = 3;
    public static final int[] EMPTY_BUFFER = new int[0];

//    private static int[] INITIAL_STATE = new int[]{3,4,2,5,1}; // 5
//    private static int[] INITIAL_STATE = new int[]{5,6,4,10,7,9,3,1,2,8}; // 10
//    private static int[] INITIAL_STATE = new int[]{13,12,2,7,8,3,11,15,5,1,6,9,14,10,4}; // 15
//    private static int[] INITIAL_STATE = new int[]{1,10,3,15,5,9,17,16,14,6,8,12,11,20,13,19,7,4,18,2}; // 20
//    private static int[] INITIAL_STATE = new int[]{4,25,15,7,28,17,6,5,23,29,19,27,13,21,9,1,26,22,11,18,16,12,10,30,2,3,8,14,24,20}; // 30
//    private static int[] INITIAL_STATE = new int[]{7,13,21,4,16,31,30,35,40,23,8,19,33,24,38,28,29,14,26,6,25,32,1,3,5,22,37,9,20,2,10,34,18,17,11,27,12,15,39,36}; // 40
//    private static int[] INITIAL_STATE = new int[]{13,23,24,28,39,17,38,31,9,5,30,40,26,32,8,36,19,16,33,6,20,12,14,25,27,22,34,11,21,4,10,18,15,7,2,37,1,29,35,3}; // 40
//    private static int[] INITIAL_STATE = new int[]{39,2,22,20,23,30,31,46,49,8,21,5,29,11,19,33,38,17,6,35,42,34,15,7,48,3,9,37,28,41,40,24,26,50,4,43,12,18,13,10,36,1,25,32,45,47,16,14,27,44}; // 50
    private static int[] INITIAL_STATE = new int[]{21,33,30,58,17,6,41,3,12,22,56,16,20,35,27,44,36,24,10,19,53,54,9,42,59,34,13,39,37,29,15,25,60,50,31,46,38,51,1,49,47,8,52,57,48,18,5,55,43,7,26,32,40,28,23,2,11,45,14,4}; // 60
//    private static int[] INITIAL_STATE = new int[]{14,34,9,13,27,46,41,25,5,44,47,22,35,7,17,49,54,2,32,42,39,50,12,52,66,31,24,36,6,18,45,60,28,37,65,43,48,38,58,33,8,10,55,61,53,62,19,26,59,16,57,15,56,20,11,29,4,40,1,51,63,30,64,23,3,21}; // 66
//    private static int[] INITIAL_STATE = new int[]{51,52,3,40,19,8,62,69,27,48,31,23,50,26,10,38,28,60,68,53,9,64,2,56,30,6,4,58,14,17,66,41,45,49,59,42,5,61,65,13,67,55,7,70,18,33,25,46,57,1,47,37,43,34,16,32,39,21,29,35,11,24,63,20,22,44,12,54,15,36}; // 70

    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        IProcessor processor;

        if (rank == CONTROLLER_RANK) {
            processor = new Controller(INITIAL_STATE, size - 1);
        } else {
            processor = new Worker(rank);
        }

        processor.run();

        LOG.debug("Terminated");
        MPI.Finalize();
    }

}
