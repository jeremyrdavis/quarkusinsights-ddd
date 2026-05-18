package io.arrogantprogrammer.quarkusinsights.programming.domain;

/**
 * Value object — the sequential, human-visible number of an episode (Episode 1, Episode 2, …).
 *
 * <p>Layer: domain. The number must be greater than or equal to 1. Cross-aggregate uniqueness
 * (no two episodes share the same number) is not enforceable here because a single aggregate
 * cannot see the others; that rule is enforced in the application service, the persistence
 * adapter, and a database UNIQUE constraint. See SPEC.md "three-place" rule.
 *
 * @param value the underlying integer; ≥ 1
 */
public record EpisodeNumber(int value) {

    /**
     * Canonical constructor enforcing the lower bound.
     *
     * @param value the underlying integer
     * @throws IllegalArgumentException if {@code value} is less than 1
     */
    public EpisodeNumber {
        if (value < 1) {
            throw new IllegalArgumentException("Episode number must be >= 1, was " + value);
        }
    }

    /**
     * Convenience factory mirroring the canonical constructor.
     *
     * @param value the underlying integer
     * @return an {@code EpisodeNumber} wrapping the value
     * @throws IllegalArgumentException if {@code value} is less than 1
     */
    public static EpisodeNumber of(int value) {
        return new EpisodeNumber(value);
    }
}
