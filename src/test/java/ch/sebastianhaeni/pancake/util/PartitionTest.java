package ch.sebastianhaeni.pancake.util;

import ch.sebastianhaeni.pancake.model.Node;
import org.junit.Test;

import java.util.Stack;

import static org.junit.Assert.assertEquals;

public class PartitionTest {

    @Test
    public void testPartition() {
        Stack<Node> stack = new Stack<>();
        stack.push(new Node(new int[]{1, 3, 2, 4}));
        stack.push(new Node(new int[]{2, 3, 1, 4}));
        Partition p = new Partition(stack, 2);

        assertEquals(2, p.get(0).size());
        assertEquals(2, p.get(1).size());
    }

}
