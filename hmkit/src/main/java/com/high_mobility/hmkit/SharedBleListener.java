package com.high_mobility.hmkit;

/**
 * Created by ttiganik on 22/06/16.
 */
public interface SharedBleListener {
    /**
     * Called when ble state has changed to available or not. Not available state can be called
     * multiple times.
     *
     * @param available true if bluetooth is available
     */
    void bluetoothChangedToAvailable(boolean available);
}
