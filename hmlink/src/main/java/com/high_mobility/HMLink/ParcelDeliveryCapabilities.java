package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 14/10/2016.
 */

public class ParcelDeliveryCapabilities extends CapabilityType {

    private final Capability.Available deliveredParcelsCapability;

    public ParcelDeliveryCapabilities(byte aByte) throws CommandParseException {
        super(Type.PARCEL_DELIVERY);
        deliveredParcelsCapability = Capability.availableCapability(aByte);
    }

    public Capability.Available getDeliveredParcelsCapability() {
        return deliveredParcelsCapability;
    }
}
