package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 13/04/16.
 */
// TODO: move constants/ rename this class
public class LinkException extends Exception {
    /// Bluetooth is turned off
    public static final int BLUETOOTH_OFF = 1;
    /// A custom command has not yet received a response
    public static final int CUSTOM_COMMAND_IN_PROGRESS = 2;
    /// Framework encountered an internal error (commonly releated to invalid data received)
    public static final int INTERNAL_ERROR = 3;
    /// Bluetooth failed to act as expected
    public static final int BLUETOOTH_FAILURE = 4;
    /// The signature for the command was invalid
    public static final int INVALID_SIGNATURE = 5;
    /// The Certificates storage database is full
    public static final int STORAGE_FULL = 6;
    /// Command timed out
    public static final int TIME_OUT = 7;
    /// The Link is not connected
    public static final int NOT_CONNECTED = 8;
    /// The app is not authorised with the connected link to perform the action
    public static final int UNAUTHORIZED = 9;
    /// Bluetooth Low Energy is unavailable for this device
    public static final int UNSUPPORTED = 10;
}
