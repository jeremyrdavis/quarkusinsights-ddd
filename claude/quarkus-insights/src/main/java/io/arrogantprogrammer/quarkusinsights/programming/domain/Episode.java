package io.arrogantprogrammer.quarkusinsights.programming.domain;

import io.arrogantprogrammer.quarkusinsights.shared.DomainEvent;
import io.arrogantprogrammer.quarkusinsights.shared.PersonId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Aggregate root — the source of truth for a single show in the Episodes subdomain.
 *
 * <p>Layer: domain. Pure POJO; no imports from {@code jakarta.persistence}, {@code jakarta.ws.rs},
 * or {@code io.quarkus.*}. Every business rule (status transitions, scheduling preconditions,
 * publish preconditions, idempotent assignments) is enforced inside this class — there is no
 * legal way to mutate state by going around these methods.
 *
 * <p>Lifecycle:
 * <pre>
 *   SCHEDULED ──goLive──&gt; LIVE ──publish──&gt; PUBLISHED
 *        │
 *        └─ cancel ──&gt; CANCELED   (terminal)
 * </pre>
 *
 * <p>Each behavior method either accepts the call and records the outcome (appending a
 * {@link DomainEvent} to an internal buffer) or refuses it with a typed exception. The
 * application service drains the buffer via {@link #drainEvents()} after persistence and
 * publishes the events through a {@link io.arrogantprogrammer.quarkusinsights.shared.DomainEventPublisher}.
 */
public final class Episode {

    private final EpisodeId id;
    private final EpisodeNumber number;
    private EpisodeTitle title;
    private AirDate airDate;
    private EpisodeStatus status;
    private Abstract synopsis;
    private final Set<PersonId> presenters;
    private final Set<PersonId> speakers;
    private String cancellationReason;
    private final List<DomainEvent> recordedEvents;

    private Episode(
            EpisodeId id,
            EpisodeNumber number,
            EpisodeTitle title,
            AirDate airDate,
            EpisodeStatus status,
            Abstract synopsis,
            Set<PersonId> presenters,
            Set<PersonId> speakers,
            String cancellationReason
    ) {
        this.id = id;
        this.number = number;
        this.title = title;
        this.airDate = airDate;
        this.status = status;
        this.synopsis = synopsis;
        this.presenters = new LinkedHashSet<>(presenters);
        this.speakers = new LinkedHashSet<>(speakers);
        this.cancellationReason = cancellationReason;
        this.recordedEvents = new ArrayList<>();
    }

    /**
     * Factory — schedule a brand-new episode.
     *
     * <p>Pre-state: no aggregate exists.
     * <p>Postcondition: status = {@link EpisodeStatus#SCHEDULED}; air date ≥ today.
     * <p>Emits: {@link EpisodeScheduled}.
     *
     * @param number  the episode's sequential number; never {@code null}
     * @param title   the episode's title; never {@code null}
     * @param airDate the scheduled air date; never {@code null}; must not be before {@code today}
     * @param today   today's date, supplied by the caller for testability; never {@code null}
     * @param now     the current instant, supplied by the caller for testability; never {@code null}
     * @return the newly scheduled aggregate, with one recorded {@link EpisodeScheduled} event
     * @throws NullPointerException     if any argument is {@code null}
     * @throws InvalidAirDateException  if {@code airDate} falls strictly before {@code today}
     */
    public static Episode schedule(
            EpisodeNumber number,
            EpisodeTitle title,
            AirDate airDate,
            LocalDate today,
            Instant now
    ) {
        Objects.requireNonNull(number, "number");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(airDate, "airDate");
        Objects.requireNonNull(today, "today");
        Objects.requireNonNull(now, "now");
        if (airDate.isBefore(today)) {
            throw new InvalidAirDateException(
                    "Air date must not be in the past: airDate=" + airDate.value() + ", today=" + today,
                    airDate.value(), today);
        }
        EpisodeId id = EpisodeId.generate();
        Episode episode = new Episode(
                id, number, title, airDate, EpisodeStatus.SCHEDULED,
                null, Set.of(), Set.of(), null);
        episode.recordedEvents.add(new EpisodeScheduled(id, number, title, airDate, now));
        return episode;
    }

    /**
     * Rehydration factory — reconstitute an aggregate from persisted state without
     * recording any events.
     *
     * <p>For use by the persistence adapter's mapper only. Callers from the application
     * or interface layers should never use this directly.
     *
     * @param id                 the persisted episode identifier; never {@code null}
     * @param number             the persisted number; never {@code null}
     * @param title              the persisted title; never {@code null}
     * @param airDate            the persisted air date; never {@code null}
     * @param status             the persisted status; never {@code null}
     * @param synopsis           the persisted abstract; may be {@code null}
     * @param presenters         the persisted presenter set; never {@code null}
     * @param speakers           the persisted speaker set; never {@code null}
     * @param cancellationReason the persisted cancellation reason; may be {@code null} (non-null only when status is CANCELED)
     * @return the rehydrated aggregate
     * @throws NullPointerException if a required argument is {@code null}
     */
    public static Episode rehydrate(
            EpisodeId id,
            EpisodeNumber number,
            EpisodeTitle title,
            AirDate airDate,
            EpisodeStatus status,
            Abstract synopsis,
            Set<PersonId> presenters,
            Set<PersonId> speakers,
            String cancellationReason
    ) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(number, "number");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(airDate, "airDate");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(presenters, "presenters");
        Objects.requireNonNull(speakers, "speakers");
        return new Episode(id, number, title, airDate, status, synopsis,
                presenters, speakers, cancellationReason);
    }

    /**
     * Submit (or replace) the abstract for this episode.
     *
     * <p>Pre-state: status = {@link EpisodeStatus#SCHEDULED}.
     * <p>Postcondition: the new abstract becomes the current abstract; the previous one,
     * if any, is discarded.
     * <p>Emits: {@link AbstractSubmitted}.
     *
     * @param text the new abstract text; never {@code null}
     * @param now  the current instant; never {@code null}
     * @throws NullPointerException           if any argument is {@code null}
     * @throws IllegalEpisodeStateException   if the episode is not in {@code SCHEDULED}
     */
    public void submitAbstract(AbstractText text, Instant now) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(now, "now");
        if (status != EpisodeStatus.SCHEDULED) {
            throw new IllegalEpisodeStateException(status, "submit abstract for");
        }
        Abstract replacement = Abstract.submit(text, now);
        this.synopsis = replacement;
        this.recordedEvents.add(new AbstractSubmitted(id, replacement.id(), now));
    }

    /**
     * Assign a presenter (host) to this episode. Idempotent — re-assigning the same
     * presenter has no effect and records no event.
     *
     * <p>Pre-state: status ∈ { {@link EpisodeStatus#SCHEDULED}, {@link EpisodeStatus#LIVE} }.
     * <p>Postcondition: the presenter set contains {@code personId}.
     * <p>Emits: {@link PresenterAssigned} only if the presenter was newly added.
     *
     * @param personId the presenter's identifier; never {@code null}
     * @param now      the current instant; never {@code null}
     * @throws NullPointerException         if any argument is {@code null}
     * @throws IllegalEpisodeStateException if the episode is in a terminal state
     */
    public void assignPresenter(PersonId personId, Instant now) {
        Objects.requireNonNull(personId, "personId");
        Objects.requireNonNull(now, "now");
        requireAssignableState("assign presenter to");
        if (presenters.add(personId)) {
            recordedEvents.add(new PresenterAssigned(id, personId, now));
        }
    }

    /**
     * Assign a speaker (guest) to this episode. Idempotent — re-assigning the same
     * speaker has no effect and records no event.
     *
     * <p>Pre-state: status ∈ { {@link EpisodeStatus#SCHEDULED}, {@link EpisodeStatus#LIVE} }.
     * <p>Postcondition: the speaker set contains {@code personId}.
     * <p>Emits: {@link SpeakerAssigned} only if the speaker was newly added.
     *
     * @param personId the speaker's identifier; never {@code null}
     * @param now      the current instant; never {@code null}
     * @throws NullPointerException         if any argument is {@code null}
     * @throws IllegalEpisodeStateException if the episode is in a terminal state
     */
    public void assignSpeaker(PersonId personId, Instant now) {
        Objects.requireNonNull(personId, "personId");
        Objects.requireNonNull(now, "now");
        requireAssignableState("assign speaker to");
        if (speakers.add(personId)) {
            recordedEvents.add(new SpeakerAssigned(id, personId, now));
        }
    }

    /**
     * Transition this episode from SCHEDULED to LIVE.
     *
     * <p>Pre-state: status = {@link EpisodeStatus#SCHEDULED} and air date ≤ today.
     * <p>Postcondition: status = {@link EpisodeStatus#LIVE}.
     * <p>Emits: {@link EpisodeWentLive}.
     *
     * @param today today's date, supplied by the caller; never {@code null}
     * @param now   the current instant; never {@code null}
     * @throws NullPointerException         if any argument is {@code null}
     * @throws IllegalEpisodeStateException if the episode is not in {@code SCHEDULED}
     * @throws InvalidAirDateException      if today has not yet reached the air date
     */
    public void goLive(LocalDate today, Instant now) {
        Objects.requireNonNull(today, "today");
        Objects.requireNonNull(now, "now");
        if (status != EpisodeStatus.SCHEDULED) {
            throw new IllegalEpisodeStateException(status, "go live for");
        }
        if (!airDate.hasArrivedBy(today)) {
            throw new InvalidAirDateException(
                    "Cannot go live before air date: airDate=" + airDate.value() + ", today=" + today,
                    airDate.value(), today);
        }
        this.status = EpisodeStatus.LIVE;
        this.recordedEvents.add(new EpisodeWentLive(id, now));
    }

    /**
     * Transition this episode from LIVE to the terminal PUBLISHED state.
     *
     * <p>Pre-state: status = {@link EpisodeStatus#LIVE}; abstract is present;
     * presenters and speakers are each non-empty.
     * <p>Postcondition: status = {@link EpisodeStatus#PUBLISHED}.
     * <p>Emits: {@link EpisodePublished}.
     *
     * @param now the current instant; never {@code null}
     * @throws NullPointerException                if {@code now} is {@code null}
     * @throws IllegalEpisodeStateException        if the episode is not in {@code LIVE}
     * @throws MissingPublishPreconditionException if the abstract is missing or there
     *                                             are no presenters or no speakers
     */
    public void publish(Instant now) {
        Objects.requireNonNull(now, "now");
        if (status != EpisodeStatus.LIVE) {
            throw new IllegalEpisodeStateException(status, "publish");
        }
        if (synopsis == null) {
            throw new MissingPublishPreconditionException("abstract");
        }
        if (presenters.isEmpty()) {
            throw new MissingPublishPreconditionException("presenter");
        }
        if (speakers.isEmpty()) {
            throw new MissingPublishPreconditionException("speaker");
        }
        this.status = EpisodeStatus.PUBLISHED;
        this.recordedEvents.add(new EpisodePublished(id, now));
    }

    /**
     * Cancel this episode before it has gone live.
     *
     * <p>Pre-state: status = {@link EpisodeStatus#SCHEDULED}; reason is non-blank.
     * <p>Postcondition: status = {@link EpisodeStatus#CANCELED}; cancellation reason is recorded.
     * <p>Emits: {@link EpisodeCanceled}.
     *
     * @param reason a human-supplied reason for cancellation; must not be {@code null} or blank
     * @param now    the current instant; never {@code null}
     * @throws NullPointerException         if any argument is {@code null}
     * @throws IllegalArgumentException     if {@code reason} is blank after trimming
     * @throws IllegalEpisodeStateException if the episode is not in {@code SCHEDULED}
     */
    public void cancel(String reason, Instant now) {
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(now, "now");
        String trimmed = reason.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Cancellation reason must not be blank");
        }
        if (status != EpisodeStatus.SCHEDULED) {
            throw new IllegalEpisodeStateException(status, "cancel");
        }
        this.status = EpisodeStatus.CANCELED;
        this.cancellationReason = trimmed;
        this.recordedEvents.add(new EpisodeCanceled(id, trimmed, now));
    }

    private void requireAssignableState(String operation) {
        if (status != EpisodeStatus.SCHEDULED && status != EpisodeStatus.LIVE) {
            throw new IllegalEpisodeStateException(status, operation);
        }
    }

    /**
     * Drain and return all events the aggregate has recorded since the last drain. The
     * internal buffer is cleared by this call.
     *
     * @return the recorded events in the order they were emitted; never {@code null}, may be empty
     */
    public List<DomainEvent> drainEvents() {
        List<DomainEvent> drained = List.copyOf(recordedEvents);
        recordedEvents.clear();
        return drained;
    }

    /**
     * The aggregate's identifier.
     *
     * @return the {@link EpisodeId}
     */
    public EpisodeId id() {
        return id;
    }

    /**
     * The aggregate's sequential number.
     *
     * @return the {@link EpisodeNumber}
     */
    public EpisodeNumber number() {
        return number;
    }

    /**
     * The aggregate's title.
     *
     * @return the {@link EpisodeTitle}
     */
    public EpisodeTitle title() {
        return title;
    }

    /**
     * The aggregate's air date.
     *
     * @return the {@link AirDate}
     */
    public AirDate airDate() {
        return airDate;
    }

    /**
     * The aggregate's current lifecycle status.
     *
     * @return the {@link EpisodeStatus}
     */
    public EpisodeStatus status() {
        return status;
    }

    /**
     * The aggregate's current abstract, if one has been submitted.
     *
     * @return the {@link Abstract} wrapped in an {@link Optional}, or {@link Optional#empty()}
     */
    public Optional<Abstract> synopsis() {
        return Optional.ofNullable(synopsis);
    }

    /**
     * An unmodifiable, insertion-ordered view of the presenter set.
     *
     * @return the assigned presenters
     */
    public Set<PersonId> presenters() {
        return Collections.unmodifiableSet(presenters);
    }

    /**
     * An unmodifiable, insertion-ordered view of the speaker set.
     *
     * @return the assigned speakers
     */
    public Set<PersonId> speakers() {
        return Collections.unmodifiableSet(speakers);
    }

    /**
     * The reason supplied when the episode was canceled, if it has been.
     *
     * @return the trimmed cancellation reason, or {@link Optional#empty()} if the episode
     *         has not been canceled
     */
    public Optional<String> cancellationReason() {
        return Optional.ofNullable(cancellationReason);
    }
}
