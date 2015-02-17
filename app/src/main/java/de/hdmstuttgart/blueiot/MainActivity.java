package de.hdmstuttgart.blueiot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

/**
 * The MainActivity is the Activity that is shown when the application is started.
 * It uses a ListView in order to display BluetoothDevices that were found while scanning.
 * Displaying each of the devices as a ListItem (Custom View, see 'res\layout\listitem_device.xml') is done by the associated BleDeviceListAdapter.
 * Apart from the ListView, the ActionBar (see 'res\menu\menu_main.xml') is the main component of this Activity, allowing the user to initiate a Bluetooth-Scan
 */
public class MainActivity extends ActionBarActivity {
    //Custom Adapter for the ListView
    private BleDeviceListAdapter bleDeviceListAdapter;

    //Used for triggering (asynchronous) events
    private Handler handler;

    //Helper fields for Bluetooth-Connectivity
    private boolean isScanning;
    private boolean isBluetoothSupported;
    private boolean isBleSupported;

    //BluetoothAdapter of the device
    private BluetoothAdapter bluetoothAdapter;

    //Context Menu IDs
    private static final int CONTEXT_MENU_INSPECT = 0;
    private static final int CONTEXT_MENU_BALANCE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup for the ListView and its Adapter
        ListView listView = (ListView) this.findViewById(R.id.listView);
        this.bleDeviceListAdapter = new BleDeviceListAdapter(this);
        listView.setAdapter(this.bleDeviceListAdapter);

        //Used for asynchronous tasks
        this.handler = new Handler();

        //Bluetooth Components
        BluetoothManager bluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            this.bluetoothAdapter = bluetoothManager.getAdapter();
        }

        //Check if Bluetooth is supported on the device
        if (this.bluetoothAdapter != null) {
            this.isBluetoothSupported = true;

            //Check if BLE is supported on the device
            if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                this.isBleSupported = true;
            }
        }

        //Handle clicks being made onto the ListView's Items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //If still scanning for BLE-Devices, stop scanning immediately before starting the new Activity
                if (isScanning) {
                    scanLeDevice(false);
                }

                BluetoothDevice device = bleDeviceListAdapter.getDevice(position);
                if (device != null) {
                    if ((device.getName() != null && device.getName().contains(BlueIOTHelper.BLUEIOT_DEVICE_NAME)) || device.getAddress().equals(BlueIOTHelper.BLUEIOT_DEVICE_ADDRESS)) {
                        //Pass over the BluetoothDevice to the new Activity using the Intent
                        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                        intent.putExtra("device", device);

                        //Start DetailActivity
                        startActivity(intent);
                    }
                }
                else {
                    //Unable to find the BluetoothDevice in the ListAdapter
                    Toast.makeText(MainActivity.this, "Unable to get the selected Bluetooth Device. Try scanning again...", Toast.LENGTH_LONG).show();
                    bleDeviceListAdapter.clear();
                }
            }
        });

        //Enable the ContextMenu for the ListView
        this.registerForContextMenu(listView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_scan:
                //Check if Bluetooth is supported on the device
                if (this.isBluetoothSupported) {
                    //Check if BLE is supported on the device
                    if (this.isBleSupported) {
                        //Check if Bluetooth is currently enabled
                        if (this.bluetoothAdapter.isEnabled()) {
                            //If already scanning, stop the scan. Else, start a new scan
                            scanLeDevice(!this.isScanning);
                        }
                        else {
                            //Show a Dialog, allowing the User to turn Bluetooth ON
                            //Start scanning for BLE-Devices if the User does turn it on (see onActivityResult())
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            this.startActivityForResult(enableBtIntent, 1337);
                        }
                    }
                    else {
                        Toast.makeText(this, "Can't Scan: BLE not supported.", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toast.makeText(this, "Can't Scan: Bluetooth not supported.", Toast.LENGTH_LONG).show();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Stop scanning and clear any data from the Adapter
        scanLeDevice(false);
        this.bleDeviceListAdapter.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Check for predefined 'Enable-Bluetooth-Intent'
        if (requestCode == 1337) {
            //If Bluetooth has been activated, start scanning
            if (resultCode == -1) {
                if (!this.isScanning) {
                    scanLeDevice(true);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        //Get Reference to the Item that was selected
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        BluetoothDevice device = this.bleDeviceListAdapter.getDevice(acmi.position);

        //Set the header title
        if (device.getName() != null) {
            menu.setHeaderTitle(device.getName());
        }
        else {
            if (device.getAddress() != null) {
                menu.setHeaderTitle(device.getAddress());
            }
            else {
                menu.setHeaderTitle("Bluetooth Device");
            }
        }

        //Add Context Menu Items
        menu.add(Menu.NONE, CONTEXT_MENU_INSPECT, Menu.NONE, "Inspect Device");
        menu.add(Menu.NONE, CONTEXT_MENU_BALANCE, Menu.NONE, "Balance Ball");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case CONTEXT_MENU_INSPECT: {
                //Stop scanning
                if (this.isScanning) {
                    scanLeDevice(false);
                }

                //Start an Activity, that allows inspecting the BLE-Device
                BluetoothDevice device = this.bleDeviceListAdapter.getDevice(acmi.position);
                Intent intent = new Intent(this, InspectDeviceActivity.class);
                intent.putExtra("device", device);

                startActivity(intent);

                return true;
            }
            case CONTEXT_MENU_BALANCE: {
                //Stop scanning
                if (this.isScanning) {
                    scanLeDevice(false);
                }

                BluetoothDevice device = this.bleDeviceListAdapter.getDevice(acmi.position);
                if (device != null) {
                    //Allow blueIOT ONLY!
                    if ((device.getName() != null && device.getName().contains(BlueIOTHelper.BLUEIOT_DEVICE_NAME)) || device.getAddress().equals(BlueIOTHelper.BLUEIOT_DEVICE_ADDRESS)) {
                        //Start new Activity to start drawing
                        Intent intent = new Intent(this, DrawActivity.class);
                        intent.putExtra("device", device);
                        this.startActivity(intent);
                    }
                }
                else {
                    //Unable to find the BluetoothDevice in the ListAdapter
                    Toast.makeText(this, "Unable to get the selected Bluetooth Device. Try scanning again...", Toast.LENGTH_LONG).show();
                    this.bleDeviceListAdapter.clear();
                }

                return true;
            }
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Delete previously added devices when resuming --> forces a new scan
        if (this.bleDeviceListAdapter != null) {
            this.bleDeviceListAdapter.clear();
            this.bleDeviceListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Device Scan Callback adding detected BluetoothDevices into the ListAdapter.
     */
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

    /**
     * Initiates/Terminates a Scan for BluetoothDevices with the previously defined LeScanCallback
     * For higher API-Levels, don't use the deprecated methods
     * @param enable Indicates whether to start|stop the Scan-Process
     */
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
}