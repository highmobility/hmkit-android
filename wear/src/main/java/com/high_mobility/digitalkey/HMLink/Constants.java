package com.high_mobility.digitalkey.HMLink;

import java.util.UUID;

public class Constants {

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
