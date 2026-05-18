package io.arrogantprogrammer.quarkusinsights.programming.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * REST request DTO — JSON body for {@code POST /api/episodes} (schedule a new episode).
 *
 * <p>Layer: interfaces. Bean Validation constraints fire before the resource method body
 * and produce HTTP 400 automatically; value-object construction inside the resource then
 * applies finer-grained domain-level validation.
 *
 * @param number  the episode's sequential number; must be a positive integer
 * @param title   the episode's title; must be non-blank
 * @param airDate the scheduled air date; must be present (must not be in the past — enforced by the domain)
 */
public record ScheduleEpisodeRequest(
        @NotNull @Positive Integer number,
        @NotBlank String title,
        @NotNull LocalDate airDate
) {
}
