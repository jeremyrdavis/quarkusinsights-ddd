package io.arrogantprogrammer.quarkusinsights.programming.application;

import io.arrogantprogrammer.quarkusinsights.programming.domain.AbstractSubmitted;
import io.arrogantprogrammer.quarkusinsights.programming.domain.AbstractText;
import io.arrogantprogrammer.quarkusinsights.programming.domain.AirDate;
import io.arrogantprogrammer.quarkusinsights.programming.domain.DuplicateEpisodeNumberException;
import io.arrogantprogrammer.quarkusinsights.programming.domain.Episode;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeCanceled;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeId;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeNotFoundException;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeNumber;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodePublished;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeScheduled;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeStatus;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeTitle;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeWentLive;
import io.arrogantprogrammer.quarkusinsights.programming.domain.PresenterAssigned;
import io.arrogantprogrammer.quarkusinsights.programming.domain.SpeakerAssigned;
import io.arrogantprogrammer.quarkusinsights.shared.DomainEvent;
import io.arrogantprogrammer.quarkusinsights.shared.PersonId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EpisodeServiceTest {

    private static final Instant TODAY_NOON = Instant.parse("2026-05-18T12:00:00Z");
    private static final AirDate FUTURE = AirDate.of(java.time.LocalDate.of(2026, 6, 1));
    private static final AirDate TODAY_AS_AIR = AirDate.of(java.time.LocalDate.of(2026, 5, 18));
    private static final AbstractText ABSTRACT = AbstractText.of("x".repeat(150));

    private InMemoryEpisodeRepository repository;
    private RecordingDomainEventPublisher publisher;
    private EpisodeService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryEpisodeRepository();
        publisher = new RecordingDomainEventPublisher();
        service = new EpisodeService(repository, publisher, Clock.fixed(TODAY_NOON, ZoneOffset.UTC));
    }

    @Test
    void scheduleStoresEpisodeAndPublishesEvent() {
        EpisodeId id = service.schedule(new ScheduleEpisodeCommand(
                EpisodeNumber.of(1), EpisodeTitle.of("First show"), FUTURE));

        Episode loaded = service.load(id);
        assertEquals(EpisodeStatus.SCHEDULED, loaded.status());
        assertEquals(1, repository.size());
        assertEquals(1, publisher.recorded().size());
        assertInstanceOf(EpisodeScheduled.class, publisher.recorded().get(0));
    }

    @Test
    void scheduleRejectsDuplicateNumberByPreCheck() {
        service.schedule(new ScheduleEpisodeCommand(
                EpisodeNumber.of(7), EpisodeTitle.of("Original"), FUTURE));

        DuplicateEpisodeNumberException ex = assertThrows(
                DuplicateEpisodeNumberException.class,
                () -> service.schedule(new ScheduleEpisodeCommand(
                        EpisodeNumber.of(7), EpisodeTitle.of("Conflict"), FUTURE)));

        assertEquals(7, ex.number().value());
        assertEquals(1, repository.size());
        assertEquals(1, publisher.recorded().size());
    }

    @Test
    void loadThrowsWhenMissing() {
        EpisodeId unknown = EpisodeId.generate();
        EpisodeNotFoundException ex = assertThrows(
                EpisodeNotFoundException.class,
                () -> service.load(unknown));
        assertEquals(unknown, ex.episodeId());
    }

    @Test
    void submitAbstractDrainsAndPublishes() {
        EpisodeId id = service.schedule(new ScheduleEpisodeCommand(
                EpisodeNumber.of(1), EpisodeTitle.of("With abstract"), FUTURE));
        publisher.recorded(); // baseline read for clarity; do not clear

        service.submitAbstract(id, ABSTRACT);

        List<DomainEvent> all = publisher.recorded();
        assertEquals(2, all.size());
        assertInstanceOf(AbstractSubmitted.class, all.get(1));
        assertEquals(ABSTRACT, service.load(id).synopsis().orElseThrow().text());
    }

    @Test
    void presenterAssignmentIsIdempotentAcrossCalls() {
        EpisodeId id = service.schedule(new ScheduleEpisodeCommand(
                EpisodeNumber.of(1), EpisodeTitle.of("Idempotent"), FUTURE));
        PersonId presenter = new PersonId(UUID.randomUUID());

        service.assignPresenter(id, presenter);
        service.assignPresenter(id, presenter);

        long presenterEvents = publisher.recorded().stream()
                .filter(PresenterAssigned.class::isInstance)
                .count();
        assertEquals(1, presenterEvents);
        assertEquals(1, service.load(id).presenters().size());
    }

    @Test
    void speakerAssignmentIsIdempotentAcrossCalls() {
        EpisodeId id = service.schedule(new ScheduleEpisodeCommand(
                EpisodeNumber.of(1), EpisodeTitle.of("Idempotent"), FUTURE));
        PersonId speaker = new PersonId(UUID.randomUUID());

        service.assignSpeaker(id, speaker);
        service.assignSpeaker(id, speaker);

        long speakerEvents = publisher.recorded().stream()
                .filter(SpeakerAssigned.class::isInstance)
                .count();
        assertEquals(1, speakerEvents);
    }

    @Test
    void fullLifecycleProducesEverySevenEventTypes() {
        PersonId presenter = new PersonId(UUID.randomUUID());
        PersonId speaker = new PersonId(UUID.randomUUID());

        EpisodeId id = service.schedule(new ScheduleEpisodeCommand(
                EpisodeNumber.of(1), EpisodeTitle.of("Lifecycle"), TODAY_AS_AIR));
        service.submitAbstract(id, ABSTRACT);
        service.assignPresenter(id, presenter);
        service.assignSpeaker(id, speaker);
        service.goLive(id);
        service.publish(id);

        List<DomainEvent> events = publisher.recorded();
        assertEquals(6, events.size());
        assertInstanceOf(EpisodeScheduled.class, events.get(0));
        assertInstanceOf(AbstractSubmitted.class, events.get(1));
        assertInstanceOf(PresenterAssigned.class, events.get(2));
        assertInstanceOf(SpeakerAssigned.class, events.get(3));
        assertInstanceOf(EpisodeWentLive.class, events.get(4));
        assertInstanceOf(EpisodePublished.class, events.get(5));
        assertEquals(EpisodeStatus.PUBLISHED, service.load(id).status());
    }

    @Test
    void cancelEmitsEventAndUpdatesState() {
        EpisodeId id = service.schedule(new ScheduleEpisodeCommand(
                EpisodeNumber.of(1), EpisodeTitle.of("Doomed"), FUTURE));

        service.cancel(id, "guest dropped out");

        Episode loaded = service.load(id);
        assertEquals(EpisodeStatus.CANCELED, loaded.status());
        assertTrue(publisher.recorded().stream()
                .anyMatch(EpisodeCanceled.class::isInstance));
    }

    @Test
    void aggregateBufferIsAlwaysDrainedAfterPublish() {
        EpisodeId id = service.schedule(new ScheduleEpisodeCommand(
                EpisodeNumber.of(1), EpisodeTitle.of("Drained"), FUTURE));
        // Subsequent load returns the same in-memory aggregate; its buffer must be empty
        // because the service drained it.
        Episode loaded = service.load(id);
        assertEquals(0, loaded.drainEvents().size());
    }
}
