package ch.sebastianhaeni.pancake.processor;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.sebastianhaeni.pancake.dto.Node;
import ch.sebastianhaeni.pancake.dto.WorkPacket;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class WorkerTest {

	@Test
	public void testSplitWork() {
		Worker worker = new Worker(0);
		Node n1 = new Node(new int[] { 1, 2, 3, 4 });
		Node n2 = new Node(new int[] { 2, 3, 4, 1 });
		Node n3 = new Node(new int[] { 3, 4, 1, 2 });
		Node n4 = new Node(new int[] { 4, 1, 2, 3 });
		List<Node> list = new ArrayList<>();
		list.add(n1);
		list.add(n2);
		list.add(n3);
		list.add(n4);
		WorkPacket[] workPackets = worker.splitWork(10, list);

		assertEquals(2, workPackets.length);
		assertArrayEquals(new int[] { 1, 2, 3, 4 }, workPackets[0].getNode().getState());
		assertArrayEquals(new int[] { 3, 4, 1, 2 }, workPackets[1].getNode().getState());

		assertArrayEquals(new int[] { 2, 3, 4, 1 }, list.get(0).getState());
		assertArrayEquals(new int[] { 4, 1, 2, 3 }, list.get(1).getState());
	}

}
