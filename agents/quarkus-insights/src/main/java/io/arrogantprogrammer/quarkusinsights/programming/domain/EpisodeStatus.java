package io.arrogantprogrammer.quarkusinsights.programming.domain;

/**
 * Value object — the lifecycle status of an {@link Episode}.
 *
 * <p>Layer: domain. Lifecycle:
 * <pre>
 *   SCHEDULED ──goLive──&gt; LIVE ──publish──&gt; PUBLISHED
 *        │
 *        └─ cancel ──&gt; CANCELED   (terminal)
 * </pre>
 * {@code PUBLISHED} and {@code CANCELED} are terminal — there is no "unpublish" or
 * "uncancel". An episode in those states stays there.
 */
public enum EpisodeStatus {

    /** The episode has been scheduled with a number, title, and air date, but is not yet live. */
    SCHEDULED,

    /** The episode is currently live (on or after its air date). */
    LIVE,

    /** Terminal — the episode has been published. */
    PUBLISHED,

    /** Terminal — the episode has been canceled before going live. */
    CANCELED;

    /**
     * Whether this status admits no further transitions.
     *
     * @return {@code true} if this is {@link #PUBLISHED} or {@link #CANCELED}
     */
    public boolean isTerminal() {
        return this == PUBLISHED || this == CANCELED;
    }
}
