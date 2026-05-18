package io.arrogantprogrammer.quarkusinsights.programming.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.time.Clock;

/**
 * Application-layer CDI producer — exposes a system {@link Clock} as a singleton bean so
 * the application service has a single, swappable source for "today" and "now".
 *
 * <p>Layer: application. Tests can replace this bean with a fixed clock via
 * {@code QuarkusMock.installMockForType} when behavior is date-dependent.
 */
@ApplicationScoped
public class SystemClockProducer {

    /**
     * Produce the application's wall-clock instance.
     *
     * @return a UTC system clock
     */
    @Produces
    @ApplicationScoped
    public Clock systemClock() {
        return Clock.systemUTC();
    }
}
