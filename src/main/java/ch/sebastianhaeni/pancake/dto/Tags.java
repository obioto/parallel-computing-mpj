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
     * The worker passes the excess work back to the controller which then can redistribute it.
     */
    EXCESS,

    /**
     * When the worker passes the excess work back to the controller, the controller doesn't know how much work is
     * sent back, this message contains the length of the excess work.
     */
    EXCESS_LENGTH;

    /**
     * Create an integer for MPI.
     *
     * @return unique integer per tag
     */
    public int tag() {
        return this.ordinal();
    }
}
