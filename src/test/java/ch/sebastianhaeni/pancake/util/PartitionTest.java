package ch.sebastianhaeni.pancake.util;
import java.util.Stack;

import org.junit.Test;

import ch.sebastianhaeni.pancake.dto.Node;

import static org.junit.Assert.assertEquals;

public class PartitionTest {

    @Test
    public void testPartition() {
        Stack<Node> stack = new Stack<>();
        stack.push(new Node(new int[] { 1 }));
        stack.push(new Node(new int[] { 2 }));
        Partition p = new Partition(stack, 2);

        assertEquals(2, p.size());
        assertEquals(1, p.get(0).size());
        assertEquals(1, p.get(1).size());
        assertEquals(1, p.get(0).get(0).getState()[0]);
        assertEquals(2, p.get(1).get(0).getState()[0]);
    }

}
