package io.arrogantprogrammer.quarkusinsights.programming.domain;

/**
 * Domain exception — raised when a scheduling attempt would create a second episode with
 * a number that is already in use.
 *
 * <p>Layer: domain. Thrown in three places per the spec's three-place uniqueness rule: by the
 * application service after a pre-check, by the persistence adapter when it catches a
 * database UNIQUE constraint violation, and (defensively) by any other code path that adds
 * a new episode. Translated by the REST adapter to HTTP 409.
 */
public class DuplicateEpisodeNumberException extends RuntimeException {

    private final EpisodeNumber number;

    /**
     * Create a new duplicate-number exception.
     *
     * @param number the offending episode number; never {@code null}
     */
    public DuplicateEpisodeNumberException(EpisodeNumber number) {
        super("Episode number already exists: " + number.value());
        this.number = number;
    }

    /**
     * The episode number that was already in use.
     *
     * @return the offending number
     */
    public EpisodeNumber number() {
        return number;
    }
}
