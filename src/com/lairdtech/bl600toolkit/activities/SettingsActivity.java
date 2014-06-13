package com.lairdtech.bl600toolkit.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.lairdtech.bl600toolkit.R;
import com.lairdtech.bl600toolkit.target.MyTarget;

public class SettingsActivity extends Activity implements OnCheckedChangeListener {
    public static boolean runInBackground = false;
    private Switch switchRunInBackground;
    /*
     * when the settings screen is opened the "FragmentsContainerActivity" and
     * all the fragments that it has goes into the onPause phase. This
     * boolean is used so that the app does not disconnect with the BLE devices
     * when the settings screen is opened
     */
    public static boolean inSettingsScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        bindViews(null);
        setDefaultViewValues();
        setListeners();
    }

    public void bindViews(View view) {
        switchRunInBackground = (Switch) findViewById(R.id.switchRunInBackground);
    }

    public void setDefaultViewValues() {
        if (runInBackground == true) {
            switchRunInBackground.setChecked(true);
        } else {
            switchRunInBackground.setChecked(false);
        }
    }

    public void setListeners() {
        switchRunInBackground.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
        case R.id.switchRunInBackground:
            if (isChecked == true) {
                runInBackground = true;
                MyTarget.infoMsg("App Runs In The Background");
            } else {
                runInBackground = false;
                MyTarget.infoMsg("App Does Not Run In The Background");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            inSettingsScreen = false;
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        inSettingsScreen = false;
        finish();
    }

    public static boolean checkRunInBackground() {
        if (inSettingsScreen == true) {
            /*
             * we are in settings screen, let BLE connections keep running
             */
            return true;
        } else if (runInBackground == true) {
            // let app run in background
            return true;
        } else {
            // stop app from running in the background
            return false;
        }
    }
}