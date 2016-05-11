package com.high_mobility.digitalkey;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;

import com.high_mobility.digitalkey.HMLink.Broadcasting.Link;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by ttiganik on 27/04/16.
 */
public class LinkGridViewAdapter extends FragmentGridPagerAdapter {
    Link[] links;
    PeripheralActivity activity;

    public LinkGridViewAdapter(PeripheralActivity activity, FragmentManager fm) {
        super(fm);
        this.activity = activity;
    }

    public void setLinks(Link[] links) {
        this.links = links;
        notifyDataSetChanged();
    }

    public LinkFragment getCurrentFragment(GridViewPager pager) {
        try {
            Method m = this.getClass().getSuperclass().getDeclaredMethod("makeFragmentName", int.class, long.class);
            Field f = this.getClass().getSuperclass().getDeclaredField("mFragmentManager");
            f.setAccessible(true);
            FragmentManager fm = (FragmentManager) f.get(this);
            m.setAccessible(true);
            String tag;
            tag = (String) m.invoke(null, pager.getId(), (long) pager.getCurrentItem().x);
            return (LinkFragment)fm.findFragmentByTag(tag);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Fragment getFragment(int row, int column) {
        LinkFragment fragment = LinkFragment.newInstance(this, links[column]);
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

    void didClickLock(Link link) {
        activity.didClickLock(link);
    }

    void didClickUnlock(Link link) {
        activity.didClickUnlock(link);
    }
}
