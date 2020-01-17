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
package com.highmobility.hmkit;

import com.highmobility.hmkit.error.LinkError;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import static com.highmobility.hmkit.HMLog.e;

class LinkCommand {
    boolean finished;
    Link.CommandCallback commandCallback;
    Timer timeoutTimer;

    Long commandStartTime;
    ThreadManager threadManager;

    LinkCommand(Link.CommandCallback callback, ThreadManager threadManager) {
        finished = false;
        this.threadManager = threadManager;
        this.commandCallback = callback;
        startTimeoutTimer();
        commandStartTime = Calendar.getInstance().getTimeInMillis();
    }

    void dispatchResponse(byte[] response) {
        cancelTimeoutTimer();
        finished = true;
        threadManager.postToMain(new Runnable() {
            @Override
            public void run() {
                if (commandCallbackExists()) {
                    commandCallback.onCommandSent();
                }
            }
        });
    }

    void dispatchError(final int error) {
        final LinkError.Type errorType = getErrorType(error);
        dispatchError(errorType, error, getErrorMessage(errorType));
    }

    private void dispatchError(final LinkError.Type error, final int code, final String message) {
        cancelTimeoutTimer();
        finished = true;

        threadManager.postToMain(new Runnable() {
            @Override
            public void run() {
                if (commandCallbackExists()) {
                    commandCallback.onCommandFailed(new LinkError(error, code, message));
                }
            }
        });
    }

    private boolean commandCallbackExists() {
        if (commandCallback == null) {
            e("cannot dispatch the result: no callback");
            return false;
        }

        return true;
    }

    void startTimeoutTimer() {
        timeoutTimer = new Timer();
        timeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                dispatchError(LinkError.Type.TIME_OUT, 0, "command timeout");
            }
        }, Link.commandTimeout);
    }

    void cancelTimeoutTimer() {
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
            timeoutTimer = null;
        }
    }

    static LinkError.Type getErrorType(int errorType) {
        switch (errorType) {
            case 5:
                return LinkError.Type.STORAGE_FULL;
            case 9:
                return LinkError.Type.TIME_OUT;
            case 7:
            case 6:
            case 8:
                return LinkError.Type.UNAUTHORIZED;
            default:
                return LinkError.Type.INTERNAL_ERROR;
        }
    }

    static String getErrorMessage(LinkError.Type type) {
        switch (type) {
            case STORAGE_FULL:
                return "Storage is full";
            case TIME_OUT:
                return "Time out";
            case UNAUTHORIZED:
                return "Unauthorised";
            default:
                return "Internal error";
        }
    }
}