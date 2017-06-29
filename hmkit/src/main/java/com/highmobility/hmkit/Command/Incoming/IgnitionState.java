package com.highmobility.hmkit.Command.Incoming;
import com.highmobility.hmkit.ByteUtils;
import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.DoorLockState;

/**
 * Created by ttiganik on 13/09/16.
 */
public class IgnitionState extends IncomingCommand {
    boolean on;

    /**
     *
     * @return the ignition state
     */
    public boolean isOn() {
        return on;
    }

    public IgnitionState(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length < 4) throw new CommandParseException();

        on = ByteUtils.getBool(bytes[3]);
    }
}
