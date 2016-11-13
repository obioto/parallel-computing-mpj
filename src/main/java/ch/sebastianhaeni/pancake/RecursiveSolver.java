package ch.sebastianhaeni.pancake;

import ch.sebastianhaeni.pancake.dto.Node;
import ch.sebastianhaeni.pancake.dto.SearchResult;

import java.util.Arrays;
import java.util.List;

public class RecursiveSolver {

    public static void main(String[] args) {

//        Node root = new Node(new int[]{46, 53, 18, 4, 56, 11, 57, 59, 55, 25, 6, 34, 41, 13, 39, 52, 7, 33, 43, 21, 32, 51, 5, 1, 58, 49, 54, 9, 24, 17, 19, 27, 50, 42, 22, 30, 20, 3, 14, 28, 40, 10, 12, 29, 37, 8, 36, 2, 26, 38, 16, 60, 15, 48, 35, 45, 31, 44, 23, 47});
//        Node root = new Node(new int[]{2, 6, 10, 5, 1, 3, 8, 4, 7, 9});
//        Node root = new Node(new int[]{3, 1, 2});
//        Node root = new Node(new int[]{11, 7, 15, 6, 17, 3, 19, 16, 4, 10, 8, 12, 20, 14, 2, 5, 1, 9, 13, 18});
//        Node root = new Node(new int[]{5, 2, 7, 10, 13, 16, 14, 6, 8, 18, 15, 11, 1, 12, 3, 4, 9, 17});
        Node root = new Node(new int[]{4,25,15,7,28,17,6,5,23,29,19,27,13,21,9,1,26,22,11,18,16,12,10,30,2,3,8,14,24,20});

        System.out.format("Solving a pancake pile of height %d.\n", root.getState().length);

        long start = System.currentTimeMillis();
        Node solution = solve(root);
        long end = System.currentTimeMillis();

        System.out.format("%dms passed\n", (end - start));

        if (solution == null) {
            System.out.println("No solution found");
            return;
        }
        System.out.format("Found solution after %d flips.\n", solution.getDepth());

        StringBuilder sb = new StringBuilder();
        Node current = solution;
        while (current.getParent() != null) {
            sb.insert(0, Arrays.toString(current.getParent().getState()) + "\n");
            current = current.getParent();
        }

        System.out.println(sb.toString());
    }

    private static Node solve(Node root) {
        if (root.isSolution()) {
            return root;
        }

        Node solutionNode = null;
        int bound = root.getOptimisticDistanceToSolution();
        int maxBound = bound * 10;

        while (solutionNode == null) {
            System.out.println("Searching with bound " + bound);
            SearchResult result = search(root, bound);

            if (result.getSolutionNode() != null) {
                solutionNode = result.getSolutionNode();
            }

            if (result.getBound() >= maxBound) {
                return null;
            }

            bound = result.getBound();
        }

        return solutionNode;
    }

    private static SearchResult search(Node node, int bound) {
        int newBound = node.getDepth() + node.getOptimisticDistanceToSolution();

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

            if (result.getSolutionNode() != null) {
                return result;
            }

            if (result.getBound() < min) {
                min = result.getBound();
            }
        }

        return new SearchResult(min);
    }

}
