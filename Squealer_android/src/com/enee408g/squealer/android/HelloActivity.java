package com.enee408g.squealer.android;

import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import com.enee408g.squealer.android.ReceiverFragment;

public class HelloActivity extends FragmentActivity {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
	MainPagerAdapter mMainPagerAdapter;
    ViewPager mViewPager;

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);
        
        mMainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.//
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mMainPagerAdapter);
        
        // Set up PreferenceHelper
        PreferenceHelper.setContext(this);
    }
    
    public class MainPagerAdapter extends FragmentStatePagerAdapter {

		public MainPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case 0: return new ReceiverFragment();
			case 1: return new TransmitterFragment();
			default: return null;
			}
		}
		@Override
		public int getCount() {
			return 2;
		}
    }
   
}