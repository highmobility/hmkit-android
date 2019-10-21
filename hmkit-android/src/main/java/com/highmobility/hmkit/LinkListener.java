package com.highmobility.hmkit;

import com.highmobility.value.Bytes;

public interface LinkListener {
    /**
     * Callback that is invoked when the Link's state changes.
     *
     * @param link     The link that had a state change.
     * @param oldState The old state of the link.
     */
    void onStateChanged(Link link, Link.State oldState);

    /**
     * Callback for when a command has been received by the Link.
     *
     * @param link  The link that received the command.
     * @param bytes The command bytes
     */
    void onCommandReceived(Link link, Bytes bytes);
}
