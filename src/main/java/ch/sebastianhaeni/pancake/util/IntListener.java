package ch.sebastianhaeni.pancake.util;

import ch.sebastianhaeni.pancake.dto.Tags;
import mpi.MPI;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class IntListener {

    private final Tags tag;
    private final BiConsumer<Integer, Integer> consumer;
    private final Status status;

    private IntListener(Tags tag, BiConsumer<Integer, Integer> consumer, Status status) {
        this.tag = tag;
        this.consumer = consumer;
        this.status = status;

        listen();
    }

    public static void create(Tags tag, BiConsumer<Integer, Integer> consumer, Status status) {
        new IntListener(tag, consumer, status);
    }

    private void listen() {
        int[] result = new int[1];
        CompletableFuture
            .supplyAsync(() -> MPI.COMM_WORLD.Recv(result, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, tag.tag()))
            .thenAccept(response -> listenCallback(response.source, result[0]));
    }

    private void listenCallback(int source, int result) {
        if (status.isDone()) {
            return;
        }

        consumer.accept(source, result);
        listen();
    }

}
