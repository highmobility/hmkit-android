package com.high_mobility.HMLink;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by ttiganik on 14/10/2016.
 */
public class CapabilityType {
    public enum Type {
        DIGITAL_KEY, CHASSIS, PARKING, HEALTH, POI, PARCEL_DELIVERY;

        static Type capabilityType(byte value) throws CommandParseException {
            switch (value) {
                case 0x10:
                    return DIGITAL_KEY;
                case 0x11:
                    return CHASSIS;
                case 0x12:
                    return PARKING;
                case 0x13:
                    return HEALTH;
                case 0x14:
                    return POI;
                case 0x15:
                    return PARCEL_DELIVERY;
                default:
                    throw new CommandParseException();
            }
        }
    }

    byte[] bytes;

    public Type getType() {
        return type;
    }
    Type type;

    CapabilityType(byte[] bytes) throws CommandParseException {
        this.bytes = bytes;
        this.type = Type.capabilityType(bytes[2]);
    }

    CapabilityType(Type type) throws CommandParseException {
        this.type = type;
    }

    static CapabilityType capabilityFromIncomingCapability(byte[] bytes) throws CommandParseException {
        if (bytes.length < 4) throw new CommandParseException();
        Type type = Type.capabilityType(bytes[2]);

        switch (type) {
            case DIGITAL_KEY: return new DigitalKeyCapabilities(Arrays.copyOfRange(bytes, 3, 3 + 2));
            case CHASSIS: return new ChassisCapabilities(Arrays.copyOfRange(bytes, 3, 3 + 2));
            case PARKING: return new ParkingCapabilities(bytes[3]);
            case HEALTH: return new HealthCapabilities(bytes[3]);
            case POI: return new POICapabilities(bytes[3]);
            case PARCEL_DELIVERY: return new ParcelDeliveryCapabilities(bytes[3]);
            default: throw new CommandParseException();
        }
    }
}
