package io.arrogantprogrammer.quarkusinsights.programming.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Value object — the calendar date on which an episode is scheduled to air.
 *
 * <p>Layer: domain. Accepts any date including dates in the past — rehydration of an
 * already-aired episode from storage requires this. The "must not be in the past" rule
 * applies only to {@code schedule(...)} on the {@link Episode} aggregate and is checked
 * there against today's date, not in this value object's constructor.
 *
 * @param value the underlying {@link LocalDate}; never {@code null}
 */
public record AirDate(LocalDate value) {

    /**
     * Canonical constructor enforcing non-null.
     *
     * @param value the underlying date
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public AirDate {
        Objects.requireNonNull(value, "AirDate value must not be null");
    }

    /**
     * Convenience factory mirroring the canonical constructor.
     *
     * @param value the underlying date
     * @return an {@code AirDate} wrapping the value
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public static AirDate of(LocalDate value) {
        return new AirDate(value);
    }

    /**
     * Whether this air date has been reached as of the given day.
     *
     * @param today the day to compare against; never {@code null}
     * @return {@code true} if {@code today} is the same day as or later than this air date
     * @throws NullPointerException if {@code today} is {@code null}
     */
    public boolean hasArrivedBy(LocalDate today) {
        Objects.requireNonNull(today, "today must not be null");
        return !today.isBefore(value);
    }

    /**
     * Whether this air date falls strictly before the given day.
     *
     * @param today the day to compare against; never {@code null}
     * @return {@code true} if this date is strictly before {@code today}
     * @throws NullPointerException if {@code today} is {@code null}
     */
    public boolean isBefore(LocalDate today) {
        Objects.requireNonNull(today, "today must not be null");
        return value.isBefore(today);
    }
}
