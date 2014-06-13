package com.lairdtech.bl600toolkit.fragments;

import java.util.UUID;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lairdtech.bl600toolkit.R;
import com.lairdtech.bl600toolkit.activities.SettingsActivity;
import com.lairdtech.bl600toolkit.blewrapper.BleCommonCharacteristics;
import com.lairdtech.bl600toolkit.blewrapper.BleDefinedUUIDs;
import com.lairdtech.bl600toolkit.blewrapper.BleWrapper;
import com.lairdtech.bl600toolkit.blewrapper.BleWrapperUiCallback;
import com.lairdtech.bl600toolkit.graphs.BloodPressureGraph;
import com.lairdtech.bl600toolkit.target.CommonUiViewsForScreens;
import com.lairdtech.bl600toolkit.target.LayoutSetupInterface;
import com.lairdtech.bl600toolkit.target.MyTarget;

public class BloodPressureFragment extends Fragment implements BleWrapperUiCallback, LayoutSetupInterface{
    private BleWrapper mBleWrapper;
    private CommonUiViewsForScreens mCommonUiViewsForScreens;
    private BleCommonCharacteristics mBleCommonCharacteristics;
    private TextView tvSystolicValue;
    private TextView tvDiastolicValue;
    private TextView tvArterialPressureValue;
    private BloodPressureGraph mGraph;
    // what are we looking for    
    private final UUID[] SERVICE_UUIDS_TO_SEARCH_DEVICE_FOR = {
            BleDefinedUUIDs.Service.BLOOD_PRESSURE
    };
    private final UUID[] CHAR_UUIDS_TO_SEARCH_DEVICE_FOR = {
            BleDefinedUUIDs.Characteristic.BLOOD_PRESSURE_MEASUREMENT
    };
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {        
        View view = inflater.inflate(R.layout.fragment_blood_pressure, container, false);
        mCommonUiViewsForScreens = new CommonUiViewsForScreens(view, getActivity(), this);
        mBleCommonCharacteristics = new BleCommonCharacteristics(mCommonUiViewsForScreens);
        mBleWrapper = new BleWrapper(
                getActivity(),
                this,
                mBleCommonCharacteristics.mergeSpecificAndCommonServicesUUIDs(SERVICE_UUIDS_TO_SEARCH_DEVICE_FOR),
                mBleCommonCharacteristics.mergeSpecificAndCommonCharsUUIDs(CHAR_UUIDS_TO_SEARCH_DEVICE_FOR)
                );
        mBleWrapper.initialize();
        mCommonUiViewsForScreens.setBleWrapper(mBleWrapper);
        
        bindViews(view);
        setDefaultViewValues();
        setListeners();
        
        return view;    
    }
    
    
// LayoutSetupInterface
    @Override
    public void bindViews(View view){
        mCommonUiViewsForScreens.bindViews(view);
        tvSystolicValue = (TextView) view.findViewById(R.id.valueSystolic);
        tvDiastolicValue = (TextView) view.findViewById(R.id.valueDiastolic);
        tvArterialPressureValue = (TextView) view.findViewById(R.id.valueArterialPressure);
        mGraph = new BloodPressureGraph(getActivity().getApplicationContext(), view);
    }

    @Override
    public void setDefaultViewValues(){
        mCommonUiViewsForScreens.setDefaultViewValues();
        
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvSystolicValue.setText(R.string.no_multiple_data_found);
                tvDiastolicValue.setText(R.string.no_multiple_data_found);
                tvArterialPressureValue.setText(R.string.no_multiple_data_found);
                
                if(mGraph != null){
                	mGraph.clearGraph();
                }
            }
        });
        /*
         * do more specific stuff for this class here
         */
    }
    
	@Override
    public void setListeners(){
	    mCommonUiViewsForScreens.setListeners();
        /*
         * do specific stuff for this class here
         */
    }
    
	@Override
    public void uiInvalidateBtnState(){
	    mCommonUiViewsForScreens.uiInvalidateBtnState();
	}
    
// UiBleWrapperCallback callback's
    @Override
    public void uiStartScanning(){
    	uiInvalidateBtnState();
    }
    
    @Override
    public void uiStopScanning(){
    	uiInvalidateBtnState();
    }
    
    @Override
    public void uiDeviceConnected(final BluetoothGatt gatt){
        mCommonUiViewsForScreens.setTvDeviceNameValue(gatt.getDevice().getName());
        uiInvalidateBtnState();
        /*
         * do specific stuff for this class here
         */
    }
    
    @Override
    public void uiOnOperationsFinished(){
        mCommonUiViewsForScreens.uiInvalidateBtnState();
        /*
         * do specific stuff for this class here
         */
    }
    
    @Override
    public void uiDeviceDisconnected(final BluetoothGatt gatt){
        uiInvalidateBtnState();
        setDefaultViewValues();
        mGraph.setStartTime(0);
        /*
         * do specific stuff for this class here
         */
    }
    
    @Override
    public void uiOnReadRemoteRssi(final BluetoothGatt gatt,
            final int rssi,
            final int status){
    	
        if(status == BluetoothGatt.GATT_SUCCESS) {
            mBleCommonCharacteristics.commonOnReadRemoteRssi(gatt, rssi);
            /*
             * do specific stuff for this class here
             */
        }
    }
    
    @Override
    public void uiOnCharacteristicRead(final BluetoothGatt gatt,
            final BluetoothGattCharacteristic characteristic,
            final int status){
        
        if(status == BluetoothGatt.GATT_SUCCESS){            
            mBleCommonCharacteristics.commonOnCharacteristicRead(gatt, characteristic);
            /*
             * specific characteristic reads if necessary here
             */
        }
    }
    
    @Override
    public void uiOnCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
        UUID charUUID = characteristic.getUuid();

        if(BleDefinedUUIDs.Characteristic.BLOOD_PRESSURE_MEASUREMENT.equals(charUUID)){
            final float systolicResult;
            final float diastolicResult;
            final float arterialPressureResult;
            systolicResult = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, 1);
            diastolicResult = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, 3);
            arterialPressureResult = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SFLOAT, 5);
            final float[] values = {
                    systolicResult,
                    diastolicResult,
                    arterialPressureResult
            };
            MyTarget.infoMsg("Blood Pressure, Systolic: " + systolicResult +
                    " Diastolic: " + diastolicResult +
                    " Arterial Pressure: " + arterialPressureResult);
            
            mGraph.startTimer();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvSystolicValue.setText("" + systolicResult);
                    tvDiastolicValue.setText("" + diastolicResult);
                    tvArterialPressureValue.setText("" + arterialPressureResult);
                    mGraph.addNewData(values[0], values[1], values[2]);
                }
            });
        }
        /*
         * set more notification reads here if necessary
         */
    }
    
 // other
    @Override
    public void storeCharacteristics(BluetoothGattService service, BluetoothGattCharacteristic ch){
        /*
         * store characteristics if necessary here        
         */
    }
    
    @Override
    public void onPause(){
        super.onPause();        
        if(SettingsActivity.checkRunInBackground() == true){
            // do nothing and just let the app run in the background
        } else if(mBleWrapper.isConnected() == true){
            mBleWrapper.disconnect();
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
    }
}