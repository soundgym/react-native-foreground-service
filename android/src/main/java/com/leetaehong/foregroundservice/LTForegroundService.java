/*
 * Copyright (c) 2011-2019, Zingaya, Inc. All rights reserved.
 */

package com.leetaehong.foregroundservice;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import com.leetaehong.foregroundservice.NotificationHelper.NotificationType;

import static com.leetaehong.foregroundservice.Constants.NOTIFICATION_CONFIG;

public class LTForegroundService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(Constants.ACTION_FOREGROUND_SERVICE_START)) {
                if (intent.getExtras() != null && intent.getExtras().containsKey(NOTIFICATION_CONFIG)) {
                    Bundle notificationConfig = intent.getExtras().getBundle(NOTIFICATION_CONFIG);
                    if (notificationConfig != null && notificationConfig.containsKey("id")) {
                        Notification notification = NotificationHelper.getInstance(getApplicationContext())
                                .buildNotification(getApplicationContext(), notificationConfig, NotificationType.FOREGROUND);
                        if(notificationConfig.getBoolean("ongoing")) {
                            notification.flags |= Notification.FLAG_ONGOING_EVENT;
                            notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                        }
                        startForeground((int)notificationConfig.getDouble("id"), notification);
                    }
                }
            } else if (action.equals(Constants.ACTION_FOREGROUND_SERVICE_STOP)) {
                stopSelf();
            }
        }
        return START_NOT_STICKY;

    }
}