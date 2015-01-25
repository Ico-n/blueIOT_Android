package de.hdmstuttgart.blueiot;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class AccelerationSurfaceView extends SurfaceView {
    private Context context;

    private AccelerationSurfaceThread thread;
    public AccelerationSurfaceThread getThread() {
        return this.thread;
    }

    public AccelerationSurfaceView(Context context) {
        super(context);
        this.context = context;
    }

    public void initialize(final BluetoothDevice device) {
        SurfaceHolder surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //Start new 'Drawing'-Thread
                thread = new AccelerationSurfaceThread(holder, context, device);
                thread.setRunning(true);
                thread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                //Update the Surface Size
                thread.setSurfaceSize(width, height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                //Stop the Thread when the Surface is destroyed
                boolean retry = true;
                thread.setRunning(false);
                while (retry) {
                    try {
                        thread.join();
                        retry = false;
                    } catch (InterruptedException ex) {}
                }
            }
        });
    }
}