package ch.sebastianhaeni.pancake.util;

public class Status {
    private boolean done = false;

    public boolean isDone() {
        return done;
    }

    public void done() {
        this.done = true;
    }
}
