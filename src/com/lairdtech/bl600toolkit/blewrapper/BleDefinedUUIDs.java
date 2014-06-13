package com.lairdtech.bl600toolkit.blewrapper;

import java.util.UUID;

public class BleDefinedUUIDs {
    
    public static class Service {
        final static public UUID HEART_RATE                  = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
        final static public UUID HEALTH_THERMOMETER          = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb");
        final static public UUID BLOOD_PRESSURE              = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb");
        final static public UUID BATTERY                     = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
        // proximity profile
        final static public UUID LINK_LOSS                   = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");
        final static public UUID IMMEDIATE_ALERT             = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
        final static public UUID TX_POWER                    = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    };
    
    public static class Characteristic {
        final static public UUID MANUFACTURER_STRING         = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
        final static public UUID MODEL_NUMBER_STRING         = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
        final static public UUID FIRMWARE_REVISION_STRING    = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
        final static public UUID HEART_RATE_MEASUREMENT      = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
        final static public UUID BODY_SENSOR_LOCATION        = UUID.fromString("00002a38-0000-1000-8000-00805f9b34fb");
        final static public UUID TEMPERATURE_MEASUREMENT     = UUID.fromString("00002a1c-0000-1000-8000-00805f9b34fb");
        final static public UUID BLOOD_PRESSURE_MEASUREMENT  = UUID.fromString("00002a35-0000-1000-8000-00805f9b34fb");
        final static public UUID ALERT_LEVEL                 = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
        final static public UUID TX_POWER_LEVEL              = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
        final static public UUID APPEARANCE                  = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb");
        final static public UUID BATTERY_LEVEL               = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    }
    
    public static class Descriptor {
        final static public UUID CHAR_CLIENT_CONFIG          = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    }
}