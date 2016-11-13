package ch.sebastianhaeni.pancake.dto;

import java.io.Serializable;

public class SearchResult implements Serializable {
    private Node solutionNode;
    private int bound;

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
