package ch.sebastianhaeni.pancake;

import ch.sebastianhaeni.pancake.dto.Node;

import java.util.Arrays;
import java.util.Stack;

public class RecursiveSolver {

    private static Stack<Node> stack = new Stack<>();
    private static int candidateBound = Integer.MAX_VALUE;

    private RecursiveSolver() {
    }

    public static void main(String[] args) {

                Node root = new Node(new int[]{46, 53, 18, 4, 56, 11, 57, 59, 55, 25, 6, 34, 41, 13, 39, 52, 7, 33, 43, 21, 32, 51, 5, 1, 58, 49, 54, 9, 24, 17, 19, 27, 50, 42, 22, 30, 20, 3, 14, 28, 40, 10, 12, 29, 37, 8, 36, 2, 26, 38, 16, 60, 15, 48, 35, 45, 31, 44, 23, 47});
        //        Node root = new Node(new int[]{2, 6, 10, 5, 1, 3, 8, 4, 7, 9});
        //        Node root = new Node(new int[]{3, 1, 2});
        //        Node root = new Node(new int[]{11, 7, 15, 6, 17, 3, 19, 16, 4, 10, 8, 12, 20, 14, 2, 5, 1, 9, 13, 18});
        //        Node root = new Node(new int[]{5, 2, 7, 10, 13, 16, 14, 6, 8, 18, 15, 11, 1, 12, 3, 4, 9, 17});
//        Node root = new Node(new int[] { 4, 25, 15, 7, 28, 17, 6, 5, 23, 29, 19, 27, 13, 21, 9, 1, 26, 22, 11, 18, 16, 12, 10, 30, 2, 3, 8, 14, 24, 20 });
//        Node root = new Node(new int[]{2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11});

        System.out.format("Solving a pancake pile of height %d.\n", root.getState().length);

        long start = System.currentTimeMillis();
        solve(root);
        long end = System.currentTimeMillis();

        showSolution(start, end);
    }

    private static void showSolution(long start, long end) {
        System.out.format("%dms passed\n", (end - start));
        System.out.format("Found solution after %d flips.\n", stack.size());

        StringBuilder sb = new StringBuilder();

        while (!stack.isEmpty()) {
            sb.insert(0, Arrays.toString(stack.pop().getState()) + '\n');
        }

        System.out.println(sb.toString());
    }

    private static void solve(Node root) {
        root = root.augment();
        stack.push(root);
        stack.peek().calcDistance();
        stack.peek().nextNodes();
        int bound = root.getDistance();

        int stateBound;

        while (stack.peek().getDistance() != 0) {
            if (stack.peek().getDistance() + stack.peek().getDepth() > bound) {
                stateBound = stack.peek().getDepth() + stack.peek().getDistance();
                candidateBound = stateBound < candidateBound ? stateBound : candidateBound;
                stack.pop();
            } else if (stack.peek().getChildren().empty()) {
                if (stack.peek().getDepth() == 0) {
                    bound = candidateBound;
                    System.out.println("Searching with bound " + candidateBound);
                    candidateBound = Integer.MAX_VALUE;
                    stack.peek().nextNodes();
                } else {
                    stack.pop();
                }
            } else {
                stack.push(stack.peek().getChildren().pop());
                stack.peek().nextNodes();
            }
        }
    }


}
