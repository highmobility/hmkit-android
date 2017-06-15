package com.high_mobility.hmkit.Command.Incoming;

import com.high_mobility.hmkit.Command.CommandParseException;

/**
 * Created by ttiganik on 13/09/16.
 *
 * This is an evented command that is sent from the car every time the rooftop state changes.
 * It is also sent when a Get Rooftop State is received by the car.
 */
public class RooftopState extends IncomingCommand {
    /**
     * The possible states of the rooftop.
     */
    float dimmingPercentage;
    float openPercentage;

    public RooftopState(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 5) throw new CommandParseException();

        dimmingPercentage =  (int)bytes[3] / 100f;
        openPercentage =  (int)bytes[4] / 100f;
    }

    /**
     *
     * @return the dim percentage of the rooftop
     */
    public float getDimmingPercentage() {
        return dimmingPercentage;
    }

    /**
     *
     * @return the percentage of how much the rooftop is open
     */
    public float getOpenPercentage() {
        return openPercentage;
    }
}
