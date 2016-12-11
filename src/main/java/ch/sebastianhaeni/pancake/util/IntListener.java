package ch.sebastianhaeni.pancake.util;

import ch.sebastianhaeni.pancake.dto.Tags;
import mpi.MPI;

import java.util.function.BiConsumer;

public class IntListener implements Runnable {
    private final Tags tag;
    private final BiConsumer<Integer, Integer> consumer;
    private final Status status;
    private final int source;

    public IntListener(Tags tag, BiConsumer<Integer, Integer> consumer, Status status) {
        this(tag, consumer, status, MPI.ANY_SOURCE);
    }

    public IntListener(Tags tag, BiConsumer<Integer, Integer> consumer, Status status, int source) {
        this.tag = tag;
        this.consumer = consumer;
        this.status = status;
        this.source = source;
    }

    @Override
    public void run() {
        int[] result = new int[1];
        mpi.Status response = MPI.COMM_WORLD.Recv(result, 0, 1, MPI.INT, source, tag.tag());

        if (status.isDone()) {
            return;
        }

        consumer.accept(response.source, result[0]);

        if (!status.isDone()) {
            (new Thread(new IntListener(tag, consumer, status, source))).start();
        }
    }

}
