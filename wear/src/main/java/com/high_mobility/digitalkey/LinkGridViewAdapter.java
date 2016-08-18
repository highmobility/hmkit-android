package com.high_mobility.digitalkey;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.wearable.view.FragmentGridPagerAdapter;

import com.high_mobility.HMLink.Shared.ConnectedLink;

/**
 * Created by ttiganik on 27/04/16.
 */
public class LinkGridViewAdapter extends FragmentGridPagerAdapter {
    ConnectedLink[] links;
    LinkFragment[] fragments;
    PeripheralActivity activity;
    FragmentManager fm;

    public LinkGridViewAdapter(PeripheralActivity activity, FragmentManager fm) {
        super(fm);
        this.activity = activity;
        this.fm = fm;
    }

    public void setLinks(ConnectedLink[] links) {
        this.links = links;
        fragments = new LinkFragment[links.length];
        notifyDataSetChanged();
    }

    public LinkFragment getFragment(ConnectedLink link) {
        int linkIndex = -1;
        for (int i = 0; i < links.length; i++){
            if (links[i] == link) {
                linkIndex = i;
                break;
            }
        }

        if (linkIndex >= 0)
            return fragments[linkIndex];
        else
            return null;
    }

    @Override
    public Fragment getFragment(int row, int column) {
        LinkFragment fragment = LinkFragment.newInstance(this, links[column]);
        fragments[column] = fragment;
        return fragment;
    }

    @Override
    public int getRowCount() {
        return (links == null || links.length == 0)? 0 : 1;
    }

    @Override
    public int getColumnCount(int i) {
        return links == null ? 0 : links.length;
    }

    void didClickLock(ConnectedLink link) {
        activity.didClickLock(link);
    }

    void didClickUnlock(ConnectedLink link) {
        activity.didClickUnlock(link);
    }
}
