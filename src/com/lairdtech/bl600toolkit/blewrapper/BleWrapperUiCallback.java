package com.lairdtech.bl600toolkit.blewrapper;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;


public interface BleWrapperUiCallback {
    public void uiStartScanning();   
    public void uiStopScanning(); 
    public void uiDeviceConnected(final BluetoothGatt gatt);
    public void uiOnOperationsFinished();
    public void uiDeviceDisconnected(final BluetoothGatt gatt);
    public void uiOnReadRemoteRssi(
            final BluetoothGatt gatt,
            final int rssi,
            final int status);
    public void uiOnCharacteristicRead(
            BluetoothGatt gatt,
            final BluetoothGattCharacteristic characteristic,
            final int status);
    public void uiOnCharacteristicChanged(
            BluetoothGatt gatt,
            final BluetoothGattCharacteristic characteristic);
    public void storeCharacteristics(
            BluetoothGattService service,
            BluetoothGattCharacteristic ch);
}