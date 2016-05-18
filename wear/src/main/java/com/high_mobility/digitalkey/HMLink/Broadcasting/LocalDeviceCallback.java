package com.high_mobility.digitalkey.HMLink.Broadcasting;

/**
 * Created by ttiganik on 13/04/16.
 */
public interface LocalDeviceCallback {
    /***
     * Callback for when the state changed.
     * @param state The new state of the LocalDevice.
     * @param oldState The old state of the LocalDevice.
     */
    void localDeviceStateChanged(LocalDevice.State state, LocalDevice.State oldState);

    /***
     * Callback for when a new Link has been received.
     * @param link The new link.
     */
    void localDeviceDidReceiveLink(Link link);

    /***
     * Callback for when a Link has been lost.
     * @param link The now disconnected link.
     */
    void localDeviceDidLoseLink(Link link);
}
