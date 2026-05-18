package io.arrogantprogrammer.quarkusinsights.programming.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValueObjectsTest {

    @Nested
    class EpisodeNumberRules {

        @Test
        void acceptsOne() {
            assertEquals(1, EpisodeNumber.of(1).value());
        }

        @Test
        void rejectsZero() {
            assertThrows(IllegalArgumentException.class, () -> EpisodeNumber.of(0));
        }

        @Test
        void rejectsNegative() {
            assertThrows(IllegalArgumentException.class, () -> EpisodeNumber.of(-3));
        }
    }

    @Nested
    class EpisodeTitleRules {

        @Test
        void trimsAndAcceptsTypicalTitle() {
            assertEquals("Hexagonal Architecture", EpisodeTitle.of("  Hexagonal Architecture  ").value());
        }

        @Test
        void rejectsNull() {
            assertThrows(NullPointerException.class, () -> EpisodeTitle.of(null));
        }

        @Test
        void rejectsBlank() {
            assertThrows(IllegalArgumentException.class, () -> EpisodeTitle.of("   "));
        }

        @Test
        void rejectsTooLong() {
            String tooLong = "x".repeat(EpisodeTitle.MAX_LENGTH + 1);
            assertThrows(IllegalArgumentException.class, () -> EpisodeTitle.of(tooLong));
        }

        @Test
        void acceptsMaxLength() {
            String atLimit = "x".repeat(EpisodeTitle.MAX_LENGTH);
            assertEquals(EpisodeTitle.MAX_LENGTH, EpisodeTitle.of(atLimit).value().length());
        }
    }

    @Nested
    class AirDateRules {

        @Test
        void rejectsNull() {
            assertThrows(NullPointerException.class, () -> AirDate.of(null));
        }

        @Test
        void acceptsPastDate() {
            AirDate d = AirDate.of(LocalDate.of(2020, 1, 1));
            assertTrue(d.isBefore(LocalDate.of(2026, 1, 1)));
        }

        @Test
        void hasArrivedByDetectsSameDayAndLater() {
            AirDate d = AirDate.of(LocalDate.of(2026, 5, 18));
            assertAll(
                    () -> assertTrue(d.hasArrivedBy(LocalDate.of(2026, 5, 18))),
                    () -> assertTrue(d.hasArrivedBy(LocalDate.of(2026, 5, 19))),
                    () -> assertFalse(d.hasArrivedBy(LocalDate.of(2026, 5, 17)))
            );
        }
    }

    @Nested
    class AbstractTextRules {

        @Test
        void rejectsTooShort() {
            String tooShort = "x".repeat(AbstractText.MIN_LENGTH - 1);
            assertThrows(IllegalArgumentException.class, () -> AbstractText.of(tooShort));
        }

        @Test
        void acceptsAtMinLength() {
            String atMin = "x".repeat(AbstractText.MIN_LENGTH);
            assertEquals(AbstractText.MIN_LENGTH, AbstractText.of(atMin).value().length());
        }

        @Test
        void rejectsTooLong() {
            String tooLong = "x".repeat(AbstractText.MAX_LENGTH + 1);
            assertThrows(IllegalArgumentException.class, () -> AbstractText.of(tooLong));
        }

        @Test
        void rejectsNull() {
            assertThrows(NullPointerException.class, () -> AbstractText.of(null));
        }

        @Test
        void trimsBeforeValidating() {
            String padded = "   " + "x".repeat(AbstractText.MIN_LENGTH) + "   ";
            AbstractText text = AbstractText.of(padded);
            assertEquals(AbstractText.MIN_LENGTH, text.value().length());
        }
    }

    @Nested
    class EpisodeStatusRules {

        @Test
        void publishedAndCanceledAreTerminal() {
            assertTrue(EpisodeStatus.PUBLISHED.isTerminal());
            assertTrue(EpisodeStatus.CANCELED.isTerminal());
        }

        @Test
        void scheduledAndLiveAreNotTerminal() {
            assertFalse(EpisodeStatus.SCHEDULED.isTerminal());
            assertFalse(EpisodeStatus.LIVE.isTerminal());
        }
    }

    @Nested
    class IdRules {

        @Test
        void episodeIdRejectsNullUuid() {
            assertThrows(NullPointerException.class, () -> new EpisodeId(null));
        }

        @Test
        void abstractIdRejectsNullUuid() {
            assertThrows(NullPointerException.class, () -> new AbstractId(null));
        }

        @Test
        void episodeIdRoundTripsThroughString() {
            EpisodeId generated = EpisodeId.generate();
            EpisodeId parsed = EpisodeId.of(generated.value().toString());
            assertEquals(generated, parsed);
        }
    }
}
