package com.highmobility.hmkit.Command.VehicleStatus;
import com.highmobility.hmkit.Command.Command.Identifier;
import com.highmobility.hmkit.Command.CommandParseException;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class Fueling extends FeatureState {
    public Fueling(byte[] bytes) throws CommandParseException {
        super(Identifier.FUELING);

        if (bytes.length < 5) throw new CommandParseException();

    }
}