package com.highmobility.hmkit.Command.VehicleStatus;
import com.highmobility.hmkit.Command.Command.Identifier;
import com.highmobility.hmkit.Command.CommandParseException;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class TextInput extends FeatureState {
    public TextInput(byte[] bytes) throws CommandParseException {
        super(Identifier.TEXT_INPUT);

        if (bytes.length < 5) throw new CommandParseException();

    }
}