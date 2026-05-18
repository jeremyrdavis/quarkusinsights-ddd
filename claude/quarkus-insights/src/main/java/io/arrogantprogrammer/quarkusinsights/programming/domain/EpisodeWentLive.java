package io.arrogantprogrammer.quarkusinsights.programming.domain;

import io.arrogantprogrammer.quarkusinsights.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event — recorded when an episode transitions from SCHEDULED to LIVE.
 *
 * <p>Layer: domain. Emitted by {@link Episode#goLive(java.time.LocalDate)}.
 *
 * @param episodeId  the identifier of the episode that went live
 * @param occurredAt the recording instant (audit / ordering only)
 */
public record EpisodeWentLive(
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
    public EpisodeWentLive {
        Objects.requireNonNull(episodeId, "episodeId");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
