/*
 * Copyright (c) 2011-2019, Zingaya, Inc. All rights reserved.
 */

package com.leetaehong.foregroundservice;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Intent;
import com.leetaehong.foregroundservice.NotificationHelper.NotificationType;
import android.os.Bundle;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import static com.leetaehong.foregroundservice.Constants.ERROR_INVALID_CONFIG;
import static com.leetaehong.foregroundservice.Constants.ERROR_SERVICE_ERROR;
import static com.leetaehong.foregroundservice.Constants.NOTIFICATION_CONFIG;
import static com.leetaehong.foregroundservice.Constants.BACKGROUND_CONFIG;

public class LTForegroundServiceModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public LTForegroundServiceModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
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
            mNotificationHelper.scheduleAlarm(this.reactContext);
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
}