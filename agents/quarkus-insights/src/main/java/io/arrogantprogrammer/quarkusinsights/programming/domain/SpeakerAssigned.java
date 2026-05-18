package io.arrogantprogrammer.quarkusinsights.programming.domain;

import io.arrogantprogrammer.quarkusinsights.shared.DomainEvent;
import io.arrogantprogrammer.quarkusinsights.shared.PersonId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event — recorded when a speaker (guest) is newly assigned to an episode.
 *
 * <p>Layer: domain. Emitted by {@link Episode#assignSpeaker(PersonId)} only on a
 * non-idempotent call: reassigning an already-assigned speaker records nothing.
 *
 * @param episodeId  the identifier of the episode
 * @param speakerId  the identifier of the assigned speaker
 * @param occurredAt the recording instant (audit / ordering only)
 */
public record SpeakerAssigned(
        EpisodeId episodeId,
        PersonId speakerId,
        Instant occurredAt
) implements DomainEvent {

    /**
     * Canonical constructor enforcing non-null on every component.
     *
     * @param episodeId  the identifier of the episode
     * @param speakerId  the identifier of the assigned speaker
     * @param occurredAt the recording instant
     * @throws NullPointerException if any component is {@code null}
     */
    public SpeakerAssigned {
        Objects.requireNonNull(episodeId, "episodeId");
        Objects.requireNonNull(speakerId, "speakerId");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
