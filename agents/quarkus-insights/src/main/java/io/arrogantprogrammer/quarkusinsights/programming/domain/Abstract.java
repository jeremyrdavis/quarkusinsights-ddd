package io.arrogantprogrammer.quarkusinsights.programming.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Inside-aggregate entity — the synopsis text for an {@link Episode}, identified by its
 * own {@link AbstractId}.
 *
 * <p>Layer: domain. Lives entirely inside the Episode aggregate boundary. Abstracts are
 * never edited in place; submitting a new abstract replaces the previous one wholesale,
 * generating a new {@link AbstractId} and a new submission timestamp.
 *
 * @param id          the abstract's identifier
 * @param text        the abstract's synopsis text
 * @param submittedAt the instant at which this abstract was submitted (audit only)
 */
public record Abstract(AbstractId id, AbstractText text, Instant submittedAt) {

    /**
     * Canonical constructor enforcing non-null on every component.
     *
     * @param id          the abstract identifier
     * @param text        the synopsis text
     * @param submittedAt the submission instant
     * @throws NullPointerException if any component is {@code null}
     */
    public Abstract {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(submittedAt, "submittedAt");
    }

    /**
     * Construct a freshly-submitted abstract with a generated identifier and a {@code now}
     * timestamp.
     *
     * @param text the synopsis text; never {@code null}
     * @param now  the submission instant; never {@code null}
     * @return a new {@code Abstract}
     * @throws NullPointerException if any argument is {@code null}
     */
    public static Abstract submit(AbstractText text, Instant now) {
        return new Abstract(AbstractId.generate(), text, now);
    }
}
