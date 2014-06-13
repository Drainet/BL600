package com.lairdtech.bl600toolkit.target;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lairdtech.bl600toolkit.R;
import com.lairdtech.bl600toolkit.blewrapper.BleWrapper;

/******************
 * This method is responsible for all the common views that exist throughout all the fragments,
 * for example all fragments display the device name, rssi, battery and a connect button
 * 
 * in this way we eliminate duplicate code and it's easier to maintain as all the common views are in one place.
 * Let's say you want to display the address of the BLE device to all the fragments (heart rate, temperature, blood pressure etc.)
 * 		1) add the new view in the common_views.xml layout
 * 		2) add the new view object in this class and setup it where appropriate.
 ******************/

public class CommonUiViewsForScreens implements OnClickListener{
    private BleWrapper mBleWrapper;
    private LayoutSetupInterface mLayoutSetupInterface;
    private Activity mActivity;
    private View mView;
    private TextView mTvDeviceNameValue;
    private TextView mTvRssiValue;
    private TextView mTvBatteryValue;
    private Button btnConnect;
    private ProgressBar pbSpinnerReadingData;
    
    public CommonUiViewsForScreens(View view, Activity activity, LayoutSetupInterface layoutSetupInterface){
        mView = view;
        mActivity = activity;
        mLayoutSetupInterface = layoutSetupInterface;
    }
    
    public void setBleWrapper(BleWrapper bleWrapper) {
        mBleWrapper = bleWrapper;
    }
    
    public void setTvDeviceNameValue(final String deviceNameValue) {
    	mActivity.runOnUiThread(new Runnable() {
    		@Override
    		public void run() {
    			mTvDeviceNameValue.setText(deviceNameValue);
    		}
    	});
    }
    
    public void setTvRssiValue(final String rssiValue) {
    	mActivity.runOnUiThread(new Runnable() {
    		@Override
    		public void run() {
    	        mTvRssiValue.setText(rssiValue);
    		}
    	});
    }
    
    public void setTvBatteryValue(final String batteryDeviceValue) {
    	mActivity.runOnUiThread(new Runnable() {
    		@Override
    		public void run() {
    	        mTvBatteryValue.setText(batteryDeviceValue);
    		}
    	});
    }
    
    public void bindViews(View view){
        MyTarget.debugMsg("bindViews of CommonUiViewsForScreens");
        mTvDeviceNameValue = (TextView) mView.findViewById(R.id.valueDeviceName);
        mTvRssiValue = (TextView) mView.findViewById(R.id.valueDeviceRssi);
        mTvBatteryValue = (TextView) mView.findViewById(R.id.valueBattery);
        btnConnect = (Button) mView.findViewById(R.id.btnConnect);
        pbSpinnerReadingData = (ProgressBar) mView.findViewById(R.id.pbSpinnerReadingData);
    }
	
    public void setDefaultViewValues(){
    	mActivity.runOnUiThread(new Runnable() {
    		@Override
    		public void run() {
    	        mTvDeviceNameValue.setText(R.string.no_multiple_data_found);
    	        mTvRssiValue.setText(R.string.no_multiple_data_found);
    	        mTvBatteryValue.setText(R.string.no_multiple_data_found);
    	        pbSpinnerReadingData.setVisibility(View.INVISIBLE);
    		}
    	});
    }
	
	public void setListeners() {
        btnConnect.setOnClickListener(this);
	}
	
    public void uiInvalidateBtnState(){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyTarget.debugMsg("invalidateBtnState UI THREAD");
                if(mBleWrapper.isScanning() == false && mBleWrapper.isConnecting() == false && mBleWrapper.isConnected() == false){
                    // connect with device
                    btnConnect.setText(mActivity.getString(R.string.connect));
                    pbSpinnerReadingData.setVisibility(View.INVISIBLE);
                } else if(mBleWrapper.isScanning() == true && mBleWrapper.isConnecting() == false && mBleWrapper.isConnected() == false){
                    // scanning for devices
                    btnConnect.setText(mActivity.getString(R.string.searching));
                    pbSpinnerReadingData.setVisibility(View.VISIBLE);
                } else if(mBleWrapper.isScanning() == false && mBleWrapper.isConnecting() == true && mBleWrapper.isConnected() == false){
                    // connecting with device
                    btnConnect.setText(mActivity.getString(R.string.connecting));
                    pbSpinnerReadingData.setVisibility(View.VISIBLE);
                } else if(mBleWrapper.isScanning() == false && mBleWrapper.isOperationsFinished() == false && mBleWrapper.isConnected() == true){
                    // retrieving device data
                    btnConnect.setText(mActivity.getString(R.string.retrieving_data));
                    pbSpinnerReadingData.setVisibility(View.VISIBLE);
                } else if(mBleWrapper.isScanning() == false && mBleWrapper.isOperationsFinished() == true && mBleWrapper.isConnected() == true){
                    // disconnect
                    btnConnect.setText(mActivity.getString(R.string.disconnect));
                    pbSpinnerReadingData.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
    
    @Override
    public void onClick(View view){
        int btnId = view.getId();
        switch(btnId){
        case R.id.btnConnect:
            if(mBleWrapper.isScanning() == false && mBleWrapper.isConnected() == false){
                MyTarget.debugMsg("onClick START SEARCHING FOR DEVICES");
                mBleWrapper.startScanning();
            } else if(mBleWrapper.isScanning() == true && mBleWrapper.isConnected() == false){
                MyTarget.debugMsg("onClick STOP SEARCHING FOR DEVICES");
                mBleWrapper.stopScanning();
            } else if(mBleWrapper.isScanning() == false && mBleWrapper.isOperationsFinished() == false){
                MyTarget.infoMsg("onClick wait for read/write operations to finish");
                MyTarget.toastMsg(mActivity, "wait for the process to finish");
            } else if(mBleWrapper.isScanning() == false && mBleWrapper.isOperationsFinished() == true && mBleWrapper.isConnecting() == false && mBleWrapper.isConnected() == true){
                MyTarget.infoMsg("onClick start disconnecting from device");
                mBleWrapper.disconnect();
                uiInvalidateBtnState();
                mLayoutSetupInterface.setDefaultViewValues();
            }
        }
    }
}