package com.highmobility.common;

import com.highmobility.hmkit.ConnectedLink;

public interface IBroadcastingView extends IView {
    void setStatusText(String text);
    void showPairingView(boolean show);
    Class getLinkActivityClass();
    void updateLink(ConnectedLink link);
}
