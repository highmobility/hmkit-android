package com.high_mobility.HMLink.Shared;

/**
 * Created by ttiganik on 13/04/16.
 */
public interface BroadcasterListener {
    /***
     * Callback for when the state has changed.
     * This is always called on the main thread when a change in the broadcaster's state occurs,
     * also when the same state was set again.
     *
     * @param state The new state of the Broadcaster.
     * @param oldState The old state of the Broadcaster.
     */
    void onStateChanged(Broadcaster.State state, Broadcaster.State oldState);

    /***
     * Callback for when a new link has been received by the Broadcaster.
     *
     * This is always called on the main thread when a new link has successfully connected to the
     * broadcaster. If there was an old connection up, then this doesn't trigger.
     *
     * @param link The link that connected successfully to the Broadcaster.
     */
    void onLinkReceived(ConnectedLink link);

    /***
     * Callback for when a known link has disconnected from the broadcaster.
     *
     * This is always called on the main thread when a known link has disconnected, but only when
     * the link was first received by the broadcaster (no connection alive before).
     *
     * @param link The link that disconnected from the broadcaster.
     */
    void onLinkLost(ConnectedLink link);
}
