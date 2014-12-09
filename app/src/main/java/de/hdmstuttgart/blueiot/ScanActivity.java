package de.hdmstuttgart.blueiot;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.List;
import java.util.UUID;

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
            }, 10000);

            isScanning = true;
            bluetoothAdapter.startLeScan(leScanCallback);
        }
        else {
            isScanning = false;
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    //Device Scan Callback
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
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
        if (device != null) {
            //TODO
            //Create Intent to start new Activity
            //Put extra information

            if (isScanning) {
                bluetoothAdapter.stopLeScan(leScanCallback);
                isScanning = false;
            }

            BluetoothGatt bluetoothGatt = device.connectGatt(this, false, gattCallback);

            //Start Activity
        }
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("TAG", "Connected to GATT Server");
                gatt.discoverServices();
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("TAG", "Disconnected from GATT-Server");
            }
            else {
                Log.d("TAG", "onConnectionStateChange status: " + status + " , newState: " + newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d("TAG", "onServicesDiscovered received status: " + status);
            List<BluetoothGattService> gattServices = gatt.getServices();
            for (BluetoothGattService service : gattServices) {
                UUID serviceUUID = service.getUuid();
                Log.d("TAG", "Service UUID: " + serviceUUID.toString());

                List<BluetoothGattCharacteristic> gattCharacteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : gattCharacteristics) {
                    Log.d("TAG", "Characteristic found: ID = " + characteristic.getInstanceId());
                    byte[] values = characteristic.getValue();
					/*String str = "";
					try {
						str = new String(values, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

					Log.d("TAG", "Byte Value: " + str);*/
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d("TAG", "onCharacteristicRead status: " + status);
        }
    };
}