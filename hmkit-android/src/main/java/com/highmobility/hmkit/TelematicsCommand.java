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

import com.highmobility.hmkit.error.TelematicsError;
import com.highmobility.value.Bytes;

class TelematicsCommand {
    Telematics.CommandCallback commandCallback;
    final Callback callback;
    final ThreadManager threadManager;

    boolean finished;

    TelematicsCommand(Callback callback, Telematics.CommandCallback commandCallback, ThreadManager
            threadManager) {
        finished = false;
        this.threadManager = threadManager;
        this.commandCallback = commandCallback;
        this.callback = callback;
    }

    void dispatchError(final TelematicsError.Type type, final int code, final String message) {
        threadManager.postToMain(new Runnable() {
            @Override
            public void run() {
                finished = true;
                callback.onCommandFinished(TelematicsCommand.this);
                if (commandCallback != null) {
                    final TelematicsError error = new TelematicsError(type, code, message);
                    commandCallback.onCommandFailed(error);
                    commandCallback = null;
                }
            }
        });
    }

    public void dispatchResult(final Bytes response) {
        threadManager.postToMain(new Runnable() {
            @Override
            public void run() {
                finished = true;
                callback.onCommandFinished(TelematicsCommand.this);
                if (commandCallback != null) {
                    commandCallback.onCommandResponse(response);
                    commandCallback = null;
                }
            }
        });
    }

    static abstract class Callback {
        abstract void onCommandFinished(TelematicsCommand command);
    }
}