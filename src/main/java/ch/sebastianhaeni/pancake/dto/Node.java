package ch.sebastianhaeni.pancake.dto;

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
     * The parent node. This is only used to figure out the solution path after the solution has been found.
     */
    private final Node parent;

    /**
     * Position this node's parent was flipped at.
     */
    private final int flipPosition;

    /**
     * Stack of child nodes generated from this node.
     */
    private final Stack<Node> children = new Stack<>();

    /**
     * Pancake pile size.
     */
    private final int size;
    private int distance;

    public Node(int[] state) {
        this(state, 0, null, -1, 0);
    }

    private Node(int[] state, int depth, Node parent, int flipPosition,int distance) {
        this.state = state;
        this.depth = depth;
        this.parent = parent;
        this.flipPosition = flipPosition;
        this.size = state.length;
        this.distance=distance;
    }

    public void calcDistance() {
        distance = 0;

        for (int i = 0; i < size - 1; i++) {
            if (Math.abs(state[i] - state[i + 1]) > 1) {
                distance++;
            }
        }
    }

    public boolean isSolution() {
        for (int i = 0; i < size - 1; i++) {
            if (state[i] != state[i + 1] - 1) {
                return false;
            }
        }

        return true;
    }

    public void nextNodes() {
        for (int i = 2; i < size; i++) {
            if (Math.abs(state[i - 1] - state[i]) > 1) {
                if (Math.abs(state[i] - state[0]) > 1) {
                    children.push(flip(i, distance));
                } else {
                    children.push(flip(i, distance - 1));
                }
            }
        }
    }

    /**
     * Flips the prefix at the defined flip position.
     *
     * @param flipPosition the position where the state shall be reversed
     * @param distance predetermined optimistic distance
     * @return prefix reversed state
     */
    Node flip(int flipPosition, int distance) {

        int[] flipped = new int[size];

        for (int i = 0; i < flipPosition; i++) {
            flipped[i] = state[flipPosition - i - 1];
        }

        System.arraycopy(state, flipPosition, flipped, flipPosition, size - flipPosition);

        return new Node(flipped, getDepth() + 1, this, flipPosition, distance);
    }

    public Node augment() {
        int[] augmentedState = new int[size + 1];
        System.arraycopy(state, 0, augmentedState, 0, size);
        augmentedState[size] = size + 1;
        return new Node(augmentedState);
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

    public int getDistance() {
        return distance;
    }

    public Stack<Node> getChildren() {
        return children;
    }
}
