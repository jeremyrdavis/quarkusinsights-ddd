package io.arrogantprogrammer.quarkusinsights.programming.domain;

import io.arrogantprogrammer.quarkusinsights.shared.DomainEvent;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event — recorded when an episode transitions from SCHEDULED to the terminal CANCELED state.
 *
 * <p>Layer: domain. Emitted by {@link Episode#cancel(String)}.
 *
 * @param episodeId  the identifier of the canceled episode
 * @param reason     the human-supplied reason for cancellation (non-blank)
 * @param occurredAt the recording instant (audit / ordering only)
 */
public record EpisodeCanceled(
        EpisodeId episodeId,
        String reason,
        Instant occurredAt
) implements DomainEvent {

    /**
     * Canonical constructor enforcing non-null on every component.
     *
     * @param episodeId  the identifier of the episode
     * @param reason     the cancellation reason
     * @param occurredAt the recording instant
     * @throws NullPointerException if any component is {@code null}
     */
    public EpisodeCanceled {
        Objects.requireNonNull(episodeId, "episodeId");
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
