package com.aqib.icrave;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

/**
 * Entry point into app.
 */
public class MainActivity extends RootActivity {

    MainPagerAdapter mPagerAdapter;

    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);

        mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
    }


}
