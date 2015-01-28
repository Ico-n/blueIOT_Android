package de.hdmstuttgart.blueiot;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Activity with the only UI-Component being a custom SurfaceView that can be drawn onto.
 */
public class DrawActivity extends ActionBarActivity {
    //Custom Surface View that can be drawn onto
    private AccelerationSurfaceView accelerationSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize BluetoothDevice
        BluetoothDevice device = this.getIntent().getParcelableExtra("device");

        //Instantiate new SurfaceView
        this.accelerationSurfaceView = new AccelerationSurfaceView(this);
        if (device != null) {
            //Pass over the BluetoothDevice and start the Drawing-Thread
            this.accelerationSurfaceView.initialize(device);
        }

        //Display the SurfaceView
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
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_setFadingEnabled:
                //Enable/Disable the fading-effect
                this.accelerationSurfaceView.getThread().setFadingEnabled();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Stop the Thread that's drawing continuously onto the SurfaceView
        //--> onSurfaceDestroyed-Callback is already too late to join the Thread
        this.accelerationSurfaceView.getThread().setRunning(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}