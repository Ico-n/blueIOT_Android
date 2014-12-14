package de.hdmstuttgart.blueiot;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
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
            }, 5000);

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
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            Log.d("TAG", "onCharacteristicRead");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            Log.d("TAG", "onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            //super.onCharacteristicChanged(gatt, characteristic);

            //TODO
            //REGEX

            String value = characteristic.getStringValue(0);
            Log.d("TAG", "onCharacteristicChanged - Value: " + value);

            try {
                String[] values = value.split(",");
                if (values.length == 4) {
                    int x = Integer.parseInt(values[0].trim());
                    int y = Integer.parseInt(values[1].trim());
                    int z = Integer.parseInt(values[2].trim());
                    int height = Integer.parseInt(values[3].trim());

                    Log.d("TAG", "X=" + x + " , Y=" + y + " , Z= " + z + " , Height=" + height);
                }
            } catch (Exception ex) {}
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);

            Log.d("TAG", "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

            Log.d("TAG", "onDescriptorWrite Status: " + status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);

            Log.d("TAG", "onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);

            Log.d("TAG", "onReadRemoteRssi");
        }

        /*
        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
        */

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //super.onServicesDiscovered(gatt, status);

            Log.d("TAG", "onServicesDiscovered received status: " + status);
            List<BluetoothGattService> gattServices = gatt.getServices();
            for (BluetoothGattService service : gattServices) {
                if (service.getUuid().equals(UUID.fromString("06CCE3A0-AF8C-11E3-A5E2-0800200C9A66"))) {
                    List<BluetoothGattCharacteristic> gattCharacteristics = service.getCharacteristics();
                    Log.d("TAG", "Number of Characteristics for Service " + service.getUuid().toString() + ": " + gattCharacteristics.size());

                    for (BluetoothGattCharacteristic characteristic : gattCharacteristics) {
                        if (characteristic.getUuid().equals(UUID.fromString("06CCE3A2-AF8C-11E3-A5E2-0800200C9A66"))) {

                            //Enable local notifications
                            boolean characteristicNotified = gatt.setCharacteristicNotification(characteristic, true);
                            Log.d("TAG", "setCharacteristicNotification: " + characteristicNotified);

                            //Enable remote notifications
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                        //else if (characteristic.getUuid().equals(UUID.fromString("06CCE3A3-AF8C-11E3-A5E2-0800200C9A66"))) {
                        //}
                    }
                }
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //super.onConnectionStateChange(gatt, status, newState);

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
    };
}