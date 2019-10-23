package com.highmobility.hmkit;

import com.highmobility.hmkit.error.AuthenticationError;
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
     * @param link  The link.
     * @param error The authentication error.
     */
    // TODO: 21/10/2019 call this after onErrorCommand. (or any time state is set to not_authenticated)
    //  Also in HMApiCallbackEnteredProximity if the state is not authenticated. But only if error
    //  command was not called before(then this was called)
    void onAuthenticationFailed(Link link, AuthenticationError error);

    /**
     * Callback for when a command has been received by the Link.
     *
     * @param link  The link that received the command.
     * @param bytes The command bytes
     */
    void onCommandReceived(Link link, Bytes bytes);
}
