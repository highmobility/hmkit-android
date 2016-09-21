package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 13/09/16.
 *
 * This notification is sent when a Get Vehicle Status command is received by the car.
 *
 */
public class VehicleStatus extends Notification {
    public VehicleStatus(byte[] bytes) throws CommandParseException {
        super(bytes);
    }
}
