package io.arrogantprogrammer.quarkusinsights.ddd.programming.application;

import io.arrogantprogrammer.quarkusinsights.ddd.programming.domain.EpisodeAirDateValueObject;
import io.arrogantprogrammer.quarkusinsights.ddd.programming.domain.EpisodeTitleValueObject;

public record ScheduleEpisodeCommand(EpisodeTitleValueObject title, String description, EpisodeAirDateValueObject airDate) {
}
