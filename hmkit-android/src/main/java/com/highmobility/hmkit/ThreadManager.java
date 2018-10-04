package com.highmobility.hmkit;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

/**
 * Handles main and work thread.
 */
class ThreadManager {
    private final Handler mainHandler;
    private final Handler workHandler;

    public ThreadManager(Context context) {
        mainHandler = new Handler(context.getMainLooper());
        HandlerThread workThread = new HandlerThread("HMKit work thread");
        workThread.start();
        workHandler = new Handler(workThread.getLooper());
    }

    /**
     * Post to main thread.
     */
    void postToMain(Runnable runnable) {
        if (Looper.myLooper() != mainHandler.getLooper()) {
            mainHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * Post to work thread.
     */
    void postToWork(Runnable runnable) {
        workHandler.post(runnable);
    }

    /**
     * Post to work thread after a delay.
     */
    void postDelayed(Runnable runnable, long delay) {
        workHandler.postDelayed(runnable, delay);
    }

    void cancelDelayed(Runnable runnable) {
        workHandler.removeCallbacks(runnable);
    }
}
