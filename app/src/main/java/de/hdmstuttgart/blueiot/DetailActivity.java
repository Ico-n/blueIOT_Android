package de.hdmstuttgart.blueiot;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.UUID;

public class DetailActivity extends Activity {

    private TextView textView_X;
    private TextView textView_Y;
    private TextView textView_Z;
    private TextView textView_Height;

    private BluetoothDevice device;
    private BluetoothGatt bluetoothGatt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        textView_X = (TextView) findViewById(R.id.textView_X);
        textView_Y = (TextView) findViewById(R.id.textView_Y);
        textView_Z = (TextView) findViewById(R.id.textView_Z);
        textView_Height = (TextView) findViewById(R.id.textView_Height);

        //Initialize BlueotothDevice
        device = this.getIntent().getParcelableExtra("device");

        //Initiate connection process
        if (device != null) {
            bluetoothGatt = device.connectGatt(this, false, gattCallback);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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

    //TODO
    //Override onPause && onStop in order to disconnect from the remote BLE-Device

    @Override
    protected void onPause() {
        super.onPause();

        /*
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
        */
    }

    @Override
    protected void onStop() {
        super.onStop();

        /*
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
        */
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("TAG", "Connected to GATT-Server");

                //Start discovering all Services on the BLE-Remote-Device (i.e. blueIOT)
                boolean success = gatt.discoverServices();
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("TAG", "Disconnected from GATT-Server");
            }
            else {
                Log.d("TAG", "onConnectionStateChange status: " + status + ", newState: " + newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //super.onServicesDiscovered(gatt, status);

            BluetoothGattService gattService = gatt.getService(UUID.fromString("06CCE3A0-AF8C-11E3-A5E2-0800200C9A66"));
            if (gattService != null) {
                BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(UUID.fromString("06CCE3A2-AF8C-11E3-A5E2-0800200C9A66"));
                if (characteristic != null) {
                    //Enable local notifications (i.e. Android-Application)
                    boolean isNotificationSet = gatt.setCharacteristicNotification(characteristic, true);
                    Log.d("TAG", "setCharacteristicNotification Success: " + isNotificationSet);

                    //Enable remote notifications on the BLE-Server (i.e. blueIOT)
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                    if (descriptor != null) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);

                        /*
                        After both types of notifications have been set, the BLE-Remote-Device will continuously push new values
                        into the Android-App. These push notifications will be dealt with inside regular calls to onCharacteristicChanged()
                         */
                    }
                }
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
            //super.onCharacteristicChanged(gatt, characteristic);

            //ToDo
            //Regex

            //Read the String value from the Characteristic
            String value = characteristic.getStringValue(0);
            Log.d("TAG", "onCharacteristicChanged - Value: " + value);

            try {
                /*
                The Value will be one String, containing X,Y and Z from the Accelerometer and the Altitude from the Barometer
                All Values will be separated by a comma (",") in the above mentioned order
                 */
                String[] values = value.split(",");
                if (values.length == 4) {
                    final int x = Integer.parseInt(values[0].trim());
                    final int y = Integer.parseInt(values[1].trim());
                    final int z = Integer.parseInt(values[2].trim());
                    final int height = Integer.parseInt(values[3].trim());

                    //Display received values in the UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView_X.setText(String.valueOf(x));
                            textView_Y.setText(String.valueOf(y));
                            textView_Z.setText(String.valueOf(z));
                            textView_Height.setText(String.valueOf(height));
                        }
                    });
                }
            }
            catch (Exception ex) {}
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
        * Requires higher API-Level ...
        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
        */
    };
}