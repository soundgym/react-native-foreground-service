package com.leetaehong.foregroundservice;

import static com.leetaehong.foregroundservice.Constants.MSG_ADDED_VALUE;
import static com.leetaehong.foregroundservice.Constants.MSG_CLIENT_CONNECT;
import static com.leetaehong.foregroundservice.Constants.NOTIFICATION_CONFIG;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;


public class LTSensorListner implements SensorEventListener {
    private final String TAG = "LTSensorListner";
    private SensorManager mSensorManager;
    private Sensor mStepCounter;

    private Messenger mServiceCallback = null;
    private Messenger mClientCallback = null;

    private long lastUpdate = 0;
    private int delay;


    public LTSensorListner(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        mClientCallback = new Messenger(new CallbackHandler(Looper.getMainLooper()));

    }

    public int start(int delay) {
        this.delay = delay;
        mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (mStepCounter != null) {
            mSensorManager.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_FASTEST);
            return 1;
        }
        return 0;
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > delay) {
                int stepCount = (int)sensorEvent.values[0];


                lastUpdate = curTime;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mServiceCallback = new Messenger(service);

            // connect to service
            Message connect_msg = Message.obtain( null, MSG_CLIENT_CONNECT);
            connect_msg.replyTo = mClientCallback;
            try {
                mServiceCallback.send(connect_msg);
                Log.d(TAG, "Send MSG_CLIENT_CONNECT message to Service");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mServiceCallback = null;
        }
    };

    private class CallbackHandler extends Handler
    {

        public CallbackHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADDED_VALUE:
                    Log.d(TAG, "Recevied MSG_ADDED_VALUE message from service ~ value :" + msg.arg1);
                    break;
            }
        }
    }
}
