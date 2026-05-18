package io.arrogantprogrammer.quarkusinsights.programming.application;

import io.arrogantprogrammer.quarkusinsights.shared.DomainEvent;
import io.arrogantprogrammer.quarkusinsights.shared.DomainEventPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Application-layer adapter — publishes domain events by both logging them and firing them
 * on the CDI {@link Event} bus.
 *
 * <p>Layer: application. The CDI fire is forward-compatible: when other bounded contexts
 * (People, Engagement, Catalog) arrive later, they may add {@code @Observes DomainEvent}
 * methods without any change to the Episode subdomain. The log line is the immediately
 * useful audit trail until then.
 */
@ApplicationScoped
public class LoggingDomainEventPublisher implements DomainEventPublisher {

    private static final Logger LOG = Logger.getLogger(LoggingDomainEventPublisher.class);

    @Inject
    Event<DomainEvent> eventBus;

    /**
     * Log each event at {@code INFO} level and dispatch it on the CDI event bus.
     *
     * @param events the events to publish; never {@code null}, may be empty
     */
    @Override
    public void publish(List<? extends DomainEvent> events) {
        for (DomainEvent event : events) {
            LOG.infof("Domain event published: %s", event);
            eventBus.fire(event);
        }
    }
}
