package de.hdmstuttgart.blueiot;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.List;

public class BlueIOTManager {

    private BluetoothDevice device;

    public BluetoothDevice getDevice() {
        return this.device;
    }

    public BlueIOTManager(BluetoothDevice device) {
        this.device = device;
    }

    public static List<BluetoothDevice> getBleDevices() {
        List<BluetoothDevice> devices = new ArrayList<>();

        //TODO
        //Start / Stop Scanning

        return devices;
    }

    public int getXAxis() {
        return 0;
    }

    public int getYAxis() {
        return 0;
    }

    public int getZAxis() {
        return 0;
    }

    public int getHeight() {
        return 0;
    }

    public String getSensorValues() {
        return "";
    }

    public void setNotification() {

    }
}