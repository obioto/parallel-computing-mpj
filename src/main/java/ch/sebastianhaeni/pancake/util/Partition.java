package ch.sebastianhaeni.pancake.util;

import ch.sebastianhaeni.pancake.dto.Node;

import java.util.Stack;

public class Partition {
    private final Stack<Node> stack;
    private final int size;

    public Partition(Stack<Node> stack, int stackCount) {
        this.stack = stack;
        this.size = (int) Math.ceil(stack.size() / stackCount);
    }

    public Stack<Node> get(int index) {
        int start = index * size;
        int end = Math.min(start + size, stack.size());
        Stack<Node> nodes = new Stack<>();
        nodes.addAll(stack.subList(start, end));
        return nodes;
    }

}
