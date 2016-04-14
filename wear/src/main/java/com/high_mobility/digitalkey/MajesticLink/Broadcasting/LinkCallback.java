package com.high_mobility.digitalkey.MajesticLink.Broadcasting;

import com.high_mobility.digitalkey.MajesticLink.Constants;

/**
 * Created by ttiganik on 13/04/16.
 */
public interface LinkCallback {
    void linkStateDidChange(Link link, Link.State oldState);
    void linkDidExecuteCommand(Link link, Constants.Command command, Constants.Error error);
    byte[] linkDidReceiveCustomCommand(Link link, byte[] bytes);
    void linkDidReceivePairingRequest(Link link, byte[] serialNumber, Constants.ApprovedCallback approvedCallback, float timeout);
}
