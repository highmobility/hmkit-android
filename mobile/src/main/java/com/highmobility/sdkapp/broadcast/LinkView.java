package com.highmobility.sdkapp.broadcast;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.highmobility.common.ILinkView;
import com.highmobility.common.ILinkViewController;
import com.highmobility.common.LinkViewController;
import com.highmobility.sdkapp.R;

public class LinkView extends Activity implements ILinkView {
    static final String TAG = "LinkView";

    private ILinkViewController controller;
    @BindView (R.id.buttons_view) LinearLayout buttonsView;
    @BindView (R.id.lock_button) Button lockButton;
    @BindView (R.id.revoke_button) Button revokeButton;
    @BindView (R.id.progress_bar) ProgressBar progressBar;
    @BindView (R.id.link_status_text) TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.link_view);
        ButterKnife.bind(this);
        controller = new LinkViewController(this);

        lockButton.setOnClickListener(v -> controller.onLockDoorsClicked());
        revokeButton.setOnClickListener(view -> controller.onRevokeClicked());
    }

    @Override protected void onPause() {
        super.onPause();
    }

    @Override protected void onResume() {
        super.onResume();
    }

    @Override
    public void showLoadingView(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            buttonsView.setVisibility(View.GONE);
        }
        else {
            progressBar.setVisibility(View.GONE);
            buttonsView.setVisibility(View.VISIBLE);
            statusText.setText(null);
        }
    }

    @Override
    public void onDoorsLocked(boolean locked) {
        if (locked) {
            lockButton.setText("unlock");
        }
        else {
            lockButton.setText("lock");
        }
    }

    @Override
    public void onTrunkLocked(boolean locked) {

    }

    @Override
    public void enableTrunkButton(boolean enable) {

    }

    @Override
    public void enableLockButton(boolean enable) {
        lockButton.setEnabled(enable);
    }

    @Override
    public Activity getActivity() {
        return this;
    }
}
