package de.hdmstuttgart.blueiot;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class BleDeviceListAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> bleDevices;
    private LayoutInflater inflater;

    public BleDeviceListAdapter(Context context) {
        super();
        this.bleDevices = new ArrayList<BluetoothDevice>();
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addDevice(BluetoothDevice device) {
        if (!this.bleDevices.contains(device)) {
            this.bleDevices.add(device);
        }
    }

    public BluetoothDevice getDevice(int position) {
        return this.bleDevices.get(position);
    }

    public void clear() {
        this.bleDevices.clear();
    }

    @Override
    public int getCount() {
        return this.bleDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return this.bleDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.listitem_device, null);
        TextView textView_Name = (TextView) convertView.findViewById(R.id.device_name);
        TextView textView_Address = (TextView) convertView.findViewById(R.id.device_address);

        BluetoothDevice device = this.bleDevices.get(position);
        String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0 ) {
            textView_Name.setText(deviceName);
        }
        else {
            textView_Name.setText("Unknown Device");
        }
        textView_Address.setText(device.getAddress());

        return convertView;
    }
}