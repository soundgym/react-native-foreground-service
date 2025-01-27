package com.leetaehong.foregroundservice;

import static com.leetaehong.foregroundservice.Constants.BACKGROUND_CONFIG;
import static com.leetaehong.foregroundservice.Constants.MSG_ADD_VALUE;
import static com.leetaehong.foregroundservice.Constants.MSG_CLIENT_CONNECT;
import static com.leetaehong.foregroundservice.Constants.MSG_CLIENT_DISCONNECT;
import static com.leetaehong.foregroundservice.Constants.NOTIFICATION_CONFIG;
import static com.leetaehong.foregroundservice.NotificationHelper.NotificationType;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.facebook.react.jstasks.HeadlessJsTaskContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LTForegroundTask extends HeadlessJsTaskService {

    @Override
    protected @Nullable
    HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Bundle notificationConfig = extras.getBundle(BACKGROUND_CONFIG);
            String taskName = notificationConfig.getString("taskName");
            if(taskName == null) {
                taskName = "BackgroundTask";
            }
            return  new HeadlessJsTaskConfig(
                    taskName,
                    Arguments.fromBundle(extras),
                    0, // timeout for the task
                    true // optional: defines whether or not  the task is allowed in foreground. Default is false
            );
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if (extras == null) {
            throw new IllegalArgumentException("Extras cannot be null");
        }
        if (action != null) {
            if (action.equals(Constants.ACTION_FOREGROUND_SERVICE_START)) {
                Bundle notificationConfig = intent.getExtras().getBundle(BACKGROUND_CONFIG);
                if (notificationConfig != null && notificationConfig.containsKey("id")) {
                    Notification notification = NotificationHelper.getInstance(getApplicationContext())
                            .buildNotification(getApplicationContext(), notificationConfig,NotificationType.BACKGROUND);
                    if(notificationConfig.getBoolean("ongoing")) {
                        notification.flags |= Notification.FLAG_ONGOING_EVENT;
                        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                    }
                    startForeground((int)notificationConfig.getDouble("id"), notification);
                }
            } else if (action.equals(Constants.ACTION_FOREGROUND_SERVICE_STOP)) {
                stopSelf();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }


}
