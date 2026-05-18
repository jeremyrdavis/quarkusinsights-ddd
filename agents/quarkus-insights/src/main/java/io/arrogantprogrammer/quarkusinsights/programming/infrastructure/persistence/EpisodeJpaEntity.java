package io.arrogantprogrammer.quarkusinsights.programming.infrastructure.persistence;

import io.arrogantprogrammer.quarkusinsights.programming.domain.EpisodeStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Persistence-adapter entity — the row-shaped JPA model for an episode.
 *
 * <p>Layer: infrastructure (persistence). Deliberately separate from the
 * {@link io.arrogantprogrammer.quarkusinsights.programming.domain.Episode} aggregate, which
 * is a pure POJO. Translation between the two is the {@link EpisodeMapper}'s job; no other
 * class should touch this entity directly.
 *
 * <p>The {@code UNIQUE} constraint on {@code number} is the database half of the
 * "three-place" episode-number uniqueness rule — the service pre-check is fast and friendly,
 * the DB constraint catches concurrent races, and {@link JpaEpisodeRepository} translates
 * the constraint violation back into a domain exception so callers never see infrastructure
 * errors.
 */
@Entity
@Table(
        name = "episode",
        uniqueConstraints = @UniqueConstraint(name = "uk_episode_number", columnNames = "number")
)
public class EpisodeJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "number", nullable = false)
    public int number;

    @Column(name = "title", nullable = false, length = 200)
    public String title;

    @Column(name = "air_date", nullable = false)
    public LocalDate airDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    public EpisodeStatus status;

    @Column(name = "abstract_id")
    public UUID abstractId;

    @Column(name = "abstract_text", length = AbstractTextColumnLimits.MAX_LENGTH)
    public String abstractText;

    @Column(name = "abstract_submitted_at")
    public Instant abstractSubmittedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "episode_presenter",
            joinColumns = @JoinColumn(name = "episode_id", nullable = false)
    )
    @Column(name = "person_id", nullable = false)
    public Set<UUID> presenters = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "episode_speaker",
            joinColumns = @JoinColumn(name = "episode_id", nullable = false)
    )
    @Column(name = "person_id", nullable = false)
    public Set<UUID> speakers = new HashSet<>();

    @Column(name = "cancellation_reason")
    public String cancellationReason;

    @Version
    @Column(name = "version", nullable = false)
    public Long version;

    /**
     * JPA-required no-arg constructor.
     */
    public EpisodeJpaEntity() {
    }

    private static final class AbstractTextColumnLimits {
        static final int MAX_LENGTH = 5_000;
    }
}
