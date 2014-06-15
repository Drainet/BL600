package com.lairdtech.bl600toolkit.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

/*
 * responsible for how many and which screens to display for scrolling
 */

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT = 1;
    private ViewPager mPager;
	
	public MyFragmentPagerAdapter(FragmentManager fm, ViewPager mPager) {
		super(fm);
        this.mPager = mPager;
	}

	/** This method will be invoked when a page is requested to create */
	@Override
	public Fragment getItem(int position) {
		switch(position){
            case 0:
            	HeartRateFragment hrFragment = new HeartRateFragment();
				return hrFragment;
//                ChoiceFragment choicesFragment = new ChoiceFragment();
//                choicesFragment.setPageAdapterForButtons(mPager);
//                return choicesFragment;
//			case 1:
//				HeartRateFragment hrFragment = new HeartRateFragment();
//				return hrFragment;
//				
//			case 2:
//				TemperatureFragment tempFragment = new TemperatureFragment();
//				return tempFragment;
//				
//            case 3:
//                BloodPressureFragment bpFragment = new BloodPressureFragment();
//                return bpFragment;
//            case 4:
//                ProximityFragment proximityFragment = new ProximityFragment();
//                return proximityFragment;
		}
		return null;
	}
	
	/** Returns the number of pages */
	@Override
	public int getCount() {		
		return PAGE_COUNT;
	}
}