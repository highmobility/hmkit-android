package com.high_mobility.HMLink.Commands;

/**
 * Created by ttiganik on 25/05/16.
 */
public class AutoCommand {
    static final byte ACK_BYTE = 0x01;
    static final byte ERROR_BYTE = 0x02;

    public enum Type {
        UNKNOWN((byte)0xFF),
        CONTROL_MODE_AVAILABLE((byte)0x01),
        CONTROL_MODE_CHANGED((byte)0x02),
        START_CONTROL_MODE((byte)0x03),
        STOP_CONTROL_MODE((byte)0x04),
        CONTROL_COMMAND((byte)0x05),
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

    public enum ControlMode {
        UNAVAILABLE((byte)0x01),
        AVAILABLE((byte)0x02),
        STARTED((byte)0x03),
        FAILED_TO_START((byte)0x04),
        ABORTED((byte)0x05),
        ENDED((byte)0x06);

        private byte type;

        ControlMode(byte command) {
            this.type = command;
        }

        public byte getValue() {
            return type;
        }
    }


    Type type;

    byte[] bytes;

    // outgoing bytes generators
    public static byte[] controlModeAvailableBytes() {
        return new byte[] { Type.CONTROL_MODE_AVAILABLE.getValue()};
    }

    public static byte[] controlModeChangedBytes(ControlMode controlMode, int angle) {
        // TODO: test this
        byte msb = (byte) ((angle & 0xFF00) >> 8);
        byte lsb = (byte) (angle & 0xFF);
        return new byte[] { Type.CONTROL_MODE_CHANGED.getValue(), controlMode.getValue(), msb, lsb };
    }

    public static byte[] startControlModeBytes() {
        return new byte[] { Type.START_CONTROL_MODE.getValue() };
    }

    public static byte[] stopControlMode() {
        return new byte[] { Type.STOP_CONTROL_MODE.getValue() };
    }

    public static byte[] controlCommandBytes(int speed, int angle)
    {
        // TODO: test this
        byte msb = (byte) ((angle & 0xFF00) >> 8);
        byte lsb = (byte) (angle & 0xFF);
        return new byte[] { Type.CONTROL_COMMAND.getValue(), (byte)speed, msb, lsb };
    }

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