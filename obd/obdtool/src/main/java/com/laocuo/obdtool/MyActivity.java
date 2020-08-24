package com.laocuo.obdtool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MyActivity extends AppCompatActivity implements BluetoothSPP.BluetoothConnectionListener, View.OnClickListener, BluetoothSPP.OnDataReceivedListener {

    private Context mContext;

    private BluetoothSPP mBluetoothSPP;

    private Button mSelect;

    private TextView mReceiveData;

    private EditText mSendData;

    private Button mSend;

    private boolean isConnected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        mSelect = findViewById(R.id.select);
        mSelect.setOnClickListener(this);
        mReceiveData = findViewById(R.id.receiveData);
        mSendData = findViewById(R.id.sendData);
        mSend = findViewById(R.id.send);
        mSend.setOnClickListener(this);
        mSend.setEnabled(false);
        mContext = this;
        mBluetoothSPP = new BluetoothSPP(mContext);
        mBluetoothSPP.setBluetoothConnectionListener(this);
        mBluetoothSPP.setOnDataReceivedListener(this);
        if (!mBluetoothSPP.isBluetoothEnabled()) {
            // Do somthing if bluetooth is disable
            mBluetoothSPP.enable();
            toast("请先打开蓝牙");
            finish();
        } else {
            // Do something if bluetooth is already enable
            toast("startService");
            mBluetoothSPP.setupService();
            mBluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothSPP.setBluetoothConnectionListener(null);
        mBluetoothSPP.setOnDataReceivedListener(null);
        mBluetoothSPP.stopService();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK) {
                String address = data.getExtras().getString(BluetoothState.EXTRA_DEVICE_ADDRESS);
                toast("connect " + address);
                mBluetoothSPP.connect(data);
            }
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                mBluetoothSPP.setupService();
                mBluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
            } else {
                // Do something if user doesn't choose any device (Pressed back)
            }
        }
    }

    @Override
    public void onDeviceConnected(String name, String address) {
        toast("onDeviceConnected");
        isConnected = true;
        mSend.setEnabled(true);
    }

    @Override
    public void onDeviceDisconnected() {
        toast("onDeviceDisconnected");
        isConnected = false;
        mSend.setEnabled(false);
    }

    @Override
    public void onDeviceConnectionFailed() {
        toast("onDeviceConnectionFailed");
        isConnected = false;
        mSend.setEnabled(false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.select:
                select();
                break;
            case R.id.send:
                send();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDataReceived(byte[] data, String message) {
        try {
            mReceiveData.setText(new String(data, "utf-8") + "\n" + message);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            mReceiveData.setText(ByteUtil.bytesToHexString(data) + "\n" + message);
        }
    }

    private void select() {
        Intent intent = new Intent(getApplicationContext(), DeviceList.class);
        intent.putExtra("layout_list", R.layout.devices_list);
        startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
    }

    private void send() {
        if (isConnected) {
            String input = mSendData.getText().toString().trim();
            if (input != null && !"".equals(input)) {
                mBluetoothSPP.send(input.getBytes(), true);
            }
        }
    }

    private void toast(String msg) {
        Toast.makeText(MyActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
