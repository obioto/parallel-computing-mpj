package ch.sebastianhaeni.pancake.processor;

import ch.sebastianhaeni.pancake.ParallelSolver;
import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.dto.WorkPacket;
import ch.sebastianhaeni.pancake.model.Node;
import ch.sebastianhaeni.pancake.util.IntListener;
import ch.sebastianhaeni.pancake.util.Partition;
import ch.sebastianhaeni.pancake.util.Status;
import mpi.MPI;
import mpi.Request;

import java.util.Stack;

import static ch.sebastianhaeni.pancake.ParallelSolver.CONTROLLER_RANK;
import static ch.sebastianhaeni.pancake.ParallelSolver.EMPTY_BUFFER;

public abstract class Worker implements IProcessor {

    final Status status = new Status();
    private int splitDestination;
    Stack<Node> stack = new Stack<>();
    int bound;
    int candidateBound;
    Request splitCommand;

    Worker() {
        splitDestination = (MPI.COMM_WORLD.Rank() + 1) % MPI.COMM_WORLD.Size();
        if (splitDestination == ParallelSolver.CONTROLLER_RANK) {
            splitDestination++;
        }
    }

    @Override
    public void run() {
        listenToSplit();
        listenToKill();

        waitForWork();

        work();
    }

    abstract void work();

    private void listenToKill() {
        (new Thread(new IntListener(Tags.KILL, (source, result) -> {
            status.done();
            MPI.COMM_WORLD.Send(EMPTY_BUFFER, 0, 0, MPI.INT, CONTROLLER_RANK, Tags.IDLE.tag());
        }, status, MPI.ANY_SOURCE, 1))).start();
    }

    void listenToSplit() {
        splitCommand = MPI.COMM_WORLD.Irecv(EMPTY_BUFFER, 0, 0, MPI.INT, MPI.ANY_SOURCE, Tags.SPLIT.tag());
    }

    void requestWork(int data) {
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

        if (status.isDone()) {
            return;
        }

        WorkPacket work = (WorkPacket) packetBuf[0];

        stack = work.getStack();
        bound = work.getBound();
        candidateBound = work.getCandidateBound();
    }

    void splitAndSend(int destination) {
        Partition partition = new Partition(stack, 2);
        stack = partition.get(0);

        WorkPacket packet = new WorkPacket(bound, candidateBound);
        packet.setStack(partition.get(1));

        MPI.COMM_WORLD.Isend(new WorkPacket[]{packet}, 0, 1, MPI.OBJECT, destination, Tags.WORK.tag());
    }

}
