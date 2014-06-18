package com.lairdtech.bl600toolkit.activities;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.lairdtech.bl600toolkit.R;
import com.lairdtech.bl600toolkit.blewrapper.BleWrapper;
import com.lairdtech.bl600toolkit.fragments.MyFragmentPagerAdapter;
import com.lairdtech.bl600toolkit.target.MyTarget;

/*
 * It holds the choices screen and all the fragments with BLE functionalities
 */

public class FragmentsContainerActivity extends FragmentActivity{
    public static final int ENABLE_BT_REQUEST_ID = 1;
    public static final int SET_OFF_SCREEN_PAGE_LIMIT =1;
    public static final boolean SHOW__APP_TITLE = false;
    public static final boolean SHOW_APP_LOGO = false;
    // in this class BleWrapper is only used for checking if BT/BLE hardware exists and if it's enabled
    private BleWrapper mBleWrapper;
    private ViewPager mViewPager;
    private ActionBar mActionBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragments_container);

        mBleWrapper = new BleWrapper(this, null, null, null);
        FragmentManager fm = getSupportFragmentManager();
        
        // check if we have BT and BLE on board
        if (mBleWrapper.checkBleHardwareAvailable() == false) {
            MyTarget.toastMsg(this, "BLE Hardware is required but not available!");
            finish();
        }
        
        setActionBar();
        
        mViewPager = (ViewPager) findViewById(R.id.pager);        
        mViewPager.setOffscreenPageLimit(SET_OFF_SCREEN_PAGE_LIMIT);
        MyFragmentPagerAdapter mFragmentPagerAdapter = new MyFragmentPagerAdapter(fm, mViewPager);
        mViewPager.setAdapter(mFragmentPagerAdapter);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener(){
            @Override
            public void onPageScrollStateChanged(int arg0) {
                if (arg0 == ViewPager.SCROLL_STATE_IDLE) {}
                if (arg0 == ViewPager.SCROLL_STATE_DRAGGING) {}
                if (arg0 == ViewPager.SCROLL_STATE_SETTLING) {}
            }
            
            @Override
            public void onPageScrolled(int position, float from, int pixels) {}
            
            @Override
            public void onPageSelected(int position) {
                mActionBar.setSelectedNavigationItem(0);
            }
        });
        
        setTabs();
    }
    
    private void setActionBar() {
        mActionBar = getActionBar();
        mActionBar.setDisplayShowTitleEnabled(SHOW__APP_TITLE);
        mActionBar.setDisplayShowHomeEnabled(SHOW_APP_LOGO);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }
    
    private void setTabs() {
        Tab tab;
//        tab = mActionBar.newTab().setText(getString(R.string.choices))
//                .setTabListener(mTabListener);
//        mActionBar.addTab(tab);
//        
//        tab = mActionBar.newTab().setText(getString(R.string.heart_rate))
//                .setTabListener(mTabListener);
//        mActionBar.addTab(tab);

        tab = mActionBar.newTab().setText(getString(R.string.soil_moisture))
                .setTabListener(mTabListener);
        mActionBar.addTab(tab);
//
//        tab = mActionBar.newTab().setText(getString(R.string.blood_pressure))
//                .setTabListener(mTabListener);
//        mActionBar.addTab(tab);
//
//        tab = mActionBar.newTab().setText(getString(R.string.proximity))
//                .setTabListener(mTabListener);
//        mActionBar.addTab(tab);
    }
    
    /** Defining tab listener */
    private final TabListener mTabListener = new TabListener() {

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {}

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            mViewPager.setCurrentItem(tab.getPosition());            
        }

        @Override
        public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {}
    };
    
    @Override
    public void onResume() {
        super.onResume();
        /*
         *  on every Resume check if BT is enabled
         *  user could turn it off while app was in background etc
         */
        if (mBleWrapper.isBtEnabled() == false) {
            // BT is not turned on - ask user to make it enabled
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_ID);
            // see onActivityResult to check what is the status of our request
        }
    }
    
    @Override
    public void onPause(){
        super.onPause();
        
        if(SettingsActivity.checkRunInBackground() == true && SettingsActivity.inSettingsScreen == false){
            MyTarget.toastMsg(this, "App is running in the background");
        } else{
            
        }
        
    }
    @Override
    public void onBackPressed() {
        MyTarget.debugMsg("fragments container onBackPressed");
        /*
         * if user is not looking at the choices screen and the back button is
         * pressed they will be taken directly to the choices screen. When the
         * back button is pressed while looking at the choices screen the app
         * will close
         */
        if (mActionBar.getSelectedNavigationIndex() != 0
                && mViewPager.getCurrentItem() != 0) {
            // set to choices tab
            mActionBar.setSelectedNavigationItem(0);
            // set to choices screen
            mViewPager.setCurrentItem(0);
        } else {
            // user is looking at the choices screen
            finish();
        }
    }

    /* check if user agreed to enable BT */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_BT_REQUEST_ID) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // user didn't enabled BT
                MyTarget.toastMsg(this, "Sorry, BT has to be turned ON for this App!");
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}