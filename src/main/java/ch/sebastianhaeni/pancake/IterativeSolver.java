package ch.sebastianhaeni.pancake;

import ch.sebastianhaeni.pancake.model.Node;
import ch.sebastianhaeni.pancake.util.Generator;

import java.util.Stack;

import static ch.sebastianhaeni.pancake.util.Output.showSolution;

public final class IterativeSolver {

    private static final Stack<Node> NODES = new Stack<>();

    private IterativeSolver() {
    }

    public static void main(String[] args) {

        Node root = new Node(Generator.alternate(20));
//        Node root = new Node(Generator.random(40));

        System.out.format("Solving a pancake pile of height %d.\n", root.getState().length);

        long start = System.currentTimeMillis();
        solve(root);
        long end = System.currentTimeMillis();

        showSolution(NODES, end - start);
    }

    private static void solve(Node root) {
        root = root.augment();
        NODES.push(root);
        NODES.peek().nextNodes();

        int bound = root.getDistance();
        int candidateBound = Integer.MAX_VALUE;

        while (NODES.peek().gap() > 0) {
            if (NODES.peek().getDistance() + NODES.peek().getDepth() > bound) {
                int stateBound = NODES.peek().getDepth() + NODES.peek().getDistance();
                if (stateBound < candidateBound) {
                    candidateBound = stateBound;
                }
                NODES.pop();
            } else if (NODES.peek().getChildren().empty()) {
                if (NODES.peek().getDepth() == 0) {
                    bound = candidateBound;
                    System.out.println("Searching with bound " + candidateBound);
                    candidateBound = Integer.MAX_VALUE;
                    NODES.peek().nextNodes();
                } else {
                    NODES.pop();
                }
            } else {
                NODES.push(NODES.peek().getChildren().pop());
                NODES.peek().nextNodes();
            }
        }
    }

}
