package com.highmobility.hmkit.error;

public class RevokeError {
    public enum Type {
        NONE,
        /// Bluetooth is off
        BLUETOOTH_OFF,
        /// Framework encountered an internal error
        INTERNAL_ERROR,
        /// Bluetooth failed to act as expected
        BLUETOOTH_FAILURE,
        /// Revoke failed on the device side
        FAILED,
        /// The signature was invalid
        INVALID_SIGNATURE,
        /// Command timed out
        TIME_OUT,
        /// The Link is not connected
        NOT_CONNECTED,
        /// The app is not authorised with the connected link to perform the action
        UNAUTHORIZED,
        /// Custom command is in progress. Wait for this to complete.
        COMMAND_IN_PROGRESS
    }

    Type errorType;
    int errorCode;
    String message;

    public RevokeError(Type type, int errorCode, String message) {
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
