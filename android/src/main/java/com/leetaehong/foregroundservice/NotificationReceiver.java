package com.leetaehong.foregroundservice;

import static com.leetaehong.foregroundservice.Constants.BACKGROUND_CONFIG;
import static com.leetaehong.foregroundservice.NotificationHelper.NotificationType;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;

import java.util.HashMap;
import java.util.Map;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "com.leeteahong.foregroundservice.alarm";
    @Override
    public void onReceive(Context context, Intent intent) {
        // here to restart the service
        Intent newIntent = new Intent(context.getApplicationContext(), LTForegroundTask.class);
        newIntent.setAction(Constants.ACTION_FOREGROUND_SERVICE_START);
        Map<String, Object> notificationConfig = new HashMap();
        Map<String, Object> backgroundConfig = new HashMap();
        backgroundConfig.put("id",9600);
        backgroundConfig.put("title","걸음수!!!");
        backgroundConfig.put("icon","ic_stat_ic_notification");
        backgroundConfig.put("priority",-2);
        backgroundConfig.put("ongoing",true);
        backgroundConfig.put("notificationType",NotificationType.BACKGROUND);
        backgroundConfig.put("text", "9899 (보)");
        backgroundConfig.put("channelId","SoundgymForegroundServiceChannel");
        newIntent.putExtra(BACKGROUND_CONFIG, Arguments.toBundle((ReadableMap) backgroundConfig));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(newIntent);
        } else {
            context.startService(newIntent);
        }
//        Notification updateNotification = NotificationHelper.getInstance(context.getApplicationContext()).buildNotification(context.getApplicationContext(), Arguments.toBundle((ReadableMap) backgroundConfig),NotificationType.BACKGROUND);
//        NotificationHelper.getInstance(context.getApplicationContext()).updateNotification((int) ((ReadableMap) backgroundConfig).getDouble("id"), updateNotification);
    }
}