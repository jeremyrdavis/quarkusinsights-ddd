package io.arrogantprogrammer.quarkusinsights.programming.interfaces.rest;

import jakarta.validation.constraints.NotBlank;

/**
 * REST request DTO — JSON body for {@code POST /api/episodes/{id}/abstract}.
 *
 * <p>Layer: interfaces. Length bounds (100–5,000 chars) are enforced by the
 * {@link io.arrogantprogrammer.quarkusinsights.programming.domain.AbstractText} value-object
 * constructor inside the resource method; this DTO only checks for presence.
 *
 * @param text the synopsis text; non-blank
 */
public record SubmitAbstractRequest(@NotBlank String text) {
}
