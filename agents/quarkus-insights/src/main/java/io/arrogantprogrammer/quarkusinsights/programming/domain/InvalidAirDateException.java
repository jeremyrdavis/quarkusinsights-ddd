package io.arrogantprogrammer.quarkusinsights.programming.domain;

import java.time.LocalDate;

/**
 * Domain exception — raised either when an episode is being scheduled with an air date in
 * the past, or when {@link Episode#goLive(LocalDate)} is invoked before the air date is
 * reached.
 *
 * <p>Layer: domain. Translated by the REST adapter to HTTP 400.
 */
public class InvalidAirDateException extends RuntimeException {

    private final LocalDate airDate;
    private final LocalDate today;

    /**
     * Create a new invalid-air-date exception.
     *
     * @param message a human-readable description of the violation; never {@code null}
     * @param airDate the offending air date; never {@code null}
     * @param today   the comparison date (today) used to detect the violation; never {@code null}
     */
    public InvalidAirDateException(String message, LocalDate airDate, LocalDate today) {
        super(message);
        this.airDate = airDate;
        this.today = today;
    }

    /**
     * The offending air date.
     *
     * @return the air date
     */
    public LocalDate airDate() {
        return airDate;
    }

    /**
     * The day against which the violation was detected.
     *
     * @return today
     */
    public LocalDate today() {
        return today;
    }
}
