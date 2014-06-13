package com.lairdtech.bl600toolkit.fragments;

import java.util.UUID;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.lairdtech.bl600toolkit.R;
import com.lairdtech.bl600toolkit.activities.SettingsActivity;
import com.lairdtech.bl600toolkit.blewrapper.BleCommonCharacteristics;
import com.lairdtech.bl600toolkit.blewrapper.BleDefinedUUIDs;
import com.lairdtech.bl600toolkit.blewrapper.BleWrapper;
import com.lairdtech.bl600toolkit.blewrapper.BleWrapperUiCallback;
import com.lairdtech.bl600toolkit.target.CommonUiViewsForScreens;
import com.lairdtech.bl600toolkit.target.LayoutSetupInterface;
import com.lairdtech.bl600toolkit.target.MyTarget;

public class ProximityFragment extends Fragment implements BleWrapperUiCallback, LayoutSetupInterface, OnClickListener, OnCheckedChangeListener{
    private BleWrapper mBleWrapper;
    private CommonUiViewsForScreens mCommonUiViewsForScreens;
    private BleCommonCharacteristics mBleCommonCharacteristics;
    private BluetoothGattCharacteristic mBluetoothGattCharImmediateAlert;
    private BluetoothGattCharacteristic mBluetoothGattCharLinkLoss;  
    private TextView tvTxPowerValue;  
    private RadioGroup radioGroupLinkLoss;
    private RadioGroup radioGroupImmediateAlert;
    private Button btnImmediateAlert;
    // what are we looking for
    private final UUID[] SERVICE_UUIDS_TO_SEARCH_DEVICE_FOR = {
            BleDefinedUUIDs.Service.LINK_LOSS,
            BleDefinedUUIDs.Service.IMMEDIATE_ALERT,
            BleDefinedUUIDs.Service.TX_POWER,
    };
    private final UUID[] CHAR_UUIDS_TO_SEARCH_DEVICE_FOR = {
            BleDefinedUUIDs.Characteristic.ALERT_LEVEL,
            BleDefinedUUIDs.Characteristic.TX_POWER_LEVEL,
    };
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {        
        View view = inflater.inflate(R.layout.fragment_proximity, container, false);        
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
    public void bindViews(View view){
        mCommonUiViewsForScreens.bindViews(view);
        tvTxPowerValue = (TextView) view.findViewById(R.id.valueTxPower);
        radioGroupLinkLoss = (RadioGroup) view.findViewById(R.id.radioGroupLinkLoss);
        radioGroupImmediateAlert = (RadioGroup) view.findViewById(R.id.radioGroupImmediateAlert);
        btnImmediateAlert = (Button) view.findViewById(R.id.btnImmediateAlert);        
    }
    
    public void setDefaultViewValues(){        
        mCommonUiViewsForScreens.setDefaultViewValues();
        
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvTxPowerValue.setText(" " + getString(R.string.no_multiple_data_found));
            }
        });
        /*
         * do more specific stuff for this class here
         */
    }
    
    @Override
    public void setListeners(){
        mCommonUiViewsForScreens.setListeners();
        btnImmediateAlert.setOnClickListener(this);
        radioGroupLinkLoss.setOnCheckedChangeListener(this);
        radioGroupImmediateAlert.setOnCheckedChangeListener(this);
        /*
         * do more specific stuff for this class here
         */
    }
    
    @Override
    public void uiInvalidateBtnState(){
        mCommonUiViewsForScreens.uiInvalidateBtnState();
        /*
         * do specific stuff for this class here
         */
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
        /*
         * do specific stuff for this class here
         */
    }

    @Override
    public void uiOnReadRemoteRssi(final BluetoothGatt gatt,
            final int rssi,
            final int status){
    	
        if(status == BluetoothGatt.GATT_SUCCESS) {
            mCommonUiViewsForScreens.setTvRssiValue(rssi + " db");
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
            UUID serviceUUID = characteristic.getService().getUuid();
            UUID charUUID = characteristic.getUuid();
            
            mBleCommonCharacteristics.commonOnCharacteristicRead(gatt, characteristic);
            
            if(BleDefinedUUIDs.Service.TX_POWER.equals(serviceUUID)){
                if(BleDefinedUUIDs.Characteristic.TX_POWER_LEVEL.equals(charUUID)){
                    final int result = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT8, 0);
                    MyTarget.infoMsg("Proximity Tx Power: " + result);
                    
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvTxPowerValue.setText(" " + result);
                        }
                    });
                }
            }
            /*
             * more specific characteristic reads if necessary here
             */
        }
    }
    
    @Override
    public void uiOnCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){        
        /*
         * notifications here if necessary
         */
    }
    
