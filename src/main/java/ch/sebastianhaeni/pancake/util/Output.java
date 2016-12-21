package ch.sebastianhaeni.pancake.util;

import ch.sebastianhaeni.pancake.model.Node;

import java.util.Vector;

public class Output {

    public static void showSolution(Vector<Node> nodes, long millis) {
        showSolution(nodes);
        showTime(millis);
    }

    public static void showCount(int[] state, int count, long millis) {
        System.out.format("Der folgende Stapel hat %d Loesungen:\n", count);

        String stateString = getStateRepresentation(null, -1, state);
        System.out.format("state 0: %s\n", stateString);
        showTime(millis);
    }

    private static void showTime(long millis) {
        System.out.format("time: %f sec\n", millis / 1000f);
    }

    public static void showSolution(Vector<Node> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            int[] state = nodes.get(i).getState();

            String stateString = getStateRepresentation(nodes, i, state);

            System.out.format("state %d: %s\n", i, stateString);
        }
    }

    private static String getStateRepresentation(Vector<Node> nodes, int i, int[] state) {
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
        return sb.toString();
    }

}
