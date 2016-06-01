package com.high_mobility.HMLink.Scanning;

/**
 * Created by ttiganik on 01/06/16.
 */
public interface ExternalDeviceListener {
    void onStateChanged(ExternalDevice.State oldState);
    void onRSSIChanged(int RSSI);
    byte[] onCommandReceived(byte[] command);
}
