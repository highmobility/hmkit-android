package com.high_mobility.digitalkey.broadcast;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ttiganik on 24/05/16.
 */
public class ViewUtils {
    static void enableView(View view, boolean enable) {
        view.setEnabled(enable);

        if ( view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;

            for ( int idx = 0 ; idx < group.getChildCount() ; idx++ ) {
                enableView(group.getChildAt(idx), enable);
            }
        }
    }

}
