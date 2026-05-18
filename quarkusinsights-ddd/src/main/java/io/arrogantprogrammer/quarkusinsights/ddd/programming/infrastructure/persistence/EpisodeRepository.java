package io.arrogantprogrammer.quarkusinsights.ddd.programming.infrastructure.persistence;

import io.arrogantprogrammer.quarkusinsights.ddd.programming.domain.EpisodeAggregate;
import io.arrogantprogrammer.quarkusinsights.ddd.programming.domain.EpisodeTitleValueObject;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Date;

@ApplicationScoped
public class EpisodeRepository implements PanacheRepository<EpisodeEntity> {

    public boolean titleIsUnique(EpisodeTitleValueObject title) {
        return count("title", title.value()) == 0;
    }

    public EpisodeAggregate persist(EpisodeAggregate episodeAggregate) {
        Log.debugf("Persisting episode: %s", episodeAggregate.getTitle().value());
        EpisodeEntity episodeEntity = EpisodeMapper.toEntity(episodeAggregate);
        persist(episodeEntity);
        return EpisodeAggregate.rehydrate(
                episodeEntity.getTitle(),
                episodeEntity.getDescription(),
                episodeEntity.getScheduledDate().toLocalDate()
        );
    }

    private class EpisodeMapper {
        static EpisodeEntity toEntity(EpisodeAggregate episodeAggregate){
            Log.debugf("Mapping episode aggregate to entity: %s", episodeAggregate.getTitle().value());
            return new EpisodeEntity(
                    episodeAggregate.getTitle().value(),
                    episodeAggregate.getDescription(),
                    Date.valueOf(episodeAggregate.getAirDate().value())
            );
        }
    }
}
