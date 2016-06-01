package com.high_mobility.HMLink.Scanning;

/**
 * Created by ttiganik on 01/06/16.
 */
public interface ExternalDeviceManagerListener {
    void onStateChanged(ExternalDeviceManager.State oldState);
    void onDeviceEnteredProximity(ExternalDevice device);
    void onDeviceExitedroximity(ExternalDevice device);
}
