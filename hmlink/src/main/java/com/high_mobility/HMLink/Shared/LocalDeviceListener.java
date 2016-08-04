package com.high_mobility.HMLink.Shared;

/**
 * Created by ttiganik on 13/04/16.
 */
public interface LocalDeviceListener {
    /***
     * Callback for when the state has changed.
     * This is always called on the main thread when a change in the device's state occurs,
     * also when the same state was set again.
     *
     * @param state The new state of the LocalDevice.
     * @param oldState The old state of the LocalDevice.
     */
    void onStateChanged(LocalDevice.State state, LocalDevice.State oldState);

    /***
     * Callback for when a new link has been received by the LocalDevice.
     *
     * This is always called on the main thread when a new link has successfully connected to the
     * local device. If there was an old connection up, then this doesn't trigger.
     *
     * @param link The link that connected successfully to the LocalDevice.
     */
    void onLinkReceived(Link link);

    /***
     * Callback for when a known link has disconnected from the local device.
     *
     * This is always called on the main thread when a known link has disconnected, but only when
     * the link was first received by the local device (no connection alive before).
     *
     * @param link The link that disconnected from the local device.
     */
    void onLinkLost(Link link);
}
