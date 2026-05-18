package io.arrogantprogrammer.quarkusinsights.programming.application;

import io.arrogantprogrammer.quarkusinsights.programming.domain.AirDate;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeNumber;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeTitle;

import java.util.Objects;

/**
 * Application command — request to schedule a new episode.
 *
 * <p>Layer: application. Carries already-validated value objects from the calling adapter
 * (REST, CLI, message consumer, …) into the service. Adapters construct the value objects
 * at the boundary; any validation failure there surfaces as a value-object exception
 * before this command is ever built.
 *
 * @param number  the episode's sequential number; never {@code null}
 * @param title   the episode's title; never {@code null}
 * @param airDate the scheduled air date; never {@code null}
 */
public record ScheduleEpisodeCommand(EpisodeNumber number, EpisodeTitle title, AirDate airDate) {

    /**
     * Canonical constructor enforcing non-null on every field.
     *
     * @param number  the episode number
     * @param title   the episode title
     * @param airDate the scheduled air date
     * @throws NullPointerException if any field is {@code null}
     */
    public ScheduleEpisodeCommand {
        Objects.requireNonNull(number, "number");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(airDate, "airDate");
    }
}
