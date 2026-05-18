package io.arrogantprogrammer.quarkusinsights.programming.infrastructure.persistence;

import io.arrogantprogrammer.quarkusinsights.programming.domain.Abstract;
import io.arrogantprogrammer.quarkusinsights.programming.domain.AbstractId;
import io.arrogantprogrammer.quarkusinsights.programming.domain.AbstractText;
import io.arrogantprogrammer.quarkusinsights.programming.domain.AirDate;
import io.arrogantprogrammer.quarkusinsights.programming.domain.Episode;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeId;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeNumber;
import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeTitle;
import io.arrogantprogrammer.quarkusinsights.shared.PersonId;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence-adapter mapper — translates between the domain {@link Episode} aggregate
 * (pure POJO) and the {@link EpisodeJpaEntity} (row-shaped JPA model).
 *
 * <p>Layer: infrastructure (persistence). All knowledge of how the aggregate is laid out in
 * the database lives here; the domain never imports a persistence type, and the JPA entity
 * never knows about the aggregate's lifecycle methods. Replacing the persistence technology
 * means rewriting this class and the repository — nothing else.
 */
@ApplicationScoped
public class EpisodeMapper {

    /**
     * Translate a domain aggregate into a brand-new JPA entity suitable for {@code persist()}.
     *
     * @param episode the aggregate; never {@code null}
     * @return a fresh, transient JPA entity carrying the aggregate's state
     */
    public EpisodeJpaEntity toNewEntity(Episode episode) {
        EpisodeJpaEntity entity = new EpisodeJpaEntity();
        applyTo(entity, episode);
        return entity;
    }

    /**
     * Copy the aggregate's current state onto an already-managed JPA entity, leaving the
     * entity's JPA version alone (Hibernate manages it on flush).
     *
     * @param entity  the managed entity; never {@code null}
     * @param episode the aggregate; never {@code null}
     */
    public void applyTo(EpisodeJpaEntity entity, Episode episode) {
        entity.id = episode.id().value();
        entity.number = episode.number().value();
        entity.title = episode.title().value();
        entity.airDate = episode.airDate().value();
        entity.status = episode.status();
        episode.synopsis().ifPresentOrElse(
                a -> {
                    entity.abstractId = a.id().value();
                    entity.abstractText = a.text().value();
                    entity.abstractSubmittedAt = a.submittedAt();
                },
                () -> {
                    entity.abstractId = null;
                    entity.abstractText = null;
                    entity.abstractSubmittedAt = null;
                });
        entity.presenters.clear();
        for (PersonId id : episode.presenters()) {
            entity.presenters.add(id.value());
        }
        entity.speakers.clear();
        for (PersonId id : episode.speakers()) {
            entity.speakers.add(id.value());
        }
        entity.cancellationReason = episode.cancellationReason().orElse(null);
    }

    /**
     * Rehydrate a domain aggregate from a JPA entity.
     *
     * @param entity the persisted entity; never {@code null}
     * @return the rehydrated aggregate with an empty events buffer
     */
    public Episode toDomain(EpisodeJpaEntity entity) {
        Abstract synopsis = null;
        if (entity.abstractId != null) {
            synopsis = new Abstract(
                    new AbstractId(entity.abstractId),
                    AbstractText.of(entity.abstractText),
                    entity.abstractSubmittedAt);
        }
        Set<PersonId> presenters = entity.presenters.stream()
                .map(PersonId::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<PersonId> speakers = entity.speakers.stream()
                .map(PersonId::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return Episode.rehydrate(
                new EpisodeId(entity.id),
                EpisodeNumber.of(entity.number),
                EpisodeTitle.of(entity.title),
                AirDate.of(entity.airDate),
                entity.status,
                synopsis,
                presenters,
                speakers,
                entity.cancellationReason);
    }
}
