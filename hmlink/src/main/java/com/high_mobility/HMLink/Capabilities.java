package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 14/09/16.
 */
public class Capabilities extends Incoming {
    Capabilities(byte[] bytes) throws CommandParseException {
        super(bytes);
    }
}
