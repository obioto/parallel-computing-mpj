package ch.sebastianhaeni.pancake;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class Node implements Serializable {
    private final int[] state;
    private final int depth;
    private final Node parent;
    private final int flipPosition;

    public Node(int[] state) {
        this(state, 0, null, -1);
    }

    private Node(int[] state, int depth, Node parent, int flipPosition) {
        this.state = state;
        this.depth = depth;
        this.parent = parent;
        this.flipPosition = flipPosition;
    }

    public int getOptimisticDistanceToSolution() {
        int distance = 0;
        int current = 1;

        for (int i = 1; i < state.length; i++) {
            int pancake = state[i];
            if (Math.abs(pancake - current) != 1) {
                distance++;
            }
            current = pancake;
        }

        return distance;
    }

    public int getDepth() {
        return depth;
    }

    public boolean isSolution() {
        int current = 1;

        for (int i = 1; i < state.length; i++) {
            int pancake = state[i];
            if (pancake - current != 1) {
                return false;
            }
            current = pancake;
        }

        return true;
    }

    public List<Node> nextNodes() {
        AbstractList<Node> list = new ArrayList<>();

        int current = state[1];

        for (int i = 1; i < state.length; i++) {
            int pancake = state[i];
            if (pancake - current != 1) {
                int flipPosition = i + 1;
                if (flipPosition != this.flipPosition) {
                    list.add(flip(flipPosition));
                }
            }
            current = pancake;
        }

        return list;
    }

    Node flip(int flipPosition) {

        int[] flipped = new int[state.length];

        for (int i = 0; i < flipPosition; i++) {
            flipped[i] = state[flipPosition - i - 1];
        }

        System.arraycopy(state, flipPosition, flipped, flipPosition, state.length - flipPosition);

        return new Node(flipped, getDepth() + 1, this, flipPosition);
    }

    public int[] getState() {
        return state;
    }

    public Node getParent() {
        return parent;
    }

}
