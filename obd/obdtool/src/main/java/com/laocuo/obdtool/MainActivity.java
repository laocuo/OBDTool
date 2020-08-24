package com.laocuo.obdtool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleReadRssiResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattCharacter;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.inuker.bluetooth.library.receiver.listener.BluetoothBondListener;
import com.laocuo.obdtool.widget.CommonDialog;
import com.laocuo.obdtool.widget.DialogManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "OBD";

    private final int REQUEST_DEVICE = 1;

    private Context mContext;

    private BluetoothClient mBluetoothClient;

    private Button mSearch, mRead;

    private TextView mCurrent;

    private SharedPreferences mSharedPreferences;

    private String deviceName;

    private String deviceAddress;

    private boolean isConnected;

    private UUID read_UUID_chara, read_UUID_service;

    private UUID write_UUID_chara, write_UUID_service;

    private UUID notify_UUID_chara, notify_UUID_service;

    private UUID indicate_UUID_chara, indicate_UUID_service;

    private UUID heartRateService = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
    private UUID heartRateCharacteristics = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb");
    private UUID heartRateNotify = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");

    //ANS
    private UUID dataService = UUID.fromString("00001811-0000-1000-8000-00805f9b34fb");
    private UUID dataCharacteristics = UUID.fromString("00002a44-0000-1000-8000-00805f9b34fb");

    private final BluetoothStateListener mBluetoothStateListener = new BluetoothStateListener() {
        @Override
        public void onBluetoothStateChanged(boolean openOrClosed) {
            Log.i(TAG, "onBluetoothStateChanged:" + openOrClosed);
            if (openOrClosed) {
                connectBluetooth();
            }
        }
    };

    private final BluetoothBondListener mBluetoothBondListener = new BluetoothBondListener() {
        @Override
        public void onBondStateChanged(String mac, int bondState) {
            // bondState = Constants.BOND_NONE, BOND_BONDING, BOND_BONDED
            Log.i(TAG, "onBondStateChanged:" + mac + " " + bondState);
        }
    };

    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {

        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status == Constants.STATUS_CONNECTED) {
                isConnected = true;
                Toast.makeText(mContext, deviceName + "连接成功", Toast.LENGTH_SHORT).show();
            } else if (status == Constants.STATUS_DISCONNECTED) {
                isConnected = false;
                Toast.makeText(mContext, deviceName + "连接失败", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final int MSG_OPEN = 1;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_OPEN:
                    openBluetooth();
                    break;
            }
        }
    };

    private ListView mListView;

    private SimpleAdapter mAdapter;

    private List<Map<String, String>> mContents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mSearch = findViewById(R.id.search);
        mSearch.setOnClickListener(this);
        mRead = findViewById(R.id.read);
        mRead.setOnClickListener(this);
        mCurrent = findViewById(R.id.current);
        mListView = findViewById(R.id.btlist);
        mAdapter = new SimpleAdapter(
                this,
                mContents,
                android.R.layout.simple_list_item_2,
                new String[]{"name", "address"},
                new int[]{android.R.id.text1, android.R.id.text2});
        mListView.setAdapter(mAdapter);

        mSharedPreferences = getSharedPreferences("sp_obd", MODE_PRIVATE);
        deviceName = mSharedPreferences.getString("device_name", "");
        deviceAddress = mSharedPreferences.getString("device_address", "");
        mCurrent.setText("当前设备:" + deviceName + " " + deviceAddress);
        mBluetoothClient = BT.getInstance().getBTClient();
        if (!mBluetoothClient.isBluetoothOpened()) {
            mHandler.sendEmptyMessageDelayed(MSG_OPEN, 1000);
        } else {
            connectBluetooth();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBluetoothClient.registerBluetoothBondListener(mBluetoothBondListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBluetoothClient.unregisterBluetoothBondListener(mBluetoothBondListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothClient.unregisterBluetoothStateListener(mBluetoothStateListener);
        mBluetoothClient.unregisterConnectStatusListener(deviceAddress, mBleConnectStatusListener);
        if (isConnected) {
            mBluetoothClient.disconnect(deviceAddress);
        }
//        mBluetoothClient.closeBluetooth();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.search) {
            Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
            startActivityForResult(intent, REQUEST_DEVICE);
        } else if (view.getId() == R.id.read) {
//            readHeartRate();
            readRssi();
//            readData();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DEVICE && resultCode == 0 && data != null) {
            deviceName = data.getStringExtra("name");
            deviceAddress = data.getStringExtra("address");
            Log.i(TAG, "onActivityResult:" + deviceName + " " + deviceAddress);
            mCurrent.setText("当前设备:" + deviceName + " " + deviceAddress);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString("device_name", deviceName);
            editor.putString("device_address", deviceAddress);
            editor.commit();
            connectBluetooth();
        }
    }

    private void openBluetooth() {
        DialogManager.show(CommonDialog.getDialog("打开蓝牙?", new CommonDialog.ClickListener() {
            @Override
            public void positive() {
                mBluetoothClient.registerBluetoothStateListener(mBluetoothStateListener);
                mBluetoothClient.openBluetooth();
            }

            @Override
            public void negative() {
                finish();
            }
        }), getSupportFragmentManager());
    }

    private void connectBluetooth() {
        Log.i(TAG, "connectBluetooth:" + deviceAddress);
        if (TextUtils.isEmpty(deviceAddress)) {
            Toast.makeText(mContext, "请先搜索蓝牙设备", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(mContext, "正在连接" + deviceName, Toast.LENGTH_SHORT).show();
        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)   // 连接如果失败重试3次
                .setConnectTimeout(30000)   // 连接超时30s
                .setServiceDiscoverRetry(3)  // 发现服务如果失败重试3次
                .setServiceDiscoverTimeout(20000)  // 发现服务超时20s
                .build();
        mBluetoothClient.registerConnectStatusListener(deviceAddress, mBleConnectStatusListener);
        mBluetoothClient.connect(deviceAddress, options, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile data) {
                if (code == Constants.REQUEST_SUCCESS) {
                    List<BleGattService> serviceList = data.getServices();
                    for (BleGattService service : serviceList) {
                        Log.i(TAG, "service UUID:" + service.getUUID());
                        List<BleGattCharacter> characterList = service.getCharacters();
                        for (BleGattCharacter characteristic : characterList) {
                            int charaProp = characteristic.getProperty();
                            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                                read_UUID_chara = characteristic.getUuid();
                                read_UUID_service = service.getUUID();
                                addContent("read_chara\n" + read_UUID_chara, "read_service\n" + read_UUID_service);
                                Log.i(TAG, "read_chara=" + read_UUID_chara + "----read_service=" + read_UUID_service);
                            }
                            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                                write_UUID_chara = characteristic.getUuid();
                                write_UUID_service = service.getUUID();
                                addContent("write_chara\n" + write_UUID_chara, "write_service\n" + write_UUID_service);
                                Log.i(TAG, "write_chara=" + write_UUID_chara + "----write_service=" + write_UUID_service);
//                                openlock(write_UUID_service, write_UUID_chara);
                            }
//                            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
//                                write_UUID_chara = characteristic.getUuid();
//                                write_UUID_service = service.getUUID();
//                                addContent("write_chara\n" + write_UUID_chara, "write_service\n" + write_UUID_service);
//                                Log.i(TAG, "write_chara=" + write_UUID_chara + "----write_service=" + write_UUID_service);
//                            }
                            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                notify_UUID_chara = characteristic.getUuid();
                                notify_UUID_service = service.getUUID();
                                addContent("notify_chara\n" + notify_UUID_chara, "notify_service\n" + notify_UUID_service);
                                Log.i(TAG, "notify_chara=" + notify_UUID_chara + "----notify_service=" + notify_UUID_service);
                            }
                            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                                indicate_UUID_chara = characteristic.getUuid();
                                indicate_UUID_service = service.getUUID();
                                addContent("indicate_chara\n" + indicate_UUID_chara, "indicate_service\n" + indicate_UUID_service);
                                Log.i(TAG, "indicate_chara=" + indicate_UUID_chara + "----indicate_service=" + indicate_UUID_service);
                            }
                        }
                    }
                }
            }
        });
    }

    private void openlock(UUID service, UUID character) {
        String open = "openlock";
        mBluetoothClient.write(deviceAddress, service, character, open.getBytes(), new BleWriteResponse() {
            @Override
            public void onResponse(int code) {
                Log.i(TAG, "openlock: code=" + code);
                if (code == Constants.REQUEST_SUCCESS) {

                }
            }
        });
    }

    private void readHeartRate() {
        if (!isConnected) return;
//        Log.i(TAG, "readHeartRate start");
        mBluetoothClient.read(deviceAddress, heartRateService, heartRateCharacteristics, new BleReadResponse() {
            @Override
            public void onResponse(int code, byte[] data) {
                Log.i(TAG, "readHeartRate: code=" + code);
                if (code == Constants.REQUEST_SUCCESS) {
                    Log.i(TAG, "readHeartRate:" + ByteUtil.bytesToHexString(data));
                }
            }
        });
    }

    private void readData() {
        if (!isConnected) return;
        Log.i(TAG, "readData start");
        mBluetoothClient.read(deviceAddress, dataService, dataCharacteristics, new BleReadResponse() {
            @Override
            public void onResponse(int code, byte[] data) {
                Log.i(TAG, "readData: code=" + code);
                if (code == Constants.REQUEST_SUCCESS) {
                    Log.i(TAG, "readData:" + ByteUtil.bytesToHexString(data));
                }
            }
        });
    }

    private void readRssi() {
        if (!isConnected) return;
//        Log.i(TAG, "readRssi start");
        mBluetoothClient.readRssi(deviceAddress, new BleReadRssiResponse() {
            @Override
            public void onResponse(int code, Integer rssi) {
                if (code == Constants.REQUEST_SUCCESS) {
                    Log.i(TAG, "readRssi:" + rssi);
                }
            }
        });
    }

    private void addContent(String name, String address) {
        Map<String, String> item = new HashMap<>();
        item.put("name", name);
        item.put("address", address);
        mContents.add(item);
        mAdapter.notifyDataSetChanged();
    }
}
