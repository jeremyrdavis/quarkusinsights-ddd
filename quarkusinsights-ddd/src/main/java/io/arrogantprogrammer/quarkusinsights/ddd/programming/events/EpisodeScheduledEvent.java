package io.arrogantprogrammer.quarkusinsights.ddd.programming.events;

import io.arrogantprogrammer.quarkusinsights.ddd.programming.domain.EpisodeAirDateValueObject;
import io.arrogantprogrammer.quarkusinsights.ddd.programming.domain.EpisodeTitleValueObject;

public record EpisodeScheduledEvent (
        EpisodeTitleValueObject title,
        String description,
        EpisodeAirDateValueObject airDate) implements DomainEvent{
}
