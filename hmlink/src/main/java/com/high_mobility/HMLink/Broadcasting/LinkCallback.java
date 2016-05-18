package com.high_mobility.HMLink.Broadcasting;

import com.high_mobility.HMLink.Constants;

/**
 * Created by ttiganik on 13/04/16.
 */
public interface LinkCallback {
    void linkStateDidChange(Link link, Link.State oldState);
    byte[] linkDidReceiveCustomCommand(Link link, byte[] bytes);

    void linkDidReceivePairingRequest(Link link, Constants.ApprovedCallback approvedCallback);
    void linkPairingDidTimeout(Link link);
}
