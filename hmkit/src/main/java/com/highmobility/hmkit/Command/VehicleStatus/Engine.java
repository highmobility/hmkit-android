package com.highmobility.hmkit.Command.VehicleStatus;
import com.highmobility.hmkit.ByteUtils;
import com.highmobility.hmkit.Command.Command.Identifier;
import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.DoorLockState;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class Engine extends FeatureState {
    boolean on;

    /**
     *
     * @return the ignition state
     */
    public boolean isOn() {
        return on;
    }

    public Engine(byte[] bytes) throws CommandParseException {
        super(Identifier.ENGINE);

        if (bytes.length < 4) throw new CommandParseException();
        on = ByteUtils.getBool(bytes[3]);
    }
}