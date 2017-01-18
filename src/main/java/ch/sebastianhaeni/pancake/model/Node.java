package ch.sebastianhaeni.pancake.model;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * A node in a search tree.
 */
public class Node implements Serializable {
    private static final long serialVersionUID = -8209803922325444946L;

    /**
     * The state behind this node.
     */
    private final int[] state;

    /**
     * The depth this node is at in the search tree.
     */
    private final int depth;

    /**
     * List of child nodes generated from this node.
     */
    private final ArrayDeque<Node> children = new ArrayDeque<>();

    /**
     * Gap heuristic value.
     */
    private final int gap;

    public Node(int[] state) {
        this(state, 0);
    }

    private Node(int[] state, int depth) {
        this.state = state;
        this.depth = depth;
        this.gap = gap();
    }

    public Node(int[] state, int depth, int gap) {
        this.state = state;
        this.depth = depth;
        this.gap = gap;
    }

    /**
     * Calculates the gap value. This operation should only be called once for the root node.
     *
     * @return gap distance
     */
    private int gap() {
        int gap = 0;

        for (int i = 1; i < state.length; i++) {
            if (Math.abs(state[i] - state[i - 1]) > 1) {
                gap++;
            }
        }
        return gap;
    }

    /**
     * Expands this node.
     * The gap value can be pre determined so we don't have to loop through again.
     */
    public void nextNodes() {
        int firstValue = state[0];
        int previousValue = state[1];
        int currentValue = state[2];

        boolean[] diffs = new boolean[state.length];
        int previousDiff = previousValue - firstValue;
        int currentDiff = Math.abs(currentValue - previousValue);
        diffs[0] = previousDiff == 1 || previousDiff == -1;
        diffs[1] = currentDiff == 1 || currentDiff == -1;

        for (int i = 2; i < state.length; i++) {
            currentValue = state[i];

            if (i < state.length - 1) {
                int nextDiff = state[i + 1] - currentValue;
                diffs[i] = nextDiff == 1 || nextDiff == -1;

                if (diffs[i - 1] && (diffs[i - 2] || diffs[i])) {
                    // skip sorted pancakes
                    continue;
                }
            }

            int newDiff = currentValue - firstValue;
            currentDiff = currentValue - previousValue;

            boolean currentDiffOne = currentDiff == 1 || currentDiff == -1;
            boolean newDiffOne = newDiff == 1 || newDiff == -1;
            previousValue = currentValue;

            if (currentDiffOne != newDiffOne) {
                children.push(flip(i, gap + (currentDiffOne ? 1 : -1)));
                continue;
            }
            children.push(flip(i, gap));
        }
    }

    /**
     * Flips the prefix at the defined flip position.
     *
     * @param flipPosition the position where the state shall be reversed
     * @param gap          predetermined optimistic gap for the new node
     * @return prefix reversed state
     */
    Node flip(int flipPosition, int gap) {

        int[] flipped = new int[state.length];

        for (int i = 0; i < flipPosition; i++) {
            flipped[i] = state[flipPosition - i - 1];
        }

        System.arraycopy(state, flipPosition, flipped, flipPosition, state.length - flipPosition);

        return new Node(flipped, depth + 1, gap);
    }

    /**
     * Augments the state of this node. An additional item is added at the end of the state.
     *
     * @return new instanced node with augmented state
     */
    public Node augment() {
        int[] augmentedState = new int[state.length + 1];
        System.arraycopy(state, 0, augmentedState, 0, state.length);
        augmentedState[state.length] = state.length + 1;
        return new Node(augmentedState, getDepth());
    }

    public int getDepth() {
        return depth;
    }

    public int[] getState() {
        return state;
    }

    public ArrayDeque<Node> getChildren() {
        return children;
    }

    public int getGap() {
        return gap;
    }

    @Override
    public String toString() {
        return "Node{" +
            "state=" + Arrays.toString(state) +
            ", depth=" + depth +
            ", children=" + children +
            ", gap=" + gap +
            '}';
    }
}
