package com.highmobility.hmkit.Command.Incoming;
import com.highmobility.hmkit.Command.CommandParseException;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by ttiganik on 13/09/16.
 */
public class SendMessage extends IncomingCommand {
    String recipientHandle;
    String text;

    /**
     *
     * @return The recipient handle (e.g. phone number)
     */
    public String getRecipientHandle() {
        return recipientHandle;
    }

    /**
     *
     * @return The text message
     */
    public String getText() {
        return text;
    }

    public SendMessage(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length < 4) throw new CommandParseException();
        int recipientSize = bytes[3];
        try {
            recipientHandle = new String(Arrays.copyOfRange(bytes, 4, 4 + recipientSize), "UTF-8");
            int messageSizePosition = 4 + recipientSize;
            int messageSize = bytes[messageSizePosition];
            text = new String(Arrays.copyOfRange(bytes, messageSizePosition + 1, messageSizePosition + 1 + messageSize), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new CommandParseException();
        }
    }
}
