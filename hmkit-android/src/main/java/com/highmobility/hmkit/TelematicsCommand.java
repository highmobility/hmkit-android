package com.highmobility.hmkit;

import com.highmobility.hmkit.error.TelematicsError;
import com.highmobility.value.Bytes;

class TelematicsCommand {
    Telematics.CommandCallback commandCallback;
    Callback callback;

    ThreadManager threadManager;
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