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

    private final Type errorType;
    private final int errorCode;
    private final String message;

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