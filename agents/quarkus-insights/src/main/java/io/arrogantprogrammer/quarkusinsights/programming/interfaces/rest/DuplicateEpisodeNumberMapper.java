package io.arrogantprogrammer.quarkusinsights.programming.interfaces.rest;

import io.arrogantprogrammer.quarkusinsights.programming.domain.DuplicateEpisodeNumberException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * REST exception mapper — {@link DuplicateEpisodeNumberException} → HTTP 409.
 *
 * <p>Layer: interfaces.
 */
@Provider
public class DuplicateEpisodeNumberMapper implements ExceptionMapper<DuplicateEpisodeNumberException> {

    /**
     * Translate the domain exception into a structured 409 response.
     *
     * @param exception the thrown exception
     * @return a 409 response with a JSON {@link ErrorResponse} body
     */
    @Override
    public Response toResponse(DuplicateEpisodeNumberException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("duplicate_episode_number", exception.getMessage()))
                .build();
    }
}
