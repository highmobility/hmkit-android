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

    static void init() {

    }

    static void d(String message, Object... args) {
        if (HMKit.loggingLevel.getValue() >= Level.DEBUG.getValue()) {
            Timber.tag(getTag());
            try {
                Timber.d(String.format(message, args));
            } catch (Exception e) {
                Timber.d(message);
            }
        }
    }

    static void w(String message, Object... args) {
        if (HMKit.loggingLevel.getValue() >= Level.NONE.getValue()) {
            Timber.tag(getTag());
            try {
                Timber.w(String.format(message, args));
            } catch (Exception e) {
                Timber.w(message);
            }
        }
    }

    static void i(String message, Object... args) {
        if (HMKit.loggingLevel.getValue() >= Level.ALL.getValue()) {
            Timber.tag(getTag());
            try {
                Timber.i(String.format(message, args));
            } catch (Exception e) {
                Timber.i(message);
            }
        }
    }

    static void e(String message, Object... args) {
        if (HMKit.loggingLevel.getValue() >= Level.NONE.getValue()) {
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
