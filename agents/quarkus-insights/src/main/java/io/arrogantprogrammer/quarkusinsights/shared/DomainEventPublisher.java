package io.arrogantprogrammer.quarkusinsights.shared;

import java.util.List;

/**
 * Shared kernel — port through which application services dispatch domain events after
 * a successful aggregate save.
 *
 * <p>Layer: domain (shared kernel). The aggregate records events into its internal buffer;
 * the application service drains the buffer and hands the events to an implementation of
 * this port. Implementations may forward to CDI {@code Event<>}, a message broker, or a
 * logger — the domain neither knows nor cares.
 */
public interface DomainEventPublisher {

    /**
     * Dispatch a batch of events that have already been persisted as part of the same
     * unit of work that produced them.
     *
     * @param events the events to publish, in the order they were recorded by the aggregate;
     *               never {@code null}, may be empty (in which case the call is a no-op)
     */
    void publish(List<? extends DomainEvent> events);
}
