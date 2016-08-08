package com.high_mobility.HMLink.Shared;

import com.high_mobility.HMLink.DeviceCertificate;

import java.util.UUID;

/**
 * Created by ttiganik on 13/04/16.
 */
public class Device {
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

    public static LoggingLevel loggingLevel = LoggingLevel.Debug;

    protected static UUID SERVICE_UUID = UUID.fromString("713D0100-503E-4C75-BA94-3148F18D941E");
    protected static UUID READ_CHAR_UUID = UUID.fromString("713D0102-503E-4C75-BA94-3148F18D941E");
    protected static UUID WRITE_CHAR_UUID = UUID.fromString("713D0103-503E-4C75-BA94-3148F18D941E");
    protected static UUID PING_CHAR_UUID = UUID.fromString("713D0104-503E-4C75-BA94-3148F18D941E");

    DeviceCertificate certificate;

    public String getName() {
        return null;
    }

    public DeviceCertificate getCertificate() {
        return certificate;
    }
}
