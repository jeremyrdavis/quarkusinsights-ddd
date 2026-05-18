package io.arrogantprogrammer.quarkusinsights.programming.interfaces.rest;

import io.arrogantprogrammer.quarkusinsights.programming.application.EpisodeService;
import io.arrogantprogrammer.quarkusinsights.programming.application.ScheduleEpisodeCommand;
import io.arrogantprogrammer.quarkusinsights.programming.domain.AbstractText;
import io.arrogantprogrammer.quarkusinsights.programming.domain.AirDate;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeId;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeNumber;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeTitle;
import io.arrogantprogrammer.quarkusinsights.shared.PersonId;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.UUID;

/**
 * REST adapter — exposes the {@link EpisodeService} over HTTP at {@code /api/episodes}.
 *
 * <p>Layer: interfaces. Methods are intentionally thin: parse the request DTO, construct
 * domain value objects (which validate at the boundary), delegate to the service, and
 * shape the response. No branching on business state, no exception handling — domain
 * exceptions bubble up to dedicated {@code ExceptionMapper}s.
 */
@Path("/api/episodes")
@Produces(MediaType.APPLICATION_JSON)
public class EpisodesResource {

    @Inject
    EpisodeService service;

    /**
     * Schedule a new episode.
     *
     * @param request the scheduling payload; validated by Bean Validation
     * @param uriInfo injected URI builder used to construct the {@code Location} header
     * @return {@code 201 Created} with the new episode's representation and a {@code Location} header
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response schedule(@Valid ScheduleEpisodeRequest request, @Context UriInfo uriInfo) {
        EpisodeId id = service.schedule(new ScheduleEpisodeCommand(
                EpisodeNumber.of(request.number()),
                EpisodeTitle.of(request.title()),
                AirDate.of(request.airDate())));
        URI location = uriInfo.getAbsolutePathBuilder().path(id.value().toString()).build();
        return Response.created(location)
                .entity(EpisodeResponse.from(service.load(id)))
                .build();
    }

    /**
     * Fetch an episode by identifier.
     *
     * @param id the episode UUID from the path
     * @return {@code 200 OK} with the episode representation
     */
    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") @NotNull UUID id) {
        return Response.ok(EpisodeResponse.from(service.load(new EpisodeId(id)))).build();
    }

    /**
     * Submit (or replace) the abstract of an existing episode.
     *
     * @param id      the episode UUID
     * @param request the new abstract payload
     * @return {@code 200 OK} with the updated episode representation
     */
    @POST
    @Path("/{id}/abstract")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response submitAbstract(
            @PathParam("id") @NotNull UUID id,
            @Valid SubmitAbstractRequest request) {
        EpisodeId episodeId = new EpisodeId(id);
        service.submitAbstract(episodeId, AbstractText.of(request.text()));
        return Response.ok(EpisodeResponse.from(service.load(episodeId))).build();
    }

    /**
     * Assign a presenter to an existing episode (idempotent).
     *
     * @param id      the episode UUID
     * @param request the presenter payload
     * @return {@code 200 OK} with the updated episode representation
     */
    @POST
    @Path("/{id}/presenters")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response assignPresenter(
            @PathParam("id") @NotNull UUID id,
            @Valid AssignPersonRequest request) {
        EpisodeId episodeId = new EpisodeId(id);
        service.assignPresenter(episodeId, new PersonId(request.personId()));
        return Response.ok(EpisodeResponse.from(service.load(episodeId))).build();
    }

    /**
     * Assign a speaker to an existing episode (idempotent).
     *
     * @param id      the episode UUID
     * @param request the speaker payload
     * @return {@code 200 OK} with the updated episode representation
     */
    @POST
    @Path("/{id}/speakers")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response assignSpeaker(
            @PathParam("id") @NotNull UUID id,
            @Valid AssignPersonRequest request) {
        EpisodeId episodeId = new EpisodeId(id);
        service.assignSpeaker(episodeId, new PersonId(request.personId()));
        return Response.ok(EpisodeResponse.from(service.load(episodeId))).build();
    }

    /**
     * Transition an episode from SCHEDULED to LIVE.
     *
     * @param id the episode UUID
     * @return {@code 200 OK} with the updated episode representation
     */
    @POST
    @Path("/{id}/go-live")
    public Response goLive(@PathParam("id") @NotNull UUID id) {
        EpisodeId episodeId = new EpisodeId(id);
        service.goLive(episodeId);
        return Response.ok(EpisodeResponse.from(service.load(episodeId))).build();
    }

    /**
     * Transition an episode from LIVE to PUBLISHED.
     *
     * @param id the episode UUID
     * @return {@code 200 OK} with the updated episode representation
     */
    @POST
    @Path("/{id}/publish")
    public Response publish(@PathParam("id") @NotNull UUID id) {
        EpisodeId episodeId = new EpisodeId(id);
        service.publish(episodeId);
        return Response.ok(EpisodeResponse.from(service.load(episodeId))).build();
    }

    /**
     * Cancel a scheduled episode.
     *
     * @param id      the episode UUID
     * @param request the cancellation payload (carries a non-blank reason)
     * @return {@code 200 OK} with the updated episode representation
     */
    @POST
    @Path("/{id}/cancel")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cancel(
            @PathParam("id") @NotNull UUID id,
            @Valid CancelEpisodeRequest request) {
        EpisodeId episodeId = new EpisodeId(id);
        service.cancel(episodeId, request.reason());
        return Response.ok(EpisodeResponse.from(service.load(episodeId))).build();
    }
}
