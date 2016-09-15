package com.high_mobility.HMLink.Command;

/**
 * Created by ttiganik on 13/09/16.
 */
public class VehicleStatus extends Incoming {
    // TODO:
    public VehicleStatus(byte[] bytes) throws CommandParseException {
        super(bytes);
    }
}
