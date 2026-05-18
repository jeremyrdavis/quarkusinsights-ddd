package io.arrogantprogrammer.quarkusinsights.shared;

import java.time.Instant;

/**
 * Shared kernel — marker interface for facts recorded by any aggregate in the application.
 *
 * <p>Layer: domain (shared kernel). A domain event is an immutable, past-tense record of
 * something that has already happened. Subscribers, when they exist, treat events as the
 * authoritative trigger for downstream behavior; the producing aggregate is decoupled from
 * those subscribers entirely.
 *
 * <p>The {@code occurredAt} timestamp is for audit and ordering only — wall-clock skew makes
 * it unsuitable for business decisions.
 */
public interface DomainEvent {

    /**
     * The instant this fact was recorded by the aggregate that produced it.
     *
     * @return the recording instant; never {@code null}
     */
    Instant occurredAt();
}
