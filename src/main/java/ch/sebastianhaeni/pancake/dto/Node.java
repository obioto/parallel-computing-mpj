package ch.sebastianhaeni.pancake.dto;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * A node in a search tree.
 */
public class Node implements Serializable {
    /**
     * The state behind this node.
     */
    private final int[] state;

    /**
     * The depth this node is at in the search tree.
     */
    private final int depth;

    /**
     * The parent node. This is only used to figure out the solution path after the solution has been found.
     */
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

        // 1.07 from https://en.wikipedia.org/wiki/Pancake_sorting
        return (int) (distance * 1.07);
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

        for (int i = 1; i < state.length; i++) {
            int flipPosition = i + 1;
            if (flipPosition != this.flipPosition) {
                Node flip = flip(flipPosition);
                list.add(flip);
            }
        }

        return list;
    }

    /**
     * Flips the prefix at the defined flip position.
     *
     * @param flipPosition the position where the state shall be reversed
     * @return prefix reversed state
     */
    Node flip(int flipPosition) {

        int[] flipped = new int[state.length];

        for (int i = 0; i < flipPosition; i++) {
            flipped[i] = state[flipPosition - i - 1];
        }

        System.arraycopy(state, flipPosition, flipped, flipPosition, state.length - flipPosition);

        return new Node(flipped, getDepth() + 1, this, flipPosition);
    }

    public int getDepth() {
        return depth;
    }

    public int[] getState() {
        return state;
    }

    public Node getParent() {
        return parent;
    }

    public int getFlipPosition() {
        return flipPosition;
    }
}
