package ch.sebastianhaeni.pancake.processor;

import java.util.ArrayDeque;

import ch.sebastianhaeni.pancake.ParallelSolver;
import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.dto.WorkPacket;
import ch.sebastianhaeni.pancake.model.Node;
import ch.sebastianhaeni.pancake.util.Partition;
import mpi.MPI;
import mpi.Request;

import static ch.sebastianhaeni.pancake.ParallelSolver.CONTROLLER_RANK;
import static ch.sebastianhaeni.pancake.ParallelSolver.EMPTY_BUFFER;

public abstract class Worker implements IProcessor {

    private int splitDestination;
    protected ArrayDeque<Node> nodes = new ArrayDeque<>();
    protected int bound;
    protected int candidateBound;
    protected Request splitCommand;
    protected Request killCommand;

    protected Worker() {
        splitDestination = (MPI.COMM_WORLD.Rank() + 1) % MPI.COMM_WORLD.Size();
        if (splitDestination == ParallelSolver.CONTROLLER_RANK) {
            splitDestination++;
        }
    }

    @Override
    public void run() {
        killCommand = MPI.COMM_WORLD.Irecv(EMPTY_BUFFER, 0, 0, MPI.INT, MPI.ANY_SOURCE, Tags.KILL.tag());
        listenToSplit();
        waitForWork();

        work();
    }

    protected abstract void work();

    protected void listenToSplit() {
        splitCommand = MPI.COMM_WORLD.Irecv(EMPTY_BUFFER, 0, 0, MPI.INT, MPI.ANY_SOURCE, Tags.SPLIT.tag());
    }

    protected void requestWork(int data) {
        int[] boundBuf = new int[2];
        boundBuf[0] = candidateBound;
        boundBuf[1] = data;

        MPI.COMM_WORLD.Isend(boundBuf, 0, 2, MPI.INT, CONTROLLER_RANK, Tags.IDLE.tag());
        MPI.COMM_WORLD.Isend(EMPTY_BUFFER, 0, 0, MPI.INT, splitDestination, Tags.SPLIT.tag());

        waitForWork();
    }

    private void waitForWork() {
        Object[] packetBuf = new Object[1];

        MPI.COMM_WORLD.Recv(packetBuf, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, Tags.WORK.tag());
        MPI.COMM_WORLD.Send(EMPTY_BUFFER, 0, 0, MPI.INT, CONTROLLER_RANK, Tags.WORKING.tag());

        WorkPacket work = (WorkPacket) packetBuf[0];

        nodes = work.getNodes();
        bound = work.getBound();
        candidateBound = work.getCandidateBound();
    }

    protected void splitAndSend(int destination) {
        Partition partition = new Partition(nodes, 2);
        nodes = partition.get(0);

        WorkPacket packet = new WorkPacket(bound, candidateBound);
        packet.setNodes(partition.get(1));
        System.out.format("Worker %d sending %d packets to %d\n", MPI.COMM_WORLD.Rank(), packet.getNodes().size(), destination);

        MPI.COMM_WORLD.Isend(new WorkPacket[] { packet }, 0, 1, MPI.OBJECT, destination, Tags.WORK.tag());
    }

}
