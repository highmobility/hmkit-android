package com.highmobility.hmkit.error;

public class LinkError {
    public enum Type {
        NONE,
        /// Bluetooth is off
        BLUETOOTH_OFF,
        /// A custom command has not yet received a response
        COMMAND_IN_PROGRESS,
        /// Framework encountered an internal error (commonly related to invalid data received)
        INTERNAL_ERROR,
        /// Bluetooth failed to act as expected
        BLUETOOTH_FAILURE,
        /// The signature for the command was invalid
        INVALID_SIGNATURE,
        /// The Certificates storage database is full
        STORAGE_FULL,
        /// Command timed out
        TIME_OUT,
        /// The Link is not connected
        NOT_CONNECTED,
        /// The app is not authorised with the connected link to perform the action
        UNAUTHORIZED,
        /// Bluetooth Low Energy is unavailable for this device
        UNSUPPORTED,
        /// The command is too big. Max size is defined in Constants
        COMMAND_TOO_BIG
    }

    Type errorType;
    int errorCode;
    String message;

    public LinkError(Type type, int errorCode, String message) {
        this.errorCode = errorCode;
        this.errorType = type;
        this.message = message;
    }

    public Type getType() {
        return errorType;
    }

    public int getCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}