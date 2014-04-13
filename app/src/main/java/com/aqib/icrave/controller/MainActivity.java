package com.aqib.icrave.controller;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.aqib.icrave.R;

/**
 * Entry point into app.
 */
public class MainActivity extends RootActivity {

    MainPagerAdapter mPagerAdapter;
    ViewPager mViewPager;
    android.app.ActionBar actionBar;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);

        mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @SuppressLint("NewApi")
            @Override
            public void onPageSelected(int position) {
                actionBar = getActionBar();
                actionBar.setSelectedNavigationItem(position);
            }
        });

        mViewPager.setAdapter(mPagerAdapter);
        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {}

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {}
        };

        actionBar.addTab(actionBar.newTab().setText("Home").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("History").setTabListener(tabListener));
    }


}
