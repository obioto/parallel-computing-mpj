package ch.sebastianhaeni.pancake.util;

import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.dto.WorkPacket;
import mpi.MPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class WorkPacketListener {
    private static final Logger LOG = LogManager.getLogger("WorkPacketListener");

    private final Tags tag;
    private final BiConsumer<Integer, WorkPacket> consumer;
    private final Status status;

    private WorkPacketListener(Tags tag, BiConsumer<Integer, WorkPacket> consumer, Status status) {
        this.tag = tag;
        this.consumer = consumer;
        this.status = status;

        listen();
    }

    public static void create(Tags tag, BiConsumer<Integer, WorkPacket> consumer, Status status) {
        new WorkPacketListener(tag, consumer, status);
    }

    private void listen() {
        Object[] packetBuf = new Object[1];
        CompletableFuture
            .supplyAsync(() -> MPI.COMM_WORLD.Recv(packetBuf, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, tag.tag()))
            .thenAccept(response -> listenCallback(response.source, packetBuf[0]));
    }

    private void listenCallback(int source, Object result) {
        if (status.isDone()) {
            return;
        }

        consumer.accept(source, (WorkPacket) result);
        listen();
    }

}
