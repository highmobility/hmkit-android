package com.high_mobility.HMLink.Command.VehicleStatus;

import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Command.Identifier;

/**
 * Created by ttiganik on 21/12/2016.
 */

public class RooftopState extends FeatureState {
    /**
     * The possible states of the rooftop.
     */
    float dimmingPercentage;
    float openPercentage;

    RooftopState(byte[] bytes) throws CommandParseException {
        super(Identifier.ROOFTOP);

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
