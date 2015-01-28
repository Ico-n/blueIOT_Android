package de.hdmstuttgart.blueiot;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

/**
 * This Activity is used to display information that has been gathered on a remote BluetoothDevice.
 * It will display an ExpandableListView containing every BluetoothGattService and its associated BluetoothGattCharacteristics
 */
public class InspectDeviceActivity extends ActionBarActivity {
    private BluetoothDevice device;
    private BluetoothGatt bluetoothGatt;

    //Custom ListAdapter used for the ExpandableListView
    private BleExpandableListAdapter listAdapter;

    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspect_device);

        //Setup for the ListView and its Adapter
        ExpandableListView listView = (ExpandableListView) this.findViewById(R.id.expandableListView);
        this.listAdapter = new BleExpandableListAdapter(this);
        listView.setAdapter(this.listAdapter);

        //Get the BluetoothDevice from the Intent that started this Activity
        this.device = this.getIntent().getParcelableExtra("device");
        if (this.device != null) {
            //Set the Activity Title
            if (this.device.getName() != null && this.device.getName().length() > 0) {
                this.setTitle(this.device.getName());
            }
            else {
                this.setTitle(this.device.getAddress());
            }

            //Initiate connection process
            if (!this.isConnected) {
                connectToBleDevice();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_inspect_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_disconnect:
                disconnectFromBleDevice();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.listAdapter.clear();
        if (this.bluetoothGatt != null && this.isConnected) {
            try {
                disconnectFromBleDevice();
            } catch (Exception ex) {}
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        this.listAdapter.clear();
        if (this.bluetoothGatt != null && this.isConnected) {
            try {
                disconnectFromBleDevice();
            } catch (Exception ex) {}
        }
    }

    /**
     * BluetoothGattCallback that is used to connect to a remote BLE-Device.
     * Defines callback-methods that are used for each step in the process of connecting/reading/writing for that device.
     */
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //Start discovering all Services on the BLE-Remote-Device
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //super.onServicesDiscovered(gatt, status);

            //Iterate all services & save the service itself and all of the characteristics included into the adapter
            for (final BluetoothGattService service : gatt.getServices()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listAdapter.addService(service);
                        listAdapter.addCharacteristics(service, service.getCharacteristics());
                        listAdapter.notifyDataSetChanged();
                    }
                });
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        /*
        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
        */
    };

    /**
     * Connects to a remote BLE-Device using the predefined BluetoothGattCallback
     */
    private void connectToBleDevice() {
        if (!this.isConnected) {
            this.bluetoothGatt = this.device.connectGatt(this, false, this.gattCallback);
            this.isConnected = true;
        }
    }

    /**
     * Disconnects from a remote BLE-Device
     */
    private void disconnectFromBleDevice() {
        if (this.isConnected && this.bluetoothGatt != null) {
            this.bluetoothGatt.disconnect();
            this.isConnected = false;
        }
    }
}