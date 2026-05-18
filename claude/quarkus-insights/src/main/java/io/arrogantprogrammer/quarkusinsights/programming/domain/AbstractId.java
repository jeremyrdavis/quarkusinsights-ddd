package io.arrogantprogrammer.quarkusinsights.programming.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object — opaque identifier for an {@link Abstract} inside-aggregate entity.
 *
 * <p>Layer: domain. A new {@code AbstractId} is generated each time an episode's abstract
 * is submitted; the previous Abstract (and its ID) are discarded.
 *
 * @param value the underlying UUID; never {@code null}
 */
public record AbstractId(UUID value) {

    /**
     * Canonical constructor enforcing non-null.
     *
     * @param value the underlying UUID
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public AbstractId {
        Objects.requireNonNull(value, "AbstractId value must not be null");
    }

    /**
     * Generate a fresh, random {@code AbstractId}.
     *
     * @return a new {@code AbstractId} backed by a random UUID
     */
    public static AbstractId generate() {
        return new AbstractId(UUID.randomUUID());
    }

    /**
     * Parse a string-form UUID into an {@code AbstractId}.
     *
     * @param raw the canonical UUID string
     * @return an {@code AbstractId} wrapping the parsed UUID
     * @throws NullPointerException if {@code raw} is {@code null}
     * @throws IllegalArgumentException if {@code raw} is not a valid UUID
     */
    public static AbstractId of(String raw) {
        Objects.requireNonNull(raw, "AbstractId raw value must not be null");
        return new AbstractId(UUID.fromString(raw));
    }
}
