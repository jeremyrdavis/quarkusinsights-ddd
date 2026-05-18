package io.arrogantprogrammer.quarkusinsights.programming.interfaces.rest;

import io.arrogantprogrammer.quarkusinsights.programming.domain.Episode;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST response DTO — JSON view of an {@link Episode} aggregate's current state.
 *
 * <p>Layer: interfaces. Decoupled from the domain aggregate and from the JPA entity so the
 * wire format can evolve independently of either. The mapping is one-way: domain → DTO.
 *
 * @param id                 the episode identifier
 * @param number             the sequential number
 * @param title              the title
 * @param airDate            the scheduled air date
 * @param status             the lifecycle status, serialized as its enum name
 * @param synopsis           the current abstract, or {@code null} if none has been submitted
 * @param presenters         the assigned presenter identifiers
 * @param speakers           the assigned speaker identifiers
 * @param cancellationReason the cancellation reason, or {@code null} when not canceled
 */
public record EpisodeResponse(
        UUID id,
        int number,
        String title,
        LocalDate airDate,
        EpisodeStatus status,
        AbstractView synopsis,
        List<UUID> presenters,
        List<UUID> speakers,
        String cancellationReason
) {

    /**
     * Project the given aggregate into a response DTO.
     *
     * @param episode the aggregate to project; never {@code null}
     * @return a JSON-serializable response view
     */
    public static EpisodeResponse from(Episode episode) {
        AbstractView synopsis = episode.synopsis()
                .map(a -> new AbstractView(a.id().value(), a.text().value(), a.submittedAt()))
                .orElse(null);
        return new EpisodeResponse(
                episode.id().value(),
                episode.number().value(),
                episode.title().value(),
                episode.airDate().value(),
                episode.status(),
                synopsis,
                episode.presenters().stream().map(p -> p.value()).toList(),
                episode.speakers().stream().map(p -> p.value()).toList(),
                episode.cancellationReason().orElse(null));
    }

    /**
     * Inner DTO — JSON view of the inside-aggregate Abstract entity.
     *
     * @param id          the abstract identifier
     * @param text        the synopsis text
     * @param submittedAt the submission timestamp
     */
    public record AbstractView(UUID id, String text, Instant submittedAt) {
    }
}
