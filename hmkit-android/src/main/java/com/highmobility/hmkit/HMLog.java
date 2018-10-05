package com.highmobility.hmkit;

import android.os.Build;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class HMLog {
    private static final int MAX_TAG_LENGTH = 23;
    private static final int CALL_STACK_INDEX = 2;
    private static final Pattern ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$");
    private static final String LOG_PREFIX = "hmkit-";

    static void init() {
        Timber.plant(new Timber.DebugTree());
    }

    static void d(String message, Object... args) {
        if (HMKit.loggingLevel.getValue() >= Level.DEBUG.getValue()) {
            // don't call to this class again(HMLog.d(Level.DEBUG, message, args)), will mess up
            // stack index and log tag.
            Timber.tag(getTag());
            Timber.d(message, args);
        }
    }

    static void d(Level level, String message, Object... args) {
        if (HMKit.loggingLevel.getValue() >= level.getValue()) {
            Timber.tag(getTag());
            Timber.d(message, args);
        }
    }

    void i(Level level, String message, Object... args) {
        if (HMKit.loggingLevel.getValue() >= level.getValue()) {
            Timber.tag(getTag());
            Timber.i(message, args);
        }
    }

    static void i(String message, Object... args) {
        Timber.tag(getTag());
        Timber.i(message, args);
    }

    static void e(Throwable t, String message, Object... args) {
        Timber.tag(getTag());
        Timber.e(t, message, args);
    }

    static void e(String message, Object... args) {
        Timber.tag(getTag());
        Timber.e(message, args);
    }

    static final String getTag() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        if (stackTrace.length <= CALL_STACK_INDEX) {
            return LOG_PREFIX;
        }

        return LOG_PREFIX + createStackElementTag(stackTrace[CALL_STACK_INDEX]);
    }

    // Timber does not allow overriding of DebugTree's stack index. https://github
    // .com/JakeWharton/timber/pull/314
    @Nullable static String createStackElementTag(@NotNull StackTraceElement element) {
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
