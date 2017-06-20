package com.highmobility.digitalkey;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.highmobility.common.ILinkView;
import com.highmobility.common.ILinkViewController;
import com.highmobility.common.LinkViewController;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LinkView extends WearableActivity implements ILinkView {
    static final String TAG = "LinkView";

    private ILinkViewController controller;
    @BindView (R.id.lock_button) Button lockButton;
    @BindView (R.id.progress_bar) ProgressBar progressBar;
    @BindView (R.id.link_status_text) TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.link_view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ButterKnife.bind(this);
        controller = new LinkViewController(this);
        lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.onLockDoorsClicked();
            }
        });
    }

    @Override
    public void showLoadingView(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            lockButton.setVisibility(View.GONE);
        }
        else {
            statusText.setText(null);
            progressBar.setVisibility(View.GONE);
            lockButton.setVisibility(View.VISIBLE);
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
