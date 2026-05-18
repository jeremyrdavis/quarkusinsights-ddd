package io.arrogantprogrammer.quarkusinsights.ddd.programming.application;

import io.arrogantprogrammer.quarkusinsights.ddd.programming.domain.EpisodeAggregate;
import io.arrogantprogrammer.quarkusinsights.ddd.programming.events.DomainEvent;
import io.arrogantprogrammer.quarkusinsights.ddd.programming.infrastructure.EpisodeDTO;
import io.arrogantprogrammer.quarkusinsights.ddd.programming.infrastructure.persistence.EpisodeRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Collection;

@ApplicationScoped
public class EpisodeApplicationService {

    @Inject
    EpisodeRepository episodeRepository;

    @Inject
    Event<DomainEvent> eventPublisher;

    /**
     * Schedules a new episode based on the provided request DTO.
     *
     * 1. Validate that the title does not already exist in the database
     * 2. Create an Episode
     * 3. Persist the data about the episode in the database
     * 4. Notify the rest of the system that the episode has been scheduled
     *
     * @param scheduleEpisodeCommand The details of the episode to schedule.
     */
    @Transactional
    public EpisodeDTO scheduleEpisode(ScheduleEpisodeCommand scheduleEpisodeCommand) {
        Log.debugf("Received ScheduleEpisodeCommand: %s", scheduleEpisodeCommand);
        // validate that the title does not already exist in the database
        if(!episodeRepository.titleIsUnique(scheduleEpisodeCommand.title())) {
            throw new IllegalArgumentException("Episode title must be unique");
        }

        // Create an Episode
        EpisodeAggregate episodeAggregate = EpisodeAggregate.createAndSchedule(
                scheduleEpisodeCommand.title(),
                scheduleEpisodeCommand.description(),
                scheduleEpisodeCommand.airDate()
        );

        Collection<DomainEvent> events = episodeAggregate.getDomainEvents();

        // Persist the data
        episodeRepository.persist(episodeAggregate);
        Log.debugf("Persisted episode: %s", episodeAggregate.getTitle().value());

        // Notify the rest of the system
        events.forEach(event -> {
            eventPublisher.fire(event);
            Log.debugf("Notified rest of system about event: %s", event);
        });
        return new EpisodeDTO(
                episodeAggregate.getTitle().value(),
                episodeAggregate.getDescription(),
                episodeAggregate.getAirDate().value()
        );
    }
}
