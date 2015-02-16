package de.hdmstuttgart.blueiot;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Custom ListAdapter that is used to scan for BLE-Devices nearby
 */
public class BleDeviceListAdapter extends BaseAdapter {
    //Internal Collection
    private ArrayList<BluetoothDevice> bleDevices;

    private LayoutInflater inflater;

    /**
     * Constructor
     * @param context ApplicationContext used to inflate layout components
     */
    public BleDeviceListAdapter(Context context) {
        super();
        this.bleDevices = new ArrayList<>();
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Adds a device to the collection
     * @param device The BluetoothDevice to be added
     */
    public void addDevice(BluetoothDevice device) {
        if (!this.bleDevices.contains(device)) {
            this.bleDevices.add(device);
        }
    }

    /**
     * Allows retrieving a device from the adapter
     * @param position The position in the adapter
     * @return The BluetoothDevice at the specified position
     */
    public BluetoothDevice getDevice(int position) {
        return this.bleDevices.get(position);
    }

    /**
     * Clears the internal collection
     */
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

    /**
     * Adapter method that is called for each item in the internal collection (i.e. this.bleDevices) in order to provide a View to add to the ListView
     * @param position The position in the adapter
     * @param convertView The old view to reuse, if possible. Should check if this view is non-null.
     * @param parent The parent that the view will be attached to
     * @return A View that will be displayed in the ListView
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Inflate custom layout resource
        convertView = this.inflater.inflate(R.layout.listitem_device, null);

        //Get References to UI-Components
        TextView textView_Name = (TextView) convertView.findViewById(R.id.device_name);
        TextView textView_Address = (TextView) convertView.findViewById(R.id.device_address);

        //Customize TextViews
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