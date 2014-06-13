package com.lairdtech.bl600toolkit.target;

import android.view.View;

public interface LayoutSetupInterface {
    public void bindViews(View view);
    public void setDefaultViewValues();
    public void setListeners();
    public void uiInvalidateBtnState();   
}