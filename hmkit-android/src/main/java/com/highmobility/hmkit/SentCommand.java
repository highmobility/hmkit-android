package com.highmobility.hmkit;

import android.os.Handler;
import android.util.Log;

import com.highmobility.hmkit.error.LinkError;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import static com.highmobility.hmkit.Broadcaster.TAG;

class SentCommand {
    boolean finished;
    Link.CommandCallback commandCallback;
    Timer timeoutTimer;

    Long commandStartTime;
    Handler dispatchThread;

    SentCommand(Link.CommandCallback callback, Handler dispatchThread) {
        finished = false;
        this.dispatchThread = dispatchThread;
        this.commandCallback = callback;
        startTimeoutTimer();
        commandStartTime = Calendar.getInstance().getTimeInMillis();
    }

    void dispatchResult(byte[] response) {
        final LinkError.Type errorCode = getErrorCode(response);
        if (errorCode == LinkError.Type.NONE) {
            cancelTimeoutTimer();
            finished = true;
            dispatchThread.post(new Runnable() {
                @Override
                public void run() {
                    commandCallback.onCommandSent();
                }
            });
        } else {
            dispatchError(errorCode, 0, "");
        }
    }

    void dispatchError(final LinkError.Type type, final int errorCode, final String message) {
        cancelTimeoutTimer();
        finished = true;

        if (commandCallback == null) {
            Log.d(TAG, "cannot dispatch the result: no callback reference");
            return;
        }

        dispatchThread.post(new Runnable() {
            @Override
            public void run() {
                commandCallback.onCommandFailed(new LinkError(type, errorCode, message));
            }
        });
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

    static LinkError.Type getErrorCode(byte[] bytes) {
        if (bytes == null || bytes.length < 2) return LinkError.Type.NONE;
        if (bytes[0] != 0x02) return LinkError.Type.NONE;
        return errorCodeForByte(bytes[1]);
    }

    static LinkError.Type errorCodeForByte(byte errorByte) {
        switch (errorByte) {
            case 0x05:
                return LinkError.Type.STORAGE_FULL;
            case 0x09:
                return LinkError.Type.TIME_OUT;
            case 0x07:
                return LinkError.Type.UNAUTHORIZED;
            case 0x06:
            case 0x08:
                return LinkError.Type.UNAUTHORIZED;
            default:
                return LinkError.Type.INTERNAL_ERROR;
        }
    }
}