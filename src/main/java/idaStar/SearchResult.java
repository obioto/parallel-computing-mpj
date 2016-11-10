package idaStar;
public class SearchResult {
	public Node solutionNode;
	public int t;

	public SearchResult(Node node) {
		this.solutionNode = node;
	}

	public SearchResult(int f) {
		this.t = f;
	}
}
