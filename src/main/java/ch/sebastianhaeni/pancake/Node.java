package ch.sebastianhaeni.pancake;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

class Node {
    private final int[] pancakes;
    private final int flipCount;

    Node(int[] pancakes, int flipCount) {
        this.pancakes = pancakes;
        this.flipCount = flipCount;
    }

    int getOptimisticDistanceToSolution() {
        int distance = 0;
        int current = 1;

        for (int i = 1; i < pancakes.length; i++) {
            int pancake = pancakes[i];
            if (Math.abs(pancake - current) != 1) {
                distance++;
            }
            current = pancake;
        }

        return distance;
    }

    int getFlipCount() {
        return flipCount;
    }

    boolean isSolution() {
        int current = 1;

        for (int i = 1; i < pancakes.length; i++) {
            int pancake = pancakes[i];
            if (pancake - current != 1) {
                return false;
            }
            current = pancake;
        }

        return true;
    }

    List<Node> nextNodes() {
        AbstractList<Node> list = new ArrayList<>();

        int current = pancakes[1];

        for (int i = 1; i < pancakes.length; i++) {
            int pancake = pancakes[i];
            if (pancake - current != 1) {
                list.add(flip(i + 1));
            }
            current = pancake;
        }

        return list;
    }

    Node flip(int flipPosition) {

        int[] flipped = new int[pancakes.length];

        for (int i = 0; i < flipPosition; i++) {
            flipped[i] = pancakes[flipPosition - i - 1];
        }

        System.arraycopy(pancakes, flipPosition, flipped, flipPosition, pancakes.length - flipPosition);

        return new Node(flipped, getFlipCount() + 1);
    }

    int[] getPancakes() {
        return pancakes;
    }
}
