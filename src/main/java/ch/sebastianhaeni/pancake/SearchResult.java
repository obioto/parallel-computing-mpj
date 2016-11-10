package ch.sebastianhaeni.pancake;

class SearchResult {
    Node solutionNode;
    int bound;

    SearchResult(Node node) {
        this.solutionNode = node;
    }

    SearchResult(int bound) {
        this.bound = bound;
    }
}
