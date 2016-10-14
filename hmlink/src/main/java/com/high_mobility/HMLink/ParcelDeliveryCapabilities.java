package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 14/10/2016.
 */

public class ParcelDeliveryCapabilities extends CapabilityType {

    private final Capabilities.Available deliveredParcelsCapability;

    public ParcelDeliveryCapabilities(byte aByte) throws CommandParseException {
        super(Type.PARCEL_DELIVERY);
        deliveredParcelsCapability = Capabilities.availableCapability(aByte);
    }

    public Capabilities.Available getDeliveredParcelsCapability() {
        return deliveredParcelsCapability;
    }
}
