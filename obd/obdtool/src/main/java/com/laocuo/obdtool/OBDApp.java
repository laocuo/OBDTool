package com.laocuo.obdtool;

import android.app.Application;

public class OBDApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BT.getInstance().initBTClient(this);
    }
}
