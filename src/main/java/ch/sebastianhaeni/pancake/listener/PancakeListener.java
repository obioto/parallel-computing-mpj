package ch.sebastianhaeni.pancake.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mpi.Request;

import static ch.sebastianhaeni.pancake.ParallelSolver.SLEEP_MILLIS;

public abstract class PancakeListener extends Thread {
    private static final Logger LOG = LogManager.getLogger("PancakeListener");
    private final int worker;

    private boolean stop;

    PancakeListener(int worker) {
        this.worker = worker;
    }

    public void stopListening() {
        this.stop = true;
    }

    @Override
    public void run() {
        while (!stop) {
            listen();
        }
        LOG.debug("Listener for worker {} terminated", getWorker());
    }

    boolean block(Request request) {
        while (request.Test() == null) {
            if (stop) {
                return true;
            }
            try {
                Thread.sleep(SLEEP_MILLIS);
            } catch (InterruptedException e) {
                LOG.error("Interrupted", e);
            }
        }
        return false;
    }

    protected abstract void listen();

    public int getWorker() {
        return worker;
    }
}
