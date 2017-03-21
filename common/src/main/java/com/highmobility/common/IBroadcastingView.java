package com.highmobility.common;

import com.high_mobility.HMLink.ConnectedLink;

import java.util.List;

public interface IBroadcastingView extends IView {
    void setStatusText(String text);
    void showPairingView(boolean show);
    Class getLinkActivityClass();
    void updateLink(ConnectedLink link);
}
