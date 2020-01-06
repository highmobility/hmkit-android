/*
 * The MIT License
 *
 * Copyright (c) 2014- High-Mobility GmbH (https://high-mobility.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.highmobility.hmkit.error;

/**
 * Created by root on 6/22/17.
 */

public class DownloadAccessCertificateError {
    public enum Type {
        /// Internal error
        INTERNAL_ERROR,
        /// HTTP ERROR occurred. Result code is included in field code
        HTTP_ERROR,
        /// Invalid data from the server.
        INVALID_SERVER_RESPONSE,
        /// There is a problem connecting to the server
        NO_CONNECTION,
        /// Server returned an error
        SERVER_ERROR
    }

    private final DownloadAccessCertificateError.Type type;
    final int code;
    final String message;

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
