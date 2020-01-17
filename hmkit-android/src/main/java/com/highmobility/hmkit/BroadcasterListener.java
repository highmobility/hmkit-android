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

/**
 * The listener of the Broadcaster is used to observe the device's state changes and for
 * discovered/lost Links.
 */
public interface BroadcasterListener {
    /**
     * Callback for when the Broadcaster's state has changed.
     *
     * @param oldState The old state of the Broadcaster.
     */
    void onStateChanged(Broadcaster.State oldState);

    /**
     * Callback for when a new link has connected to the Broadcaster.
     *
     * @param link The link that successfully connected to the Broadcaster.
     */
    void onLinkReceived(ConnectedLink link);

    /**
     * Callback for when a link has disconnected from the broadcaster.
     *
     * @param link The link that disconnected from the broadcaster.
     */
    void onLinkLost(ConnectedLink link);
}
