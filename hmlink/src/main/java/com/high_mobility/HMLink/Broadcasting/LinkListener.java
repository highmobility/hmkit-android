package com.high_mobility.HMLink.Broadcasting;

import com.high_mobility.HMLink.Constants;

/**
 * Created by ttiganik on 13/04/16.
 */
public interface LinkListener {
    void onStateChanged(Link link, Link.State oldState);
    byte[] onCommandReceived(Link link, byte[] bytes);

    void onPairingRequested(Link link, Constants.ApprovedCallback approvedCallback);
    void onPairingRequestTimeout(Link link);
}
