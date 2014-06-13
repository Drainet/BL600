package com.lairdtech.bl600toolkit.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.lairdtech.bl600toolkit.R;

public class DisclaimerActivity extends Activity implements OnClickListener {
    private Button btnDeclineView;
    private Button btnAcceptView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // remove action bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_disclaimer);

        /*
         * Higher API levels will not display the logo correctly without this
         * change. Does not exist in lower API targets
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)findViewById(R.id.logoLaird).setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        
        bindViews();
        setListeners();
    }
    
    public void bindViews() {
        btnDeclineView = (Button) findViewById(R.id.btnDecline);
        btnAcceptView = (Button) findViewById(R.id.btnAccept);
    }
    
    public void setListeners() {
        btnDeclineView.setOnClickListener(this);
        btnAcceptView.setOnClickListener(this);
    }
    
    @Override
    public void onClick(View view) {
        int selectedViewId = view.getId();
        Intent intent;

        switch (selectedViewId) {
        case R.id.btnAccept:
            intent = new Intent(this, FragmentsContainerActivity.class);
            startActivity(intent);
            finish();
            break;

        case R.id.btnDecline:
            finish();
            break;
        }
    }
}