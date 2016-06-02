package com.high_mobility.digitalkey.broadcast;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.high_mobility.HMLink.Broadcasting.ByteUtils;
import com.high_mobility.HMLink.Broadcasting.Link;
import com.high_mobility.digitalkey.R;

/**
 * Created by ttiganik on 24/05/16.
 */
public class LinkFragment extends Fragment {
    Link link;
    LinkPagerAdapter adapter;

    TextView serialTextView;
    LinearLayout authView;

    public static LinkFragment newInstance(LinkPagerAdapter adapter, Link link) {
        LinkFragment fragment = new LinkFragment();
        fragment.link = link;
        fragment.adapter = adapter;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.link_grid_item, container, false);
        serialTextView = (TextView)layout.findViewById(R.id.link_id_textview);
        serialTextView.setText(ByteUtils.hexFromBytes(link.getSerial()));

        Button unlockButton = (Button) layout.findViewById(R.id.unlock_button);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.onUnlockClicked(link);
            }
        });

        Button lockButton = (Button) layout.findViewById(R.id.lock_button);
        lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.onLockClicked(link);
            }
        });

        authView = (LinearLayout)layout.findViewById(R.id.link_grid_item_auth_view);
        ViewUtils.enableView(authView, link.getState() == Link.State.AUTHENTICATED);

        return layout;
    }
}
