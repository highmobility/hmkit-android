package com.high_mobility.HMLink.Shared;

/**
 * Created by ttiganik on 13/04/16.
 */
public interface BroadcasterListener {
    /***
     * Callback for when the Broadcaster's state has changed.
     * This is always called on the main thread.
     *
     * @param oldState The old state of the Broadcaster.
     */
    void onStateChanged(Broadcaster.State oldState);

    /***
     * Callback for when a new link has connected to the Broadcaster.
     *
     * This is always called on the main thread.
     *
     * @param link The link that connected successfully to the Broadcaster.
     */
    void onLinkReceived(ConnectedLink link);

    /***
     * Callback for when a known link has disconnected from the broadcaster.
     *
     * This is always called on the main thread.
     *
     * @param link The link that disconnected from the broadcaster.
     */
    void onLinkLost(ConnectedLink link);
}
