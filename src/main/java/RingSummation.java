import mpi.MPI;
import mpi.Request;

public final class RingSummation {

	private RingSummation() {
	}

	public static void main(String[] args) {
		int rank;
		int size;
		boolean iAmFirst = false;

		MPI.Init(args);

		rank = MPI.COMM_WORLD.Rank();
		size = MPI.COMM_WORLD.Size();

		int dest = (rank + 1) % size;
		int source = rank - 1;

		if (source < 0) {
			iAmFirst = true;
			source = size - 1;
		}
		int[] sum = new int[1];

		sum[0] = rank;

		Request recvHandle = MPI.COMM_WORLD.Irecv(sum, 0, sum.length, MPI.INT, source, MPI.ANY_TAG);

		if (!iAmFirst) {
			// wait for recv
			recvHandle.Wait();
			sum[0] += rank;
		}

		MPI.COMM_WORLD.Issend(sum, 0, sum.length, MPI.INT, dest, MPI.ANY_TAG);

		if (iAmFirst) {
			recvHandle.Wait();
			System.out.println(sum[0]);
		}

		MPI.Finalize();
	}

}
