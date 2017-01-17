package ch.sebastianhaeni.pancake.util;

import java.util.LinkedList;

import org.junit.Test;

import ch.sebastianhaeni.pancake.model.Node;

import static org.junit.Assert.assertEquals;

public class PartitionTest {

    @Test
    public void testPartition() {
        LinkedList<Node> nodes = new LinkedList<>();
        nodes.push(new Node(new int[]{1, 3, 2, 4}));
        nodes.push(new Node(new int[]{2, 3, 1, 4}));
        Partition p = new Partition(nodes, 2);

        assertEquals(2, p.get(0).size());
        assertEquals(2, p.get(1).size());
    }

}
