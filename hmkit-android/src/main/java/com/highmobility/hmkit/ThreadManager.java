package com.highmobility.hmkit;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

/**
 * Handles main and work thread.
 */
class ThreadManager {
    private Handler mainHandler, workHandler;
    private HandlerThread workThread;

    public ThreadManager(Context context) {
        mainHandler = new Handler(context.getMainLooper());
        workThread = new HandlerThread("HMKit work thread");
        workThread.start();
        workHandler = new Handler(workThread.getLooper());
    }

    void postToMainThread(Runnable runnable) {
        if (Looper.myLooper() != mainHandler.getLooper()) {
            mainHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    void postToWorkThread(Runnable runnable) {
        workHandler.post(runnable);
    }

    /**
     * Post to work thread after a delay.
     */
    void postDelayed(Runnable runnable, long interval) {
        workHandler.postDelayed(runnable, interval);
    }

    void cancelDelayed(Runnable runnable) {
        workHandler.removeCallbacks(runnable);
    }
}
