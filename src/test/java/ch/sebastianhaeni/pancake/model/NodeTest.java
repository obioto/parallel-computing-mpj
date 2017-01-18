package ch.sebastianhaeni.pancake.model;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class NodeTest {

    @Test
    public void testFlip1() {
        Node node = new Node(new int[]{1, 2});
        Node flip = node.flip(1, 0);
        assertArrayEquals(new int[]{1, 2}, flip.getState());
    }

    @Test
    public void testFlip2() {
        Node node = new Node(new int[]{1, 2, 3});
        Node flip = node.flip(2, 0);
        assertArrayEquals(new int[]{2, 1, 3}, flip.getState());
    }

    @Test
    public void testFlip3() {
        Node node = new Node(new int[]{2, 6, 10, 5, 1, 3, 8, 4, 7, 9});
        Node flip = node.flip(5, 0);
        assertArrayEquals(new int[]{1, 5, 10, 6, 2, 3, 8, 4, 7, 9}, flip.getState());
    }

    @Test
    public void testNextNodes1() {
        Node node = new Node(new int[]{4, 2, 5, 1, 3});
        node.nextNodes();

        assertEquals(3, node.getChildren().size());
        assertArrayEquals(node.getChildren().pop().getState(), new int[]{1, 5, 2, 4, 3});
        assertArrayEquals(node.getChildren().pop().getState(), new int[]{5, 2, 4, 1, 3});
        assertArrayEquals(node.getChildren().pop().getState(), new int[]{2, 4, 5, 1, 3});
    }

    @Test
    public void testNextNodes2() {
        Node node = new Node(new int[]{1, 2, 3, 4});
        node.nextNodes();

        assertEquals(1, node.getChildren().size());
        assertArrayEquals(node.getChildren().pop().getState(), new int[]{3, 2, 1, 4});
    }

    @Test
    public void testNextNodes3() {
        Node node = new Node(new int[]{2, 1, 4, 3, 6, 5, 8, 7, 10, 9, 12, 11, 14, 13, 15});
        node.nextNodes();

        assertEquals(2, node.getChildren().size());
        assertArrayEquals(node.getChildren().pop().getState(), new int[]{4, 5, 6, 3, 2, 1, 7});
        assertArrayEquals(node.getChildren().pop().getState(), new int[]{3, 2, 1, 6, 5, 4, 7});
    }

    @Test
    public void testNextNodes4() {
        Node node = new Node(new int[]{1, 2, 3, 6, 5, 4, 7});
        node.nextNodes();

        assertEquals(2, node.getChildren().size());
        assertArrayEquals(node.getChildren().pop().getState(), new int[]{4, 5, 6, 3, 2, 1, 7});
        assertArrayEquals(node.getChildren().pop().getState(), new int[]{3, 2, 1, 6, 5, 4, 7});
    }

    @Test
    public void testAugment() {
        Node node = new Node(new int[]{4, 2, 1, 3});
        assertArrayEquals(node.augment().getState(), new int[]{4, 2, 1, 3, 5});
    }
}
