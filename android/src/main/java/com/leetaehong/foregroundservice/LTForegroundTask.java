package com.leetaehong.foregroundservice;

import static com.leetaehong.foregroundservice.Constants.BACKGROUND_CONFIG;
import static com.leetaehong.foregroundservice.Constants.NOTIFICATION_CONFIG;
import static com.leetaehong.foregroundservice.NotificationHelper.NotificationType;
import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.facebook.react.jstasks.HeadlessJsTaskContext;

import java.util.HashMap;
import java.util.Map;

public class LTForegroundTask extends HeadlessJsTaskService {

    HeadlessJsTaskConfig headlessJsTaskConfig;

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
            headlessJsTaskConfig =  new HeadlessJsTaskConfig(
                    taskName,
                    Arguments.fromBundle(extras),
                    0, // timeout for the task
                    true // optional: defines whether or not  the task is allowed in foreground. Default is false
            );
            return headlessJsTaskConfig;
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
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent newIntent = new Intent(getApplicationContext(),this.getClass());
        Map<String, Object> notificationConfig = new HashMap();
        notificationConfig.put("id",9600);
        notificationConfig.put("title","걸음수!!!");
        notificationConfig.put("icon","ic_stat_ic_notification");
        notificationConfig.put("priority",-2);
        notificationConfig.put("ongoing",true);
        notificationConfig.put("notificationType",NotificationType.BACKGROUND);
        notificationConfig.put("text", "9899 (보)");
        notificationConfig.put("channelId","SoundgymForegroundServiceChannel");
//        newIntent.putExtra(NOTIFICATION_CONFIG, Arguments.toBundle((ReadableMap) notificationConfig));
        Notification updateNotification = NotificationHelper.getInstance(getApplicationContext()).buildNotification(getApplicationContext(), Arguments.toBundle((ReadableMap) notificationConfig),NotificationType.BACKGROUND);
        NotificationHelper.getInstance(getApplicationContext()).updateNotification((int) ((ReadableMap) notificationConfig).getDouble("id"), updateNotification);
    }


}
