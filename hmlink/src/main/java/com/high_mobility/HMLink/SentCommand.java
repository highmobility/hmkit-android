package com.high_mobility.HMLink;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;

import java.util.Calendar;


/**
 * Created by ttiganik on 09/08/16.
 */
class SentCommand {
    boolean finished;
    Constants.ResponseCallback commandCallback;
    CountDownTimer timeoutTimer;
    Long commandStartTime;
    Handler dispatchThread;

    SentCommand(Constants.ResponseCallback callback, Handler dispatchThread) {
        this.dispatchThread = dispatchThread;
        this.commandCallback = callback;
        startTimeoutTimer();
        commandStartTime = Calendar.getInstance().getTimeInMillis();
    }

    void dispatchResult(final int errorCode) {
        if (timeoutTimer != null) timeoutTimer.cancel();
        finished = true;
        if (commandCallback == null) {
            Log.d(Broadcaster.TAG, "cannot dispatch the result: no callback reference");
            return;
        }

        dispatchThread.post(new Runnable() {
            @Override
            public void run() {
                commandCallback.response(errorCode);
            }
        });
    }

    void startTimeoutTimer() {
        timeoutTimer = new CountDownTimer((long)(Constants.commandTimeout * 1000), 15000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                dispatchResult(Link.TIME_OUT);
            }
        }.start();
    }
}