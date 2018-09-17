package com.highmobility.hmkit;

import android.os.Handler;

import com.highmobility.hmkit.error.TelematicsError;
import com.highmobility.value.Bytes;

class TelematicsCommand {
    Telematics.CommandCallback commandCallback;
    Callback callback;
    Handler dispatchThread;
    boolean finished;

    TelematicsCommand(Callback callback, Telematics.CommandCallback commandCallback, Handler
            dispatchThread) {
        finished = false;
        this.dispatchThread = dispatchThread;
        this.commandCallback = commandCallback;
        this.callback = callback;
    }

    void dispatchError(final TelematicsError.Type type, final int code, final String message) {
        dispatchThread.post(new Runnable() {
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
        dispatchThread.post(new Runnable() {
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

    interface Callback {
        void onCommandFinished(TelematicsCommand command);
    }
}