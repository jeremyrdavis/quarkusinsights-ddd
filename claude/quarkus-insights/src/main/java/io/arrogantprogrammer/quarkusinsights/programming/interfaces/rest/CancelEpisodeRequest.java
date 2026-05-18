package io.arrogantprogrammer.quarkusinsights.programming.interfaces.rest;

import jakarta.validation.constraints.NotBlank;

/**
 * REST request DTO — JSON body for {@code POST /api/episodes/{id}/cancel}.
 *
 * <p>Layer: interfaces.
 *
 * @param reason the human-readable reason for cancellation; non-blank
 */
public record CancelEpisodeRequest(@NotBlank String reason) {
}
