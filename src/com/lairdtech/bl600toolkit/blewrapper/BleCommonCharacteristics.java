package com.lairdtech.bl600toolkit.blewrapper;

import java.util.UUID;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.lairdtech.bl600toolkit.target.CommonUiViewsForScreens;

/******************
 * This class is responsible for all the common BLE services 
 * and characteristics.
 * if a screen is going to have all the common services and 
 * characteristics then just create an object
 * of this class inside the screen class and use the merge 
 * methods to merge the specific and the common services/
 * characteristics.
 * For example:
 * mBleCommonCharacteristics = new BleCommonCharacteristics(mCommonUiViewsForFragments);
 * mBleWrapper = new BleWrapper(
 *      this,
 *      this,
 *      mCommonUiViewsForFragments, mBleCommonCharacteristics.mergeSpecificAndCommonServicesUUIDs(SERVICE_UUIDS_TO_SEARCH_DEVICE_FOR),
 *      mBleCommonCharacteristics.mergeSpecificAndCommonCharsUUIDs(CHAR_UUIDS_TO_SEARCH_DEVICE_FOR)
 * );
 * 
 * then you will have to use the mBleCommonCharacteristics 
 * object and call the appropriate methods wherever you need 
 * to use the common functionalities, have a look at the 
 * heartRateFragment.java, proximity.java and the other 
 * services.
 * All screens that communicate with a BLE device have the
 *  battery service and battery level characteristic. if
 *   this class didn't exist then we would have duplicate
 * code throughout our services fragments
 ******************/

public class BleCommonCharacteristics{
    private CommonUiViewsForScreens mCommonUiViewsForScreens;
    
    // what are we looking for
    private final UUID[] COMMON_SERVICE_UUIDS_TO_SEARCH_DEVICE_FOR = {
            BleDefinedUUIDs.Service.BATTERY
            /*
             * add more common services to search device for
             */
    };
    private final UUID[] COMMON_CHARS_UUIDS_TO_SEARCH_DEVICE_FOR = {
            BleDefinedUUIDs.Characteristic.BATTERY_LEVEL
            /*
             * add more common characteristics to search device for
             */
    };
    
    public BleCommonCharacteristics(CommonUiViewsForScreens commonUiViewsForScreens){
        mCommonUiViewsForScreens = commonUiViewsForScreens;
    }
    
    /*
     * merges the given UUID[] with what is in the COMMON_SERVICE_UUIDS_TO_SEARCH_DEVICE_FOR
     */
    public UUID[] mergeSpecificAndCommonServicesUUIDs(UUID[] specificServicesUUIDs){        
        int totalServicesUUIDsSize = specificServicesUUIDs.length + COMMON_SERVICE_UUIDS_TO_SEARCH_DEVICE_FOR.length;
        int specificServicesUUIDsSize = specificServicesUUIDs.length;
        int commonServicesUUIDsSize = COMMON_SERVICE_UUIDS_TO_SEARCH_DEVICE_FOR.length;
        UUID[] allServicesUUIDs = new UUID[totalServicesUUIDsSize];
        
        // copying specific uuids to the array
        for(int i=0; i<specificServicesUUIDsSize; i++ ){
            allServicesUUIDs[i] = specificServicesUUIDs[i];
        }
        // adding common uuids to the array
        for(int i=0; i<commonServicesUUIDsSize; i++ ){
            allServicesUUIDs[specificServicesUUIDsSize] = COMMON_SERVICE_UUIDS_TO_SEARCH_DEVICE_FOR[i];
        }
        
        return allServicesUUIDs;
    }
    
    /*
     * merges the given UUID[] with what is in the COMMON_CHARS_UUIDS_TO_SEARCH_DEVICE_FOR
     */
    public UUID[] mergeSpecificAndCommonCharsUUIDs(UUID[] specificCharsUUIDs){
        int totalCharUUIDsSize = specificCharsUUIDs.length + COMMON_CHARS_UUIDS_TO_SEARCH_DEVICE_FOR.length;
        int specificCharsUUIDsSize = specificCharsUUIDs.length;
        int commonCharsUUIDsSize = COMMON_CHARS_UUIDS_TO_SEARCH_DEVICE_FOR.length;
        UUID[] allCharsUUIDs = new UUID[totalCharUUIDsSize];
        
        // copying specific uuids to the array
        for(int i=0; i<specificCharsUUIDsSize; i++ ){
            allCharsUUIDs[i] = specificCharsUUIDs[i];
        }
        // adding common uuids to the array
        for(int i=0; i<commonCharsUUIDsSize; i++ ){
            allCharsUUIDs[specificCharsUUIDsSize] = COMMON_CHARS_UUIDS_TO_SEARCH_DEVICE_FOR[i];
        }
        return allCharsUUIDs;
    }
    
    public void commonOnReadRemoteRssi(BluetoothGatt gatt, final int rssi){        
        mCommonUiViewsForScreens.setTvRssiValue(rssi + " db");
        /*
         * more common things to do when the rssi is read
         */
    }
    
    // all common read characteristics operations here
    public void commonOnCharacteristicRead(BluetoothGatt gatt,
            BluetoothGattCharacteristic characteristic){
        UUID serviceUUID = characteristic.getService().getUuid();
        UUID charUUID = characteristic.getUuid();

        if(BleDefinedUUIDs.Service.BATTERY.equals(serviceUUID)){
            if(BleDefinedUUIDs.Characteristic.BATTERY_LEVEL.equals(charUUID)){
                final int result;            
                result = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                mCommonUiViewsForScreens.setTvBatteryValue(result + "%");
            }
        }
        /*
         * more common characteristic reads here 
         */
    }
    
    // all common notifications/indications characteristics reads here
    public void commonOnCharacteristicChanged(BluetoothGatt gatt,
            BluetoothGattCharacteristic characteristic){
        /*
         * common characteristics notifications or indications here
         */   
    }
}