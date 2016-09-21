package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 14/09/16.
 *
 * This notification is sent when a Get Capabilities command is received by the car.
 */
public class Capabilities extends Notification {
    Capabilities(byte[] bytes) throws CommandParseException {
        super(bytes);
    }
}
