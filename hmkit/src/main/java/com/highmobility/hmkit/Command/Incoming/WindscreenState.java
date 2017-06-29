package com.highmobility.hmkit.Command.Incoming;
import com.highmobility.hmkit.Command.CommandParseException;

/**
 * Created by ttiganik on 13/09/16.
 */
public class WindscreenState extends IncomingCommand {
    public WindscreenState(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length < 4) throw new CommandParseException();
    }
}
