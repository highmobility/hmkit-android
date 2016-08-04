package com.high_mobility.digitalkey.scan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.high_mobility.HMLink.Shared.ExternalDevice;

/**
 * Created by ttiganik on 02/06/16.
 */
public class ScanListAdapter extends ArrayAdapter<ExternalDevice> {

    public ScanListAdapter(Context context, int resource, ExternalDevice[] objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ExternalDevice device = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        TextView title = (TextView) convertView;
        title.setText(device.getName());
        return convertView;
    }
}
