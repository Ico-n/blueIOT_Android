package de.hdmstuttgart.blueiot;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class ScanActivity extends ListActivity {

    private BleDeviceListAdapter bleDeviceListAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isScanning;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        handler = new Handler();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        bleDeviceListAdapter = new BleDeviceListAdapter(this);
        setListAdapter(bleDeviceListAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        scanLeDevice(false);
        bleDeviceListAdapter.clear();
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            //Stops scanning after a defined scan period
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScanning = false;
                    bluetoothAdapter.stopLeScan(leScanCallback);
                }
            }, 5000);

            //Start scanning for BLE-Devices
            isScanning = true;
            bluetoothAdapter.startLeScan(leScanCallback);
        }
        else {
            //Stop scanning immediately
            isScanning = false;
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    //Device Scan Callback
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            //Update the UI with all BLE-Devices that were found
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bleDeviceListAdapter.addDevice(device);
                    bleDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        BluetoothDevice device = bleDeviceListAdapter.getDevice(position);

        //TODO
        //Allow "iBeacon" only (-->Name && Address)

        if (device != null) {
            //Create Intent to start new Activity, put extra information (BluetoothDevice)
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("device", device);

            if (isScanning) {
                bluetoothAdapter.stopLeScan(leScanCallback);
                isScanning = false;
            }

            //Start Activity
            startActivity(intent);
        }
    }
}