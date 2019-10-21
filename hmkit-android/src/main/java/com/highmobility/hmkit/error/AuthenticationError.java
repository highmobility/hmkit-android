package com.highmobility.hmkit.error;

public class AuthenticationError {
    public enum Type {
        NONE,
        INTERNAL_ERROR,
    }

    private final Type errorType;
    private final int errorCode;
    private final String message;

    public AuthenticationError(Type type, int errorCode, String message) {
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
