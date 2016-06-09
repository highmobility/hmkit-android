package com.high_mobility.HMLink.Commands;

/**
 * Created by ttiganik on 25/05/16.
 */
public class AutoCommand {
    static final byte ACK_BYTE = 0x01;
    static final byte ERROR_BYTE = 0x02;

    public enum Type {
        UNKNOWN((byte)0xFF),
        ACCESS((byte)0x17),
        GET_VEHICLE_STATUS((byte)0x18),
        LOCK_STATUS_CHANGED((byte)0x19);

        private byte command;

        Type(byte command) {
            this.command = command;
        }

        public byte getValue() {
            return command;
        }
    }

    Type type;

    byte[] bytes;

    // outgoing bytes generators
    public static byte[] lockDoorsBytes() {
        return new byte[] { Type.ACCESS.getValue(), 0x01 };
    }

    public static byte[] unlockDoorsBytes() {
        return new byte[] { Type.ACCESS.getValue(), 0x00 };
    }

    public static byte[] getVehicleStatusBytes() {
        return new byte[] { Type.GET_VEHICLE_STATUS.getValue() };
    }

    public AutoCommand(byte[] bytes) {
        this.bytes = bytes;
    }

    public Type getType() {
        return type;
    }

    public byte[] getBytes() {
        return bytes;
    }
}