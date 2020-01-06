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

public class TelematicsError {
    public enum Type {
        /// Trying to send a command when one is already in progress
        COMMAND_IN_PROGRESS,
        /// Access certificate with this serial does not exist
        INVALID_SERIAL,
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
        SERVER_ERROR,
        /// The command is too big. Max size is defined in Constants
        COMMAND_TOO_BIG
    }

    private final Type type;
    private final int code;
    private final String message;

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