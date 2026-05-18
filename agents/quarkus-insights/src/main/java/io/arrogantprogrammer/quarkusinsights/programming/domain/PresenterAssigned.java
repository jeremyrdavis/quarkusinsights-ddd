package io.arrogantprogrammer.quarkusinsights.programming.domain;

import io.arrogantprogrammer.quarkusinsights.shared.DomainEvent;
import io.arrogantprogrammer.quarkusinsights.shared.PersonId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event — recorded when a presenter (host) is newly assigned to an episode.
 *
 * <p>Layer: domain. Emitted by {@link Episode#assignPresenter(PersonId)} only on a
 * non-idempotent call: reassigning an already-assigned presenter records nothing.
 *
 * @param episodeId   the identifier of the episode
 * @param presenterId the identifier of the assigned presenter
 * @param occurredAt  the recording instant (audit / ordering only)
 */
public record PresenterAssigned(
        EpisodeId episodeId,
        PersonId presenterId,
        Instant occurredAt
) implements DomainEvent {

    /**
     * Canonical constructor enforcing non-null on every component.
     *
     * @param episodeId   the identifier of the episode
     * @param presenterId the identifier of the assigned presenter
     * @param occurredAt  the recording instant
     * @throws NullPointerException if any component is {@code null}
     */
    public PresenterAssigned {
        Objects.requireNonNull(episodeId, "episodeId");
        Objects.requireNonNull(presenterId, "presenterId");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
