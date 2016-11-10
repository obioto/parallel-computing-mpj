package idaStar;
import java.util.List;

public class Node {
	public int getOptimisticDistanceToSolution() {
		return 0;
	}

	public int getMovesDone() {
		return 0;
	}

	public boolean isSolution() {
		return false;
	}

	public List<Node> nextNodes() {
		return null;
	}
}
