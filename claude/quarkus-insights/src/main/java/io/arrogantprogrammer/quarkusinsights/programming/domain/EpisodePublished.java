package io.arrogantprogrammer.quarkusinsights.programming.domain;

import io.arrogantprogrammer.quarkusinsights.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event — recorded when an episode transitions from LIVE to the terminal PUBLISHED state.
 *
 * <p>Layer: domain. Emitted by {@link Episode#publish()}.
 *
 * @param episodeId  the identifier of the published episode
 * @param occurredAt the recording instant (audit / ordering only)
 */
public record EpisodePublished(
        EpisodeId episodeId,
        Instant occurredAt
) implements DomainEvent {

    /**
     * Canonical constructor enforcing non-null on every component.
     *
     * @param episodeId  the identifier of the episode
     * @param occurredAt the recording instant
     * @throws NullPointerException if any component is {@code null}
     */
    public EpisodePublished {
        Objects.requireNonNull(episodeId, "episodeId");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
