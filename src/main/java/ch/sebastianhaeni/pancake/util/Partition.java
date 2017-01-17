package ch.sebastianhaeni.pancake.util;

import ch.sebastianhaeni.pancake.model.Node;

import java.util.LinkedList;

public class Partition {
    private final LinkedList<Node> nodes;
    private final int partitionCount;

    public Partition(LinkedList<Node> nodes, int partitionCount) {
        this.nodes = nodes;
        this.partitionCount = partitionCount;
    }

    public LinkedList<Node> get(int index) {
        LinkedList<Node> nodes = new LinkedList<>();

        for (Node node : this.nodes) {
            Node element = new Node(node.getState(), node.getDepth(), node.getGap());
            nodes.push(element);

            for (int i = node.getChildren().size() - 1 - index; i >= 0; i -= partitionCount) {
                element.getChildren().push(node.getChildren().get(i));
            }
        }

        return nodes;
    }

}
