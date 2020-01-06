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

interface ScannerListener {
    /***
     * Callback for when the Scanner's state has changed.
     * This is always called on the main thread.
     *
     * @param oldState The old state of the Scanner.
     */
    void onStateChanged(Scanner.State oldState);

    /***
     * Callback for when the scanner has connected and verified a Link.
     *
     * This is always called on the main thread.
     *
     * @param link The link the Scanner connected to.
     */
    void onDeviceEnteredProximity(ScannedLink link);

    /***
     * Callback for when a known link has been disconnected.
     *
     * This is always called on the main thread.
     *
     * @param link The link that disconnected.
     */
    void onDeviceExitedProximity(ScannedLink link);
}
