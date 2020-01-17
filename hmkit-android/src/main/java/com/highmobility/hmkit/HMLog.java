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

import android.os.Build;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class HMLog {
    private static final int MAX_TAG_LENGTH = 23;
    private static final int CALL_STACK_INDEX = 2;
    private static final Pattern ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$");
    private static final String LOG_PREFIX = "hmkit-";

    protected static Level level = Level.ALL;

    static void init() {

    }

    static void d(String message, Object... args) {
        if (level.getValue() >= Level.DEBUG.getValue()) {
            Timber.tag(getTag());
            try {
                Timber.d(String.format(message, args));
            } catch (Exception e) {
                Timber.d(message);
            }
        }
    }

    static void w(String message, Object... args) {
        if (level.getValue() >= Level.NONE.getValue()) {
            Timber.tag(getTag());
            try {
                Timber.w(String.format(message, args));
            } catch (Exception e) {
                Timber.w(message);
            }
        }
    }

    static void i(String message, Object... args) {
        if (level.getValue() >= Level.ALL.getValue()) {
            Timber.tag(getTag());
            try {
                Timber.i(String.format(message, args));
            } catch (Exception e) {
                Timber.i(message);
            }
        }
    }

    static void e(String message, Object... args) {
        if (level.getValue() >= Level.NONE.getValue()) {
            Timber.tag(getTag());
            try {
                Timber.e(String.format(message, args));
            } catch (Exception e) {
                Timber.e(message);
            }
        }
    }

    static String getTag() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        if (stackTrace.length <= CALL_STACK_INDEX) {
            return LOG_PREFIX;
        }

        return LOG_PREFIX + createStackElementTag(stackTrace[CALL_STACK_INDEX]);
    }

    static String createStackElementTag(StackTraceElement element) {
        String tag = element.getClassName();
        Matcher m = ANONYMOUS_CLASS.matcher(tag);
        if (m.find()) {
            tag = m.replaceAll("");
        }
        tag = tag.substring(tag.lastIndexOf('.') + 1);
        // Tag length limit was removed in API 24.
        if (tag.length() <= MAX_TAG_LENGTH || Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return tag;
        }
        return tag.substring(0, MAX_TAG_LENGTH);
    }

    /**
     * The possible logging levels.
     */
    public enum Level {
        NONE(0), DEBUG(1), ALL(2);

        private final Integer level;

        Level(int level) {
            this.level = level;
        }

        public int getValue() {
            return level;
        }
    }
}
