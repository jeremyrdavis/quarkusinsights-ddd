package io.arrogantprogrammer.quarkusinsights.programming.domain;

import io.arrogantprogrammer.quarkusinsights.shared.DomainEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Domain event — recorded when a new episode is scheduled.
 *
 * <p>Layer: domain. Emitted by {@link Episode#schedule(EpisodeNumber, EpisodeTitle, AirDate, LocalDate)}.
 * Payload carries identifiers and primitives only — no aggregate references.
 *
 * @param episodeId    the identifier of the newly scheduled episode
 * @param number       the episode's sequential number
 * @param title        the episode's title
 * @param airDate      the scheduled air date
 * @param occurredAt   the recording instant (audit / ordering only)
 */
public record EpisodeScheduled(
        EpisodeId episodeId,
        EpisodeNumber number,
        EpisodeTitle title,
        AirDate airDate,
        Instant occurredAt
) implements DomainEvent {

    /**
     * Canonical constructor enforcing non-null on every component.
     *
     * @param episodeId  the identifier of the newly scheduled episode
     * @param number     the episode's sequential number
     * @param title      the episode's title
     * @param airDate    the scheduled air date
     * @param occurredAt the recording instant
     * @throws NullPointerException if any component is {@code null}
     */
    public EpisodeScheduled {
        Objects.requireNonNull(episodeId, "episodeId");
        Objects.requireNonNull(number, "number");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(airDate, "airDate");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
