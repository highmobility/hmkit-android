package com.highmobility.hmkit;

/**
 * The listener of the Broadcaster is used to observe the device's state changes and for
 * discovered/lost Links.
 */
public interface BroadcasterListener {
    /**
     * Callback for when the Broadcaster's state has changed.
     *
     * @param oldState The old state of the Broadcaster.
     */
    void onStateChanged(Broadcaster.State oldState);

    /**
     * Callback for when a new link has connected to the Broadcaster.
     *
     * @param link The link that successfully connected to the Broadcaster.
     */
    void onLinkReceived(ConnectedLink link);

    /**
     * Callback for when a link has disconnected from the broadcaster.
     *
     * @param link The link that disconnected from the broadcaster.
     */
    void onLinkLost(ConnectedLink link);
}
