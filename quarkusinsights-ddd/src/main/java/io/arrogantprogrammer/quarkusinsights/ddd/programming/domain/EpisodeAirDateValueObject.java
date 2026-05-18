package io.arrogantprogrammer.quarkusinsights.ddd.programming.domain;

import java.time.LocalDate;

public record EpisodeAirDateValueObject(LocalDate value) {

    public EpisodeAirDateValueObject(LocalDate value) {
        if(value == null) {
            throw new IllegalArgumentException("Air date cannot be null");
        }
        if(value.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Air date cannot be in the past");
        }
        this.value = value;
    }
}
