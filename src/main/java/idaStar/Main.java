package idaStar;
import java.util.List;

public class Main {

	public static void main(String[] args) {

		Node root = new Node();

		Node solution = solve(root);
	}

	private static Node solve(Node root) {
		Node solutionNode = null;
		int bound = root.getOptimisticDistanceToSolution();
		// 10 ist ein willkürlich gewählter Faktor zur Begrenzung der Suche
		int maxBound = bound * 10;
		while (solutionNode == null) {
			SearchResult r = search(root, bound);
			if (r.solutionNode != null) {
				solutionNode = r.solutionNode;
			}
			if (r.t >= maxBound) {
				return null;
			}
			bound = r.t;
		}
		return solutionNode;
	}

	private static SearchResult search(Node node, int bound) {
		int f = node.getMovesDone() + node.getOptimisticDistanceToSolution();
		if (f > bound) {
			return new SearchResult(f);
		}
		if (node.isSolution()) {
			return new SearchResult(node);
		}
		int min = Integer.MAX_VALUE;
		List<Node> successors = node.nextNodes();
		for (Node succ : successors) {
			SearchResult r = search(succ, bound);
			if (r.solutionNode != null) {
				return r;
			}
			if (r.t < min) {
				min = r.t;
			}
		}
		return new SearchResult(min);
	}

}
