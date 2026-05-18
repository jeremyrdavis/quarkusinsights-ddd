package io.arrogantprogrammer.quarkusinsights.programming.interfaces.rest;

import io.arrogantprogrammer.quarkusinsights.programming.domain.MissingPublishPreconditionException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * REST exception mapper — {@link MissingPublishPreconditionException} → HTTP 409.
 *
 * <p>Layer: interfaces.
 */
@Provider
public class MissingPublishPreconditionMapper implements ExceptionMapper<MissingPublishPreconditionException> {

    /**
     * Translate the domain exception into a structured 409 response.
     *
     * @param exception the thrown exception
     * @return a 409 response with a JSON {@link ErrorResponse} body
     */
    @Override
    public Response toResponse(MissingPublishPreconditionException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("missing_publish_precondition", exception.getMessage()))
                .build();
    }
}
