package de.hdmstuttgart.blueiot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void checkBluetoothStats(View view) {
        //Get References for UI-Components
        TextView textView_Bluetooth = (TextView) findViewById(R.id.textView_BluetoothSupported);
        TextView textView_BLE = (TextView) findViewById(R.id.textView_BLESupported);
        TextView textView_BluetoothEnabled = (TextView) findViewById(R.id.textView_BluetoothEnabled);

        //Get References for Bluetooth Components
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        //Check if Bluetooth is supported on the device
        if (bluetoothAdapter == null) {
            textView_Bluetooth.setText("false");
            textView_BLE.setText("false");
            textView_BluetoothEnabled.setText("false");
        }
        else {
            //Bluetooth is supported
            textView_Bluetooth.setText("true");

            Switch switch_Bluetooth = (Switch) findViewById(R.id.switch_Bluetooth);
            switch_Bluetooth.setEnabled(true);
            switch_Bluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && !bluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, 1337);
                    }
                    else if (!isChecked && bluetoothAdapter.isEnabled()) {
                        //Turn Bluetooth OFF
                        bluetoothAdapter.disable();
                    }
                }
            });

            //Check if BLE is supported on the device
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                textView_BLE.setText("true");

                Button button_StartScanActivity = (Button) findViewById(R.id.button_StartScanActivity);
                if (!button_StartScanActivity.isEnabled()) {
                    button_StartScanActivity.setEnabled(true);
                }
            }
            else {
                textView_BLE.setText("false");
            }

            //Check if Bluetooth is currently enabled
            if (bluetoothAdapter.isEnabled()) {
                textView_BluetoothEnabled.setText("true");
            }
            else {
                textView_BluetoothEnabled.setText("false");
            }
        }
    }

    public void startScanActivity(View view) {
        //Start the new Activity for Scanning BLE-Devices
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }
}