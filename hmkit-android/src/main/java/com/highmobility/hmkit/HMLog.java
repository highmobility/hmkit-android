package com.highmobility.hmkit;

import android.os.Build;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: 2019-04-04 deprecate this

//public class HMLog {
//    private static final int MAX_TAG_LENGTH = 23;
//    private static final int CALL_STACK_INDEX = 2;
//    private static final Pattern ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$");
//    private static final String LOG_PREFIX = "hmkit-";
//
//    static void init() {
//
//    }
//
//    public static void d(String message, Object... args) {
//        if (HMKit.loggingLevel.getValue() >= Level.DEBUG.getValue()) {
//            // don't call to this class again, this will mess up stack index and log tag. Always
//            // call straight to Timber.d in a method in this class.
//            try {
//                Log.d(getTag(), String.format(message, args));
//            }
//            catch (Exception e) {
//                Log.d(getTag(), message);
//            }
//        }
//    }
//
//    static void d(Level level, String message, Object... args) {
//        if (HMKit.loggingLevel.getValue() >= level.getValue()) {
//            Log.d(getTag(), String.format(message, args));
//        }
//    }
//
//    void i(Level level, String message, Object... args) {
//        if (HMKit.loggingLevel.getValue() >= level.getValue()) {
//            Log.i(getTag(), String.format(message, args));
//        }
//    }
//
//    static void i(String message, Object... args) {
//        Log.i(getTag(), String.format(message, args));
//    }
//
//    static void e(Throwable t, String message, Object... args) {
//        Log.e(getTag(), String.format(message, args), t);
//    }
//
//    public static void e(String message, Object... args) {
//        Log.e(getTag(), String.format(message, args));
//    }
//
//    static String getTag() {
//        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
//        if (stackTrace.length <= CALL_STACK_INDEX) {
//            return LOG_PREFIX;
//        }
//
//        return LOG_PREFIX + createStackElementTag(stackTrace[CALL_STACK_INDEX]);
//    }
//
//    static String createStackElementTag(StackTraceElement element) {
//        String tag = element.getClassName();
//        Matcher m = ANONYMOUS_CLASS.matcher(tag);
//        if (m.find()) {
//            tag = m.replaceAll("");
//        }
//        tag = tag.substring(tag.lastIndexOf('.') + 1);
//        // Tag length limit was removed in API 24.
//        if (tag.length() <= MAX_TAG_LENGTH || Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            return tag;
//        }
//        return tag.substring(0, MAX_TAG_LENGTH);
//    }
//
//    /**
//     * The possible logging levels.
//     */
//    public enum Level {
//        NONE(0), DEBUG(1), ALL(2);
//
//        private final Integer level;
//
//        Level(int level) {
//            this.level = level;
//        }
//
//        public int getValue() {
//            return level;
//        }
//    }
//}
