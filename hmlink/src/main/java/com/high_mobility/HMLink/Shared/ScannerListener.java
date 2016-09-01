package com.high_mobility.HMLink.Shared;

/**
 * Created by ttiganik on 01/06/16.
 */
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
