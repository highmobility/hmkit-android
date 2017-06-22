package com.highmobility.hmkit;

/**
 * Created by ttiganik on 18/08/16.
 */
public interface ConnectedLinkListener extends LinkListener {
    /**
     * Callback for when a link received a pairing request from another broadcaster.
     * User should approve or decline this request in the callback parameter, otherwise authorization
     * will be timed out.
     *
     * @param link The link that received the pairing request.
     * @param callback Object containing approve and decline functions.
     */
    void onAuthorizationRequested(ConnectedLink link, ConnectedLink.AuthorizationCallback callback);

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
