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
                thread = new AccelerationSurfaceThread(holder, context, device);
                thread.setRunning(true);
                thread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                thread.setSurfaceSize(width, height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
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