package com.highmobility.hmkit;

public interface SharedBleListener {
    /**
     * Called when ble state has changed to available or not. Not available state can be called
     * multiple times.
     *
     * @param available true if bluetooth is available
     */
    void bluetoothChangedToAvailable(boolean available);
}
