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

public class Worker implements IProcessor {

    private final Status status = new Status();
    private int splitDestination;
    private Stack<Node> stack = new Stack<>();
    private int bound;
    private int candidateBound;
    private Request splitCommand;

    public Worker() {
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

        while (!stack.isEmpty() && stack.peek().getGap() > 0 && !status.isDone()) {
            solve();
        }

        if (status.isDone()) {
            return;
        }

        Stack[] result = new Stack[1];
        result[0] = stack;
        MPI.COMM_WORLD.Isend(result, 0, 1, MPI.OBJECT, CONTROLLER_RANK, Tags.RESULT.tag());
    }

    private void listenToKill() {
        (new Thread(new IntListener(Tags.KILL, (source, result) -> {
            status.done();
            MPI.COMM_WORLD.Send(EMPTY_BUFFER, 0, 0, MPI.INT, CONTROLLER_RANK, Tags.IDLE.tag());
        }, status))).start();
    }

    private void solve() {
        candidateBound = Integer.MAX_VALUE;

        while (!stack.isEmpty() && stack.peek().getGap() > 0 && !status.isDone()) {
            int stateBound = stack.peek().getGap() + stack.peek().getDepth();
            if (stateBound > bound) {
                if (stateBound < candidateBound) {
                    candidateBound = stateBound;
                }
                stack.pop();
            } else if (stack.peek().getChildren().empty()) {
                if (stack.peek().getDepth() == 0) {
                    requestWork();
                } else {
                    stack.pop();
                }
            } else {
                stack.push(stack.peek().getChildren().pop());
                stack.peek().nextNodes();
            }

            mpi.Status response;
            if ((response = splitCommand.Test()) != null) {
                splitAndSend(response.source);
                listenToSplit();
            }
        }

        if (stack.isEmpty() && !status.isDone()) {
            requestWork();
        }
    }

    private void listenToSplit() {
        splitCommand = MPI.COMM_WORLD.Irecv(EMPTY_BUFFER, 0, 0, MPI.INT, MPI.ANY_SOURCE, Tags.SPLIT.tag());
    }

    private void requestWork() {
        int[] boundBuf = new int[1];
        boundBuf[0] = candidateBound;

        MPI.COMM_WORLD.Isend(boundBuf, 0, 1, MPI.INT, CONTROLLER_RANK, Tags.IDLE.tag());
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

    private void splitAndSend(int destination) {
        Partition partition = new Partition(stack, 2);
        stack = partition.get(0);

        WorkPacket packet = new WorkPacket(bound, candidateBound);
        packet.setStack(partition.get(1));

        MPI.COMM_WORLD.Isend(new WorkPacket[]{packet}, 0, 1, MPI.OBJECT, destination, Tags.WORK.tag());
    }

}
