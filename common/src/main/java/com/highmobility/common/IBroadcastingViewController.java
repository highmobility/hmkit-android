package com.highmobility.common;

public interface IBroadcastingViewController {
    void onDestroy();
    void onPairingApproved(boolean approved);
    void onLinkClicked();
    void onLinkViewResult(int result);
    void onDisconnectClicked();
}
