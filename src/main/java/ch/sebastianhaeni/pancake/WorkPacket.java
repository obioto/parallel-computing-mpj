package ch.sebastianhaeni.pancake;

import java.io.Serializable;

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
