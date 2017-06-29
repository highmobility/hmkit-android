package com.highmobility.hmkit.Command.Incoming;
import com.highmobility.hmkit.Command.CommandParseException;

/**
 * Created by ttiganik on 13/09/16.
 */
public class NotificationAction extends IncomingCommand {
    int actionIdentifier;

    /**
     *
     * @return The identifier of selected action item
     */
    public int getActionIdentifier() {
        return actionIdentifier;
    }

    public NotificationAction(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length < 4) throw new CommandParseException();
        actionIdentifier = (int)bytes[3];
    }
}
