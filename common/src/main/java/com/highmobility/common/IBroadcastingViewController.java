package com.highmobility.common;

import com.high_mobility.HMLink.ConnectedLink;

public interface IBroadcastingViewController {
    void onDestroy();
    void onPairingApproved(boolean approved);
    void onLinkClicked();
}
