package ch.sebastianhaeni.pancake.dto;

import ch.sebastianhaeni.pancake.processor.Worker;

import java.io.Serializable;

/**
 * A packet of work for the {@link Worker}.
 */
public class WorkPacket implements Serializable {
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
