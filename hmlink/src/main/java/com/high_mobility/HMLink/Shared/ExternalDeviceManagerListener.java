package com.high_mobility.HMLink.Shared;

/**
 * Created by ttiganik on 01/06/16.
 */
public interface ExternalDeviceManagerListener {
    void onStateChanged(ExternalDeviceManager.State oldState);
    void onDeviceEnteredProximity(ExternalDevice device);
    void onDeviceExitedProximity(ExternalDevice device);
}
