package com.highmobility.hmkit;

public interface ConnectedLinkListener extends LinkListener {
    /**
     * This interface is used by the user to either approve or decline the authorization request.
     */
    interface AuthenticationRequestCallback {
        /**
         * Approve the authorization request
         */
        void approve();
        /**
         * Decline the authorization request
         */
        void decline();
    }

    /**
     * Callback for when a link received an authorization request.
     * User should approve or decline this request in the callback parameter, otherwise authorization
     * will be timed out.
     *
     * @param link The link that received the authorization request.
     * @param callback Object containing approve and decline functions.
     */
    void onAuthenticationRequest(ConnectedLink link, AuthenticationRequestCallback callback);

    /**
     * Callback for when the authorization request has timed out.
     *
     * This occurs when the callback of the authorization
     * request has not been invoked in time, which denies the authorization.
     *
     * @param link The link that received the authorization request.
     */
    void onAuthenticationRequestTimeout(ConnectedLink link);
}
