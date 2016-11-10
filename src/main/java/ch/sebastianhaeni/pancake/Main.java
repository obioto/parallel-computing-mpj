package ch.sebastianhaeni.pancake;

import java.util.List;

public class Main {

    public static void main(String[] args) {

//        Node root = new Node(new int[]{46, 53, 18, 4, 56, 11, 57, 59, 55, 25, 6, 34, 41, 13, 39, 52, 7, 33, 43, 21, 32, 51, 5, 1, 58, 49, 54, 9, 24, 17, 19, 27, 50, 42, 22, 30, 20, 3, 14, 28, 40, 10, 12, 29, 37, 8, 36, 2, 26, 38, 16, 60, 15, 48, 35, 45, 31, 44, 23, 47}, 0);
//        Node root = new Node(new int[]{2, 6, 10, 5, 1, 3, 8, 4, 7, 9}, 0);
//        Node root = new Node(new int[]{1, 2, 3}, 0);
//        Node root = new Node(new int[]{11, 7, 15, 6, 17, 3, 19, 16, 4, 10, 8, 12, 20, 14, 2, 5, 1, 9, 13, 18}, 0);
        Node root = new Node(new int[]{5, 2, 7, 10, 13, 16, 14, 6, 8, 15, 11, 1, 12, 3, 4, 9}, 0);

        long start = System.currentTimeMillis();
        Node solution = solve(root);
        long end = System.currentTimeMillis();

        System.out.println((end - start) + "ms passed");

        if (solution == null) {
            System.out.println("No solution found");
            return;
        }
        System.out.println("Found solution after " + solution.getFlipCount() + " flips.");
    }

    private static Node solve(Node root) {
        if (root.isSolution()) {
            return root;
        }

        Node solutionNode = null;
        int bound = root.getOptimisticDistanceToSolution();
        int maxBound = bound * 10;

        while (solutionNode == null) {
            SearchResult result = search(root, bound);

            if (result.solutionNode != null) {
                solutionNode = result.solutionNode;
            }

            if (result.bound >= maxBound) {
                return null;
            }

            bound = result.bound;
        }

        return solutionNode;
    }

    private static SearchResult search(Node node, int bound) {
        int newBound = node.getFlipCount() + node.getOptimisticDistanceToSolution();

        if (newBound > bound) {
            return new SearchResult(newBound);
        }

        if (node.isSolution()) {
            return new SearchResult(node);
        }

        int min = Integer.MAX_VALUE;
        List<Node> successors = node.nextNodes();

        for (Node successor : successors) {
            SearchResult result = search(successor, bound);

            if (result.solutionNode != null) {
                return result;
            }

            if (result.bound < min) {
                min = result.bound;
            }
        }

        return new SearchResult(min);
    }

}
