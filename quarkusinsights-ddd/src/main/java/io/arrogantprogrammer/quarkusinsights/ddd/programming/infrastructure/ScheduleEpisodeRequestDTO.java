package io.arrogantprogrammer.quarkusinsights.ddd.programming.infrastructure;

import java.time.LocalDate;

public record ScheduleEpisodeRequestDTO(String title, String description, LocalDate localDate) {
}
