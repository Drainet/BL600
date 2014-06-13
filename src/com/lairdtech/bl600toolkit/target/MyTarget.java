package com.lairdtech.bl600toolkit.target;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class MyTarget {
    public static final boolean DISPLAY_DEBUG_MSGS = false;
    public static final boolean DISPLAY_INFO_MSGS = false;
    public static final boolean DISPLAY_ERROR_MSGS = false;
    public static final String TAG = "BL600 APP";
    
    
    public static void debugMsg(String msg){
        if(DISPLAY_DEBUG_MSGS == true){
            Log.d(TAG, msg);
        }
    }
    
    public static void infoMsg(String msg){
        if(DISPLAY_INFO_MSGS == true){
            Log.i(TAG, msg);
        }
    }
    
    public static void errorMsg(String msg){
        if(DISPLAY_ERROR_MSGS == true){
            Log.e(TAG, msg);
        }
    }
    
    public static void toastMsg(final Activity activity, final String msg){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(activity, msg, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 200);
                toast.show();
            }
        });
    }
}