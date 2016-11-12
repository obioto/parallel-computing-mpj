package ch.sebastianhaeni.pancake.parallel;

import ch.sebastianhaeni.pancake.Node;

import java.io.Serializable;

class WorkPacket implements Serializable {
    private final Node node;
    private final int bound;

    WorkPacket(Node node, int bound) {
        this.node = node;
        this.bound = bound;
    }

    Node getNode() {
        return node;
    }

    int getBound() {
        return bound;
    }
}
