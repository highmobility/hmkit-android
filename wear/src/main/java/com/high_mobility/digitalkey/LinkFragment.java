package com.high_mobility.digitalkey;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.high_mobility.HMLink.ByteUtils;
import com.high_mobility.HMLink.ConnectedLink;

/**
 * Fragment used to show BoxInsetLayout.
 */
public class LinkFragment extends Fragment {
	TextView textView;
	ConnectedLink link;
	LinkGridViewAdapter adapter;
	LinearLayout authView;

	public static LinkFragment newInstance(LinkGridViewAdapter adapter, ConnectedLink link) {
		LinkFragment fragment = new LinkFragment();
		fragment.link = link;
		fragment.adapter = adapter;
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.link_grid_item, container, false);
		textView = (TextView)layout.findViewById(R.id.text);
		textView.setText(ByteUtils.hexFromBytes(link.getSerial()));

		Button unlockButton = (Button) layout.findViewById(R.id.unlockButton);
		unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				adapter.didClickUnlock(link);
            }
        });

		Button lockButton = (Button) layout.findViewById(R.id.lockButton);
		lockButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				adapter.didClickLock(link);
			}
		});

		authView = (LinearLayout)layout.findViewById(R.id.link_grid_item_auth_view);
		ViewUtils.enableView(authView, link.getState() == ConnectedLink.State.AUTHENTICATED);

		return layout;
	}
}
