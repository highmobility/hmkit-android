package com.high_mobility.digitalkey;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.wearable.view.FragmentGridPagerAdapter;

import com.high_mobility.digitalkey.HMLink.Broadcasting.Link;

/**
 * Created by ttiganik on 27/04/16.
 */
public class LinkGridViewAdapter extends FragmentGridPagerAdapter {
    Link[] links;
    Context ctx;

    public LinkGridViewAdapter(Context ctx, FragmentManager fm) {
        super(fm);
        this.ctx = ctx;
    }

    public void setLinks(Link[] links) {
        this.links = links;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getFragment(int row, int column) {
        LinkFragment fragment = LinkFragment.newInstance(links[column]);
        return fragment;
    }

    @Override
    public int getRowCount() {
        return links == null ? 0 : 1;
    }

    @Override
    public int getColumnCount(int i) {
        return links == null ? 0 : links.length;
    }
}
