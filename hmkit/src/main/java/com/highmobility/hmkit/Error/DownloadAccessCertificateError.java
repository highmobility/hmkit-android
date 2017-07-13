package com.highmobility.hmkit.Error;

/**
 * Created by root on 6/22/17.
 */

public class DownloadAccessCertificateError {
    public enum Type {
        /// Internal error
        INTERNAL_ERROR,
        /// HTTP ERROR occured. Result code is included in field code
        HTTP_ERROR,
        /// Invalid data from the server.
        INVALID_SERVER_RESPONSE,
        /// There is a problem connecting to the server
        CONNECTION_ERROR,
        /// Server returned an error
        SERVER_ERROR
    }

    DownloadAccessCertificateError.Type type;
    int code;
    String message;

    public DownloadAccessCertificateError(DownloadAccessCertificateError.Type type, int errorCode, String message) {
        this.code = errorCode;
        this.type = type;
        this.message = message;
    }

    public DownloadAccessCertificateError.Type getType() {
        return type;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
