package io.arrogantprogrammer.quarkusinsights.programming.interfaces.rest;

import io.arrogantprogrammer.quarkusinsights.programming.domain.IllegalEpisodeStateException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * REST exception mapper — {@link IllegalEpisodeStateException} → HTTP 409.
 *
 * <p>Layer: interfaces. Applies to state-machine conflicts (e.g., publishing an episode
 * that is not LIVE, canceling one already terminal).
 */
@Provider
public class IllegalEpisodeStateMapper implements ExceptionMapper<IllegalEpisodeStateException> {

    /**
     * Translate the domain exception into a structured 409 response.
     *
     * @param exception the thrown exception
     * @return a 409 response with a JSON {@link ErrorResponse} body
     */
    @Override
    public Response toResponse(IllegalEpisodeStateException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("illegal_episode_state", exception.getMessage()))
                .build();
    }
}
