package com.high_mobility.digitalkey;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by ttiganik on 27/04/16.
 */
public class LinkGridItem extends View {
    public LinearLayout container;
    TextView serialTextView; // TODO: add serial textView

    public LinkGridItem(Context context) {
        super(context);
        LayoutInflater inflater =
                (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        container = (LinearLayout)inflater.inflate( R.layout.link_grid_item, null );

    }
}
