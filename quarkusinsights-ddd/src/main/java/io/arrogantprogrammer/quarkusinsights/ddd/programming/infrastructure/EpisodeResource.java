package io.arrogantprogrammer.quarkusinsights.ddd.programming.infrastructure;

import io.arrogantprogrammer.quarkusinsights.ddd.programming.application.EpisodeApplicationService;
import io.arrogantprogrammer.quarkusinsights.ddd.programming.application.ScheduleEpisodeCommand;
import io.arrogantprogrammer.quarkusinsights.ddd.programming.domain.EpisodeAirDateValueObject;
import io.arrogantprogrammer.quarkusinsights.ddd.programming.domain.EpisodeTitleValueObject;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/episodes/ddd")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EpisodeResource {

    @Inject
    private EpisodeApplicationService episodeApplicationService;

    @POST
    public Response scheduleEpisode(ScheduleEpisodeRequestDTO scheduleEpisodeRequestDTO){
        Log.debugf("Received schedule episode request: %s", scheduleEpisodeRequestDTO);
        ScheduleEpisodeCommand scheduleEpisodeCommand = new ScheduleEpisodeCommand(
                new EpisodeTitleValueObject(scheduleEpisodeRequestDTO.title()),
                scheduleEpisodeRequestDTO.description(),
                new EpisodeAirDateValueObject(scheduleEpisodeRequestDTO.localDate()));
        EpisodeDTO episodeDTO = episodeApplicationService.scheduleEpisode(scheduleEpisodeCommand);
        Log.debugf("Scheduled episode: %s", episodeDTO);
        return Response.ok(episodeDTO).build();
    }
}
