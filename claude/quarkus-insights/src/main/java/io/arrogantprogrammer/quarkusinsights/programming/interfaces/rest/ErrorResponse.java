package io.arrogantprogrammer.quarkusinsights.programming.interfaces.rest;

/**
 * REST response DTO — uniform error envelope returned by every {@code ExceptionMapper}.
 *
 * <p>Layer: interfaces.
 *
 * @param code    a stable, machine-readable error code (e.g. {@code "episode_not_found"})
 * @param message a human-readable description (safe for client display)
 */
public record ErrorResponse(String code, String message) {
}
