package ch.sebastianhaeni.pancake.processor;

import ch.sebastianhaeni.pancake.dto.Node;
import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.dto.WorkPacket;
import ch.sebastianhaeni.pancake.util.IntListener;
import ch.sebastianhaeni.pancake.util.Partition;
import ch.sebastianhaeni.pancake.util.Status;
import mpi.MPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Stack;

import static ch.sebastianhaeni.pancake.ParallelSolver.CONTROLLER_RANK;

public class Worker implements IProcessor {

    private final Logger log;
    private final Status status = new Status();
    private Stack<Node> stack = new Stack<>();
    private int bound;
    private int candidateBound;

    public Worker(int rank) {
        log = LogManager.getLogger("Worker(" + rank + ')');
    }

    @Override
    public void run() {
        (new Thread(new IntListener(Tags.KILL, (source, result) -> status.done(), status))).start();
        (new Thread(new IntListener(Tags.SPLIT, (source, result) -> splitAndSend(result), status))).start();
        waitForWork();

        while (stack.isEmpty() || (stack.peek().getDistance() != 0 && !status.isDone())) {
            solve();
        }

        Stack[] result = new Stack[1];
        result[0] = stack;
        MPI.COMM_WORLD.Isend(result, 0, 1, MPI.OBJECT, CONTROLLER_RANK, Tags.RESULT.tag());
    }

    private void solve() {
        while (stack.peek().getDistance() > 0) {
            stack.peek().calcDistance();
            stack.peek().nextNodes();
            if (stack.peek().getDistance() + stack.peek().getDepth() > bound) {
                int stateBound = stack.peek().getDepth() + stack.peek().getDistance();
                candidateBound = stateBound < candidateBound ? stateBound : candidateBound;
                stack.pop();
            } else if (stack.peek().getChildren().empty()) {
                if (stack.peek().getDepth() == 0) {

                    int[] boundBuf = new int[1];
                    boundBuf[0] = candidateBound;

                    log.info("Sending new bound {}", candidateBound);
                    MPI.COMM_WORLD.Isend(boundBuf, 0, 1, MPI.INT, CONTROLLER_RANK, Tags.IDLE.tag());
                    waitForWork();
                } else {
                    stack.pop();
                }
            } else {
                //log.info("Doing work");
                stack.push(stack.peek().getChildren().pop());
                stack.peek().nextNodes();
            }
        }
//        log.info("My stack is empty");
    }

    private void waitForWork() {
        Object[] packetBuf = new Object[1];
        log.info("Setting up work listener for worker({})", MPI.COMM_WORLD.Rank());
        MPI.COMM_WORLD.Recv(packetBuf, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, Tags.WORK.tag());
        log.info("Received work for worker {}", MPI.COMM_WORLD.Rank());

        if (status.isDone()) {
            return;
        }

        WorkPacket work = (WorkPacket) packetBuf[0];
        log.info("received a stack of work {}", work.getStack().size());

        stack = work.getStack();
        bound = work.getBound();
        candidateBound = work.getCandidateBound();
        log.info("got work with stack size {}", stack.size());

        StringBuilder sb = new StringBuilder();
        while (!stack.isEmpty()) {
            sb.insert(0, Arrays.toString(stack.pop().getState()) + '\n');
        }
        System.out.println("Worker " + MPI.COMM_WORLD.Rank() + ":\n" + sb.toString());
    }

    private void splitAndSend(int result) {
        Partition partition = new Partition(stack, 2);
        stack = partition.get(0);

        WorkPacket packet = new WorkPacket(bound, candidateBound);
        packet.setStack(partition.get(1));

        MPI.COMM_WORLD.Isend(new WorkPacket[]{packet}, 0, 1, MPI.OBJECT, result, Tags.WORK.tag());
    }

}
