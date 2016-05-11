package com.high_mobility.digitalkey.HMLink.Broadcasting;

import com.high_mobility.digitalkey.HMLink.Constants;
import com.high_mobility.digitalkey.HMLink.LinkException;

/**
 * Created by ttiganik on 13/04/16.
 */
public interface LinkCallback {
    void linkStateDidChange(Link link, Link.State oldState);
    byte[] linkDidReceiveCustomCommand(Link link, byte[] bytes);
    void linkDidReceivePairingRequest(Link link, byte[] serialNumber, Constants.ApprovedCallback approvedCallback, float timeout);
}
