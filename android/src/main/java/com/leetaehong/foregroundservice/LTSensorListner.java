package com.leetaehong.foregroundservice;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import java.util.List;


public class LTSensorListner implements SensorEventListener {
//    private final String TAG = "LTSensorListner";
    private SensorManager mSensorManager;
    private Sensor mStepCounter;
    private Context mContext;

    private long lastUpdate = 0;
    private int delay;


    public LTSensorListner(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        mContext = context;
    }

    public int start(int delay) {
        this.delay = delay;
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        if(sensors.size() > 0) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                removeListener(sensors);
            }
        }
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
//                int stepCount = (int)sensorEvent.values[0];
                Intent intent = new Intent(mContext.getApplicationContext(), LTForegroundRemoteService.class);
                intent.setAction(Constants.ACTION_FOREGROUND_SERVICE_REMOTE_UPDATE);
                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ call!!!!!!!!!!! onSensorChanged");
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mContext.startForegroundService(intent);
                } else {
                    mContext.startService(intent);
                }
                lastUpdate = curTime;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void removeListener(List<Sensor> sensors) {
        sensors.forEach(sensor -> {
            mSensorManager.unregisterListener(this,sensor);
        });
    }
}
