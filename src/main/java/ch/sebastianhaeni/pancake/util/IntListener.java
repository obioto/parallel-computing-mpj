package ch.sebastianhaeni.pancake.util;

import ch.sebastianhaeni.pancake.dto.Tags;
import mpi.MPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;

public class IntListener implements Runnable {
    private static final Logger LOG = LogManager.getLogger("WorkPacketListener");

    private final Tags tag;
    private final BiConsumer<Integer, Integer> consumer;
    private final Status status;

    public IntListener(Tags tag, BiConsumer<Integer, Integer> consumer, Status status) {
        this.tag = tag;
        this.consumer = consumer;
        this.status = status;
    }

    @Override
    public void run() {
        int[] result = new int[1];
        mpi.Status response = MPI.COMM_WORLD.Recv(result, 0, 1, MPI.INT, MPI.ANY_SOURCE, tag.tag());

        LOG.info("Got {} message for worker({})", tag.name(), MPI.COMM_WORLD.Rank());

        if (status.isDone()) {
            return;
        }

        consumer.accept(response.source, result[0]);
        (new Thread(new IntListener(tag, consumer, status))).start();
    }

}
