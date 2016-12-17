package ch.sebastianhaeni.pancake.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

public class GeneratorTest {

    @Test
    public void testAlternateEven() {
        int[] state = Generator.alternate(10);

        System.out.println(Arrays.toString(state));
        assertArrayEquals(new int[]{2, 1, 4, 3, 6, 5, 8, 7, 10, 9}, state);
    }

    @Test
    public void testAlternateOdd() {
        int[] state = Generator.alternate(9);

        System.out.println(Arrays.toString(state));
        assertArrayEquals(new int[]{2, 1, 4, 3, 6, 5, 8, 7, 9}, state);
    }
}
