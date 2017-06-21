package com.high_mobility.hmkit.Command.Capability;

import com.high_mobility.hmkit.Command.Command;
import com.high_mobility.hmkit.Command.CommandParseException;

/**
 * Created by root on 6/21/17.
 */

public class NotificationsCapability extends FeatureCapability {
    AvailableCapability.Capability notification;
    AvailableCapability.Capability notificationAction;

    public NotificationsCapability(byte[] bytes) throws CommandParseException {
        super(Command.Identifier.NOTIFICATIONS);
        if (bytes.length != 5) throw new CommandParseException();
        notification = AvailableCapability.Capability.fromByte(bytes[3]);
        notificationAction = AvailableCapability.Capability.fromByte(bytes[4]);
    }

    public AvailableCapability.Capability getNotification() {
        return notification;
    }

    public AvailableCapability.Capability getNotificationAction() {
        return notificationAction;
    }
}
