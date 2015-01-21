package de.hdmstuttgart.blueiot;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class DrawActivity extends ActionBarActivity {
    private AccelerationSurfaceView accelerationSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_draw);

        //Initialize BluetoothDevice
        BluetoothDevice device = this.getIntent().getParcelableExtra("device");
        
        this.accelerationSurfaceView = new AccelerationSurfaceView(this);
        this.accelerationSurfaceView.initialize(device);

        setContentView(this.accelerationSurfaceView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_draw, menu);
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
    protected void onPause() {
        super.onPause();

        //Stop the Thread that's drawing continuously on the SurfaceView
        //--> onSurfaceDestroyed-Callback is already too late to join the Thread
        this.accelerationSurfaceView.getThread().setRunning(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}