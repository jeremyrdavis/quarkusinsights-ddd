package io.arrogantprogrammer.quarkusinsights.programming.domain;

import io.arrogantprogrammer.quarkusinsights.shared.DomainEvent;
import io.arrogantprogrammer.quarkusinsights.shared.PersonId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EpisodeTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 18);
    private static final Instant NOW = Instant.parse("2026-05-18T12:00:00Z");
    private static final EpisodeNumber NUMBER = EpisodeNumber.of(1);
    private static final EpisodeTitle TITLE = EpisodeTitle.of("Hexagonal Architecture");
    private static final AirDate FUTURE = AirDate.of(LocalDate.of(2026, 6, 1));
    private static final AirDate TODAY_AS_AIR = AirDate.of(TODAY);
    private static final AbstractText ABSTRACT = AbstractText.of("x".repeat(150));
    private static final PersonId PRESENTER = new PersonId(UUID.randomUUID());
    private static final PersonId SPEAKER = new PersonId(UUID.randomUUID());

    private static Episode scheduled() {
        Episode e = Episode.schedule(NUMBER, TITLE, FUTURE, TODAY, NOW);
        e.drainEvents();
        return e;
    }

    private static Episode live() {
        Episode e = Episode.schedule(NUMBER, TITLE, TODAY_AS_AIR, TODAY, NOW);
        e.submitAbstract(ABSTRACT, NOW);
        e.assignPresenter(PRESENTER, NOW);
        e.assignSpeaker(SPEAKER, NOW);
        e.goLive(TODAY, NOW);
        e.drainEvents();
        return e;
    }

    @Test
    void scheduleCreatesAggregateInScheduledAndRecordsEvent() {
        Episode e = Episode.schedule(NUMBER, TITLE, FUTURE, TODAY, NOW);

        assertEquals(EpisodeStatus.SCHEDULED, e.status());
        assertEquals(NUMBER, e.number());
        assertEquals(TITLE, e.title());
        assertEquals(FUTURE, e.airDate());
        List<DomainEvent> events = e.drainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(EpisodeScheduled.class, events.get(0));
    }

    @Test
    void scheduleRejectsPastAirDate() {
        AirDate past = AirDate.of(TODAY.minusDays(1));
        InvalidAirDateException ex = assertThrows(
                InvalidAirDateException.class,
                () -> Episode.schedule(NUMBER, TITLE, past, TODAY, NOW));
        assertEquals(past.value(), ex.airDate());
        assertEquals(TODAY, ex.today());
    }

    @Test
    void scheduleAcceptsAirDateEqualToToday() {
        Episode e = Episode.schedule(NUMBER, TITLE, AirDate.of(TODAY), TODAY, NOW);
        assertEquals(EpisodeStatus.SCHEDULED, e.status());
    }

    @Test
    void drainClearsTheBuffer() {
        Episode e = Episode.schedule(NUMBER, TITLE, FUTURE, TODAY, NOW);
        assertEquals(1, e.drainEvents().size());
        assertEquals(0, e.drainEvents().size());
    }

    @Test
    void submitAbstractReplacesExistingAndRecordsEvent() {
        Episode e = scheduled();
        e.submitAbstract(ABSTRACT, NOW);
        AbstractId firstId = e.synopsis().orElseThrow().id();

        AbstractText replacement = AbstractText.of("y".repeat(200));
        e.submitAbstract(replacement, NOW);

        Abstract current = e.synopsis().orElseThrow();
        assertAll(
                () -> assertEquals(replacement, current.text()),
                () -> assertFalse(firstId.equals(current.id()))
        );
    }

    @Test
    void submitAbstractRejectedOnLiveEpisode() {
        Episode e = live();
        IllegalEpisodeStateException ex = assertThrows(
                IllegalEpisodeStateException.class,
                () -> e.submitAbstract(ABSTRACT, NOW));
        assertEquals(EpisodeStatus.LIVE, ex.currentStatus());
    }

    @Test
    void assignPresenterIsIdempotent() {
        Episode e = scheduled();
        e.assignPresenter(PRESENTER, NOW);
        e.assignPresenter(PRESENTER, NOW);

        assertEquals(1, e.presenters().size());
        List<DomainEvent> events = e.drainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(PresenterAssigned.class, events.get(0));
    }

    @Test
    void assignSpeakerIsIdempotent() {
        Episode e = scheduled();
        e.assignSpeaker(SPEAKER, NOW);
        e.assignSpeaker(SPEAKER, NOW);

        assertEquals(1, e.speakers().size());
        assertEquals(1, e.drainEvents().size());
    }

    @Test
    void assignmentsAllowedInLiveState() {
        Episode e = live();
        PersonId additional = new PersonId(UUID.randomUUID());
        e.assignPresenter(additional, NOW);
        e.assignSpeaker(additional, NOW);

        assertTrue(e.presenters().contains(additional));
        assertTrue(e.speakers().contains(additional));
    }

    @Test
    void assignmentsRejectedInTerminalStates() {
        Episode published = live();
        published.publish(NOW);
        assertThrows(IllegalEpisodeStateException.class,
                () -> published.assignPresenter(PRESENTER, NOW));
        assertThrows(IllegalEpisodeStateException.class,
                () -> published.assignSpeaker(SPEAKER, NOW));

        Episode canceled = scheduled();
        canceled.cancel("not happening", NOW);
        assertThrows(IllegalEpisodeStateException.class,
                () -> canceled.assignPresenter(PRESENTER, NOW));
    }

    @Test
    void goLiveTransitionsAndRecordsEvent() {
        Episode e = Episode.schedule(NUMBER, TITLE, TODAY_AS_AIR, TODAY, NOW);
        e.drainEvents();
        e.goLive(TODAY, NOW);

        assertEquals(EpisodeStatus.LIVE, e.status());
        List<DomainEvent> events = e.drainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(EpisodeWentLive.class, events.get(0));
    }

    @Test
    void goLiveRejectedBeforeAirDate() {
        Episode e = Episode.schedule(NUMBER, TITLE, FUTURE, TODAY, NOW);
        InvalidAirDateException ex = assertThrows(
                InvalidAirDateException.class,
                () -> e.goLive(TODAY, NOW));
        assertEquals(FUTURE.value(), ex.airDate());
    }

    @Test
    void goLiveRejectedFromNonScheduledStatus() {
        Episode e = live();
        assertThrows(IllegalEpisodeStateException.class, () -> e.goLive(TODAY, NOW));
    }

    @Test
    void publishHappyPathTransitionsAndRecordsEvent() {
        Episode e = live();
        e.publish(NOW);

        assertEquals(EpisodeStatus.PUBLISHED, e.status());
        List<DomainEvent> events = e.drainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(EpisodePublished.class, events.get(0));
    }

    @Test
    void publishRejectedWhenNotLive() {
        Episode e = scheduled();
        assertThrows(IllegalEpisodeStateException.class, () -> e.publish(NOW));
    }

    @Test
    void publishRejectedWithoutAbstract() {
        Episode e = Episode.schedule(NUMBER, TITLE, TODAY_AS_AIR, TODAY, NOW);
        e.assignPresenter(PRESENTER, NOW);
        e.assignSpeaker(SPEAKER, NOW);
        e.goLive(TODAY, NOW);

        MissingPublishPreconditionException ex = assertThrows(
                MissingPublishPreconditionException.class,
                () -> e.publish(NOW));
        assertEquals("abstract", ex.missing());
    }

    @Test
    void publishRejectedWithoutPresenter() {
        Episode e = Episode.schedule(NUMBER, TITLE, TODAY_AS_AIR, TODAY, NOW);
        e.submitAbstract(ABSTRACT, NOW);
        e.assignSpeaker(SPEAKER, NOW);
        e.goLive(TODAY, NOW);

        MissingPublishPreconditionException ex = assertThrows(
                MissingPublishPreconditionException.class,
                () -> e.publish(NOW));
        assertEquals("presenter", ex.missing());
    }

    @Test
    void publishRejectedWithoutSpeaker() {
        Episode e = Episode.schedule(NUMBER, TITLE, TODAY_AS_AIR, TODAY, NOW);
        e.submitAbstract(ABSTRACT, NOW);
        e.assignPresenter(PRESENTER, NOW);
        e.goLive(TODAY, NOW);

        MissingPublishPreconditionException ex = assertThrows(
                MissingPublishPreconditionException.class,
                () -> e.publish(NOW));
        assertEquals("speaker", ex.missing());
    }

    @Test
    void cancelTransitionsAndRecordsEvent() {
        Episode e = scheduled();
        e.cancel("guest dropped out", NOW);

        assertEquals(EpisodeStatus.CANCELED, e.status());
        assertEquals("guest dropped out", e.cancellationReason().orElseThrow());
        List<DomainEvent> events = e.drainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(EpisodeCanceled.class, events.get(0));
    }

    @Test
    void cancelRejectsBlankReason() {
        Episode e = scheduled();
        assertThrows(IllegalArgumentException.class, () -> e.cancel("   ", NOW));
    }

    @Test
    void cancelRejectedFromLive() {
        Episode e = live();
        assertThrows(IllegalEpisodeStateException.class,
                () -> e.cancel("too late", NOW));
    }

    @Test
    void rehydrateReproducesStateWithoutEvents() {
        EpisodeId id = EpisodeId.generate();
        Episode rehydrated = Episode.rehydrate(
                id, NUMBER, TITLE, FUTURE, EpisodeStatus.PUBLISHED,
                new Abstract(AbstractId.generate(), ABSTRACT, NOW),
                java.util.Set.of(PRESENTER),
                java.util.Set.of(SPEAKER),
                null);

        assertSame(EpisodeStatus.PUBLISHED, rehydrated.status());
        assertEquals(0, rehydrated.drainEvents().size());
    }
}
