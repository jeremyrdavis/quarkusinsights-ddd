package io.arrogantprogrammer.quarkusinsights.programming.domain;

/**
 * Domain exception — raised when {@link Episode#publish()} is invoked but the aggregate
 * is missing one or more required pieces of content (abstract, presenter, or speaker).
 *
 * <p>Layer: domain. Translated by the REST adapter to HTTP 409.
 */
public class MissingPublishPreconditionException extends RuntimeException {

    private final String missing;

    /**
     * Create a new missing-precondition exception.
     *
     * @param missing the human-readable name of the missing piece of content; never {@code null}
     *                (e.g., {@code "abstract"}, {@code "presenter"}, {@code "speaker"})
     */
    public MissingPublishPreconditionException(String missing) {
        super("Cannot publish episode: missing " + missing);
        this.missing = missing;
    }

    /**
     * The name of the missing precondition.
     *
     * @return the missing field name
     */
    public String missing() {
        return missing;
    }
}
