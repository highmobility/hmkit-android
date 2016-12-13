package com.high_mobility.HMLink.Command.Incoming;

import com.high_mobility.HMLink.Command.CommandParseException;

/**
 * Created by ttiganik on 13/09/16.
 *
 * This command is sent when a Get Vehicle Status command is received by the car.
 *
 */
public class VehicleStatus extends IncomingCommand {
    public enum State {
        DOOR_LOCKS(new byte[] { 0x00, (byte)0x20 }),
        TRUNK_ACCESS(new byte[] { 0x00, (byte)0x21 }),
        WAKE_UP(new byte[] { 0x00, (byte)0x22 }),
        CHARGING(new byte[] { 0x00, (byte)0x23 }),
        CLIMATE(new byte[] { 0x00, (byte)0x24 }),
        ROOFTOP(new byte[] { 0x00, (byte)0x25 }),
        HONK_FLASH(new byte[] { 0x00, (byte)0x26 }),
        REMOTE_CONTROL(new byte[] { 0x00, (byte)0x27 }),
        VALET_MODE(new byte[] { 0x00, (byte)0x28 }),
        HEART_RATE(new byte[] { 0x00, (byte)0x29 }),
        VEHICLE_LOCATION(new byte[] { 0x00, (byte)0x30 }),
        NAVI_DESTINATION(new byte[] { 0x00, (byte)0x31 }),
        DELIVERED_PARCELS(new byte[] { 0x00, (byte)0x32 });

        public static State fromIdentifier(byte[] bytes) {
            return fromIdentifier(bytes[0], bytes[1]);
        }

        public static State fromIdentifier(byte firstByte, byte secondByte) {
            if (firstByte == 0x00 && secondByte == 0x20) {
                return DOOR_LOCKS;
            }
            else if (firstByte == 0x00 && secondByte == 0x21) {
                return TRUNK_ACCESS;
            }
            else if (firstByte == 0x00 && secondByte == 0x22) {
                return WAKE_UP;
            }
            else if (firstByte == 0x00 && secondByte == 0x23) {
                return CHARGING;
            }
            else if (firstByte == 0x00 && secondByte == 0x24) {
                return CLIMATE;
            }
            else if (firstByte == 0x00 && secondByte == 0x25) {
                return ROOFTOP;
            }
            else if (firstByte == 0x00 && secondByte == 0x26) {
                return HONK_FLASH;
            }
            else if (firstByte == 0x00 && secondByte == 0x27) {
                return REMOTE_CONTROL;
            }
            else if (firstByte == 0x00 && secondByte == 0x28) {
                return VALET_MODE;
            }
            else if (firstByte == 0x00 && secondByte == 0x29) {
                return HEART_RATE;
            }
            else if (firstByte == 0x00 && secondByte == 0x30) {
                return VEHICLE_LOCATION;
            }
            else if (firstByte == 0x00 && secondByte == 0x31) {
                return NAVI_DESTINATION;
            }
            else if (firstByte == 0x00 && secondByte == 0x32) {
                return DELIVERED_PARCELS;
            }
            else {
                return null;
            }
        }

        State(byte[] identifier) {
            this.identifier = identifier;
        }
        private byte[] identifier;
        public byte[] getIdentifier() {
            return identifier;
        }
    }

    public VehicleStatus(byte[] bytes) throws CommandParseException {
        super(bytes);

    }
}
