package io.arrogantprogrammer.quarkusinsights.programming.interfaces.rest;

import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * REST exception mapper — {@link EpisodeNotFoundException} → HTTP 404.
 *
 * <p>Layer: interfaces.
 */
@Provider
public class EpisodeNotFoundMapper implements ExceptionMapper<EpisodeNotFoundException> {

    /**
     * Translate the domain exception into a structured 404 response.
     *
     * @param exception the thrown exception
     * @return a 404 response with a JSON {@link ErrorResponse} body
     */
    @Override
    public Response toResponse(EpisodeNotFoundException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("episode_not_found", exception.getMessage()))
                .build();
    }
}
