package com.high_mobility.HMLink.Command;

import com.high_mobility.HMLink.LinkException;
import com.high_mobility.HMLink.Shared.ByteUtils;

/**
 * Created by ttiganik on 25/05/16.
 */
public class Command {
    // TODO: this should not be public
    public static int errorCode(byte[] bytes) {
        if (bytes != null && bytes.length > 1) {
            if (bytes[0] != 0x01 && bytes[0] != 0x02) return LinkException.INTERNAL_ERROR;

            if (bytes[0] == 0x01) return 0;

            return codeForByte(bytes[1]);
        }

        return LinkException.INTERNAL_ERROR;
    }

    interface Type {
        byte[] getIdentifier();
    }

    public enum Auto implements Type {
        GET_CAPABILITIES(new byte[] { 0x00, (byte)0x10 }),
        CAPABILITIES(new byte[] { 0x00, (byte)0x11 }),
        GET_VEHICLE_STATUS(new byte[] { 0x00, (byte)0x12 }),
        VEHICLE_STATUS(new byte[] { 0x00, (byte)0x13 });

        public static byte[] getCapabilities() {
            return GET_VEHICLE_STATUS.getIdentifier();
        }

        public static byte[] getVehicleStatus() {
            return GET_VEHICLE_STATUS.getIdentifier();
        }

        Auto(byte[] identifier) {
            this.identifier = identifier;
        }
        private byte[] identifier;
        public byte[] getIdentifier() {
            return identifier;
        }
    }

    public enum DigitalKey implements Type {
        GET_LOCK_STATE(new byte[] { 0x00, (byte)0x20 }),
        LOCK_STATE(new byte[] { 0x00, (byte)0x21 }),
        LOCK_UNLOCK(new byte[] { 0x00, (byte)0x22 });

        public enum LockStatus {
            UNLOCKED, LOCKED
        }

        public static byte[] getLockState() {
            return GET_LOCK_STATE.getIdentifier();
        }

        public static byte[] lockDoors(boolean lock) {
            byte[] bytes = new byte[3];
            bytes[0] = LOCK_UNLOCK.getIdentifier()[0];
            bytes[1] = LOCK_UNLOCK.getIdentifier()[1];
            bytes[2] = (byte)(lock ? 0x01 : 0x00);
            return bytes;
        }

        DigitalKey(byte[] identifier) {
            this.identifier = identifier;
        }
        private byte[] identifier;
        public byte[] getIdentifier() {
            return identifier;
        }
    }

    public enum Chassis implements Type {
        GET_WINDSHIELD_HEATING_STATE(new byte[] { 0x00, (byte)0x5A }),
        WINDSHIELD_HEATING_STATE(new byte[] { 0x00, (byte)0x5B }),
        SET_WINDSHIELD_HEATING(new byte[] { 0x00, (byte)0x5C }),
        GET_ROOFTOP_STATE(new byte[] { 0x00, (byte)0x5D }),
        ROOFTOP_STATE(new byte[] { 0x00, (byte)0x5E }),
        SET_ROOFTOP_TRANSPARENCY(new byte[] { 0x00, (byte)0x5F });

        // TODO:
        public static byte[] getWindshieldHeatingStateCommand() {
            return null;
        }

        public static byte[] setWindshieldHeatingCommand() {
            return null;
        }

        public static byte[] GetRooftopStateCommand() {
            return null;
        }

        public static byte[] SetRooftopTransparencyCommand() {
            return null;
        }

        Chassis(byte[] identifier) {
            this.identifier = identifier;
        }
        private byte[] identifier;
        public byte[] getIdentifier() {
            return identifier;
        }
    }

    public enum RemoteControl implements Type {
        GET_CONTROL_MODE(new byte[] { 0x00, (byte)0x41 }),
        CONTROL_MODE(new byte[] { 0x00, (byte)0x42 }),
        START_CONTROL_MODE(new byte[] { 0x00, (byte)0x43 }),
        STOP_CONTROL_MODE(new byte[] { 0x00, (byte)0x44 }),
        CONTROL_COMMAND(new byte[] { 0x00, (byte)0x45 });

        // TODO: verify

        public static byte[] controlModeAvailableCommand() {
            return GET_CONTROL_MODE.getIdentifier();
        }

        public static byte[] startControlModeCommand() {
            return START_CONTROL_MODE.getIdentifier();
        }

        public static byte[] stopControlModeCommand() {
            return STOP_CONTROL_MODE.getIdentifier();
        }

        public static byte[] controlCommandCommand(int speed, int angle) {
            // TODO: test this
            byte msb = (byte) ((angle & 0xFF00) >> 8);
            byte lsb = (byte) (angle & 0xFF);

            return ByteUtils.concatBytes(CONTROL_COMMAND.getIdentifier(), new byte[] {(byte)speed, msb, lsb});
        }

        RemoteControl(byte[] identifier) {
            this.identifier = identifier;
        }
        private byte[] identifier;
        public byte[] getIdentifier() {
            return identifier;
        }
    }

    public enum Health implements Type {
        HEART_RATE(new byte[] { 0x00, (byte)0x60 });

        public static byte[] heartRate(int heartRate) {
            return ByteUtils.concatBytes(HEART_RATE.getIdentifier(), (byte)heartRate);
        }

        Health(byte[] identifier) {
            this.identifier = identifier;
        }
        private byte[] identifier;
        public byte[] getIdentifier() {
            return identifier;
        }
    }


    public enum PointOfInterest implements Type {
        SET_DESTINATION(new byte[] { 0x00, (byte)0x70 });

        public static byte[] setDestination(String destination) {
            return ByteUtils.concatBytes(SET_DESTINATION.getIdentifier(), destination.getBytes());
        }

        PointOfInterest(byte[] identifier) {
            this.identifier = identifier;
        }
        private byte[] identifier;
        public byte[] getIdentifier() {
            return identifier;
        }
    }

    public enum ParcelDelivery implements Type {
        GET_DELIVERED_PARCELS(new byte[] { 0x00, (byte)0x60 }),
        DELIVERED_PARCELS(new byte[] { 0x00, (byte)0x61 });

        public static byte[] getDeliveredParcels() {
            return GET_DELIVERED_PARCELS.getIdentifier();
        }

        ParcelDelivery(byte[] identifier) {
            this.identifier = identifier;
        }
        private byte[] identifier;
        public byte[] getIdentifier() {
            return identifier;
        }
    }


    byte[] identifier;
    byte[] bytes;

    Command(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getIdentifier() {
        return identifier;
    }

    public byte[] getBytes() {
        return bytes;
    }


    static int codeForByte(byte errorByte) {
        switch (errorByte) {
            case 0x05:
                return LinkException.STORAGE_FULL;
            case 0x09:
                return LinkException.TIME_OUT;
            case 0x07:
                return LinkException.UNAUTHORIZED;
            case 0x06:
            case 0x08:
                return LinkException.UNAUTHORIZED;
            default:
                return LinkException.INTERNAL_ERROR;
        }
    }
}