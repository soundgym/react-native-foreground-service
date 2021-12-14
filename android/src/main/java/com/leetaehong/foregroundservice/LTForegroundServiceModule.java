/*
 * Copyright (c) 2011-2019, Zingaya, Inc. All rights reserved.
 */

package com.leetaehong.foregroundservice;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.leetaehong.foregroundservice.NotificationHelper.NotificationType;

import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import static com.leetaehong.foregroundservice.Constants.ERROR_INVALID_CONFIG;
import static com.leetaehong.foregroundservice.Constants.ERROR_SERVICE_ERROR;
import static com.leetaehong.foregroundservice.Constants.MSG_ADDED_VALUE;
import static com.leetaehong.foregroundservice.Constants.MSG_CLIENT_CONNECT;
import static com.leetaehong.foregroundservice.Constants.NOTIFICATION_CONFIG;
import static com.leetaehong.foregroundservice.Constants.BACKGROUND_CONFIG;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class LTForegroundServiceModule extends ReactContextBaseJavaModule {
    private static final String TAG = "ForegroundServiceModule";

    private final ReactApplicationContext reactContext;

    private Messenger mServiceCallback = null;
    private Messenger mClientCallback = null;

    public LTForegroundServiceModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        mClientCallback = new Messenger(new CallbackHandler(Looper.getMainLooper()));
    }

    @Override
    public String getName() {
        return "LTForegroundService";
    }

    @ReactMethod
    public void createNotificationChannel(ReadableMap channelConfig, Promise promise) {
        if (channelConfig == null) {
            promise.reject(ERROR_INVALID_CONFIG, "LTForegroundService: Channel config is invalid");
            return;
        }
        NotificationHelper.getInstance(getReactApplicationContext()).createNotificationChannel(channelConfig, promise);
    }

    @ReactMethod
    public void startService(ReadableMap notificationConfig, Promise promise) {
        Boolean validResult = NotificationHelper.getInstance(getReactApplicationContext()).validCheckNotificationConfig(notificationConfig, promise);

        if (validResult) {
            Intent intent = new Intent(getReactApplicationContext(), LTForegroundService.class);
            intent.setAction(Constants.ACTION_FOREGROUND_SERVICE_START);
            intent.putExtra(NOTIFICATION_CONFIG, Arguments.toBundle(notificationConfig));
            ComponentName componentName = getReactApplicationContext().startService(intent);
            if (componentName != null) {
                promise.resolve(null);
            } else {
                promise.reject(ERROR_SERVICE_ERROR, "LTForegroundService: Foreground service is not started");
            }
        } else {
            promise.reject(ERROR_SERVICE_ERROR, "LTForegroundService: Foreground service is not started");
        }
    }

    @ReactMethod
    public void stopService(Promise promise) {
        Intent intent = new Intent(getReactApplicationContext(), LTForegroundService.class);
        intent.setAction(Constants.ACTION_FOREGROUND_SERVICE_STOP);
        boolean stopped = getReactApplicationContext().stopService(intent);
        if (stopped) {
            promise.resolve(null);
        } else {
            promise.reject(ERROR_SERVICE_ERROR, "LTForegroundService: Foreground service failed to stop");
        }
    }

    @ReactMethod
    public void updateService(ReadableMap notificationConfig, Promise promise) {
        Boolean validResult = NotificationHelper.getInstance(getReactApplicationContext()).validCheckNotificationConfig(notificationConfig, promise);
        if(validResult) {
            Bundle updateBundle = Arguments.toBundle(notificationConfig);
            NotificationHelper mNotificationHelper = NotificationHelper.getInstance(this.reactContext);
            NotificationType notificationType;
            if(updateBundle.getString("notificationType") == "BACKGROUND") {
                notificationType = NotificationType.BACKGROUND;
            } else {
                notificationType = NotificationType.FOREGROUND;
            }
            Notification updateNotification = mNotificationHelper.buildNotification(this.reactContext, updateBundle,notificationType);
            mNotificationHelper.updateNotification((int) updateBundle.getDouble("id"), updateNotification);
            if (updateNotification != null) {
                promise.resolve(null);
            } else {
                promise.reject(ERROR_SERVICE_ERROR, "LTForegroundService: Foreground service is not started");
            }
        } else {
            promise.reject(ERROR_SERVICE_ERROR, "LTForegroundService: Foreground service is not started");
        }
    }

    @ReactMethod
    public void backgroundStartService(ReadableMap backgroundConfig, Promise promise) {
        Boolean validResult = NotificationHelper.getInstance(getReactApplicationContext()).validCheckNotificationConfig(backgroundConfig, promise);

        if (validResult) {
            Intent intent = new Intent(getReactApplicationContext(), LTForegroundTask.class);
            intent.setAction(Constants.ACTION_FOREGROUND_SERVICE_START);
            intent.putExtra(BACKGROUND_CONFIG, Arguments.toBundle(backgroundConfig));
            ComponentName componentName = getReactApplicationContext().startService(intent);
            if (componentName != null) {
                promise.resolve(null);
            } else {
                promise.reject(ERROR_SERVICE_ERROR, "LTForegroundService: Foreground service is not started");
            }
        } else {
            promise.reject(ERROR_SERVICE_ERROR, "LTForegroundService: Foreground service is not started");
        }
    }

    @ReactMethod
    public void backgroundStopService(Promise promise) {
        Intent intent = new Intent(getReactApplicationContext(), LTForegroundTask.class);
        intent.setAction(Constants.ACTION_FOREGROUND_SERVICE_STOP);
        boolean stopped = getReactApplicationContext().stopService(intent);
        if (stopped) {
            promise.resolve(null);
        } else {
            promise.reject(ERROR_SERVICE_ERROR, "LTForegroundService: Foreground service failed to stop");
        }
    }

    @ReactMethod
    public void startRemoteService(ReadableMap notificationConfig, Promise promise) {
        Boolean validResult = NotificationHelper.getInstance(getReactApplicationContext()).validCheckNotificationConfig(notificationConfig, promise);

        if (validResult) {
            Intent intent = new Intent(getReactApplicationContext(), LTForegroundRemoteService.class);
            intent.setAction(Constants.ACTION_FOREGROUND_SERVICE_START);
            intent.putExtra(NOTIFICATION_CONFIG, Arguments.toBundle(notificationConfig));
            ComponentName componentName = getReactApplicationContext().startService(intent);
            getReactApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            if (componentName != null) {
                promise.resolve(null);
            } else {
                promise.reject(ERROR_SERVICE_ERROR, "LTForegroundService: Foreground service is not started");
            }
        } else {
            promise.reject(ERROR_SERVICE_ERROR, "LTForegroundService: Foreground service is not started");
        }
    }

    @ReactMethod
    public void updateRemoteService(ReadableMap notificationConfig, Promise promise) {
        Boolean validResult = NotificationHelper.getInstance(getReactApplicationContext()).validCheckNotificationConfig(notificationConfig, promise);

        if (validResult) {
            Bundle updateBundle = Arguments.toBundle(notificationConfig);
            Intent intent = new Intent(getReactApplicationContext(), LTForegroundRemoteService.class);
            intent.setAction(Constants.ACTION_FOREGROUND_SERVICE_UPDATE);
            intent.putExtra(NOTIFICATION_CONFIG, Arguments.toBundle(notificationConfig));
            getReactApplicationContext().startService(intent);
            if (updateBundle != null) {
                promise.resolve(null);
            } else {
                promise.reject(ERROR_SERVICE_ERROR, "LTForegroundService: Foreground service is not started");
            }
        } else {
            promise.reject(ERROR_SERVICE_ERROR, "LTForegroundService: Foreground service is not started");
        }
    }

    @ReactMethod
    public void stopRemoteService(Promise promise) {
        Intent intent = new Intent(getReactApplicationContext(), LTForegroundRemoteService.class);
        intent.setAction(Constants.ACTION_FOREGROUND_SERVICE_STOP);
        boolean stopped = getReactApplicationContext().stopService(intent);
        if (stopped) {
            promise.resolve(null);
        } else {
            promise.reject(ERROR_SERVICE_ERROR, "LTForegroundService: Foreground service failed to stop");
        }
    }

    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceCallback = new Messenger(service);

            // connect to service
            Message connect_msg = Message.obtain( null, MSG_CLIENT_CONNECT);
            connect_msg.replyTo = mClientCallback;
            try {
                mServiceCallback.send(connect_msg);
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
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("refresh",true);
                        sendEvent("fetchHealthData",obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
        }
    }

    private void setTimeout(Runnable runnable, int delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void sendEvent(String eventName, @Nullable Object params) {
        try {
            if(this.reactContext.hasActiveReactInstance()) {
                this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit(eventName, params);
            } else {
                setTimeout(() -> sendEvent(eventName,params),1000);
            }
        } catch (RuntimeException e) {
            Log.e("ERROR", "java.lang.RuntimeException: Trying to invoke JS before CatalystInstance has been set!");
        }
    }

}