package ch.sebastianhaeni.pancake.util;

import ch.sebastianhaeni.pancake.dto.Node;

import java.util.Stack;

public class Partition {
    private final Stack<Node> stack;
    private final int partitionCount;

    public Partition(Stack<Node> stack, int partitionCount) {
        this.stack = stack;
        this.partitionCount = partitionCount;
    }

    public Stack<Node> get(int index) {
        Stack<Node> nodes = new Stack<>();

        for (Node node : stack) {
            Node element = new Node(node.getState(), node.getDepth(), node.getDistance());
            nodes.push(element);

            for (int i = node.getChildren().size() - 1 - index; i >= 0; i -= partitionCount) {
                element.getChildren().push(node.getChildren().get(i));
            }
        }

        return nodes;
    }

}
