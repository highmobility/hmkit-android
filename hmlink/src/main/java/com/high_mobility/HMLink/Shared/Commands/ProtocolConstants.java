package com.high_mobility.HMLink.Shared.Commands;

import com.high_mobility.HMLink.LinkException;

/**
 * Created by ttiganik on 18/04/16.
 */
class ProtocolConstants {
    static final byte PROTOCOL_START_BYTE = 0x00;
    static final byte PROTOCOL_ESCAPE_BYTE = (byte) 0xFE;
    static final byte PROTOCOL_END_BYTE = (byte) 0xFF;

    static final byte PROTOCOL_ACK_BYTE = (byte) 0x01;
    static final byte PROTOCOL_ERROR_BYTE = (byte) 0x02;

    enum ProtocolError {
        INTERNAL_ERROR((byte) 0x01),
        COMMAND_EMPTY((byte) 0x02),
        COMMAND_UNKNOWN((byte) 0x03),
        INVALID_DATA((byte) 0x04),
        STORAGE_FULL((byte) 0x05),
        INVALID_SIGNATURE((byte) 0x06),
        UNAUTHORISED((byte) 0x07),
        INVALID_HMAC((byte) 0x08),
        TIME_OUT((byte) 0x09);

        private final byte value;

        ProtocolError(byte b) {
            this.value = b;
        }

        public static ProtocolError decode(byte b) {
            final ProtocolError[] errors = values();
            for (int i = 0; i < errors.length; i++) {
                if (errors[i].value == b) {
                    return errors[i];
                }
            }

            return null;
        }

        public LinkException getLinkException() {
            if (value == STORAGE_FULL.value) {
                return new LinkException(LinkException.LinkExceptionCode.STORAGE_FULL);
            } else if (value == TIME_OUT.value) {
                return new LinkException(LinkException.LinkExceptionCode.TIME_OUT);
            } else if (value == UNAUTHORISED.value) {
                return new LinkException(LinkException.LinkExceptionCode.UNAUTHORISED);
            } else if (value == INVALID_SIGNATURE.value
                    || value == INVALID_HMAC.value) {
                return new LinkException(LinkException.LinkExceptionCode.INVALID_SIGNATURE);
            } else {
                return new LinkException(LinkException.LinkExceptionCode.INTERNAL_ERROR);
            }
        }
    }
}