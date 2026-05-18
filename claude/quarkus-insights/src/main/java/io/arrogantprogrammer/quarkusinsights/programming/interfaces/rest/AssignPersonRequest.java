package io.arrogantprogrammer.quarkusinsights.programming.interfaces.rest;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * REST request DTO — JSON body for both {@code POST /api/episodes/{id}/presenters} and
 * {@code POST /api/episodes/{id}/speakers}. Both endpoints take the same shape (a single
 * person identifier).
 *
 * <p>Layer: interfaces.
 *
 * @param personId the assigned person's identifier; must be a non-null UUID
 */
public record AssignPersonRequest(@NotNull UUID personId) {
}
