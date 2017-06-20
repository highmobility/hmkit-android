package com.highmobility.digitalkey.scan;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ttiganik on 02/06/16.
 */
public class ScanListAdapter /*extends ArrayAdapter<ScannedLink> {

    public ScanListAdapter(Context context, int resource, List<ScannedLink> devices) {
        super(context, resource, devices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ScannedLink device = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        TextView title = (TextView) convertView;
        title.setTextColor(Color.BLACK);
        title.setText(device.getName());
        return convertView;
    }
}
*/{}