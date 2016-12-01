package ch.sebastianhaeni.pancake.dto;

public enum Tags {
    /**
     * Controller passes WORK to a worker.
     */
    WORK,

    /**
     * A worker passes a result back to the controller.
     */
    RESULT,

    /**
     * A controller passes a kill message to all workers so they can shut down and stop computing.
     */
    KILL,

    /**
     * A worker can tell the controller that it is idling.
     */
    IDLE,

    /**
     * The controller commands the worker to split it's work and send back half of it.
     */
    SPLIT,

    /**
     * Sent when the worker is working again.
     */
    WORKING;

    /**
     * Create an integer for MPI.
     *
     * @return unique integer per tag
     */
    public int tag() {
        return this.ordinal();
    }
}
