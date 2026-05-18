package io.arrogantprogrammer.quarkusinsights.programming.interfaces.rest;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * REST exception mapper — {@link IllegalArgumentException} → HTTP 400.
 *
 * <p>Layer: interfaces. Value-object constructors (and the aggregate's {@code cancel}
 * method) throw {@link IllegalArgumentException} on out-of-range or blank input; this
 * mapper turns that into the structured 400 response required by the spec's error table.
 */
@Provider
public class ValueObjectValidationMapper implements ExceptionMapper<IllegalArgumentException> {

    /**
     * Translate the validation exception into a structured 400 response.
     *
     * @param exception the thrown exception
     * @return a 400 response with a JSON {@link ErrorResponse} body
     */
    @Override
    public Response toResponse(IllegalArgumentException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("validation_failed", exception.getMessage()))
                .build();
    }
}
