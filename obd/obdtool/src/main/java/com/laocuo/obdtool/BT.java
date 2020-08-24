package com.laocuo.obdtool;

import android.content.Context;

import com.inuker.bluetooth.library.BluetoothClient;

public class BT {
    private BT() {}

    private static BT instance = new BT();

    public static BT getInstance() {
        return instance;
    }

    private BluetoothClient mBluetoothClient;

    public void initBTClient(Context context) {
        if (mBluetoothClient == null) {
            mBluetoothClient = new BluetoothClient(context);
        }
    }

    public BluetoothClient getBTClient() {
        return mBluetoothClient;
    }
}
