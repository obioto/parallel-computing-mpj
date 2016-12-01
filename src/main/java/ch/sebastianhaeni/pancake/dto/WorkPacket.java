package ch.sebastianhaeni.pancake.dto;

import ch.sebastianhaeni.pancake.processor.Worker;

import java.io.Serializable;
import java.util.Stack;

/**
 * A packet of work for the {@link Worker}.
 */
public class WorkPacket implements Serializable {
    private static final long serialVersionUID = -6551566361178729826L;

    private Stack<Node> stack;
    private final int bound;
    private final int candidateBound;

    public WorkPacket(int bound, int candidateBound) {
        this.bound = bound;
        this.candidateBound = candidateBound;
    }

    public void setStack(Stack<Node> stack) {
        this.stack = stack;
    }

    public Stack<Node> getStack() {
        return stack;
    }

    public int getBound() {
        return bound;
    }

    public int getCandidateBound() {
        return candidateBound;
    }
}
