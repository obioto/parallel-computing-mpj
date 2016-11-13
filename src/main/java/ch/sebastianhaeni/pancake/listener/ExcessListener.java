package ch.sebastianhaeni.pancake.listener;

import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.dto.WorkPacket;
import mpi.MPI;
import mpi.Request;

import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link ExcessListener} listens to workers returning back some {@link WorkPacket} when they were commanded to split.
 */
public class ExcessListener extends PancakeListener {

    private final LinkedBlockingQueue<WorkPacket> outstandingWork;

    public ExcessListener(int worker, LinkedBlockingQueue<WorkPacket> outstandingWork) {
        super(worker);
        this.outstandingWork = outstandingWork;
    }

    @Override
    protected void listen() {
        int[] lengthBuffer = new int[1];
        Request lengthRequest = MPI.COMM_WORLD.Irecv(lengthBuffer, 0, 1, MPI.INT, worker, Tags.EXCESS_LENGTH.tag());

        if (block(lengthRequest)) return;

        WorkPacket[] work = new WorkPacket[lengthBuffer[0]];
        Request dataRequest = MPI.COMM_WORLD.Irecv(work, 0, lengthBuffer[0], MPI.OBJECT, worker, Tags.EXCESS.tag());

        if (block(dataRequest)) return;

        Collections.addAll(outstandingWork, work);
    }

}
