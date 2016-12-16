package com.high_mobility.HMLink.Command;

/**
 * Created by ttiganik on 15/12/2016.
 */

public enum VehicleFeature {
    FAILURE(new byte[] { 0x00, (byte)0x02 }),
    CAPABILITIES(new byte[] { 0x00, (byte)0x10 }),
    VEHICLE_STATUS(new byte[] { 0x00, (byte)0x11 }),
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

    public static VehicleFeature fromIdentifier(byte[] bytes) {
        return fromIdentifier(bytes[0], bytes[1]);
    }

    public static VehicleFeature fromIdentifier(byte firstByte, byte secondByte) {
        if (is(FAILURE, firstByte, secondByte)) {
            return FAILURE;
        }
        else if (is(CAPABILITIES, firstByte, secondByte)) {
            return CAPABILITIES;
        }
        else if (is(VEHICLE_STATUS, firstByte, secondByte)) {
            return VEHICLE_STATUS;
        }
        else if (is(DOOR_LOCKS, firstByte, secondByte)) {
            return DOOR_LOCKS;
        }
        else if (is(TRUNK_ACCESS, firstByte, secondByte)) {
            return TRUNK_ACCESS;
        }
        else if (is(WAKE_UP, firstByte, secondByte)) {
            return WAKE_UP;
        }
        else if (is(CHARGING, firstByte, secondByte)) {
            return CHARGING;
        }
        else if (is(CLIMATE, firstByte, secondByte)) {
            return CLIMATE;
        }
        else if (is(ROOFTOP, firstByte, secondByte)) {
            return ROOFTOP;
        }
        else if (is(HONK_FLASH, firstByte, secondByte)) {
            return HONK_FLASH;
        }
        else if (is(REMOTE_CONTROL, firstByte, secondByte)) {
            return REMOTE_CONTROL;
        }
        else if (is(VALET_MODE, firstByte, secondByte)) {
            return VALET_MODE;
        }
        else if (is(HEART_RATE, firstByte, secondByte)) {
            return HEART_RATE;
        }
        else if (is(VEHICLE_LOCATION, firstByte, secondByte)) {
            return VEHICLE_LOCATION;
        }
        else if (is(NAVI_DESTINATION, firstByte, secondByte)) {
            return NAVI_DESTINATION;
        }
        else if (is(DELIVERED_PARCELS, firstByte, secondByte)) {
            return DELIVERED_PARCELS;
        }
        else {
            return null;
        }
    }

    VehicleFeature(byte[] identifier) {
        this.identifier = identifier;
    }
    private byte[] identifier;
    public byte[] getIdentifier() {
        return identifier;
    }

    static boolean is (VehicleFeature feature, byte firstByte, byte secondByte) {
        return feature.getIdentifier()[0] == firstByte && feature.getIdentifier()[1] == secondByte;
    }
}