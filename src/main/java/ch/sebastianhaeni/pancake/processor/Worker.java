package ch.sebastianhaeni.pancake.processor;

import ch.sebastianhaeni.pancake.dto.Node;
import ch.sebastianhaeni.pancake.dto.Tags;
import ch.sebastianhaeni.pancake.dto.WorkPacket;
import ch.sebastianhaeni.pancake.util.IntListener;
import ch.sebastianhaeni.pancake.util.Partition;
import ch.sebastianhaeni.pancake.util.Status;
import ch.sebastianhaeni.pancake.util.WorkPacketListener;
import mpi.MPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Stack;

import static ch.sebastianhaeni.pancake.ParallelSolver.CONTROLLER_RANK;

public class Worker implements IProcessor {

    private static final int INITIAL_WORK_DEPTH = 100;
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
        WorkPacketListener.create(Tags.WORK, (source, result) -> handleWork(result), status);
        IntListener.create(Tags.KILL, (source, result) -> status.done(), status);
        IntListener.create(Tags.SPLIT, (source, result) -> splitAndSend(result), status);

        while (stack.isEmpty() || (stack.peek().getDistance() != 0 && !status.isDone())) {
            solve();
        }

        Stack[] result = new Stack[1];
        result[0] = stack;
        MPI.COMM_WORLD.Isend(result, 0, 1, MPI.OBJECT, CONTROLLER_RANK, Tags.RESULT.tag());
    }

    private void handleWork(WorkPacket result) {
        stack = result.getStack();
        bound = result.getBound();
        candidateBound = result.getCandidateBound();
        log.info("got work with stack size {}", stack.size());
    }

    private void solve() {
        while (!stack.isEmpty() && stack.peek().getDistance() != 0 && stack.peek().getDepth() < INITIAL_WORK_DEPTH) {
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
                    return;
                }
                stack.pop();
            } else {
                //log.info("Doing work");
                stack.push(stack.peek().getChildren().pop());
                stack.peek().nextNodes();
            }
        }
    }

    private void splitAndSend(int result) {
        Partition partition = new Partition(stack, 2);
        stack = partition.get(0);

        WorkPacket packet = new WorkPacket(bound, candidateBound);
        packet.setStack(partition.get(1));

        MPI.COMM_WORLD.Isend(new WorkPacket[] { packet }, 0, 1, MPI.OBJECT, result, Tags.WORK.tag());
    }


}
