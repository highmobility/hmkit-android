package com.highmobility.hmkit.Command.Incoming;
import com.highmobility.hmkit.Command.*;
import com.highmobility.hmkit.Command.NotificationAction;

import java.util.Arrays;

/**
 * Created by ttiganik on 13/09/16.
 */
public class Notification extends IncomingCommand {
    String text;
    com.highmobility.hmkit.Command.NotificationAction[] notificationActions;

    /**
     *
     * @return Notification text
     */
    public String getText() {
        return text;
    }

    /**
     *
     * @return Notification actions
     */
    public com.highmobility.hmkit.Command.NotificationAction[] getNotificationActions() {
        return notificationActions;
    }

    public Notification(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length < 3) throw new CommandParseException();

        int textLength = bytes[3];
        text = new String(Arrays.copyOfRange(bytes, 4, 4 + textLength));

        int actionItemsCountPosition = 4 + textLength;
        int actionItemCount = bytes[actionItemsCountPosition];
        int position = actionItemsCountPosition + 1;
        notificationActions = new NotificationAction[actionItemCount];
        for (int i = 0; i < actionItemCount; i++) {
            int identifier = bytes[position];
            int notificationTextLength = bytes[position + 1];
            String notificationText = new String (Arrays.copyOfRange(bytes, position + 2, position + 2 + notificationTextLength));
            position = position + 2 + notificationTextLength;
            NotificationAction action = new NotificationAction(identifier, notificationText);
            notificationActions[i] = action;
        }
    }
}
