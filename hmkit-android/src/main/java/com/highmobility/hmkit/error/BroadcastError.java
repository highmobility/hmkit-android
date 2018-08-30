package com.highmobility.hmkit.error;

public class BroadcastError {
    public enum Type {
        NONE,
        /// Bluetooth is off
        BLUETOOTH_OFF,
        /// Bluetooth failed to act as expected
        BLUETOOTH_FAILURE,
        /// Bluetooth Low Energy is unavailable for this device
        UNSUPPORTED,
        /// SDK is not initialised
        UNINITIALIZED
    }

    Type errorType;
    int errorCode;
    String message;

    public BroadcastError(Type type, int errorCode, String message) {
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