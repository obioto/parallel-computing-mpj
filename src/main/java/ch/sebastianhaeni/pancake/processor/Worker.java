package ch.sebastianhaeni.pancake.processor;

import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.dto.WorkPacket;
import ch.sebastianhaeni.pancake.model.Node;
import mpi.MPI;
import mpi.Request;

import java.util.ArrayDeque;

import static ch.sebastianhaeni.pancake.ParallelSolver.CONTROLLER_RANK;
import static ch.sebastianhaeni.pancake.ParallelSolver.EMPTY_BUFFER;

public abstract class Worker implements IProcessor {

    protected ArrayDeque<Node> nodes = new ArrayDeque<>();
    protected int bound;
    protected int candidateBound;
    protected Request killCommand;

    protected Worker() {
    }

    @Override
    public void run() {
        killCommand = MPI.COMM_WORLD.Irecv(EMPTY_BUFFER, 0, 0, MPI.INT, MPI.ANY_SOURCE, Tags.KILL.tag());
        waitForWork();

        work();
    }

    protected abstract void work();

    protected void requestWork(int data) {
        int[] boundBuf = new int[2];
        boundBuf[0] = candidateBound;
        boundBuf[1] = data;

        MPI.COMM_WORLD.Isend(boundBuf, 0, 2, MPI.INT, CONTROLLER_RANK, Tags.IDLE.tag());

        waitForWork();
    }

    private void waitForWork() {
        Object[] packetBuf = new Object[1];

        MPI.COMM_WORLD.Recv(packetBuf, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, Tags.WORK.tag());

        WorkPacket work = (WorkPacket) packetBuf[0];

        nodes = work.getNodes();
        bound = work.getBound();
        candidateBound = work.getCandidateBound();
    }

}
