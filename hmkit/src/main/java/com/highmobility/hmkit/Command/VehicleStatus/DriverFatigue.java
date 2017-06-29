package com.highmobility.hmkit.Command.VehicleStatus;
import com.highmobility.hmkit.Command.Command.Identifier;
import com.highmobility.hmkit.Command.CommandParseException;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class DriverFatigue extends FeatureState {
    public DriverFatigue(byte[] bytes) throws CommandParseException {
        super(Identifier.DRIVER_FATIGUE);

        if (bytes.length < 5) throw new CommandParseException();

    }
}