package io.arrogantprogrammer.quarkusinsights.programming.domain;

/**
 * Domain exception — raised when an operation references an {@link EpisodeId} that does
 * not correspond to any persisted episode.
 *
 * <p>Layer: domain. Translated by the REST adapter to HTTP 404.
 */
public class EpisodeNotFoundException extends RuntimeException {

    private final EpisodeId episodeId;

    /**
     * Create a new not-found exception for the given identifier.
     *
     * @param episodeId the identifier that was not found; never {@code null}
     */
    public EpisodeNotFoundException(EpisodeId episodeId) {
        super("Episode not found: " + episodeId.value());
        this.episodeId = episodeId;
    }

    /**
     * The identifier that was not found.
     *
     * @return the missing episode identifier
     */
    public EpisodeId episodeId() {
        return episodeId;
    }
}