// other
    @Override
    public void onClick(View view){
        int btnId = view.getId();
        switch(btnId){
        case R.id.btnImmediateAlert:
            int checkedRadioBtn = radioGroupImmediateAlert.getCheckedRadioButtonId();
            
            if(checkedRadioBtn == R.id.radioImmediateAlertLow){
                // low value chosen for Immediate Alert
                writeAlertCharValue("0x00", mBluetoothGattCharImmediateAlert);
            } else if(checkedRadioBtn == R.id.radioImmediateAlertMedium){
                // medium value chosen for Immediate Alert
                writeAlertCharValue("0x01", mBluetoothGattCharImmediateAlert);
            } else if(checkedRadioBtn == R.id.radioImmediateAlertHigh){
                // high value chosen for Immediate Alert
                writeAlertCharValue("0x02", mBluetoothGattCharImmediateAlert);
            } else {
                // no radio button is chosen yet
            }
            break;
        }
    }
    
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int radioGroupId = group.getId();
        RadioButton radioButton;
        int radioButtonId;
        // get selected radio button
        radioButton = (RadioButton) group.findViewById(checkedId);
        radioButtonId = radioButton.getId();
        
        switch(radioGroupId){
            case R.id.radioGroupLinkLoss:
                if(radioButtonId == R.id.radioLinkLossAlertLow){
                    // low value chosen for Link loss
                    writeAlertCharValue("0x00", mBluetoothGattCharLinkLoss);
                } else if(radioButtonId == R.id.radioLinkLossAlertMedium){
                    // medium value chosen for Link loss
                    writeAlertCharValue("0x01", mBluetoothGattCharLinkLoss);
                } else if(radioButtonId == R.id.radioLinkLossAlertHigh){
                    // high value chosen for Link loss
                    writeAlertCharValue("0x02", mBluetoothGattCharLinkLoss);
                } else{
                    // no radio button is checked from this radio group
                }
                break;
        }
    }
    
    @Override
    public void storeCharacteristics(BluetoothGattService service, BluetoothGattCharacteristic ch){
        UUID charUUID = ch.getUuid();
        UUID serviceUUID = service.getUuid();
        
        if(BleDefinedUUIDs.Service.LINK_LOSS.equals(serviceUUID)){
            if(BleDefinedUUIDs.Characteristic.ALERT_LEVEL.equals(charUUID)){
                mBluetoothGattCharLinkLoss = ch;
            }
        } else if(BleDefinedUUIDs.Service.IMMEDIATE_ALERT.equals(serviceUUID)){
            if(BleDefinedUUIDs.Characteristic.ALERT_LEVEL.equals(charUUID)){
                mBluetoothGattCharImmediateAlert = ch;
            }
        }
        /*
         * store more characteristics if necessary here        
         */
    }
    
    public void writeAlertCharValue(final String hex, final BluetoothGattCharacteristic alertChar){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyTarget.infoMsg("Alert characteristic set value");
                if(alertChar == null) return;
                // first set it locally....                
                alertChar.setValue(mBleWrapper.parseHexStringToBytes(hex));
                // ... and then "commit" changes to the peripheral
                mBleWrapper.writeCharacteristic(alertChar);
            }
        });
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