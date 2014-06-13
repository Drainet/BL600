package com.lairdtech.bl600toolkit.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lairdtech.bl600toolkit.R;
import com.lairdtech.bl600toolkit.activities.SettingsActivity;
import com.lairdtech.bl600toolkit.target.MyTarget;

public class ChoiceFragment extends Fragment implements OnTouchListener, OnClickListener {
    private ViewPager mPager;
    private ImageView imgHeartRate;
    private ImageView imgTemp;
    private ImageView imgBloodPressure;
    private ImageView imgProximity;
    private ImageView imgContactUs;
    private ImageView imgSettings;

    public void setPageAdapterForButtons(ViewPager mPager) {
        this.mPager = mPager;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        // View needed to get textViews and progress bar
        View mFragmentView = inflater.inflate(R.layout.fragment_choices, container, false);

        /*
         * Higher API levels will not display the logo correctly without this
         * change. Does not exist in lower API targets
        */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) mFragmentView.findViewById(R.id.logoLaird).setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        bindViews(mFragmentView);
        setListeners();

        return mFragmentView;
    }
    
    public void bindViews(View view) {
        imgHeartRate = (ImageView) view.findViewById(R.id.iconServiceHeartRate);
        imgTemp = (ImageView) view.findViewById(R.id.iconServiceTemp);
        imgBloodPressure = (ImageView) view.findViewById(R.id.iconServiceBloodPressure);
        imgProximity = (ImageView) view.findViewById(R.id.iconServiceProximity);
        imgContactUs = (ImageView) view.findViewById(R.id.iconContactUs);
        imgSettings = (ImageView) view.findViewById(R.id.iconSettings);
    }
    
    public void setListeners() {
        imgHeartRate.setOnClickListener(this);
        imgTemp.setOnClickListener(this);
        imgBloodPressure.setOnClickListener(this);
        imgProximity.setOnClickListener(this);
        imgContactUs.setOnClickListener(this);
        imgSettings.setOnClickListener(this);

        imgHeartRate.setOnTouchListener(this);
        imgTemp.setOnTouchListener(this);
        imgBloodPressure.setOnTouchListener(this);
        imgProximity.setOnTouchListener(this);
        imgContactUs.setOnTouchListener(this);
        imgSettings.setOnTouchListener(this);
    }
    
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        ImageView currentImageView = (ImageView) view;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // on touch
            currentImageView.setColorFilter(Color.parseColor("#50000000"));
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            // on release
            currentImageView.setColorFilter(Color.TRANSPARENT);
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        int selectedViewId = view.getId();
        Intent intent;

        switch (selectedViewId) {
        case R.id.iconServiceHeartRate:
            MyTarget.infoMsg("Button Selected: " + getString(R.string.heart_rate));
            mPager.setCurrentItem(1);
            break;

        case R.id.iconServiceTemp:
            MyTarget.infoMsg("Button Selected: " + getString(R.string.temperature));
            mPager.setCurrentItem(2);
            break;

        case R.id.iconServiceBloodPressure:
            MyTarget.infoMsg("Button Selected: " + getString(R.string.blood_pressure));
            mPager.setCurrentItem(3);
            break;

        case R.id.iconServiceProximity:
            MyTarget.infoMsg("Button Selected: " + getString(R.string.proximity));
            mPager.setCurrentItem(4);
            break;

        case R.id.iconContactUs:
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.lairdtech.com/about-laird-technologies/contact-us/"));
            startActivity(intent);
            break;
        case R.id.iconSettings:
            SettingsActivity.inSettingsScreen = true;
            intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
            break;
        }
    }
}