package io.arrogantprogrammer.quarkusinsights.ddd.programming.domain;

import io.arrogantprogrammer.quarkusinsights.ddd.programming.events.DomainEvent;
import io.arrogantprogrammer.quarkusinsights.ddd.programming.events.EpisodeScheduledEvent;
import io.quarkus.logging.Log;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

public class EpisodeAggregate {

    EpisodeTitleValueObject title;

    String description;

    EpisodeAirDateValueObject airDate;

    Collection<DomainEvent> domainEvents;

    private EpisodeAggregate(EpisodeTitleValueObject title, String description, EpisodeAirDateValueObject airDate) {
        this.title = title;
        this.description = description;
        this.airDate = airDate;
    }

    public static EpisodeAggregate createAndSchedule(EpisodeTitleValueObject title, String description, EpisodeAirDateValueObject airDate) {
        Log.debugf("Creating and scheduling episode: %s", title.value());
        EpisodeAggregate episodeAggregate = new EpisodeAggregate(title, description, airDate);
        EpisodeScheduledEvent episodeScheduledEvent = new EpisodeScheduledEvent(
                episodeAggregate.title,
                episodeAggregate.description,
                episodeAggregate.airDate
        );
        if(episodeAggregate.domainEvents == null) {
            episodeAggregate.domainEvents = new ArrayList<>();
        }
        episodeAggregate.domainEvents.add(episodeScheduledEvent);
        return episodeAggregate;
    }

    public static EpisodeAggregate rehydrate(String title, String description, LocalDate localDate) {
        Log.debugf("Rehydrating episode: %s", title);
        return new EpisodeAggregate(
                new EpisodeTitleValueObject(title),
                description,
                new EpisodeAirDateValueObject(localDate)
        );
    }

    public EpisodeTitleValueObject getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public EpisodeAirDateValueObject getAirDate() {
        return airDate;
    }

    public Collection<DomainEvent> getDomainEvents() {
        return domainEvents;
    }
}
