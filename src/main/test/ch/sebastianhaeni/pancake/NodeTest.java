package ch.sebastianhaeni.pancake;

import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

public class NodeTest {

    @Test
    public void testFlip1() {
        Node node = new Node(new int[]{1, 2});
        Node flip = node.flip(1);
        assertArrayEquals(new int[]{1, 2}, flip.getPancakes());
    }

    @Test
    public void testFlip2() {
        Node node = new Node(new int[]{1, 2, 3});
        Node flip = node.flip(2);
        assertArrayEquals(new int[]{2, 1, 3}, flip.getPancakes());
    }

    @Test
    public void testFlip3() {
        Node node = new Node(new int[]{2, 6, 10, 5, 1, 3, 8, 4, 7, 9});
        Node flip = node.flip(5);
        assertArrayEquals(new int[]{1, 5, 10, 6, 2, 3, 8, 4, 7, 9}, flip.getPancakes());
    }

    @Test
    public void testIsSolution1() {
        Node node = new Node(new int[]{1, 2, 3});
        assertTrue(node.isSolution());
    }

    @Test
    public void testIsSolution2() {
        Node node = new Node(new int[]{2, 1, 3});
        assertFalse(node.isSolution());
    }

    @Test
    public void testNextNodes() {
        Node node = new Node(new int[]{2, 1, 3});
        List<Node> nodes = node.nextNodes();

        assertEquals(2, nodes.size());
        assertArrayEquals(nodes.get(0).getPancakes(), new int[]{1, 2, 3});
        assertArrayEquals(nodes.get(1).getPancakes(), new int[]{3, 1, 2});
    }
}
