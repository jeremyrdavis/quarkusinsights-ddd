package io.arrogantprogrammer.quarkusinsights.programming.application;

import io.arrogantprogrammer.quarkusinsights.programming.domain.DuplicateEpisodeNumberException;
import io.arrogantprogrammer.quarkusinsights.programming.domain.Episode;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeId;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeNumber;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Test double — in-memory implementation of {@link EpisodeRepository} for application-service
 * tests that do not boot Quarkus.
 *
 * <p>Lives in the test source set. Stores aggregates by identifier in a {@link HashMap} and
 * enforces the episode-number UNIQUE constraint locally so the same domain exception is
 * surfaced from both the in-memory and the database-backed adapters.
 */
public class InMemoryEpisodeRepository implements EpisodeRepository {

    private final Map<EpisodeId, Episode> byId = new HashMap<>();

    @Override
    public Optional<Episode> findById(EpisodeId id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public boolean existsByNumber(EpisodeNumber number) {
        return byId.values().stream().anyMatch(e -> e.number().equals(number));
    }

    @Override
    public void save(Episode episode) {
        for (Episode existing : byId.values()) {
            if (!existing.id().equals(episode.id()) && existing.number().equals(episode.number())) {
                throw new DuplicateEpisodeNumberException(episode.number());
            }
        }
        byId.put(episode.id(), episode);
    }

    /**
     * @return the number of stored aggregates
     */
    public int size() {
        return byId.size();
    }
}
