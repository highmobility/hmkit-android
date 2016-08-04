package com.high_mobility.HMLink.Shared;

import com.high_mobility.HMLink.Constants;

/**
 * Created by ttiganik on 13/04/16.
 */
public interface LinkListener {
    /**
     * Callback that is invoked when the Link's state changes.
     *
     * This is always called on the main thread when a change in the link's state occurs, also
     * when the same state was set again.
     *
     * @param link The link that had a state change.
     * @param oldState The old state of the link.
     */
    void onStateChanged(Link link, Link.State oldState);

    /**
     * Callback for when a command has been received by the Link.
     *
     * This is always called on the main thread when a command was received.
     *
     * @param link The link that received the command.
     * @param bytes The commands bytes that can correspond to the Auto API.
     * @return response to the command. can be null.
     */
    byte[] onCommandReceived(Link link, byte[] bytes);

    /**
     * Callback for when a link received a pairing request from another device.
     *
     * @param link The link that received the pairing request.
     * @param approvedCallback The function that must be called after the pairing has been approved
     *                         or decline
     */
    void onPairingRequested(Link link, Constants.ApprovedCallback approvedCallback);

    /**
     * Callback for when a pairing request has timed out.
     *
     * This is always called on the main thread. This occurs when the approvedCallback of a pairing
     * request has not been invoked in time, which denies the pairing.
     *
     * @param link The link that received the pairing request.
     */
    void onPairingRequestTimeout(Link link);
}
