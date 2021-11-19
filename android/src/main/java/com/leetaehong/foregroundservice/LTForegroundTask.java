package com.leetaehong.foregroundservice;

import static com.leetaehong.foregroundservice.Constants.BACKGROUND_CONFIG;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

public class LTForegroundTask extends HeadlessJsTaskService {

    @Override
    protected @Nullable
    HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Bundle backgroundConfig = extras.getBundle(BACKGROUND_CONFIG);
            String taskName = backgroundConfig.getString("taskName");
            return new HeadlessJsTaskConfig(
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
                            .buildNotification(getApplicationContext(), notificationConfig);

                    startForeground((int)notificationConfig.getDouble("id"), notification);
                }
            } else {
                stopSelf();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
