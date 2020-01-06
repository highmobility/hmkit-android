/*
 * The MIT License
 *
 * Copyright (c) 2014- High-Mobility GmbH (https://high-mobility.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
