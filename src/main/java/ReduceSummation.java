import mpi.MPI;

public final class ReduceSummation {

	private ReduceSummation() {
	}

	public static void main(String[] args) {
		int rank;
		int size;

		MPI.Init(args);

		rank = MPI.COMM_WORLD.Rank();
		size = MPI.COMM_WORLD.Size();

		int[] rankSend = { rank };
		int[] sum = new int[1];

		MPI.COMM_WORLD.Allreduce(rankSend, 0, sum, 0, 1, MPI.INT, MPI.SUM);

		System.out.println("Rank " + rank + ": " + sum[0]);

		MPI.Finalize();
	}

}
