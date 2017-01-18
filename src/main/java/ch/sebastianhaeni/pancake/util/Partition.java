package ch.sebastianhaeni.pancake.util;

import ch.sebastianhaeni.pancake.model.Node;

import java.util.ArrayDeque;

public class Partition {
    private final ArrayDeque<Node> nodes;
    private final int partitionCount;

    public Partition(ArrayDeque<Node> nodes, int partitionCount) {
        this.nodes =nodes;
        this.partitionCount = partitionCount;
    }

    public ArrayDeque<Node> get(int index) {
        ArrayDeque<Node> nodes = new ArrayDeque<>();

        for (Node node : this.nodes) {
            Node element = new Node(node.getState(), node.getDepth(), node.getGap());
            nodes.push(element);

            for (int i = node.getChildren().size() - 1 - index; i >= 0; i -= partitionCount) {
                element.getChildren().push(node.getChildren().pop());
            }
        }

        return nodes;
    }

}
