package ch.sebastianhaeni.pancake;

import ch.sebastianhaeni.pancake.model.Node;
import ch.sebastianhaeni.pancake.util.Generator;
import ch.sebastianhaeni.pancake.util.Mode;

import java.util.ArrayDeque;

import static ch.sebastianhaeni.pancake.util.Output.showCount;
import static ch.sebastianhaeni.pancake.util.Output.showSolution;

public final class IterativeSolver {

    // Change mode here!
    private static final Mode CURRENT_MODE = Mode.SOLVE;

    // Start with alternating sequence
    private static final int[] INITIAL_STATE = Generator.alternate(14);

    // Start with random sequence
    // private static final int[] INITIAL_STATE = Generator.random(25));

    // Start with predetermined sequence
    // private static final int[] INITIAL_STATE = new int[]{2, 1, 3, 4});


    private static final ArrayDeque<Node> NODES = new ArrayDeque<>();

    private IterativeSolver() {
    }

    public static void main(String[] args) {
        Node root = (new Node(INITIAL_STATE)).augment();

        switch (CURRENT_MODE) {
            case COUNT:
                countSolutions(root);
                break;
            case SOLVE:
                findSolution(root);
                break;
        }
    }

    private static void findSolution(Node root) {
        System.out.format("Solving a pancake pile of height %d.\n", root.getState().length);

        long start = System.currentTimeMillis();
        solve(root);
        long end = System.currentTimeMillis();

        showSolution(NODES, end - start);
    }

    private static void countSolutions(Node root) {
        System.out.format("Counting solutions for a pancake pile of height %d.\n", root.getState().length);

        long start = System.currentTimeMillis();
        int count = count(root);
        long end = System.currentTimeMillis();

        showCount(root.getState(), count, end - start);
    }

    private static void solve(Node root) {
        NODES.push(root);
        NODES.peek().nextNodes();

        int bound = root.getGap();
        System.out.println("Searching with bound " + bound);
        int candidateBound = Integer.MAX_VALUE;

        // Loop as long as we haven't found the solution.
        // We've found the solution if the gap is 0.
        while (NODES.peek().getGap() > 0) {
            // Calc our current bound for IDA*
            int stateBound = NODES.peek().getGap() + NODES.peek().getDepth();
            if (stateBound > bound) {
                if (stateBound < candidateBound) {
                    // New IDA* bound required, we save it here
                    candidateBound = stateBound;
                }
                NODES.pop();
            } else if (NODES.peek().getChildren().isEmpty()) {
                if (NODES.peek().getDepth() == 0) {
                    // Out of work and haven't found the solution yet
                    // We must go deeper!
                    bound = candidateBound;
                    System.out.println("Searching with bound " + candidateBound);
                    candidateBound = Integer.MAX_VALUE;
                    NODES.peek().nextNodes();
                } else {
                    // All children of the current node have been explored
                    NODES.pop();
                }
            } else {
                // Add a child to the nodes to work on
                NODES.push(NODES.peek().getChildren().pop());
                NODES.peek().nextNodes();
            }
        }
    }

    private static int count(Node root) {
        NODES.push(root);
        NODES.peek().nextNodes();

        int bound = root.getGap();
        System.out.println("Searching with bound " + bound);
        int candidateBound = Integer.MAX_VALUE;

        int count = 0;

        // Loop as long as we haven't found the solution.
        // We've found the solution if the gap is 0.
        while (!NODES.isEmpty()) {
            if (NODES.peek().getGap() == 0) {
                if (count == 0) {
                    System.out.format("Found solution with bound %d. Starting to count...\n", bound);
                }
                NODES.pop();
                count++;
                continue;
            }

            // Calc our current bound for IDA*
            int stateBound = NODES.peek().getGap() + NODES.peek().getDepth();
            if (stateBound > bound) {
                if (stateBound < candidateBound) {
                    // New IDA* bound required, we save it here
                    candidateBound = stateBound;
                }
                NODES.pop();
            } else if (NODES.peek().getChildren().isEmpty()) {
                if (NODES.peek().getDepth() == 0 && count == 0) {
                    // Out of work and haven't found the solution yet
                    // We must go deeper!
                    bound = candidateBound;
                    System.out.println("Searching with bound " + candidateBound);
                    candidateBound = Integer.MAX_VALUE;
                    NODES.peek().nextNodes();
                } else {
                    // All children of the current node have been explored
                    NODES.pop();
                }
            } else {
                // Add a child to the nodes to work on
                NODES.push(NODES.peek().getChildren().pop());
                NODES.peek().nextNodes();
            }
        }

        return count;
    }

}
