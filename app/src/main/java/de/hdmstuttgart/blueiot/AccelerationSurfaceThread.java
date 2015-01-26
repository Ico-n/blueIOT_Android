package de.hdmstuttgart.blueiot;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;

import java.util.UUID;

public class AccelerationSurfaceThread extends Thread {
    private SurfaceHolder surfaceHolder;
    private Context context;

    private Paint paint = new Paint();

    private boolean run = false;
    private boolean isConnected;

    //Display size, initially set within setSurfaceSize(width, height)
    private int canvasWidth;
    private int canvasHeight;

    //X and Y values, used for drawing onto the Canvas
    private float x;
    private float y;

    private BluetoothDevice device;
    private BluetoothGatt bluetoothGatt;

    public AccelerationSurfaceThread(SurfaceHolder surfaceHolder, Context context, BluetoothDevice device) {
        this.surfaceHolder = surfaceHolder;
        this.context = context;

        this.paint.setColor(Color.BLUE);
        this.paint.setStyle(Paint.Style.FILL);

        this.device = device;

        //Initiate connection process
        if (this.device != null && !this.isConnected) {
            connectToBlueIOT();
        }
    }

    @Override
    public void run() {
        super.run();

        while (this.run) {
            Canvas canvas = null;
            try {
                //Lock the Canvas, then draw onto it
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (this.surfaceHolder) {
                    doDraw(canvas);
                }
            }
            finally {
                //Unlock Canvas and post it back
                if (canvas != null) {
                    this.surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }

        disconnectFromBlueIOT();
    }

    private void doDraw(Canvas canvas) {
        //Save --> Draw --> Restore
        canvas.save();
        canvas.drawColor(Color.WHITE);
        canvas.drawCircle(this.x, this.y, 50, this.paint);
        canvas.restore();
    }

    private void doStart() {
        //Put the circle in the middle of the Canvas
        synchronized (this.surfaceHolder) {
            this.x = this.canvasWidth / 2;
            this.y = this.canvasHeight / 2;
        }
    }

    public void setSurfaceSize(int width, int height) {
        //Initial Setup for the Surface
        synchronized (this.surfaceHolder) {
            this.canvasWidth = width;
            this.canvasHeight = height;
            doStart();
        }
    }

    public void setRunning(boolean b) {
        this.run = b;
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //Start discovering all Services on the BLE-Remote-Device (i.e. blueIOT)
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
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
            //Read the String value from the Characteristic with Offset = 0
            String value = characteristic.getStringValue(0);
            try {
                /*
                 *    The Value will be one String, containing X,Y and Z from the Accelerometer and the Altitude from the Barometer
                 *    All Values will be separated by a comma (",") in the above mentioned order
                 */

                String[] values = value.split(",");
                if (values.length == 4) {
                    final float xAcceleration = Float.parseFloat(values[0].trim());
                    final float yAcceleration = Float.parseFloat(values[1].trim());
                    //final float zAcceleration = Float.parseFloat(values[2].trim());
                    //final float height = Float.parseFloat(values[3].trim());

                    //Update X and Y
                    x += yAcceleration / 100;
                    y += xAcceleration / 100;

                    //Stay within the Display-Bounds for X and Y
                    if (x < 25) {
                        x = 25;
                    }
                    if (x > canvasWidth - 25) {
                        x = canvasWidth - 25;
                    }

                    if (y < 25) {
                        y = 25;
                    }
                    if (y > canvasHeight - 25) {
                        y = canvasHeight - 25;
                    }
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
        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
        */
    };

    private void connectToBlueIOT() {
        if (!this.isConnected) {
            this.bluetoothGatt = this.device.connectGatt(this.context, false, this.gattCallback);
            this.isConnected = true;
        }
    }

    private void disconnectFromBlueIOT() {
        if (this.isConnected && this.bluetoothGatt != null) {
            this.bluetoothGatt.disconnect();
            this.isConnected = false;
        }
    }
}