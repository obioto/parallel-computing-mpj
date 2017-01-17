package ch.sebastianhaeni.pancake.dto;

import ch.sebastianhaeni.pancake.model.Node;
import ch.sebastianhaeni.pancake.processor.Worker;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * A packet of work for the {@link Worker}.
 */
public class WorkPacket implements Serializable {
    private static final long serialVersionUID = -6551566361178729826L;

    private LinkedList<Node> nodes;
    private final int bound;
    private final int candidateBound;

    public WorkPacket(int bound, int candidateBound) {
        this.bound = bound;
        this.candidateBound = candidateBound;
    }

    public void setNodes(LinkedList<Node> nodes) {
        this.nodes = nodes;
    }

    public LinkedList<Node> getNodes() {
        return nodes;
    }

    public int getBound() {
        return bound;
    }

    public int getCandidateBound() {
        return candidateBound;
    }
}
