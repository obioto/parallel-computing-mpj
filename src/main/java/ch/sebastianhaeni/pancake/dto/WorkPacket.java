package ch.sebastianhaeni.pancake.dto;

import java.io.Serializable;

import ch.sebastianhaeni.pancake.processor.Worker;

/**
 * A packet of work for the {@link Worker}.
 */
public class WorkPacket implements Serializable {
    private static final long serialVersionUID = -6551566361178729826L;

    private final Node node;
    private final int bound;

    public WorkPacket(Node node, int bound) {
        this.node = node;
        this.bound = bound;
    }

    public Node getNode() {
        return node;
    }

    public int getBound() {
        return bound;
    }
}
