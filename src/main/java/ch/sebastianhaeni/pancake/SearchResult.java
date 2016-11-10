package ch.sebastianhaeni.pancake;

import java.io.Serializable;

class SearchResult implements Serializable {
    Node solutionNode;
    int bound;

    SearchResult(Node node) {
        this.solutionNode = node;
    }

    SearchResult(int bound) {
        this.bound = bound;
    }
}
