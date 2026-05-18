package io.arrogantprogrammer.quarkusinsights.ddd.programming.domain;

public record EpisodeTitleValueObject(String value) {

    public EpisodeTitleValueObject(String value) {
        if(value == null) {
            throw new IllegalArgumentException("Episode title cannot be null");
        }
        this.value = value;
    }
}
