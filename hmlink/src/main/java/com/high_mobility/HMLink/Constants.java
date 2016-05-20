package com.high_mobility.HMLink;

import java.util.UUID;

public class Constants {

    public final static LoggingLevel loggingLevel = LoggingLevel.All;
    public enum LoggingLevel {
        Debug(0), Info(1), All(2);

        private Integer level;

        LoggingLevel(int level) {
            this.level = level;
        }

        public int getValue() {
            return level;
        }
    }

    public static UUID SERVICE_UUID = UUID.fromString("713D0100-503E-4C75-BA94-3148F18D941E");

    public static UUID READ_CHAR_UUID = UUID.fromString("713D0102-503E-4C75-BA94-3148F18D941E");

    public static UUID WRITE_CHAR_UUID = UUID.fromString("713D0103-503E-4C75-BA94-3148F18D941E");

    public static UUID PING_CHAR_UUID = UUID.fromString("713D0104-503E-4C75-BA94-3148F18D941E");

    public static final float registerTimeout      = 6.0f;

    public interface ApprovedCallback {
        void approve();
        void decline();
    }

    public interface DataResponseCallback {
        void response(byte[] bytes, LinkException exception);
    }

    public interface ResponseCallback {
        void response(Error error);
    }
}
