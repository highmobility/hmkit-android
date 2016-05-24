package com.high_mobility.HMLink;

import java.util.UUID;

public class Constants {
    public final static LoggingLevel loggingLevel = LoggingLevel.Debug;
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
