package com.high_mobility.digitalkey.HMLink.Broadcasting;

/**
 * Created by ttiganik on 13/04/16.
 */
public interface LocalDeviceCallback {
    /// Callback for when the state changed
    ///
    /// - parameter state: The new state of the LocalDevice
    void localDeviceStateChanged(LocalDevice.State state, LocalDevice.State oldState);

    /// Callback for when a new Link has been received.
    ///
    /// - parameter link: the new link
    void localDeviceDidReceiveLink(Link link);

    /// Callback for when a Link has been lost.
    ///
    /// - parameter link: the now disconnected link
    void localDeviceDidLoseLink(Link link);

}
