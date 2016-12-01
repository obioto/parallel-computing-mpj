package ch.sebastianhaeni.pancake.dto;

import org.junit.Test;

import static org.junit.Assert.*;

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
    public void testNextNodes() {
        Node node = new Node(new int[]{4, 2, 5, 1, 3});
        node.nextNodes();

        assertEquals(3, node.getChildren().size());
        assertArrayEquals(node.getChildren().get(0).getState(), new int[]{2, 4, 5, 1, 3});
        assertArrayEquals(node.getChildren().get(1).getState(), new int[]{5, 2, 4, 1, 3});
        assertArrayEquals(node.getChildren().get(2).getState(), new int[]{1, 5, 2, 4, 3});
    }

    @Test
    public void testAugment() {
        Node node = new Node(new int[]{4, 2, 1, 3});
        assertArrayEquals(node.augment().getState(), new int[]{4, 2, 1, 3, 5});
    }
}
