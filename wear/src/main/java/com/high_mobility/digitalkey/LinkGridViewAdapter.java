package com.high_mobility.digitalkey;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.wearable.view.FragmentGridPagerAdapter;

import com.high_mobility.HMLink.Shared.ConnectedLink;

import java.util.List;

/**
 * Created by ttiganik on 27/04/16.
 */
public class LinkGridViewAdapter extends FragmentGridPagerAdapter {
    List<ConnectedLink> links;
    LinkFragment[] fragments;
    PeripheralActivity activity;
    FragmentManager fm;

    public LinkGridViewAdapter(PeripheralActivity activity, FragmentManager fm) {
        super(fm);
        this.activity = activity;
        this.fm = fm;
    }

    public void setLinks(List<ConnectedLink> links) {
        this.links = links;
        fragments = new LinkFragment[links.size()];
        notifyDataSetChanged();
    }

    public LinkFragment getFragment(ConnectedLink link) {
        int linkIndex = -1;
        for (int i = 0; i < links.size(); i++){
            if (links.get(i) == link) {
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
        LinkFragment fragment = LinkFragment.newInstance(this, links.get(column));
        fragments[column] = fragment;
        return fragment;
    }

    @Override
    public int getRowCount() {
        return (links == null || links.size() == 0)? 0 : 1;
    }

    @Override
    public int getColumnCount(int i) {
        return links == null ? 0 : links.size();
    }

    void didClickLock(ConnectedLink link) {
        activity.didClickLock(link);
    }

    void didClickUnlock(ConnectedLink link) {
        activity.didClickUnlock(link);
    }
}
