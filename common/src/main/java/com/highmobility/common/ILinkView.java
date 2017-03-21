package com.highmobility.common;

/**
 * Created by ttiganik on 04/10/2016.
 */

public interface ILinkView extends IView {
    void showLoadingView(boolean show);

    void onDoorsLocked(boolean locked);
    void onTrunkLocked(boolean locked);

    void enableTrunkButton(boolean enable);
    void enableLockButton(boolean enable);
}
