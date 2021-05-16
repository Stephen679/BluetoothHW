package com.example.hwbluetooth;

import java.io.Serializable;

class ScannedData implements Serializable {
    private String rssi;
    private String deviceByteInfo;
    private String address;

    public ScannedData(String address, String rssi,String deviceByteInfo ) {
        this.rssi = rssi;
        this.address = address;
        this.deviceByteInfo = deviceByteInfo;
    }
    public String getAddress() {
        return address;
    }
    public String getRssi() {
        return rssi;
    }
    public String getDeviceByteInfo() {
        return deviceByteInfo;
    }
}
