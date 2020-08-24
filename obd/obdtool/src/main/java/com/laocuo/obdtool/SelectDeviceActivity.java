package com.laocuo.obdtool;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.beacon.BeaconItem;
import com.inuker.bluetooth.library.beacon.BeaconParser;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SelectDeviceActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final String TAG = "OBD";

    private Context mContext;

    private boolean isSearching;

    private BluetoothClient mBluetoothClient;

    private ListView mListView;

    private SimpleAdapter mAdapter;

    private List<Map<String, String>> mContents = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_selectdevice);
        mListView = findViewById(R.id.deviceList);
        mAdapter = new SimpleAdapter(
                this,
                mContents,
                android.R.layout.simple_list_item_2,
                new String[]{"name", "address"},
                new int[]{android.R.id.text1, android.R.id.text2});
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mBluetoothClient = BT.getInstance().getBTClient();
        query();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isSearching) {
            mBluetoothClient.stopSearch();
        }
    }

    private void query() {
        Toast.makeText(mContext, "搜索蓝牙设备", Toast.LENGTH_SHORT).show();
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
                .searchBluetoothLeDevice(2000)      // 再扫BLE设备2s
                .build();

        mBluetoothClient.search(request, new SearchResponse() {
            @Override
            public void onSearchStarted() {
                isSearching = true;
            }

            @Override
            public void onDeviceFounded(SearchResult device) {
//                Beacon beacon = new Beacon(device.scanRecord);
//                for (BeaconItem item :beacon.mItems) {
//                    BeaconParser beaconParser = new BeaconParser(item);
//                }
                Log.i(TAG, "onDeviceFounded:" + device.getName() + " " + device.getAddress());
                Map<String, String> item = new HashMap<>();
                item.put("name", device.getName());
                item.put("address", device.getAddress());
                mContents.add(item);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onSearchStopped() {
                isSearching = false;
            }

            @Override
            public void onSearchCanceled() {
                isSearching = false;
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> view, View view1, int i, long l) {
        String name = mContents.get(i).get("name");
        String address = mContents.get(i).get("address");
        Log.i(TAG, "onItemClick:" + name + " " + address);
        Intent intent = new Intent();
        intent.putExtra("name", name);
        intent.putExtra("address", address);
        setResult(0, intent);
        finish();
    }
}
