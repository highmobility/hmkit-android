package com.high_mobility.HMLink.Broadcasting;

/**
 * Created by ttiganik on 13/04/16.
 */
public interface LocalDeviceListener {
    /***
     * Callback for when the state has changed.
     * @param state The new state of the LocalDevice.
     * @param oldState The old state of the LocalDevice.
     */
    void onStateChanged(LocalDevice.State state, LocalDevice.State oldState);

    /***
     * Callback for when a new Link has been received.
     * @param link The new link.
     */
    void onLinkReceived(Link link);

    /***
     * Callback for when a Link has been lost.
     * @param link The now disconnected link.
     */
    void onLinkLost(Link link);
}
