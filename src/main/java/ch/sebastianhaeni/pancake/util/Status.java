package ch.sebastianhaeni.pancake.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Status {
    private static final Logger LOG = LogManager.getLogger("Status");

    private boolean done = false;

    public boolean isDone() {
        return done;
    }

    public void done() {
        LOG.info("done");
        this.done = true;
    }

}
