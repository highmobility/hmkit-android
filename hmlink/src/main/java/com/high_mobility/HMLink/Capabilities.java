package com.high_mobility.HMLink;

import java.util.Arrays;

/**
 * Created by ttiganik on 14/09/16.
 *
 * This command is sent when a Get Capabilities command is received by the car.
 */
public class Capabilities extends IncomingCommand {
    public enum Available {
        UNAVAILABLE, AVAILABLE, UNSUPPORTED
    }

    public enum AvailableGetState {
        UNAVAILABLE, AVAILABLE, GET_STATE_AVAILABLE, UNSUPPORTED
    }

    DigitalKeyCapabilities digitalKeyCapabilities;
    ChassisCapabilities chassisCapabilities;
    ParkingCapabilities parkingCapabilities;
    HealthCapabilities healthCapabilities;
    POICapabilities poiCapabilities;
    ParcelDeliveryCapabilities parcelDeliveryCapabilities;

    public Capabilities(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 10) throw new CommandParseException();

        byte[] digitalKeyBytes = Arrays.copyOfRange(bytes, 2, 2 + 2);
        digitalKeyCapabilities = new DigitalKeyCapabilities(digitalKeyBytes);

        byte[] chassisBytes = Arrays.copyOfRange(bytes, 4, 4 + 2);
        chassisCapabilities = new ChassisCapabilities(chassisBytes);

        parkingCapabilities = new ParkingCapabilities(bytes[6]);
        healthCapabilities = new HealthCapabilities(bytes[7]);
        poiCapabilities = new POICapabilities(bytes[8]);
        parcelDeliveryCapabilities = new ParcelDeliveryCapabilities(bytes[9]);
    }

    public DigitalKeyCapabilities getDigitalKeyCapabilities() {
        return digitalKeyCapabilities;
    }

    public ChassisCapabilities getChassisCapabilities() {
        return chassisCapabilities;
    }

    public ParkingCapabilities getParkingCapabilities() {
        return parkingCapabilities;
    }

    public HealthCapabilities getHealthCapabilities() {
        return healthCapabilities;
    }

    public POICapabilities getPoiCapabilities() {
        return poiCapabilities;
    }

    public ParcelDeliveryCapabilities getParcelDeliveryCapabilities() { return parcelDeliveryCapabilities; }

    static AvailableGetState getStateCapability(byte value) throws CommandParseException {
        switch (value) {
            case 0x00: return AvailableGetState.UNAVAILABLE;
            case 0x01: return AvailableGetState.AVAILABLE;
            case 0x02: return AvailableGetState.GET_STATE_AVAILABLE;
            case (byte)0xFF: return AvailableGetState.UNSUPPORTED;
            default: throw new CommandParseException();
        }
    }

    static Available availableCapability(byte value) throws CommandParseException {
        switch (value) {
            case 0x00: return Available.UNAVAILABLE;
            case 0x01: return Available.AVAILABLE;
            case (byte) 0xFF: return Available.UNSUPPORTED;
            default: throw new CommandParseException();
        }
    }
}
