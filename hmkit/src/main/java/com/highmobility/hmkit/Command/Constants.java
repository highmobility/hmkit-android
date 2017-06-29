package com.highmobility.hmkit.Command;

/**
 * Created by ttiganik on 16/12/2016.
 */

public class Constants {
    public enum ScreenLocation {
        FRONT, REAR;

        byte getByte() {
            switch (this) {
                case FRONT:
                    return 0x00;
                case REAR:
                    return 0x01;
                default: return 0x00;
            }
        }
    }

    public enum FrontExteriorLightState {
        INACTIVE, ACTIVE, ACTIVE_WITH_FULL_BEAM;

        byte byteValue() {
            if (this == INACTIVE) return 0x00;
            else if (this == ACTIVE) return 0x01;
            else if (this == ACTIVE_WITH_FULL_BEAM) return 0x02;
            return 0x00;
        }
    }

    public enum WasherFluidLevel { LOW, FULL }

    /**
     * The possible trunk lock states
     */
    public enum TrunkLockState {
        LOCKED, UNLOCKED, UNSUPPORTED;

        public static TrunkLockState fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return UNLOCKED;
                case 0x01: return LOCKED;
                case (byte)0xFF: return UNSUPPORTED;
            }


            throw new CommandParseException();
        }
    }

    /**
     * The possible trunk positions
     */
    public enum TrunkPosition {
        OPEN, CLOSED, UNSUPPORTED;

        public static TrunkPosition fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return CLOSED;
                case 0x01: return OPEN;
                case (byte)0xFF: return UNSUPPORTED;
            }

            throw new CommandParseException();
        }
    }

    /**
     * The possible charge states
     */
    public enum ChargingState {
        DISCONNECTED, PLUGGED_IN, CHARGING, CHARGING_COMPLETE;

        public static ChargingState fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return DISCONNECTED;
                case 0x01: return PLUGGED_IN;
                case 0x02: return CHARGING;
                case 0x03: return CHARGING_COMPLETE;
            }

            throw new CommandParseException();
        }
    }

    /**
     * The possible charge port states
     */
    public enum ChargePortState {
        CLOSED, OPEN, UNAVAILABLE;

        public static ChargePortState fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return CLOSED;
                case 0x01: return OPEN;
                case (byte)0xFF: return UNAVAILABLE;
            }

            throw new CommandParseException();
        }
    }
}
