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
     * Callback to notify about authentication failure.
     *
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
