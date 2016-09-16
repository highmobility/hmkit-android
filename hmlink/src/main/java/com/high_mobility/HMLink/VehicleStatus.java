package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 13/09/16.
 */
public class VehicleStatus extends Incoming {
    public VehicleStatus(byte[] bytes) throws CommandParseException {
        super(bytes);
    }
}
