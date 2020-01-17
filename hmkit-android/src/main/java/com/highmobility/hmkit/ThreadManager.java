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
