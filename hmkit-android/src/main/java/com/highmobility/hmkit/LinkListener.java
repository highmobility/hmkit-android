package com.highmobility.hmkit;

/**
 * Created by ttiganik on 13/04/16.
 */
public interface LinkListener {
    /**
     * Callback that is invoked when the Link's state changes.
     *
     * The state can only go from Connected>Authenticated>Connected/Disconnected.
     * If the state is Disconnected the link is gone and should be released from memory.
     *
     * @param link The link that had a state change.
     * @param oldState The old state of the link.
     */
    void onStateChanged(Link link, Link.State oldState);

    /**
     * Callback for when a command has been received by the Link.
     *
     * @param link The link that received the command.
     * @param bytes The command bytes
     */
    void onCommandReceived(Link link, byte[] bytes);
}
