package ch.sebastianhaeni.pancake.processor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.sebastianhaeni.pancake.ParallelSolver;
import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.dto.WorkPacket;
import mpi.MPI;

class Distributor extends Thread {
    private static final Logger LOG = LogManager.getLogger("Distributor");

    private final LinkedBlockingQueue<WorkPacket> outstandingWork;
    private final LinkedBlockingQueue<Integer> idleWorkers;
    private final int[] workers;
    private boolean started;
    private boolean stop;

    Distributor(LinkedBlockingQueue<Integer> idleWorkers, LinkedBlockingQueue<WorkPacket> outstandingWork, int[] workers) {
        this.idleWorkers = idleWorkers;
        this.outstandingWork = outstandingWork;
        this.workers = workers;
    }

    @Override
    public void run() {
        if (started) {
            return;
        }
        started = true;
        WorkPacket work;

        try {
            while (!stop) {
                while ((work = outstandingWork.poll(2, TimeUnit.MILLISECONDS)) != null) {
                    Integer worker = null;
                    while (!stop && worker == null) {
                        worker = idleWorkers.poll(1, TimeUnit.SECONDS);
                    }
                    if (stop) {
                        return;
                    }
                    //noinspection ConstantConditions
                    MPI.COMM_WORLD.Isend(new WorkPacket[] { work }, 0, 1, MPI.OBJECT, worker, Tags.WORK.tag());
                }

                if (idleWorkers.isEmpty()) {
                    continue;
                }

                // send work to idle workers
                for (int worker : workers) {
                    if (idleWorkers.contains(worker)) {
                        continue;
                    }
                    MPI.COMM_WORLD.Isend(ParallelSolver.EMPTY_BUFFER, 0, 0, MPI.INT, worker, Tags.SPLIT.tag());
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Exception while distributing work", e);
        }

        LOG.debug("Terminated");
    }

    void stopDistributing() {
        this.stop = true;
    }

    boolean isStarted() {
        return started;
    }
}
