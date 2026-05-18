package io.arrogantprogrammer.quarkusinsights.programming.domain;

import java.util.Objects;

/**
 * Value object — the synopsis text of an episode's {@link Abstract}.
 *
 * <p>Layer: domain. Must be non-blank and between 100 and 5,000 characters inclusive after
 * trimming. Leading and trailing whitespace are removed during construction.
 *
 * @param value the trimmed abstract text; 100–5,000 characters
 */
public record AbstractText(String value) {

    /** Minimum permitted abstract length, in characters. */
    public static final int MIN_LENGTH = 100;

    /** Maximum permitted abstract length, in characters. */
    public static final int MAX_LENGTH = 5_000;

    /**
     * Canonical constructor enforcing non-null, non-blank, and length bounds.
     *
     * @param value the raw text; will be trimmed before validation
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws IllegalArgumentException if the trimmed value is shorter than {@link #MIN_LENGTH}
     *                                  or longer than {@link #MAX_LENGTH}
     */
    public AbstractText {
        Objects.requireNonNull(value, "Abstract text must not be null");
        value = value.trim();
        if (value.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "Abstract text must be at least " + MIN_LENGTH + " characters, was " + value.length());
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Abstract text must be at most " + MAX_LENGTH + " characters, was " + value.length());
        }
    }

    /**
     * Convenience factory mirroring the canonical constructor.
     *
     * @param value the raw text
     * @return an {@code AbstractText} wrapping the trimmed value
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws IllegalArgumentException if the trimmed value falls outside the length bounds
     */
    public static AbstractText of(String value) {
        return new AbstractText(value);
    }
}
