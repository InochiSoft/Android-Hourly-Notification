package com.laregrage.testservice.helper;

import android.content.Context;

public class BundleSettings {
    private Context context;
    private Settings settings;

    public BundleSettings(Context context){
        this.context = context;
        this.settings = new Settings(context);
    }

    public int getBootPremission(){
        return settings.getIntSetting(Constants.Setting.BOOT_PERMISSION, 0);
    }

    public void setBootPremission(int value){
        settings.setIntSetting(Constants.Setting.BOOT_PERMISSION, value);
    }
}
