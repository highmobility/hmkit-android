package com.highmobility.hmkit.Error;

/**
 * Created by ttiganik on 6/22/17.
 *
 */

public class TelematicsError {
    public enum Type {
        /// Trying to send a command when one is already in progress
        COMMAND_IN_PROGRESS,
        /// Internal error
        INTERNAL_ERROR,
        /// HTTP ERROR occured. Status code is included in field code
        HTTP_ERROR,
        /// Invalid data from the server.
        INVALID_SERVER_RESPONSE,
        /// There is a problem connecting to the server
        NO_CONNECTION,
        /// The command timed out
        TIMEOUT,
        /// Server returned an error
        SERVER_ERROR
    }

    Type type;
    int code;
    String message;

    public TelematicsError(Type type, int errorCode, String message) {
        this.code = errorCode;
        this.type = type;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}