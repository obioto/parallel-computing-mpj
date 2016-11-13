package ch.sebastianhaeni.pancake.listener;

import ch.sebastianhaeni.pancake.dto.SearchResult;
import ch.sebastianhaeni.pancake.dto.Tags;
import mpi.MPI;
import mpi.Request;

import java.util.concurrent.LinkedBlockingQueue;

public class ResultListener extends PancakeListener {
    private final LinkedBlockingQueue<SearchResult> results;

    public ResultListener(int worker, LinkedBlockingQueue<SearchResult> results) {
        super(worker);
        this.results = results;
    }

    @Override
    protected void listen() {
        SearchResult result[] = new SearchResult[1];
        Request request = MPI.COMM_WORLD.Irecv(result, 0, 1, MPI.OBJECT, worker, Tags.RESULT.tag());

        if (block(request)) return;

        results.add(result[0]);
    }

}
