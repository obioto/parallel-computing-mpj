package ch.sebastianhaeni.pancake.util;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.dto.WorkPacket;
import mpi.MPI;

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
            .supplyAsync(() -> {
                LOG.info("Setting up work listener for worker({})", MPI.COMM_WORLD.Rank());
                mpi.Status recv = MPI.COMM_WORLD.Recv(packetBuf, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, tag.tag());
                LOG.info("Received work");
                return recv;
            })
            .thenAccept(response -> listenCallback(response.source, packetBuf[0]));
    }

    private void listenCallback(int source, Object result) {
        if (status.isDone()) {
            return;
        }

        WorkPacket work = (WorkPacket) result;
        LOG.info("received a stack of work {}", work.getStack().size());
        consumer.accept(source, work);
        listen();
    }

}
