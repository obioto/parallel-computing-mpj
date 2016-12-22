package ch.sebastianhaeni.pancake.util;

import ch.sebastianhaeni.pancake.dto.Tags;
import mpi.MPI;

import java.util.function.BiConsumer;

public class IntListener implements Runnable {
    private final Tags tag;
    private final BiConsumer<Integer, int[]> consumer;
    private final Status status;
    private final int source;
    private final int dataCount;

    public IntListener(Tags tag, BiConsumer<Integer, int[]> consumer, Status status, int source, int dataCount) {
        this.tag = tag;
        this.consumer = consumer;
        this.status = status;
        this.source = source;
        this.dataCount = dataCount;
    }

    @Override
    public void run() {
        int[] result = new int[dataCount];
        mpi.Status response = MPI.COMM_WORLD.Recv(result, 0, dataCount, MPI.INT, source, tag.tag());

        if (status.isDone()) {
            return;
        }

        consumer.accept(response.source, result);

        if (!status.isDone()) {
            (new Thread(new IntListener(tag, consumer, status, source, dataCount))).start();
        }
    }

}
