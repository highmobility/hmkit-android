package com.high_mobility.HMLink.Shared;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;

import com.high_mobility.HMLink.Constants;
import com.high_mobility.HMLink.LinkException;

import java.util.Calendar;


/**
 * Created by ttiganik on 09/08/16.
 */
public class SentCommand {
    boolean finished;
    Constants.DataResponseCallback commandCallback;
    CountDownTimer timeoutTimer;
    Long commandStartTime;
    Handler dispatchThread;

    SentCommand(Constants.DataResponseCallback callback, Handler dispatchThread) {
        this.dispatchThread = dispatchThread;
        this.commandCallback = callback;
        startTimeoutTimer();
        commandStartTime = Calendar.getInstance().getTimeInMillis();
    }

    void dispatchResult(final byte[] response, final LinkException exception) {
        if (timeoutTimer != null) timeoutTimer.cancel();
        finished = true;
        if (commandCallback == null) {
            Log.d(LocalDevice.TAG, "cannot dispatch the result: no callback reference");
            return;
        }

        dispatchThread.post(new Runnable() {
            @Override
            public void run() {
                commandCallback.response(response, exception);
            }
        });
    }

    void startTimeoutTimer() {
        timeoutTimer = new CountDownTimer((long)(Constants.commandTimeout * 1000), 15000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                dispatchResult(null, new LinkException(LinkException.LinkExceptionCode.TIME_OUT));
            }
        }.start();
    }
}