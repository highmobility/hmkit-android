package com.high_mobility.digitalkey.broadcast;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.high_mobility.HMLink.Shared.ConnectedLink;

import java.util.List;

/**
 * Created by ttiganik on 25/05/16.
 */
public class LinkPagerAdapter extends FragmentStatePagerAdapter {
    BroadcastActivity activity;
    FragmentManager fm;
    List<ConnectedLink> links;
    LinkFragment[] fragments;


    public LinkPagerAdapter(BroadcastActivity activity, FragmentManager fm) {
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
    public Fragment getItem(int position) {
        LinkFragment fragment = LinkFragment.newInstance(this, links.get(position));
        fragments[position] = fragment;
        return fragment;
    }

    @Override
    public int getCount() {
        return links != null ? links.size() : 0;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    void onLockClicked(ConnectedLink link) {
        activity.onLockClicked(link);
    }

    void onUnlockClicked(ConnectedLink link) {
        activity.onUnlockClicked(link);
    }
}
