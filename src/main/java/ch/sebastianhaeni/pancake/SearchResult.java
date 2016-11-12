package ch.sebastianhaeni.pancake;

import java.io.Serializable;

public class SearchResult implements Serializable {
    Node solutionNode;
    int bound;

    public SearchResult(Node node) {
        this.solutionNode = node;
    }

    public SearchResult(int bound) {
        this.bound = bound;
    }

    public Node getSolutionNode() {
        return solutionNode;
    }

    public int getBound() {
        return bound;
    }
}
