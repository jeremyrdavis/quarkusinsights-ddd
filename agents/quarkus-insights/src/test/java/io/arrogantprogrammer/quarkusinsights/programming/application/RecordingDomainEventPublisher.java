package io.arrogantprogrammer.quarkusinsights.programming.application;

import io.arrogantprogrammer.quarkusinsights.shared.DomainEvent;
import io.arrogantprogrammer.quarkusinsights.shared.DomainEventPublisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test double — captures all events dispatched through the publisher port so tests can
 * assert against the recorded history.
 */
public class RecordingDomainEventPublisher implements DomainEventPublisher {

    private final List<DomainEvent> recorded = new ArrayList<>();

    @Override
    public void publish(List<? extends DomainEvent> events) {
        recorded.addAll(events);
    }

    /**
     * @return an unmodifiable view of every event dispatched in the order received
     */
    public List<DomainEvent> recorded() {
        return Collections.unmodifiableList(recorded);
    }
}
