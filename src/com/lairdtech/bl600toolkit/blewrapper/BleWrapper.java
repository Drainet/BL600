package com.lairdtech.bl600toolkit.blewrapper;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;

import com.lairdtech.bl600toolkit.target.MyTarget;

public class BleWrapper {
    public static final long SCANNING_TIMEOUT = 5000;
    public static final long CHARACTERISTIC_OPERATION_TIMER = 2000;
    public static final int RSSI_UPDATE_TIME_INTERVAL = 1500;
    public static final int ENABLE_NOTIFICATIONS = 0;
    public static final int ENABLE_INDICATIONS = 1;
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice  mBluetoothDevice = null;
    private BluetoothGatt    mBluetoothGatt = null;
    private BleWrapperUiCallback mUiBleWrapperCallback;
    private UUID[] mServicesToSearchDeviceFor;
    private UUID[] mCharsToSearchDeviceFor;
    /*
     * when writing to a descriptor no other write or read operations should be initiated,
     * if this happens then some of the notifications or read characteristics operation will not work.
     * Queues are used so that only when an operation is finished the next one will get called.
     */
    private Queue<BluetoothGattDescriptor> writeDescriptorQueue = new LinkedList<BluetoothGattDescriptor>();
    private Queue<BluetoothGattDescriptor> writeDescriptorQueueForDisabling = new LinkedList<BluetoothGattDescriptor>();
    private Queue<BluetoothGattCharacteristic> readCharacteristicQueue = new LinkedList<BluetoothGattCharacteristic>();
    private boolean mScanning = false;
    private boolean mConnected = false;
    private boolean mConnecting = false;
    /*
     * to make sure that the user is only able to disconnect after
     * the write and read operations are finished. If the user disconnected while write/read
     * operations where on the process then unexpected errors would occur
     */
    private boolean mOperationsFinished = false;
    /*
     * When mDisconnecting is true we know that we are disconnecting from the BLE device,
     * but if it's false we don't know that the device is currently disconnecting.
     * 
     * For example, if the device gets out of range then mDisconnecting will be false
     * at the time the two devices get disconnected.
     * If the disconnect button is pressed, then mDisconnecting will be true
     */
    private boolean mDisconnecting = false;
    private Handler mScanningTimeoutHandler = new Handler();
    private Handler mRssiTimerHandler = new Handler();
    private boolean mRssiTimerEnabled = false;
    private Context mContext;
    private Activity mActivity;
    
    // getters
    public BluetoothDevice getBluetoothDevice(){return mBluetoothDevice;}
    public BluetoothGatt getBluetoothGatt(){return mBluetoothGatt;}
    public boolean isScanning(){return mScanning;}
    public boolean isConnected(){return mConnected;}
    public boolean isConnecting(){return mConnecting;}
    public boolean isOperationsFinished(){return mOperationsFinished;}
    public boolean isDisconnecting(){return mDisconnecting;}
    // setters
    public void setDisconnecting(boolean disconnecting){mDisconnecting = disconnecting;}

    /* 
     * make sure that potential scanning will take no longer
     * than <SCANNING_TIMEOUT> seconds from now on 
     */
    private void addScanningTimeout() {
        Runnable timeout = new Runnable() {
            @Override
            public void run() {
                stopScanning();
            }
        };
        mScanningTimeoutHandler.postDelayed(timeout, SCANNING_TIMEOUT);
    }

    public BleWrapper(Activity activity, BleWrapperUiCallback uiBleWrapperCallback, UUID[] servicesToSearchDeviceFor, UUID[] charsToSearchDeviceFor){
        mUiBleWrapperCallback = uiBleWrapperCallback;
        mActivity = activity;
        mContext = mActivity.getApplicationContext();
        mServicesToSearchDeviceFor = servicesToSearchDeviceFor;
        mCharsToSearchDeviceFor = charsToSearchDeviceFor;
    }

    public void startScanning(){
        if(mScanning == false){
            if(isBtEnabled() == false) {
                MyTarget.toastMsg(mActivity, "Bluetooth is not enabled");
                return;
            }
            MyTarget.debugMsg("BleWrapper - Start Scanning");
            mScanning = true;
            addScanningTimeout();
            mBluetoothAdapter.startLeScan(mServicesToSearchDeviceFor, mDeviceFoundCallback); // callback: onLeScan
            mUiBleWrapperCallback.uiStartScanning();
        }
    }

