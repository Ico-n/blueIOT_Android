# Android Application for blueIOT #

### Description ###

The blueIOT Android (4.4.2) application is used together with the blueIOT device ("Platinchen") from [Fab-Lab](http://www.fab-lab.eu/blueiot/). The app was developed with the intent to use blueIOT as a smartwatch prototype, using data from various sensors (e.g. Accelerometer, UV-Sensor or Barometer) and make use of this data on your smartphone.

This project might be of interest to you, if you're working with blueIOT as well, but also if you're working with any other custom (Arduino-Like) device that communicates via Bluetooth-Low-Energy and you're trying to make use of sensor data for a smartphone application.

The app communicates with blueIOT using the Bluetooth-Low-Energy (BLE) standard and reads / displays the accelerometer values within the application. Also, the value for the relative height of the device is being used at this point. The BLE-Standard allows extremely low power consumption when compared to the regular Bluetooth-Standard and is thus optimal for a wearable device that can run solely on a 3.3V coin cell.

The acceleration values (X, Y and Z-axis, as well as the height value) can be displayed in a line chart (we're using [GraphView](http://www.android-graphview.org/) to get this done) or visualized by a moving ball on the screen by moving blueIOT around accordingly. Furthermore, the application allows detecting and scanning remote BLE-devices and displays all of their offered services and characteristics within the application. (It might be helpful to have a thorough look into the [Android Bluetooth-Low-Energy API](https://developer.android.com/guide/topics/connectivity/bluetooth-le.html) in order to see how communication to a remote BLE-device can be achieved)

### Setup / Configuration ###

* Summary of set up
* Configuration
* Dependencies
* Database configuration
* How to run tests
* Deployment instructions