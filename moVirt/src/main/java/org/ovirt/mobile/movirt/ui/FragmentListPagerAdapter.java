package org.ovirt.mobile.movirt.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class FragmentListPagerAdapter extends FragmentPagerAdapter {
    private final String[] pageTitles;
    private final Fragment[] fragments;

    public FragmentListPagerAdapter(FragmentManager fm, String[] pageTitles, Fragment... fragments) {
        super(fm);
        this.pageTitles = pageTitles;
        this.fragments = fragments;
        if (pageTitles.length != fragments.length) {
            throw new IllegalArgumentException("Number of page titles must match number of fragments.");
        }
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pageTitles[position];
    }
}
