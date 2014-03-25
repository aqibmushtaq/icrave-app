package com.aqib.icrave.controller;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * This class will serve as an adapter for the main activity.
 */
public class MainPagerAdapter extends FragmentPagerAdapter {

    private Fragment homeFragment;
    private Fragment historyFragment;

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                if (homeFragment == null) {
                    homeFragment = new HomeFragment();
                    ((HomeFragment)homeFragment).setHistoryFragment(getHistoryFragment());
                }
                return homeFragment;
            case 1:
                return getHistoryFragment();

        }
        return null;
    }

    private Fragment getHistoryFragment () {
        if (historyFragment == null)
            historyFragment = new HistoryFragment();
        return historyFragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Home";
            case 1:
                return "History";
            default:
                return "Home";
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

}
