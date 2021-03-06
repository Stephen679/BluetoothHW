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
import java.util.HashMap;
import java.util.Iterator;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ScanActivity extends AppCompatActivity {
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 10;
    private boolean Scanning = false;
    public ArrayList<DeviceData> foundDevice = new ArrayList<>();
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
        ItemAdapter = new ItemAdapter(onItemClick);//????????????adapter
        //onCreated?????????????????????????????????
        temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Scanning) {
                    Scanning = false;
                    temp.setText("????????????");
                    mBluetoothLeScanner.stopScan(startScanCallback);
                }else{
                    if(count==1){
                        //??????????????????????????????????????????fragment???recycler view????????????oncreate???
                        recyclerView = information.getView().findViewById(R.id.ScannedListdINfragment);
                        recyclerView.setLayoutManager(new LinearLayoutManager(ScanActivity.this));
                        recyclerView.setAdapter(ItemAdapter);
                        count++;
                    }
                    Scanning = true;
                    temp.setText("????????????");
                    ItemAdapter.clearDevice();
                    foundDevice.clear();
                    mBluetoothLeScanner.startScan(startScanCallback);
                }
            }
        });

    }


    //????????????
    //??????location???bluetooth
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

    private final ScanCallback startScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            ScanRecord mScanRecord = result.getScanRecord();
            String address = device.getAddress();
            byte[] content = mScanRecord.getBytes();
            int mRssi = result.getRssi();
            foundDevice.add(new DeviceData(address,String.valueOf(mRssi),String.valueOf(content)));
            ItemAdapter.addDevice(foundDevice);
        }
    };

    com.example.hwbluetooth.ItemAdapter.OnItemClick onItemClick = new com.example.hwbluetooth.ItemAdapter.OnItemClick() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onItemClick(DeviceData selectedDevice) {
            Toast.makeText(ScanActivity.this, selectedDevice.getAddress(),Toast.LENGTH_SHORT)
                    .show();
            Bundle bundle = new Bundle();
            bundle.putString("byte",selectedDevice.getDeviceByteInfo());
            bundle.putString("MAC",selectedDevice.getAddress());
            bundle.putString("rssi",selectedDevice.getRssi());
            controller.navigate(R.id.action_information2_to_secondFragment,bundle);
            if(Scanning){//????????????????????????????????????????????????????????????????????????
                Scanning = false;
                mBluetoothLeScanner.stopScan(startScanCallback);
                temp.setText("????????????");
            }
        }
    };
}