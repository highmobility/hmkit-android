package com.highmobility.common;

import com.high_mobility.hmkit.ConnectedLink;

public interface IBroadcastingView extends IView {
    void setStatusText(String text);
    void showPairingView(boolean show);
    Class getLinkActivityClass();
    void updateLink(ConnectedLink link);
}
