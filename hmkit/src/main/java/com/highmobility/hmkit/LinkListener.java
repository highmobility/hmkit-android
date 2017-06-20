package com.highmobility.hmkit;

/**
 * Created by ttiganik on 13/04/16.
 */
public interface LinkListener {
    /**
     * Callback that is invoked when the Link's state changes.
     *
     * This is always called on the main thread.
     *
     * @param link The link that had a state change.
     * @param oldState The old state of the link.
     */
    void onStateChanged(Link link, Link.State oldState);

    /**
     * Callback for when a command has been received by the Link.
     *
     * This is always called on the main thread.
     *
     * @param link The link that received the command.
     */
    void onCommandReceived(Link link, byte[] bytes);
}
