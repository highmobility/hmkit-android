package com.high_mobility.HMLink.Shared;

/**
 * Created by ttiganik on 01/06/16.
 */
public interface ScannerListener {
    void onStateChanged(Scanner.State oldState);
    void onDeviceEnteredProximity(ScannedLink device);
    void onDeviceExitedProximity(ScannedLink device);
}
