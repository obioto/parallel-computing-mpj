package ch.sebastianhaeni.pancake.listener;

import ch.sebastianhaeni.pancake.ParallelSolver;
import ch.sebastianhaeni.pancake.dto.Tags;
import mpi.MPI;
import mpi.Request;

import java.util.concurrent.LinkedBlockingQueue;

public class IdleListener extends PancakeListener {

    private final LinkedBlockingQueue<Integer> idleWorkers;

    public IdleListener(int worker, LinkedBlockingQueue<Integer> idleWorkers) {
        super(worker);
        this.idleWorkers = idleWorkers;
    }

    @Override
    protected void listen() {
        Request request = MPI.COMM_WORLD.Irecv(ParallelSolver.EMPTY_BUFFER, 0, 0, MPI.INT, worker, Tags.IDLE.tag());

        if (block(request)) return;

        idleWorkers.add(worker);
    }

}
