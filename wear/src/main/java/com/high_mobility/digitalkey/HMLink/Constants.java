package com.high_mobility.digitalkey.HMLink;

import java.util.UUID;

public class Constants {

    public static UUID SERVICE_UUID = UUID.fromString("713D0100-503E-4C75-BA94-3148F18D941E");

    public static UUID READ_CHAR_UUID = UUID.fromString("713D0102-503E-4C75-BA94-3148F18D941E");

    public static UUID WRITE_CHAR_UUID = UUID.fromString("713D0103-503E-4C75-BA94-3148F18D941E");

    public static UUID PING_CHAR_UUID = UUID.fromString("713D0104-503E-4C75-BA94-3148F18D941E");

    public static final float feedbackTimeout      = 30.0f;
    public static final float registerTimeout      = 6.0f;
    public static final float commandTimeout       = 10.0f;
    public static final float connectionTimeout    = 5.0f;

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

    /// The values representing a *MajesticLink* command
    public enum Command {
        GET_NONCE               ((byte)0x30),
        GET_DEVICE_CERTIFICATE  ((byte)0x31),
        REGISTER_CERT           ((byte)0x32),
        STORE_CERT              ((byte)0x33),
        GET_CERT                ((byte)0x34),
        AUTHENTICATE            ((byte)0x35),
        SEC_CONTAINER           ((byte)0x36),
        RESET                   ((byte)0x37),
        REVOKE                  ((byte)0x38),
        UNKNOWN                 ((byte)0x99);

        private final byte value;

        Command(byte b) {
            this.value = b;
        }

        public static Command decode(byte b) {
            final Command[] errors = values();
            for (int i = 0; i < errors.length; i++) {
                if (errors[i].value == b) {
                    return errors[i];
                }
            }

            return null;
        }
    }
}
