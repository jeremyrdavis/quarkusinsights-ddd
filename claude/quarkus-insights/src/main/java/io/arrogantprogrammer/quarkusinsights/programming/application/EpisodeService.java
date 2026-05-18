package io.arrogantprogrammer.quarkusinsights.programming.application;

import io.arrogantprogrammer.quarkusinsights.programming.domain.AbstractText;
import io.arrogantprogrammer.quarkusinsights.programming.domain.DuplicateEpisodeNumberException;
import io.arrogantprogrammer.quarkusinsights.programming.domain.Episode;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeId;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeNotFoundException;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeRepository;
import io.arrogantprogrammer.quarkusinsights.shared.DomainEventPublisher;
import io.arrogantprogrammer.quarkusinsights.shared.PersonId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Application service — orchestrates the {@link Episode} aggregate's lifecycle.
 *
 * <p>Layer: application. Contains no business logic of its own. Each write method follows
 * the same four-step shape: load (or, for {@code schedule}, perform the cross-aggregate
 * uniqueness pre-check and construct), invoke the aggregate behavior, persist via the
 * repository port, then drain and dispatch the aggregate's recorded events through the
 * publisher port. The pre-check on {@code schedule} is the service's half of the
 * "three-place" episode-number uniqueness rule (the other two places are the DB UNIQUE
 * constraint and the persistence adapter's exception translation).
 */
@ApplicationScoped
public class EpisodeService {

    private final EpisodeRepository repository;
    private final DomainEventPublisher publisher;
    private final Clock clock;

    /**
     * Constructor injection for all collaborators.
     *
     * @param repository the persistence port; never {@code null}
     * @param publisher  the domain-event publisher port; never {@code null}
     * @param clock      the wall clock used to derive {@code today} and {@code now}; never {@code null}
     */
    public EpisodeService(EpisodeRepository repository, DomainEventPublisher publisher, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.publisher = Objects.requireNonNull(publisher, "publisher");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * Schedule a new episode.
     *
     * @param command the scheduling command; never {@code null}
     * @return the identifier assigned to the newly scheduled episode
     * @throws DuplicateEpisodeNumberException                                                                 if the number is already in use
     * @throws io.arrogantprogrammer.quarkusinsights.programming.domain.InvalidAirDateException                if the air date is in the past
     */
    @Transactional
    public EpisodeId schedule(ScheduleEpisodeCommand command) {
        Objects.requireNonNull(command, "command");
        if (repository.existsByNumber(command.number())) {
            throw new DuplicateEpisodeNumberException(command.number());
        }
        Episode episode = Episode.schedule(
                command.number(), command.title(), command.airDate(),
                today(), now());
        repository.save(episode);
        publisher.publish(episode.drainEvents());
        return episode.id();
    }

    /**
     * Submit or replace the abstract for an existing episode.
     *
     * @param episodeId the target episode; never {@code null}
     * @param text      the abstract text; never {@code null}
     * @throws EpisodeNotFoundException                                                                          if the episode does not exist
     * @throws io.arrogantprogrammer.quarkusinsights.programming.domain.IllegalEpisodeStateException             if the episode is not SCHEDULED
     */
    @Transactional
    public void submitAbstract(EpisodeId episodeId, AbstractText text) {
        Episode episode = loadOrThrow(episodeId);
        episode.submitAbstract(text, now());
        repository.save(episode);
        publisher.publish(episode.drainEvents());
    }

    /**
     * Assign a presenter (host) to an existing episode. Idempotent.
     *
     * @param episodeId  the target episode; never {@code null}
     * @param presenter  the presenter's identifier; never {@code null}
     * @throws EpisodeNotFoundException                                                                          if the episode does not exist
     * @throws io.arrogantprogrammer.quarkusinsights.programming.domain.IllegalEpisodeStateException             if the episode is terminal
     */
    @Transactional
    public void assignPresenter(EpisodeId episodeId, PersonId presenter) {
        Episode episode = loadOrThrow(episodeId);
        episode.assignPresenter(presenter, now());
        repository.save(episode);
        publisher.publish(episode.drainEvents());
    }

    /**
     * Assign a speaker (guest) to an existing episode. Idempotent.
     *
     * @param episodeId the target episode; never {@code null}
     * @param speaker   the speaker's identifier; never {@code null}
     * @throws EpisodeNotFoundException                                                                          if the episode does not exist
     * @throws io.arrogantprogrammer.quarkusinsights.programming.domain.IllegalEpisodeStateException             if the episode is terminal
     */
    @Transactional
    public void assignSpeaker(EpisodeId episodeId, PersonId speaker) {
        Episode episode = loadOrThrow(episodeId);
        episode.assignSpeaker(speaker, now());
        repository.save(episode);
        publisher.publish(episode.drainEvents());
    }

    /**
     * Transition an existing episode from SCHEDULED to LIVE.
     *
     * @param episodeId the target episode; never {@code null}
     * @throws EpisodeNotFoundException                                                                          if the episode does not exist
     * @throws io.arrogantprogrammer.quarkusinsights.programming.domain.IllegalEpisodeStateException             if the episode is not SCHEDULED
     * @throws io.arrogantprogrammer.quarkusinsights.programming.domain.InvalidAirDateException                  if the air date has not been reached
     */
    @Transactional
    public void goLive(EpisodeId episodeId) {
        Episode episode = loadOrThrow(episodeId);
        episode.goLive(today(), now());
        repository.save(episode);
        publisher.publish(episode.drainEvents());
    }

    /**
     * Transition an existing episode from LIVE to PUBLISHED.
     *
     * @param episodeId the target episode; never {@code null}
     * @throws EpisodeNotFoundException                                                                                  if the episode does not exist
     * @throws io.arrogantprogrammer.quarkusinsights.programming.domain.IllegalEpisodeStateException                     if the episode is not LIVE
     * @throws io.arrogantprogrammer.quarkusinsights.programming.domain.MissingPublishPreconditionException              if abstract / presenter / speaker is missing
     */
    @Transactional
    public void publish(EpisodeId episodeId) {
        Episode episode = loadOrThrow(episodeId);
        episode.publish(now());
        repository.save(episode);
        publisher.publish(episode.drainEvents());
    }

    /**
     * Cancel an existing episode.
     *
     * @param episodeId the target episode; never {@code null}
     * @param reason    the human-supplied reason for cancellation; non-blank
     * @throws EpisodeNotFoundException                                                              if the episode does not exist
     * @throws io.arrogantprogrammer.quarkusinsights.programming.domain.IllegalEpisodeStateException if the episode is not SCHEDULED
     * @throws IllegalArgumentException                                                              if {@code reason} is blank
     */
    @Transactional
    public void cancel(EpisodeId episodeId, String reason) {
        Episode episode = loadOrThrow(episodeId);
        episode.cancel(reason, now());
        repository.save(episode);
        publisher.publish(episode.drainEvents());
    }

    /**
     * Look up an episode by identifier.
     *
     * @param episodeId the target episode; never {@code null}
     * @return the aggregate
     * @throws EpisodeNotFoundException if the episode does not exist
     */
    public Episode load(EpisodeId episodeId) {
        return loadOrThrow(episodeId);
    }

    private Episode loadOrThrow(EpisodeId episodeId) {
        Objects.requireNonNull(episodeId, "episodeId");
        return repository.findById(episodeId)
                .orElseThrow(() -> new EpisodeNotFoundException(episodeId));
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    private Instant now() {
        return clock.instant();
    }
}
