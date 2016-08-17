package com.high_mobility.HMLink.AutoCommand;

/**
 * Created by ttiganik on 25/05/16.
 */
public class AutoCommand {
    static final byte ACK_BYTE = 0x01;
    static final byte ERROR_BYTE = 0x02;

    public enum Type {
        UNKNOWN((byte)0xFF),
        // Remote Control
        CONTROL_MODE_AVAILABLE((byte)0x01),
        CONTROL_MODE_CHANGED((byte)0x02),
        START_CONTROL_MODE((byte)0x03),
        STOP_CONTROL_MODE((byte)0x04),
        CONTROL_COMMAND((byte)0x05),
        // Digital Key
        ACCESS((byte)0x17),
        GET_VEHICLE_STATUS((byte)0x18),
        LOCK_STATUS_CHANGED((byte)0x19);

        private byte value;

        Type(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    Type type;

    byte[] bytes;

    // Outgoing bytes

    //
    // Remote Control

    public static byte[] controlModeAvailableBytes() {
        return new byte[] { Type.CONTROL_MODE_AVAILABLE.getValue()};
    }

    public static byte[] startControlModeBytes() {
        return new byte[] { Type.START_CONTROL_MODE.getValue() };
    }

    public static byte[] stopControlMode() {
        return new byte[] { Type.STOP_CONTROL_MODE.getValue() };
    }

    public static byte[] controlCommandBytes(int speed, int angle) {
        // TODO: test this
        byte msb = (byte) ((angle & 0xFF00) >> 8);
        byte lsb = (byte) (angle & 0xFF);
        return new byte[] { Type.CONTROL_COMMAND.getValue(), (byte)speed, msb, lsb };
    }

    //
    // Digital Key

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