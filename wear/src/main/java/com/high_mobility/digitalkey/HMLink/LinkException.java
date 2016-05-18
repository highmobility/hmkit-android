package com.high_mobility.digitalkey.HMLink;

/**
 * Created by ttiganik on 13/04/16.
 */
public class LinkException extends Exception {
    /// The values representing an error encountered in the *HMLink*
    public enum LinkExceptionCode {
        /// Bluetooth is turned off
        BLUETOOTH_OFF,
        /// Bluetooth is not authorised for this framework (app)
        BLUETOOTH_UNAUTHORISED,
        /// A custom command has not yet received a response
        CUSTOM_COMMAND_IN_PROGRESS,
        /// Framework encountered an internal error (commonly releated to invalid data received)
        INTERNAL_ERROR,
        /// The signature for the command was invalid
        INVALID_SIGNATURE,
        /// The Certificates storage database is full
        STORAGE_FULL,
        /// Command timed out
        TIME_OUT,
        /// The Link is not connected
        NOT_CONNECTED,
        /// The app is not authorised with the connected link to perform the action
        UNAUTHORISED,
        /// Bluetooth Low Energy is unavailable for this device
        UNSUPPORTED
    }

    public LinkExceptionCode code;

    public LinkException(LinkExceptionCode code) {
        this.code = code;
    }
}
