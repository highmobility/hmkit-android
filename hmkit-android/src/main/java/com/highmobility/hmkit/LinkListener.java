package com.highmobility.hmkit;

import com.highmobility.hmkit.error.AuthenticationError;
import com.highmobility.value.Bytes;

public interface LinkListener {
    /**
     * Callback to notify about Link state changes.
     *
     * @param link     The link whose state changed.
     * @param newState The new link state.
     * @param oldState The old link state.
     */
    void onStateChanged(Link link, Link.State newState, Link.State oldState);

    /**
     * @param link  The link.
     * @param error The authentication error.
     */
    void onAuthenticationFailed(Link link, AuthenticationError error);

    /**
     * Callback for when a command has been received by the Link.
     *
     * @param link  The link that received the command.
     * @param bytes The command bytes
     */
    void onCommandReceived(Link link, Bytes bytes);
}
