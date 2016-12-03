package ch.sebastianhaeni.pancake.util;

import java.util.Stack;

import ch.sebastianhaeni.pancake.dto.Node;

public class Partition {
    private final Stack<Node> stack;
    private final int size;
    private final int stackCount;

    public Partition(Stack<Node> stack, int stackCount) {
        this.stack = stack;
        this.size = (int) Math.ceil(((double) stack.size()) / stackCount);
        this.stackCount = stackCount;
    }

    public Stack<Node> get(int index) {
        int start = index * size;
        int end = Math.min(start + size, stack.size());
        Stack<Node> nodes = new Stack<>();
        nodes.addAll(stack.subList(start, end));
        return nodes;
    }

    public int size() {
        return stackCount;
    }
}
