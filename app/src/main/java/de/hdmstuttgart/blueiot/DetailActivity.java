package de.hdmstuttgart.blueiot;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class DetailActivity extends ActionBarActivity {
    private BluetoothDevice device;
    private BluetoothGatt bluetoothGatt;

    private boolean isConnected;

    private GraphView graphView;
    private int x_Axis_Value = 0;

    //Series used for displaying an individual value from the blueIOT-Sensors
    private List<LineGraphSeries<DataPoint>> seriesCollection = new ArrayList<>();
    private LineGraphSeries<DataPoint> series_X;
    private LineGraphSeries<DataPoint> series_Y;
    private LineGraphSeries<DataPoint> series_Z;
    private LineGraphSeries<DataPoint> series_Height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //Setup Series
        initializeSeries();

        //Setup GraphView
        initializeGraphView();

        //Initialize BluetoothDevice
        this.device = this.getIntent().getParcelableExtra("device");

        //Initiate connection process
        if (this.device != null && !this.isConnected) {
            connectToBlueIOT();
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
        switch (id) {
            case R.id.action_startStopDrawing:
                if (this.isConnected) {
                    if (this.device != null && this.bluetoothGatt != null) {
                        disconnectFromBlueIOT();
                        item.setTitle(R.string.action_detailActivity_startDrawing);
                        item.setIcon(R.drawable.ic_action_play_over_video);
                    }
                }
                else {
                    if (this.device != null && this.bluetoothGatt != null) {
                        connectToBlueIOT();
                        item.setTitle(R.string.action_detailActivity_stopDrawing);
                        item.setIcon(R.drawable.ic_action_pause_over_video);
                    }
                }

                return true;
            case R.id.action_clearData:
                clearGraphViewData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (this.bluetoothGatt != null && this.isConnected) {
            try {
                disconnectFromBlueIOT();
            } catch (Exception ex) {}
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (this.bluetoothGatt != null && this.isConnected) {
            try {
                disconnectFromBlueIOT();
            } catch (Exception ex) {}
        }
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //Start discovering all Services on the BLE-Remote-Device (i.e. blueIOT)
                gatt.discoverServices();
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
                    gatt.setCharacteristicNotification(characteristic, true);

                    //Enable remote notifications on the BLE-Server (i.e. blueIOT)
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                    if (descriptor != null) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);

                        /*
                         *   After both types of notifications have been set, the BLE-Remote-Device will continuously push new values
                         *   into the Android-App. These push notifications will be dealt with inside regular calls to onCharacteristicChanged()
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

            //Read the String value from the Characteristic with Offset = 0
            String value = characteristic.getStringValue(0);
            try {
                /*
                 *    The Value will be one String, containing X,Y and Z from the Accelerometer and the Altitude from the Barometer
                 *    All Values will be separated by a comma (",") in the above mentioned order
                 */

                String[] values = value.split(",");
                if (values.length == 4) {
                    final float x = Float.parseFloat(values[0].trim());
                    final float y = Float.parseFloat(values[1].trim());
                    final float z = Float.parseFloat(values[2].trim());
                    final float height = Float.parseFloat(values[3].trim());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            series_X.appendData(new DataPoint(x_Axis_Value, x), false, 50);
                            series_Y.appendData(new DataPoint(x_Axis_Value, y), false, 50);
                            series_Z.appendData(new DataPoint(x_Axis_Value, z), false, 50);
                            series_Height.appendData(new DataPoint(x_Axis_Value, height), false, 50);
                            x_Axis_Value = x_Axis_Value + 1;
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

    /*
    public void readOnce(View view) {
        if (this.device != null) {
            BluetoothGattCallback cb = new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    //super.onConnectionStateChange(gatt, status, newState);

                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        boolean success = gatt.discoverServices();
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    //super.onServicesDiscovered(gatt, status);

                    BluetoothGattService gattService = gatt.getService(UUID.fromString("06CCE3A0-AF8C-11E3-A5E2-0800200C9A66"));
                    if (gattService != null) {
                        BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(UUID.fromString("06CCE3A2-AF8C-11E3-A5E2-0800200C9A66"));
                        if (characteristic != null) {
                            gatt.readCharacteristic(characteristic);
                        }
                    }
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    //super.onCharacteristicRead(gatt, characteristic, status);

                    String value = characteristic.getStringValue(0);
                    Log.d("readOnce", "onCharacteristicRead Value: " + value);
                }
            };

            this.device.connectGatt(this, false, cb);
        }
    }
    */

    /*
    public void writeToBlueIOT(View view) {
        if (this.device != null) {
            BluetoothGattCallback cb = new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    //super.onConnectionStateChange(gatt, status, newState);

                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        boolean success = gatt.discoverServices();
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    //super.onServicesDiscovered(gatt, status);

                    BluetoothGattService gattService = gatt.getService(UUID.fromString("06CCE3A0-AF8C-11E3-A5E2-0800200C9A66"));
                    if (gattService != null) {
                        BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(UUID.fromString("06CCE3A3-AF8C-11E3-A5E2-0800200C9A66"));
                        if (characteristic != null) {
                            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                            characteristic.setValue(17, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                            gatt.writeCharacteristic(characteristic);
                        }
                    }
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    //super.onCharacteristicWrite(gatt, characteristic, status);

                    Log.d("WRITETOBLUEIOT", "onCharacteristicWrite: Status: " + status);
                }
            };

            this.device.connectGatt(this, false, cb);
        }
    }
    */

    private void connectToBlueIOT() {
        if (!this.isConnected) {
            this.bluetoothGatt = this.device.connectGatt(this, false, this.gattCallback);
            this.isConnected = true;
        }
    }

    private void disconnectFromBlueIOT() {
        if (this.isConnected) {
            this.bluetoothGatt.disconnect();
            this.isConnected = false;
        }
    }

    private void initializeSeries() {
        //Series configuration
        this.series_X = new LineGraphSeries<>();
        this.series_X.setTitle("X-Axis");
        this.series_X.setColor(Color.BLACK);

        this.series_Y = new LineGraphSeries<>();
        this.series_Y.setTitle("Y-Axis");
        this.series_Y.setColor(Color.BLUE);

        this.series_Z = new LineGraphSeries<>();
        this.series_Z.setTitle("Z-Axis");
        this.series_Z.setColor(Color.RED);

        this.series_Height = new LineGraphSeries<>();
        this.series_Height.setTitle("Height");
        this.series_Height.setColor(Color.GREEN);

        //TODO
        //To be removed
        this.seriesCollection.add(this.series_X);
        this.seriesCollection.add(this.series_Y);
        this.seriesCollection.add(this.series_Z);
        this.seriesCollection.add(this.series_Height);
    }

    private void initializeGraphView() {
        //Setup GraphView
        this.graphView = (GraphView) this.findViewById(R.id.graph);

        this.graphView.addSeries(this.series_X);
        this.graphView.addSeries(this.series_Y);
        this.graphView.addSeries(this.series_Z);
        this.graphView.addSeries(this.series_Height);

        this.graphView.getLegendRenderer().setVisible(true);
        this.graphView.getLegendRenderer().setTextSize(20);
        this.graphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    private void clearGraphViewData() {

        //TODO
        //Implement generic approach using this.graphView.getSeries()

        for (LineGraphSeries<DataPoint> series : this.seriesCollection) {
            Iterator<DataPoint> iterator = series.getValues(this.x_Axis_Value - 1, this.x_Axis_Value);
            DataPoint dataPoint = null;
            while (iterator.hasNext()) {
                dataPoint = iterator.next();
            }

            if (dataPoint != null) {
                series.resetData(new DataPoint[] { new DataPoint(0, dataPoint.getY()) });
            }
            else {
                series.resetData(new DataPoint[] { new DataPoint(0, 0) });
            }
        }

        this.x_Axis_Value = 1;
    }
}