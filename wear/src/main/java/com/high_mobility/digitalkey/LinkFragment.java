package com.high_mobility.digitalkey;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.high_mobility.digitalkey.HMLink.Broadcasting.Link;

/**
 * Fragment used to show BoxInsetLayout.
 */
public class LinkFragment extends Fragment {

	TextView textView;
	Button sendCmdButton;
	Link link;
	LinkGridViewAdapter adapter;

	public static LinkFragment newInstance(LinkGridViewAdapter adapter, Link link) {
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
		textView.setText(Utils.hexFromBytes(link.getSerial()));

		sendCmdButton = (Button) layout.findViewById(R.id.sendCmdButton);
        sendCmdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.didClickSendCmdButton(link);
            }
        });

		sendCmdButton.setEnabled(link.getState() == Link.State.AUTHENTICATED);

		return layout;
	}
}
