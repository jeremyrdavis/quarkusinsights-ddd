package io.arrogantprogrammer.quarkusinsights.programming.infrastructure.persistence;

import io.arrogantprogrammer.quarkusinsights.programming.domain.AbstractText;
import io.arrogantprogrammer.quarkusinsights.programming.domain.AirDate;
import io.arrogantprogrammer.quarkusinsights.programming.domain.DuplicateEpisodeNumberException;
import io.arrogantprogrammer.quarkusinsights.programming.domain.Episode;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeId;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeNumber;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeStatus;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeTitle;
import io.arrogantprogrammer.quarkusinsights.shared.PersonId;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class JpaEpisodeRepositoryTest {

    @Inject
    JpaEpisodeRepository repository;

    @Inject
    EpisodeMapper mapper;

    private static final AtomicInteger NUMBERS = new AtomicInteger(1_000);
    private static final LocalDate TODAY = LocalDate.of(2026, 5, 18);
    private static final Instant NOW = Instant.parse("2026-05-18T12:00:00Z");

    private static EpisodeNumber nextNumber() {
        return EpisodeNumber.of(NUMBERS.getAndIncrement());
    }

    private static Episode newScheduled(EpisodeNumber number) {
        return Episode.schedule(
                number,
                EpisodeTitle.of("Test " + number.value()),
                AirDate.of(LocalDate.of(2026, 6, 1)),
                TODAY, NOW);
    }

    @Test
    @TestTransaction
    void persistsAndLoadsRoundTrip() {
        Episode original = newScheduled(nextNumber());
        original.assignPresenter(new PersonId(UUID.randomUUID()), NOW);
        original.assignSpeaker(new PersonId(UUID.randomUUID()), NOW);
        original.submitAbstract(AbstractText.of("x".repeat(150)), NOW);

        repository.save(original);

        Episode loaded = repository.findById(original.id()).orElseThrow();
        assertEquals(original.id(), loaded.id());
        assertEquals(original.number(), loaded.number());
        assertEquals(original.title(), loaded.title());
        assertEquals(original.airDate(), loaded.airDate());
        assertEquals(EpisodeStatus.SCHEDULED, loaded.status());
        assertEquals(1, loaded.presenters().size());
        assertEquals(1, loaded.speakers().size());
        assertTrue(loaded.synopsis().isPresent());
        assertEquals(original.synopsis().orElseThrow().text(), loaded.synopsis().orElseThrow().text());
    }

    @Test
    @TestTransaction
    void duplicateNumberRaisesDomainExceptionNotPersistenceException() {
        EpisodeNumber shared = nextNumber();
        repository.save(newScheduled(shared));

        // Second persist of a *different* aggregate with the same number must surface as
        // the domain exception, never as a raw JPA/PSQL exception.
        Episode collision = newScheduled(shared);
        DuplicateEpisodeNumberException ex = assertThrows(
                DuplicateEpisodeNumberException.class,
                () -> repository.save(collision));
        assertEquals(shared, ex.number());
    }

    @Test
    @TestTransaction
    void existsByNumberAfterPersist() {
        EpisodeNumber number = nextNumber();
        assertFalse(repository.existsByNumber(number));

        repository.save(newScheduled(number));

        assertTrue(repository.existsByNumber(number));
    }

    @Test
    @TestTransaction
    void findByIdReturnsEmptyForUnknownId() {
        assertTrue(repository.findById(EpisodeId.generate()).isEmpty());
    }

    @Test
    @TestTransaction
    void updateInPlaceReflectsChanges() {
        Episode episode = newScheduled(nextNumber());
        repository.save(episode);

        Episode loaded = repository.findById(episode.id()).orElseThrow();
        loaded.assignPresenter(new PersonId(UUID.randomUUID()), NOW);
        repository.save(loaded);

        Episode reloaded = repository.findById(episode.id()).orElseThrow();
        assertEquals(1, reloaded.presenters().size());
    }

    @Test
    @TestTransaction
    void mapperRoundTripPreservesAllFields() {
        Episode original = newScheduled(nextNumber());
        original.submitAbstract(AbstractText.of("y".repeat(200)), NOW);
        PersonId p = new PersonId(UUID.randomUUID());
        original.assignPresenter(p, NOW);

        EpisodeJpaEntity entity = mapper.toNewEntity(original);
        Episode reconstructed = mapper.toDomain(entity);

        assertNotNull(reconstructed);
        assertEquals(original.id(), reconstructed.id());
        assertEquals(original.title(), reconstructed.title());
        assertEquals(original.synopsis().orElseThrow().id(),
                reconstructed.synopsis().orElseThrow().id());
        assertTrue(reconstructed.presenters().contains(p));
    }
}