    public void stopScanning(){
        if(mScanning == true){
            MyTarget.debugMsg("BleWrapper - Stop Scanning");
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mDeviceFoundCallback);
            mUiBleWrapperCallback.uiStopScanning();
        }
    }

    public void disconnect(){
        MyTarget.debugMsg("BleWrapper - disconnect");
        mDisconnecting = true;
        if(writeDescriptorQueueForDisabling.size() > 0){
            addOrRemoveNotificationsOrIndicationsForCharacteristics(writeDescriptorQueueForDisabling.element().getCharacteristic().getService(), writeDescriptorQueueForDisabling.element().getCharacteristic(), false);
        } else{
            mBluetoothGatt.disconnect(); //callback --> onConnectionStateChange
        }
    }

    /* initialize BLE and get BT Manager & Adapter */
    public boolean initialize() {
        MyTarget.debugMsg("BleWrapper - initialize");
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return false;
            }
        }

        if(mBluetoothAdapter == null) mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        }
        return true;
    }

    // request new RSSI value for the connection
    public void readPeriodicalyRssiValue(final boolean repeat) {
        mRssiTimerEnabled = repeat;
        // check if we should stop checking RSSI value
        if(mConnected == false || mBluetoothGatt == null || mRssiTimerEnabled == false) {
            mRssiTimerEnabled = false;
            return;
        }

        mRssiTimerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mBluetoothGatt == null || mBluetoothAdapter == null || mConnected == false){
                    mRssiTimerEnabled = false;
                    return;
                }

                // request RSSI value
                mBluetoothGatt.readRemoteRssi(); // callback: onReadRemoteRssi
                // and call it once more in the future
                readPeriodicalyRssiValue(mRssiTimerEnabled);
            }
        }, RSSI_UPDATE_TIME_INTERVAL);
    }
    
    /*
     * enables/disables notifications/indications for characteristic
     */
    public void setNotificationsForCharacteristic(BluetoothGattCharacteristic ch, boolean enabled, int notificationOrIndication) {
        MyTarget.debugMsg("BleWrapper - setNotificationsForCharacteristic --> char: " + BleNamesResolver.resolveCharacteristicName("" + ch.getUuid()) +
                " enabled: " + enabled +
                " notificationOrIndication: " + notificationOrIndication);
        if (mBluetoothAdapter == null || mBluetoothGatt == null) return;   
        
        boolean success = mBluetoothGatt.setCharacteristicNotification(ch, enabled);
        if(!success) {
            MyTarget.errorMsg("BleWrapper - Setting proper notification status for characteristic failed!");
            return;
        }
        addWriteDescriptorToQueue(ch, enabled, notificationOrIndication);
    }
    
    /*
     * Responsible for adding the appropriate descriptor to the queue for writing.
     * The parameter "enabled" if it's true it enables notifications/indications else it disables notifications/indications
     * The parameter "notificationOrIndication" if it's 0 it will write for notifications. if it's 1 it will write for indications
     */
    public void addWriteDescriptorToQueue(BluetoothGattCharacteristic ch, boolean enabled, int notificationOrIndication){
        MyTarget.debugMsg("BleWrapper - setWriteDescriptor");
        MyTarget.infoMsg("setWriteDescriptor - For characteristic with UUID: " + ch.getUuid() +
                " and name: " + BleNamesResolver.resolveCharacteristicName("" + ch.getUuid()) +
                " enabled: " + enabled +
                " notificationOrIndication: " + notificationOrIndication);
        // This is also sometimes required (e.g. for heart rate monitors) to enable notifications/indications
        // see: https://developer.bluetooth.org/gatt/descriptors/Pages/DescriptorViewer.aspx?u=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml
        BluetoothGattDescriptor descriptor = ch.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        if(descriptor == null) return;
        
        if(notificationOrIndication == BleWrapper.ENABLE_NOTIFICATIONS){
            // set notifications, heart rate measurement etc
            byte[] val = enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            descriptor.setValue(val);
        } else if(notificationOrIndication == BleWrapper.ENABLE_INDICATIONS){
            // set indications, temperature measurement, blood pressure measurement etc
            byte[] val = enabled ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            descriptor.setValue(val);
        }
        
        if(mDisconnecting == false){
            writeDescriptorQueue.add(descriptor);
        } else{
            initiateWriteDescriptorForDisablingNotificationsOrIndications();
        }
    }

    /*
     * while a writeDescriptor or a readCharacteristic operation is taking place and another writeDescriptor or 
     * ReadCharacteristic operation gets initiated then unexpected issues will occur.
     * For this reason we add the writeDescriptor and readCharacteristic operations in queues
     * and they get called one by one immediately at the end of an operation until no operations are left
     */
    public void initiateWriteDescriptorAndReadCharacteristic() {
        MyTarget.debugMsg("BleWrapper - initiateWriteDescriptorAndReadCharacteristic");
        
        int counter = 0;
        
        MyTarget.infoMsg("writeDescriptorQueue total to write: " + writeDescriptorQueue.size());
        Iterator<BluetoothGattDescriptor> writeDescriptorIter = writeDescriptorQueue.iterator();
        while(writeDescriptorIter.hasNext()){
            counter++;
            BluetoothGattDescriptor temp = writeDescriptorIter.next();
            MyTarget.infoMsg("writeDescriptorQueue list: " + counter + " with characteristic UUID: " + temp.getCharacteristic().getUuid() +
                    " and name: " + BleNamesResolver.resolveCharacteristicName("" + temp.getCharacteristic().getUuid()));
        }
        MyTarget.infoMsg("readCharacteristicQueue total to read: " + readCharacteristicQueue.size());            
        counter = 0;
        Iterator<BluetoothGattCharacteristic> readCharacteristicIter = readCharacteristicQueue.iterator();
        while(readCharacteristicIter.hasNext()){
            counter++;
            BluetoothGattCharacteristic temp = readCharacteristicIter.next();
            MyTarget.infoMsg("readCharacteristicQueue list: " + counter + " with characteristic UUID: " + temp.getUuid() +
                    " and name: " + BleNamesResolver.resolveCharacteristicName("" + temp.getUuid()));
        }

        // if there are more descriptors to write, write them
        if(writeDescriptorQueue.size() > 0){
            MyTarget.infoMsg("Writing descriptor for enabling characteristic with UUID: " + writeDescriptorQueue.element().getCharacteristic().getUuid() +
                    " and name: " + BleNamesResolver.resolveCharacteristicName("" + writeDescriptorQueue.element().getCharacteristic().getUuid()));
            boolean writeDescriptorSuccess = mBluetoothGatt.writeDescriptor(writeDescriptorQueue.element()); // callback: BleWrapper-->onDescriptorWrite
            MyTarget.infoMsg("writeDescriptor initiated?: " + writeDescriptorSuccess);

        }
        // done with the writeDescriptor operations, start readCharacteristics operations
        else if(readCharacteristicQueue.size() > 0){
            MyTarget.infoMsg("reading Characteristic with UUID: " + readCharacteristicQueue.element().getUuid() +
                    " and name: " + BleNamesResolver.resolveCharacteristicName("" + readCharacteristicQueue.element().getUuid()));
            boolean readCharacteristicSuccess = mBluetoothGatt.readCharacteristic(readCharacteristicQueue.element()); // callback: BleWrapper-->onCharacteristicRead
            MyTarget.infoMsg("readCharacteristic initiated?: " + readCharacteristicSuccess);
        } else{
            mOperationsFinished = true;

            mUiBleWrapperCallback.uiOnOperationsFinished();           
        }
    }

    public void initiateWriteDescriptorForDisablingNotificationsOrIndications() {
        MyTarget.debugMsg("BleWrapper - initiateWriteDescriptorForDisablingNotificationsOrIndications");

        MyTarget.infoMsg("writeDescriptorQueueForDisabling total to write: " + writeDescriptorQueueForDisabling.size());
        int counter = 0;
        Iterator<BluetoothGattDescriptor> writeDescriptorQueueForDisablingIter = writeDescriptorQueueForDisabling.iterator();
        while(writeDescriptorQueueForDisablingIter.hasNext()){
            counter++;
            BluetoothGattDescriptor temp = writeDescriptorQueueForDisablingIter.next();            
            MyTarget.infoMsg("writeDescriptorQueueForDisabling characteristic with UUID: " + counter + " " + temp.getCharacteristic().getUuid() +
                    " and name: " + BleNamesResolver.resolveCharacteristicName("" + temp.getCharacteristic().getUuid()));
        }

        // if there is more descriptors to write, write them
        if(writeDescriptorQueueForDisabling.size() > 0){
            MyTarget.infoMsg("Writing descriptor for disabling characteristic with UUID: " + writeDescriptorQueueForDisabling.element().getCharacteristic().getUuid() +
                    " and name: " + BleNamesResolver.resolveCharacteristicName("" + writeDescriptorQueueForDisabling.element().getCharacteristic().getUuid()));
            boolean writeDescriptorSuccess = mBluetoothGatt.writeDescriptor(writeDescriptorQueueForDisabling.element()); // callback: BleWrapper-->onDescriptorWrite
            MyTarget.infoMsg("writeDescriptor initiated?: " + writeDescriptorSuccess);
        } else{
            mBluetoothGatt.disconnect();
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                if(state == BluetoothDevice.BOND_BONDING){
                    MyTarget.infoMsg("BONDING...");
                    MyTarget.toastMsg(mActivity, "Bonding");
                } else if(state == BluetoothDevice.BOND_BONDED){
                    MyTarget.infoMsg("BONDED");
                    MyTarget.toastMsg(mActivity, "Bonded");
                    mActivity.unregisterReceiver(mReceiver);
                    initiateWriteDescriptorAndReadCharacteristic();
                } else if(state == BluetoothDevice.BOND_NONE){
                    MyTarget.infoMsg("NOT BONDED");
                    MyTarget.toastMsg(mActivity, "Not Bonded Yet");
                }
            }
        }
    };

    /* close GATT client completely */
    public void closeGatt() {
        MyTarget.debugMsg("BleWrapper - close Gatt Client");
        if(mBluetoothGatt == null) return;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
    
    public boolean connect(final String deviceAddress){
        MyTarget.debugMsg("BleWrapper - connect");
        if(mBluetoothAdapter == null) return false;
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        if (mBluetoothDevice == null) {
            // we got wrong address - that device is not available!
            MyTarget.infoMsg("Device is not available");
            return false;
        }
        mActivity.runOnUiThread(new Runnable(){
            @Override
            public void run() {
                mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mBleCallback);
            }
        });
        return true;
    }
    
    // BLUETOOTH BLE CALLBACKS
    // defines callback for scanning results
    private BluetoothAdapter.LeScanCallback mDeviceFoundCallback = new BluetoothAdapter.LeScanCallback() {
        @Override // comes from: startLeScan
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            MyTarget.debugMsg("BleWrapper - onLeScan");
            if(device == null) return;
            mConnecting = true;
            stopScanning();
            connect(device.getAddress());
        }
    };

    // gatt client callbacks
    private final BluetoothGattCallback mBleCallback = new BluetoothGattCallback() {
        @Override // comes from: connectGatt
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            MyTarget.debugMsg("BleWrapper - onConnectionStateChange");

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                MyTarget.infoMsg("Success!! Connected with device");
                MyTarget.toastMsg(mActivity, "Connected!");
                mConnected = true;
                mConnecting = false;
                mUiBleWrapperCallback.uiDeviceConnected(gatt);
                
                if(writeDescriptorQueue == null){
                    writeDescriptorQueue = new LinkedList<BluetoothGattDescriptor>();
                }
                if(writeDescriptorQueueForDisabling == null){
                    writeDescriptorQueueForDisabling = new LinkedList<BluetoothGattDescriptor>();
                }
                if(readCharacteristicQueue == null){
                    readCharacteristicQueue = new LinkedList<BluetoothGattCharacteristic>();
                }
                
                // start interacting with the device
                readPeriodicalyRssiValue(true);
                mBluetoothGatt.discoverServices(); // callback: onServicesDiscovered
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                MyTarget.infoMsg("Disconnected with device");
                MyTarget.toastMsg(mActivity, "Disconnected!");
                mConnected = false;
                mUiBleWrapperCallback.uiDeviceDisconnected(gatt);
                if(mBluetoothGatt != null){
                    closeGatt();
                }
                mDisconnecting = false;
                mConnecting = false;
                mScanning = false;
                mOperationsFinished = false;
                writeDescriptorQueue = null;
                writeDescriptorQueueForDisabling = null;
                readCharacteristicQueue = null;
            } else{
                MyTarget.errorMsg("Error, Status: " + status + " New State: " + newState);
            }
        }

        // called whenever a read remote RSSI operation is finished
        @Override // comes from: readRemoteRssi()
        public void onReadRemoteRssi(BluetoothGatt gatt, final int rssi, int status) {
            // we got new value of RSSI of the connection, pass it to the UI
            mUiBleWrapperCallback.uiOnReadRemoteRssi(gatt, rssi, status);
            
            if(status == BluetoothGatt.GATT_SUCCESS) {
                
            } else{
                MyTarget.errorMsg("Error, Status: " + status);
            }
        };

        // called once services has been discovered
        @Override // comes from: discoverServices()
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            MyTarget.debugMsg("BleWrapper - onServicesDiscovered");
            if (mBluetoothAdapter == null || mBluetoothGatt == null) return;
            
            if (status == BluetoothGatt.GATT_SUCCESS){
                MyTarget.infoMsg("GATT SUCCESS");

                BluetoothGattService tempService;
                BluetoothGattCharacteristic tempChar;
                /*
                 * Find the appropriate services and characteristics needed
                 * from the list of services found on the BLE device
                 */
                for(UUID serviceUUID : mServicesToSearchDeviceFor){
                    tempService = mBluetoothGatt.getService(serviceUUID);
                    if(tempService == null) continue;
                    MyTarget.infoMsg("Service found with UUID: " + serviceUUID + " and name: " + BleNamesResolver.resolveServiceName("" + serviceUUID));
                    for(UUID charUUID : mCharsToSearchDeviceFor){
                        tempChar = tempService.getCharacteristic(charUUID);
                        if(tempChar == null) continue;
                        MyTarget.infoMsg("Characteristic found with UUID: " + charUUID + " and name: " + BleNamesResolver.resolveCharacteristicName("" + charUUID));

                        setCharacteristics(tempService, tempChar);
                    }
                }
                // start writing descriptors and reading characteristics
                initiateWriteDescriptorAndReadCharacteristic();
            }
            else{
                MyTarget.toastMsg(mActivity, "Unable to read services, Error Status Number:" + status);
                MyTarget.errorMsg("error, status: " + status);
            }
        };
        
        // called whenever a characteristic read operation is completed
        @Override // comes from: readCharacteristic
        public void onCharacteristicRead(BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic,
                int status)
        {
            MyTarget.debugMsg("BleWrapper - onCharacteristicRead");
            
            if(BluetoothGatt.GATT_SUCCESS == status){
                MyTarget.infoMsg("GATT SUCCESS, status: " + status);
                mUiBleWrapperCallback.uiOnCharacteristicRead(gatt, characteristic, status);

                readCharacteristicQueue.remove();
                // continue reading more characteristics
                initiateWriteDescriptorAndReadCharacteristic();
            } else if(status == 137){
                MyTarget.errorMsg("Unable to read characteristic, Error Status Number:" + status + " (GATT_AUTH_FAIL)");
                MyTarget.toastMsg(mActivity, "Unable to read characteristic, Error Status Number:" + status + " (GATT_AUTH_FAIL)");
                disconnect();
            } else{
                MyTarget.errorMsg("error, status: " + status);
                MyTarget.toastMsg(mActivity, "Unable to read " + BleNamesResolver.resolveCharacteristicName("" + characteristic.getUuid()) + " value (error: " + status + ")");
                readCharacteristicQueue.remove();
                // continue reading more characteristics
                initiateWriteDescriptorAndReadCharacteristic();
            }
        }

        // called whenever a characteristic value changes through notification or indication
        @Override // comes from: setCharacteristicNotification
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            MyTarget.debugMsg("BleWrapper - onCharacteristicChanged");

            mUiBleWrapperCallback.uiOnCharacteristicChanged(gatt, characteristic); 
        }

        /*
         * called whenever a write descriptor operation is finished
         * once a write operation is finished we check the queue if there are more write operations
         * once all the write operations are done we check if any read operations exist
         * @see android.bluetooth.BluetoothGattCallback#onDescriptorWrite(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattDescriptor, int)
         */
        @Override // comes from: writeDescriptor
        public void onDescriptorWrite(BluetoothGatt gatt,
                BluetoothGattDescriptor descriptor,
                int status)
        {
            MyTarget.debugMsg("BleWrapper - onDescriptorWrite");  

            if(status == BluetoothGatt.GATT_SUCCESS){
                MyTarget.infoMsg("GATT_SUCCESS, status: " + status);  

                if(mDisconnecting == true){
                    /*
                     * remove the descriptor that we finished writing successfully
                     * and write more descriptors for disabling notifications/indications
                     */
                    writeDescriptorQueueForDisabling.remove();
                    initiateWriteDescriptorForDisablingNotificationsOrIndications();
                } else{
                    writeDescriptorQueueForDisabling.add(writeDescriptorQueue.element()); // add to disabling queue
                    writeDescriptorQueue.remove();  // remove the item that we just finished writing
                    // continue writing more descriptors
                    initiateWriteDescriptorAndReadCharacteristic();  
                }

            } else if(status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION){
                MyTarget.infoMsg("GATT INSUFFICIENT AUTHENTICATION, status: " + status);

                if(mDisconnecting == true){
                    // write more descriptors for disabling notifications/indications
                    initiateWriteDescriptorForDisablingNotificationsOrIndications();
                } else{
                    if(gatt.getDevice().getBondState() == BluetoothDevice.BOND_NONE){
                        /*
                         * we are not bonded with the device and we need to bond to enable
                         * notifications/indications to be able to read encrypted data,
                         * we register for broadcasts on bond state change
                         */
                        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                        mActivity.registerReceiver(mReceiver, filter);
                    } else{
                        MyTarget.errorMsg("GATT INSUFFICIENT AUTHENTICATION, The phone is trying to read from paired device without encryption, even if we are bonded with the current device. Android Bug? status: " + status);
                        MyTarget.toastMsg(mActivity, "Unable to write descriptor, Error Status Number:" + status);
                        mOperationsFinished = true;
                        mUiBleWrapperCallback.uiOnOperationsFinished();
                    }
                }
            } else{
                MyTarget.errorMsg("error, status:" + status);
                MyTarget.toastMsg(mActivity, "Unable to write descriptor, Error Status Number:" + status);

                if(mDisconnecting == true){
                    writeDescriptorQueueForDisabling.remove();
                    initiateWriteDescriptorForDisablingNotificationsOrIndications();
                } else{
                    disconnect();
                }
            }
        }
    }; //mBleCallback

    /*
     * responsible for adding notifications/indications of a characteristic.
     * The enabled boolean is responsible for enabling/disabling the notifications/indications
     * true == enabling and false == disabling
     */
    public void addOrRemoveNotificationsOrIndicationsForCharacteristics(BluetoothGattService tempService,
            BluetoothGattCharacteristic tempChar, boolean enabled){
        MyTarget.debugMsg("BleWrapper - addOrRemoveNotificationsOrIndicationsForCharacteristics");
        
        UUID serviceUUID = tempService.getUuid();
        UUID charUUID = tempChar.getUuid();
        if(BleDefinedUUIDs.Service.HEART_RATE.equals(serviceUUID)){
            if(BleDefinedUUIDs.Characteristic.HEART_RATE_MEASUREMENT.equals(charUUID)){
                setNotificationsForCharacteristic(tempChar, enabled, BleWrapper.ENABLE_NOTIFICATIONS);
            }
        } else if(BleDefinedUUIDs.Service.HEALTH_THERMOMETER.equals(serviceUUID)){
            if(BleDefinedUUIDs.Characteristic.TEMPERATURE_MEASUREMENT.equals(charUUID)){
                setNotificationsForCharacteristic(tempChar, enabled, BleWrapper.ENABLE_INDICATIONS);
            }
        } else if(BleDefinedUUIDs.Service.BLOOD_PRESSURE.equals(serviceUUID)){
            if(BleDefinedUUIDs.Characteristic.BLOOD_PRESSURE_MEASUREMENT.equals(charUUID)){
                setNotificationsForCharacteristic(tempChar, enabled, BleWrapper.ENABLE_INDICATIONS);
            }
        }
        /*
         * more notifications/indications here
         */
    }
    
    public void setCharacteristics(BluetoothGattService tempService,
            BluetoothGattCharacteristic tempChar)
    {
        MyTarget.debugMsg("BleWrapper - setCharacteristics");
        
        UUID serviceUUID = tempService.getUuid();
        UUID charUUID = tempChar.getUuid();

        addOrRemoveNotificationsOrIndicationsForCharacteristics(tempService, tempChar, true);
        
        if(BleDefinedUUIDs.Service.TX_POWER.equals(serviceUUID)){
            if(BleDefinedUUIDs.Characteristic.TX_POWER_LEVEL.equals(charUUID)){
                readCharacteristicQueue.add(tempChar);
            }
        } else if(BleDefinedUUIDs.Service.LINK_LOSS.equals(serviceUUID)){
            if(BleDefinedUUIDs.Characteristic.ALERT_LEVEL.equals(charUUID)){
                /*
                 * as we don't want to read this characteristic immediately we
                 * send it to the class which has the storeCharacteristics() method
                 * and from inside that method we do something more specific.
                 * for example this characteristics value is needed when a button is pressed
                 * so through the storeCharacteristics() method we store the characteristic for later use
                 */
                mUiBleWrapperCallback.storeCharacteristics(tempService, tempChar);
            }
        } else if(BleDefinedUUIDs.Service.IMMEDIATE_ALERT.equals(serviceUUID)){
            if(BleDefinedUUIDs.Characteristic.ALERT_LEVEL.equals(charUUID)){
                mUiBleWrapperCallback.storeCharacteristics(tempService, tempChar);
            }
        } else if(BleDefinedUUIDs.Service.BATTERY.equals(serviceUUID)){
            if(BleDefinedUUIDs.Characteristic.BATTERY_LEVEL.equals(charUUID)){
                readCharacteristicQueue.add(tempChar);
            }
        } else if(BleDefinedUUIDs.Service.HEART_RATE.equals(serviceUUID)){
            if(BleDefinedUUIDs.Characteristic.BODY_SENSOR_LOCATION.equals(charUUID)){
                readCharacteristicQueue.add(tempChar);
            }
        }
        /*
         * more characteristics for reading or storing here if necessary
         */
    }

    // other functionalities
    public byte[] parseHexStringToBytes(final String hex) {
        String tmp = hex.substring(2).replaceAll("[^[0-9][a-f]]", "");
        byte[] bytes = new byte[tmp.length() / 2]; // every two letters in the string are one byte finally
        String part = "";
        
        for(int i = 0; i < bytes.length; ++i) {
            part = "0x" + tmp.substring(i*2, i*2+2);
            bytes[i] = Long.decode(part).byteValue();
        }
        return bytes;
    }
    
    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if(mBluetoothGatt == null) return false;
        return mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /* before any action check if BT is turned ON and enabled,
     * call this in onResume to be always sure that BT is ON when Your
     * application is put into the foreground */
    public boolean isBtEnabled() {
        final BluetoothManager manager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        if(manager == null) return false;

        final BluetoothAdapter adapter = manager.getAdapter();
        if(adapter == null) return false;

        return adapter.isEnabled();
    }

    // run test and check if this device has BT and BLE hardware available
    public boolean checkBleHardwareAvailable() {
        // First check general Bluetooth Hardware:
        // get BluetoothManager...
        final BluetoothManager manager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        if(manager == null) return false;
        // .. and then get adapter from manager
        final BluetoothAdapter adapter = manager.getAdapter();
        if(adapter == null) return false;
        // and then check if BT LE is also available
        boolean hasBle = mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        return hasBle;
    }
}