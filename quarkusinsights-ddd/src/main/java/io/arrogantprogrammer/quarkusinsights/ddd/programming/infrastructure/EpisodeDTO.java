package io.arrogantprogrammer.quarkusinsights.ddd.programming.infrastructure;

import java.time.LocalDate;

public record EpisodeDTO(String title, String description, LocalDate airDate) {
}
