package ch.sebastianhaeni.pancake.util;

import ch.sebastianhaeni.pancake.model.Node;

import java.util.Vector;

public class Output {

    public static void showSolution(Vector<Node> nodes, long millis) {
        showSolution(nodes);
        System.out.format("time: %f sec\n", millis / 1000f);
    }

    public static void showSolution(Vector<Node> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            int[] state = nodes.get(i).getState();

            StringBuilder sb = new StringBuilder();
            int flipPosition = -1;
            if (i > 0) {
                int[] prevState = nodes.get(i - 1).getState();
                for (int j = state.length - 1; j >= 0; j--) {
                    if (state[j] != prevState[j]) {
                        flipPosition = j;
                        break;
                    }
                }
            }
            for (int j = 0; j < state.length; j++) {
                sb.append(state[j]);
                if (j == state.length - 1) {
                    break;
                }
                if (flipPosition == j) {
                    sb.append("| ");
                } else {
                    sb.append(", ");
                }
            }

            System.out.format("state %d: %s\n", i, sb.toString());
        }
    }

}
