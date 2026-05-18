package io.arrogantprogrammer.quarkusinsights.programming.domain;

/**
 * Domain exception — raised when a behavior method is invoked from a state in which it is
 * not legal (e.g., publishing what is not LIVE, canceling what is already CANCELED).
 *
 * <p>Layer: domain. Translated by the REST adapter to HTTP 409.
 */
public class IllegalEpisodeStateException extends RuntimeException {

    private final EpisodeStatus currentStatus;
    private final String operation;

    /**
     * Create a new state-machine conflict exception.
     *
     * @param currentStatus the status the aggregate was in when the operation was attempted; never {@code null}
     * @param operation     the name of the attempted operation (for human-readable messages); never {@code null}
     */
    public IllegalEpisodeStateException(EpisodeStatus currentStatus, String operation) {
        super("Cannot " + operation + " an episode in status " + currentStatus);
        this.currentStatus = currentStatus;
        this.operation = operation;
    }

    /**
     * The status the aggregate was in when the operation was rejected.
     *
     * @return the current status
     */
    public EpisodeStatus currentStatus() {
        return currentStatus;
    }

    /**
     * The name of the attempted operation.
     *
     * @return the operation name
     */
    public String operation() {
        return operation;
    }
}
