package com.high_mobility.HMLink.Shared;

/**
 * Created by ttiganik on 01/06/16.
 */
public interface ExternalDeviceListener {
    void onStateChanged(ExternalDevice.State oldState);
    byte[] onCommandReceived(byte[] command);
}
