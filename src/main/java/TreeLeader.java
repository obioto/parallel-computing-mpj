import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mpi.MPI;
import mpi.Status;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class TreeLeader {

	private static int rank = 0;
	private static int size = 0;

	// message types
	private static final int IS_CANDIDATE = 0;
	private static final int IS_LEADER = 1;

	// nb. of processes (i.e nodes of the graph) 
	static final int N = 10;
	// incidence list of our graph  
	//
	//   0  1
	//   |    
	// 2-3-4  5-6-7
	//   |    |
	//   8    9
	//
	static final int INC_LIST[][] = {
					{ 3 },          // 0
					{},             // 1
					{ 3 },          // 2
					{ 0, 2, 4, 8 }, // 3
					{ 3 },          // 4
					{ 6, 9 },       // 5
					{ 5, 7 },       // 6
					{ 6 },          // 7
					{ 3 },          // 8
					{ 5 } };        // 9
	private static int[] neighbours = null;

	private TreeLeader() {
	}

	public static int findLeader() {
		int currentLeader = getRank();
		int[] buf = new int[1];
		buf[0] = currentLeader;

		if (neighbours.length == 0) {
			// I'm alone
			return buf[0];
		}

		if (neighbours.length == 1) {
			// I'm a leaf
			MPI.COMM_WORLD.Send(buf, 0, 1, MPI.INT, neighbours[0], IS_CANDIDATE);
			MPI.COMM_WORLD.Recv(buf, 0, 1, MPI.INT, neighbours[0], IS_LEADER);

			return buf[0];
		}

		// I'm connected to more than 1 node
		List<Integer> received = new ArrayList<>();

		while (received.size() != neighbours.length - 1) {
			Status recv = MPI.COMM_WORLD.Recv(buf, 0, 1, MPI.INT, MPI.ANY_SOURCE, IS_CANDIDATE);
			if (buf[0] > currentLeader) {
				currentLeader = buf[0];
			}
			received.add(recv.source);
		}

		int dest = Arrays.stream(neighbours)
						.filter(neighbour -> !received.contains(neighbour))
						.findFirst()
						.orElseThrow(() -> new IllegalStateException("Not supposed to happen"));

		buf[0] = currentLeader;
		MPI.COMM_WORLD.Isend(buf, 0, 1, MPI.INT, dest, IS_CANDIDATE);

		Status recv = MPI.COMM_WORLD.Recv(buf, 0, 1, MPI.INT, dest, MPI.ANY_TAG);

		if (recv.tag == IS_CANDIDATE) {
			if (buf[0] > currentLeader) {
				currentLeader = buf[0];
			}
			buf[0] = currentLeader;
			received.add(dest);
		}

		received.forEach(node -> MPI.COMM_WORLD.Isend(buf, 0, 1, MPI.INT, node, IS_LEADER));

		return currentLeader;
	}

	public static void main(String[] args) {
		MPI.Init(args);
		setSize(MPI.COMM_WORLD.Size());
		setRank(MPI.COMM_WORLD.Rank());
		if (getSize() != N) {
			System.out.println("run with -np " + N);
		} else {
			setNeighbours(INC_LIST[getRank()]); // our edges in the tree graph
			int leader = findLeader();
			System.out.println("******rank " + getRank() + ", leader: " + leader);
		}
		MPI.Finalize();
	}

	public static void setNeighbours(int[] neighbours) {
		TreeLeader.neighbours = neighbours;
	}

	public static int getSize() {
		return size;
	}

	public static void setSize(int size) {
		TreeLeader.size = size;
	}

	public static int getRank() {
		return rank;
	}

	public static void setRank(int rank) {
		TreeLeader.rank = rank;
	}
}
 