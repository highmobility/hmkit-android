package com.high_mobility.digitalkey;

import android.content.Context;
import android.support.wearable.view.GridPagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.high_mobility.digitalkey.HMLink.Broadcasting.Link;

/**
 * Created by ttiganik on 27/04/16.
 */
public class LinkGridViewAdapter extends GridPagerAdapter {
    Link[] links;
    Context ctx;

    public LinkGridViewAdapter(Context ctx) {
        this.ctx = ctx;
    }

    public void setLinks(Link[] links) {
        this.links = links;
    }

    @Override
    public int getRowCount() {
        return links.length;
    }

    @Override
    public int getColumnCount(int i) {
        return 0;
    }

    @Override
    public Object instantiateItem(ViewGroup viewGroup, int i, int i1) {
        LinkGridItem item = new LinkGridItem(ctx);
        viewGroup.addView(item);
        return item;
    }

    @Override
    public void destroyItem(ViewGroup viewGroup, int i, int i1, Object o) {
        viewGroup.removeView((View) o);
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view.equals(o);
    }
}
