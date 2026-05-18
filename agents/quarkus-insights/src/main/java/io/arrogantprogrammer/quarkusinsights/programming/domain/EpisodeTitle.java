package io.arrogantprogrammer.quarkusinsights.programming.domain;

import java.util.Objects;

/**
 * Value object — the human-readable title of an episode.
 *
 * <p>Layer: domain. The title must be non-blank and between 1 and 200 characters inclusive
 * after trimming. Leading and trailing whitespace are removed during construction so callers
 * cannot smuggle in a title that is technically "non-empty" but visibly blank.
 *
 * @param value the trimmed title text; 1–200 characters
 */
public record EpisodeTitle(String value) {

    /** Maximum permitted title length, in characters. */
    public static final int MAX_LENGTH = 200;

    /**
     * Canonical constructor enforcing non-null, non-blank, and length bounds.
     *
     * @param value the raw title text; will be trimmed before validation
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws IllegalArgumentException if the trimmed value is blank or longer than {@link #MAX_LENGTH}
     */
    public EpisodeTitle {
        Objects.requireNonNull(value, "Episode title must not be null");
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Episode title must not be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Episode title must be at most " + MAX_LENGTH + " characters, was " + value.length());
        }
    }

    /**
     * Convenience factory mirroring the canonical constructor.
     *
     * @param value the raw title text
     * @return an {@code EpisodeTitle} wrapping the trimmed value
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws IllegalArgumentException if the trimmed value is blank or longer than {@link #MAX_LENGTH}
     */
    public static EpisodeTitle of(String value) {
        return new EpisodeTitle(value);
    }
}
