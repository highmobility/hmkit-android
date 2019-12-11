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