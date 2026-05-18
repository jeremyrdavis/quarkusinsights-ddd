package io.arrogantprogrammer.quarkusinsights.programming.domain;

import java.util.Optional;

/**
 * Repository port — the abstract data interface the application service uses to load and
 * persist {@link Episode} aggregates.
 *
 * <p>Layer: domain. Implementations live in the infrastructure layer (e.g., a JPA-backed
 * adapter). Swapping the implementation (Panache → jOOQ, Postgres → in-memory) must not
 * require changes to the domain or to the application service.
 */
public interface EpisodeRepository {

    /**
     * Look up an episode by its identifier.
     *
     * @param id the episode identifier; never {@code null}
     * @return the aggregate if found, otherwise {@link Optional#empty()}
     */
    Optional<Episode> findById(EpisodeId id);

    /**
     * Whether an episode with the given number already exists. Used by the application
     * service for the pre-check half of the three-place uniqueness rule.
     *
     * @param number the candidate episode number; never {@code null}
     * @return {@code true} if an episode with that number is already persisted
     */
    boolean existsByNumber(EpisodeNumber number);

    /**
     * Persist (insert or update) the given aggregate.
     *
     * <p>Implementations must translate the database UNIQUE-constraint violation on
     * episode number into a {@link DuplicateEpisodeNumberException} so callers never see
     * raw infrastructure exceptions.
     *
     * @param episode the aggregate to save; never {@code null}
     * @throws DuplicateEpisodeNumberException if persisting would violate the
     *                                         episode-number UNIQUE constraint
     */
    void save(Episode episode);
}
