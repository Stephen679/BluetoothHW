package com.example.hwbluetooth;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ScanActivity extends AppCompatActivity {
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 10;
    private boolean Scanning = false;
    public ArrayList<ScannedData> foundDevice = new ArrayList<>();
    Button temp;
    Information information;
    com.example.hwbluetooth.ItemAdapter ItemAdapter;
    private int count = 1;
    RecyclerView recyclerView;
    NavController controller;
    BluetoothLeScanner mBluetoothLeScanner
            =mBluetoothAdapter.getBluetoothLeScanner();
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    @Nullable
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        controller = Navigation.findNavController(this,R.id.fragment2);
        information = new Information();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment2,information).commitAllowingStateLoss();
        checkPermission();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        temp = findViewById(R.id.temp);
        ItemAdapter = new ItemAdapter(onItemClick);//先初始化adapter
        //onCreated方法只會在開始時做一次
        temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Scanning) {
                    Scanning = false;
                    temp.setText("開始掃描");
//                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mBluetoothLeScanner.stopScan(startScanCallback);
                }else{
                    if(count==1){
                        //在第一次按按鈕時初始化並得到fragment的recycler view，而非在oncreate時
                        recyclerView = information.getView().findViewById(R.id.ScannedListdINfragment);
                        recyclerView.setLayoutManager(new LinearLayoutManager(ScanActivity.this));
                        recyclerView.setAdapter(ItemAdapter);
                        count++;
                    }
                    Scanning = true;
                    temp.setText("停止掃描");
                    ItemAdapter.clearDevice();
                    foundDevice.clear();
                    mBluetoothLeScanner.startScan(startScanCallback);
                }
            }
        });

    }


    //取得權限
    //打開location和bluetooth
    private void checkPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int hasGone = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasGone != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_FINE_LOCATION_PERMISSION);
            }
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this,"Not support Bluetooth", Toast.LENGTH_SHORT).show();
                finish();
            }
            if(!mBluetoothAdapter.isEnabled()){
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBtIntent);
            }
        }else finish();
    }

/*@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            new Thread(()->{
                foundDevice.add(new ScannedData(device.getAddress(),String.valueOf(rssi),scanRecord.toString()));
                ArrayList newList = getNonoverlapped(foundDevice);
                runOnUiThread(()->{//返回主執行續
                    ItemAdapter.addDevice(newList);//動態變動的，setItemadpter也只會在onCreate做一次
                    // 因為我在後面有呼叫notifydatasetchanged所以資料才可以有變動
                    //當資料有變動的時候，記得要呼叫 notifyDataSetChanged
                });
            }).start();
        }
    };*/


    private final ScanCallback startScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            ScanRecord mScanRecord = result.getScanRecord();
            String address = device.getAddress();
            byte[] content = mScanRecord.getBytes();
            int mRssi = result.getRssi();
            ArrayList newList = getNonoverlapped(foundDevice);
            newList.add(new ScannedData(address,String.valueOf(mRssi),String.valueOf(content)));
            ItemAdapter.addDevice(newList);
        }
    };

    /*檢查重複*/
    private ArrayList getNonoverlapped(ArrayList list) {
        ArrayList tempList = new ArrayList<>();
        try {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                Object obj = it.next();
                if (!tempList.contains(obj)) {
                    tempList.add(obj);
                } else {
                    tempList.set(getIndex(tempList, obj), obj);//若重複，則由新的取代
                }
            }
            return tempList;
        } catch (ConcurrentModificationException e) {
            return tempList;
        }
    }
    //抓出該值在陣列的哪處
    private int getIndex(ArrayList temp, Object obj) {
        for (int i = 0; i < temp.size(); i++) {
            if (temp.get(i).toString().contains(obj.toString())) {
                return i;
            }
        }
        return -1;
    }
    com.example.hwbluetooth.ItemAdapter.OnItemClick onItemClick = new com.example.hwbluetooth.ItemAdapter.OnItemClick() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onItemClick(ScannedData selectedDevice) {
            Toast.makeText(ScanActivity.this, selectedDevice.getAddress(),Toast.LENGTH_SHORT)
                    .show();
            Bundle bundle = new Bundle();
            bundle.putString("byte",selectedDevice.getDeviceByteInfo());
            bundle.putString("MAC",selectedDevice.getAddress());
            bundle.putString("rssi",selectedDevice.getRssi());
            controller.navigate(R.id.action_information2_to_secondFragment,bundle);
        }
    };
}