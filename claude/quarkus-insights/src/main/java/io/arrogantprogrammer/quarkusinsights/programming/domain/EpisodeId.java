package io.arrogantprogrammer.quarkusinsights.programming.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object — opaque identifier for an {@link Episode} aggregate.
 *
 * <p>Layer: domain. Wraps a UUID so callers cannot accidentally swap it for an unrelated
 * identifier (a Person ID, an Abstract ID, etc.). Generated when an episode is scheduled
 * and immutable thereafter.
 *
 * @param value the underlying UUID; never {@code null}
 */
public record EpisodeId(UUID value) {

    /**
     * Canonical constructor enforcing non-null.
     *
     * @param value the underlying UUID
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public EpisodeId {
        Objects.requireNonNull(value, "EpisodeId value must not be null");
    }

    /**
     * Generate a fresh, random {@code EpisodeId}.
     *
     * @return a new {@code EpisodeId} backed by a random UUID
     */
    public static EpisodeId generate() {
        return new EpisodeId(UUID.randomUUID());
    }

    /**
     * Parse a string-form UUID into an {@code EpisodeId}.
     *
     * @param raw the canonical UUID string
     * @return an {@code EpisodeId} wrapping the parsed UUID
     * @throws NullPointerException if {@code raw} is {@code null}
     * @throws IllegalArgumentException if {@code raw} is not a valid UUID
     */
    public static EpisodeId of(String raw) {
        Objects.requireNonNull(raw, "EpisodeId raw value must not be null");
        return new EpisodeId(UUID.fromString(raw));
    }
}
