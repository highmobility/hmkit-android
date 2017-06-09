package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 18/08/16.
 */
public interface ConnectedLinkListener extends LinkListener {
    /**
     * Callback for when a link received a pairing request from another broadcaster.
     *
     * @param link The link that received the pairing request.
     * @param callback The function that must be called after the pairing has been approved
     *                         or decline
     */
    void onAuthorizationRequested(ConnectedLink link, Constants.ApprovedCallback callback);

    /**
     * Callback for when a pairing request has timed out.
     *
     * This is always called on the main thread. This occurs when the approvedCallback of a pairing
     * request has not been invoked in time, which denies the pairing.
     *
     * @param link The link that received the pairing request.
     */
    void onAuthorizationTimeout(ConnectedLink link);
}
