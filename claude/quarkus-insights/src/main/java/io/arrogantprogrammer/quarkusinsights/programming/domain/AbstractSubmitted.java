package io.arrogantprogrammer.quarkusinsights.programming.domain;

import io.arrogantprogrammer.quarkusinsights.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event — recorded when an abstract is submitted (or re-submitted) for an episode.
 *
 * <p>Layer: domain. Emitted by {@link Episode#submitAbstract(AbstractText)}. On resubmission,
 * the previous abstract is replaced wholesale and a new {@link AbstractId} appears in this event.
 *
 * @param episodeId   the identifier of the episode whose abstract was submitted
 * @param abstractId  the identifier of the newly created {@link Abstract} entity
 * @param occurredAt  the recording instant (audit / ordering only)
 */
public record AbstractSubmitted(
        EpisodeId episodeId,
        AbstractId abstractId,
        Instant occurredAt
) implements DomainEvent {

    /**
     * Canonical constructor enforcing non-null on every component.
     *
     * @param episodeId  the identifier of the episode
     * @param abstractId the identifier of the new abstract
     * @param occurredAt the recording instant
     * @throws NullPointerException if any component is {@code null}
     */
    public AbstractSubmitted {
        Objects.requireNonNull(episodeId, "episodeId");
        Objects.requireNonNull(abstractId, "abstractId");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
