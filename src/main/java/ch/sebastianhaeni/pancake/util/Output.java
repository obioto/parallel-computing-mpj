package ch.sebastianhaeni.pancake.util;

import ch.sebastianhaeni.pancake.model.Node;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class Output {

    public static void showSolution(ArrayDeque<Node> nodes, long millis) {
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

    public static void showSolution(ArrayDeque<Node> nodes) {
        ArrayList<Node> solution = new ArrayList<>(nodes);
        String solutionString = "";
        for (int i = 0; i < nodes.size() && solution.get(i).getDepth() >= 0; i++) {
            int[] state = solution.get(i).getState();

            String stateString = getStateRepresentation(solution, i, state);

            solutionString = String.format("state %d: %s\n", nodes.size() - i - 1, stateString) + solutionString;
        }

        System.out.print(solutionString);
    }

    private static String getStateRepresentation(ArrayList<Node> nodes, int i, int[] state) {
        StringBuilder sb = new StringBuilder();
        int flipPosition = -1;
        if (nodes != null && i < nodes.size() - 1) {
            int[] prevState = nodes.get(i + 1).getState();
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
                sb.append("\u001B[34m|\u001B[0m ");
            } else {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

}
