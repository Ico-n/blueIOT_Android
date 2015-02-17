# Android Application for blueIOT #

## Description ##

The blueIOT Android (4.4.2) application is used together with the blueIOT device ("Platinchen") from [Fab-Lab](http://www.fab-lab.eu/blueiot/). The app was developed with the intent to use blueIOT as a smartwatch prototype, using data from various sensors (e.g. Accelerometer, UV-Sensor or Barometer) and make use of this data on your smartphone.

This project might be of interest to you, if you're working with blueIOT as well, but also if you're working with any other custom (Arduino-Like) device that communicates via Bluetooth-Low-Energy and you're trying to make use of sensor data for a smartphone application.

The app communicates with blueIOT using the Bluetooth-Low-Energy (BLE) standard and reads / displays the accelerometer values within the application. Also, the value for the relative height of the device is being used at this point. The BLE-Standard allows extremely low power consumption when compared to the regular Bluetooth-Standard and is thus optimal for a wearable device that can run solely on a 3.3V coin cell.

The acceleration values (X, Y and Z-axis, as well as the height value) can be displayed in a line chart (we're using [GraphView](http://www.android-graphview.org/) to get this done) or visualized by a moving ball on the screen by moving blueIOT around accordingly. Furthermore, the application allows detecting and scanning remote BLE-devices and displays all of their offered services and characteristics within the application. (It might be helpful to have a thorough look into the [Android Bluetooth-Low-Energy API](https://developer.android.com/guide/topics/connectivity/bluetooth-le.html) in order to see how communication to a remote BLE-device can be achieved)

## Setup / Configuration ##

In order to get this project running, you will need the [Android Studio IDE](http://developer.android.com/sdk/index.html) which uses the gradle build automation system. Make sure, that the dependencies for the AppCompat-Library and the GraphView in the build-file are inserted correctly:

```
#!java
compile 'com.android.support:appcompat-v7:21.0.2'
compile 'com.jjoe64:graphview:4.0.0'
```

Also, you will need an Android device that

* Runs on KitKat (4.4) or higher
* Supports the Bluetooth-Low-Energy standard

**Note:** The Android emulator itself does not support BLE. Thus, you will need a real device.

In addition to these requisites, you must configure the *BlueIOTHelper*-Class to return the correct values for your specific blueIOT-device. An example configuration could look like this:


```
#!java
public class BlueIOTHelper {
    public static final String BLUEIOT_DEVICE_NAME = "iBeacon";
    public static final String BLUEIOT_DEVICE_ADDRESS = "00:07:80:7F:A6:E0";
    public static final String BLUEIOT_PRIMARY_SERVICE_UUID = "06CCE3A0-AF8C-11E3-A5E2-0800200C9A66";
    public static final String BLUEIOT_CHARACTERISTIC_NOTIFICATION_UUID = "06CCE3A2-AF8C-11E3-A5E2-0800200C9A66";
    public static final String BLUEIOT_DESCRIPTOR_NOTIFICATION_UUID = "00002902-0000-1000-8000-00805f9b34fb";
}
```

Make sure to adjust these values for your specific blueIOT-device, otherwise you won't be able to see anything because these values will be used later in the process of communicating with blueIOT.

## Getting Started ##

1. Discover remote BLE-devices
2. Display sensor data in line chart
3. Scan BLE-device for services and characteristics
4. Visualize sensor data by balancing the ball

### Discovering Devices ###

Start discovering remote BLE-Devices by clicking on the button in the ActionBar.

//TODO: Screenshot

This will initiate a BLE-Scan (5 second duration only, because it is battery-intensive) for remote devices. Internally, a scan-method is called on the BluetoothManager-Object with a predefined ScanCallback (handling actions to be done for each device that is detected), as shown below:

```
#!java
//Stops scanning after a defined scan period
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bluetoothAdapter.stopLeScan(leScanCallback);
                }
            }, 5000);
```


Any device that is found during the scan, will be added into the ListView as a separate ListItem, that will then be used for further actions.

### Displaying data in a Line Chart ###

In order to see the sensor values from the accelerometer in a line chart, simply tap onto your blueIOT-device, that - after a successful scan - should appear as a ListItem in the ListView.

Note: only blueIOT allowed as of yet

//TODO: Screenshot

### Scanning a Device ###

//TODO: Screenshot

### Balancing the ball ###