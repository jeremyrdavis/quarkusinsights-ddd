package io.arrogantprogrammer.quarkusinsights.programming.infrastructure.persistence;

import io.arrogantprogrammer.quarkusinsights.programming.domain.DuplicateEpisodeNumberException;
import io.arrogantprogrammer.quarkusinsights.programming.domain.Episode;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeId;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeNumber;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Persistence-adapter implementation of {@link EpisodeRepository} backed by Hibernate ORM.
 *
 * <p>Layer: infrastructure (persistence). Implements the third place of the spec's
 * "three-place" episode-number uniqueness rule: on a {@link #save(Episode)} that hits the
 * database {@code UNIQUE} constraint (most commonly a race with another transaction that
 * passed the service's pre-check), this class catches the underlying SQL state {@code 23505}
 * (or Hibernate's {@code ConstraintViolationException}) and re-throws
 * {@link DuplicateEpisodeNumberException}, so callers never see a raw infrastructure error.
 *
 * <p>Reads and writes are issued via {@link EntityManager}. Transaction boundaries are
 * the application service's responsibility (the service annotates its methods
 * {@code @Transactional}); this repository simply runs inside whatever transaction the
 * caller has opened.
 */
@ApplicationScoped
public class JpaEpisodeRepository implements EpisodeRepository {

    @Inject
    EntityManager em;

    @Inject
    EpisodeMapper mapper;

    /**
     * Look up an episode by identifier. Within an active transaction the result is the
     * managed entity already in the persistence context (so subsequent mutations are
     * tracked automatically).
     *
     * @param id the episode identifier; never {@code null}
     * @return the rehydrated aggregate, or {@link Optional#empty()} if none exists
     */
    @Override
    public Optional<Episode> findById(EpisodeId id) {
        EpisodeJpaEntity entity = em.find(EpisodeJpaEntity.class, id.value());
        return Optional.ofNullable(entity).map(mapper::toDomain);
    }

    /**
     * Service-side pre-check half of the three-place uniqueness rule.
     *
     * @param number the candidate number; never {@code null}
     * @return {@code true} if an episode with that number is already persisted
     */
    @Override
    public boolean existsByNumber(EpisodeNumber number) {
        Long count = em.createQuery(
                        "select count(e) from EpisodeJpaEntity e where e.number = :n", Long.class)
                .setParameter("n", number.value())
                .getSingleResult();
        return count > 0;
    }

    /**
     * Insert a new episode row, or update the existing one if the aggregate has been
     * loaded earlier in the same transaction.
     *
     * @param episode the aggregate to save; never {@code null}
     * @throws DuplicateEpisodeNumberException if the {@code UNIQUE} constraint on
     *                                         episode number rejects the insert
     */
    @Override
    public void save(Episode episode) {
        try {
            EpisodeJpaEntity existing = em.find(EpisodeJpaEntity.class, episode.id().value());
            if (existing == null) {
                em.persist(mapper.toNewEntity(episode));
            } else {
                mapper.applyTo(existing, episode);
            }
            em.flush();
        } catch (PersistenceException e) {
            if (isUniqueViolation(e)) {
                throw new DuplicateEpisodeNumberException(episode.number());
            }
            throw e;
        }
    }

    private static boolean isUniqueViolation(Throwable t) {
        Throwable cursor = t;
        while (cursor != null) {
            if (cursor instanceof org.hibernate.exception.ConstraintViolationException) {
                return true;
            }
            if (cursor instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
                return true;
            }
            cursor = cursor.getCause();
        }
        return false;
    }
}
