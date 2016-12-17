package ch.sebastianhaeni.pancake.util;

import java.util.Random;

public class Generator {

    public static int[] random(int length) {
        int[] state = sequence(length);

        shuffleArray(state);

        return state;
    }

    public static int[] alternate(int length) {
        int[] state = sequence(length);

        for (int i = 0; i < length; i += 2) {
            if (i + 1 == length) {
                break;
            }
            int a = state[i];
            state[i] = state[i + 1];
            state[i + 1] = a;
        }

        return state;
    }

    private static int[] sequence(int length) {
        int[] state = new int[length];
        for (int i = 0; i < length; i++) {
            state[i] = i + 1;
        }
        return state;
    }

    private static void shuffleArray(int[] array) {
        Random rnd = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            int a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
    }
}
