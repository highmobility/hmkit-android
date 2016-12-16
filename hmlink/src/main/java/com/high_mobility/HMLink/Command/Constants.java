package com.high_mobility.HMLink.Command;

/**
 * Created by ttiganik on 16/12/2016.
 */

public class Constants {
    /**
     * The possible states of the car lock.
     */
    public enum LockState {
        LOCKED, UNLOCKED;

        public static LockState fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return UNLOCKED;
                case 0x01: return LOCKED;
            }

            throw new CommandParseException();
        }
    }

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
}
