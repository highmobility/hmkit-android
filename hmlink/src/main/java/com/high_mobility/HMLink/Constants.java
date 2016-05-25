package com.high_mobility.HMLink;

public class Constants {
    public static LoggingLevel loggingLevel = LoggingLevel.Debug;
    public enum LoggingLevel {
        None(0), Debug(1), All(2);

        private Integer level;

        LoggingLevel(int level) {
            this.level = level;
        }

        public int getValue() {
            return level;
        }
    }

    public static final float registerTimeout      = 6.0f;

    public interface ApprovedCallback {
        void approve();
        void decline();
    }

    public interface DataResponseCallback {
        void response(byte[] bytes, LinkException exception);
    }
}
