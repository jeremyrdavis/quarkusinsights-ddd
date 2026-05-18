package io.arrogantprogrammer.quarkusinsights.programming.interfaces.rest;

import io.arrogantprogrammer.quarkusinsights.programming.domain.InvalidAirDateException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * REST exception mapper — {@link InvalidAirDateException} → HTTP 400.
 *
 * <p>Layer: interfaces. Covers both "schedule with a past air date" and
 * "go live before the air date has been reached".
 */
@Provider
public class InvalidAirDateMapper implements ExceptionMapper<InvalidAirDateException> {

    /**
     * Translate the domain exception into a structured 400 response.
     *
     * @param exception the thrown exception
     * @return a 400 response with a JSON {@link ErrorResponse} body
     */
    @Override
    public Response toResponse(InvalidAirDateException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("invalid_air_date", exception.getMessage()))
                .build();
    }
}
