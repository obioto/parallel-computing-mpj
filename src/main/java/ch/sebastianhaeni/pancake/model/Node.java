package ch.sebastianhaeni.pancake.model;

import java.io.Serializable;
import java.util.Stack;

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
     * Stack of child nodes generated from this node.
     */
    private final Stack<Node> children = new Stack<>();

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
     */
    public void nextNodes() {
        for (int i = 2; i < state.length; i++) {
            int gap = this.gap;

            int currentDiff = Math.abs(state[i] - state[i - 1]);
            int newDiff = Math.abs(state[i] - state[0]);

            if (currentDiff != 1 && newDiff == 1) {
                gap -= 1;
            } else if (currentDiff == 1 && newDiff != 1) {
                gap += 1;
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

        return new Node(flipped, getDepth() + 1, gap);
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

    public Stack<Node> getChildren() {
        return children;
    }

    public int getGap() {
        return gap;
    }
}
