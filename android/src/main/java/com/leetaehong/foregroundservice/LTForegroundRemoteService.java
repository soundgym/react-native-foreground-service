package com.leetaehong.foregroundservice;

import static com.leetaehong.foregroundservice.Constants.MSG_ADD_VALUE;
import static com.leetaehong.foregroundservice.Constants.MSG_APP_DESTROY;
import static com.leetaehong.foregroundservice.Constants.MSG_CLIENT_CONNECT;
import static com.leetaehong.foregroundservice.Constants.MSG_CLIENT_DISCONNECT;
import static com.leetaehong.foregroundservice.Constants.NOTIFICATION_CONFIG;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.facebook.react.bridge.Arguments;

import java.util.ArrayList;
import java.util.Calendar;

public class LTForegroundRemoteService extends Service {
    private final String TAG = "RemoteService";
    private Bundle prevBundle;
    private ArrayList<Messenger> mClientCallbacks = new ArrayList();
    final Messenger mMessenger = new Messenger(new CallbackHandler());

    @Override
    public IBinder onBind(Intent intent) { return mMessenger.getBinder(); }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(Constants.ACTION_FOREGROUND_SERVICE_START)) {
                if (intent.getExtras() != null && intent.getExtras().containsKey(NOTIFICATION_CONFIG)) {
                    Bundle notificationConfig = intent.getExtras().getBundle(NOTIFICATION_CONFIG);
                    if (notificationConfig != null && notificationConfig.containsKey("id")) {
                        Notification notification = NotificationHelper.getInstance(getApplicationContext())
                                .buildNotification(getApplicationContext(), notificationConfig, NotificationHelper.NotificationType.FOREGROUND);
                        if(notificationConfig.getBoolean("ongoing")) {
                            notification.flags |= Notification.FLAG_ONGOING_EVENT;
                            notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                        }
                        startForeground((int)notificationConfig.getDouble("id"), notification);
                        Log.d(TAG,"################### user info");
                        Log.d(TAG,notificationConfig.getString("uid"));
                        Log.d(TAG,notificationConfig.getString("userToken"));
                        Log.d(TAG,"################### user info");
                    }
                }
            } else if (action.equals(Constants.ACTION_FOREGROUND_SERVICE_STOP)) {
                stopSelf();
            } else if (action.equals(Constants.ACTION_FOREGROUND_SERVICE_UPDATE)) {
                Bundle notificationConfig = intent.getExtras().getBundle(NOTIFICATION_CONFIG);
                // 최근 데이터 저장
                prevBundle = notificationConfig;
                Notification updateNotification = NotificationHelper.getInstance(getApplicationContext())
                        .buildNotification(getApplicationContext(), notificationConfig, NotificationHelper.NotificationType.BACKGROUND);
                NotificationHelper.getInstance(getApplicationContext()).updateNotification((int) notificationConfig.getDouble("id"),updateNotification);
            } else if(action.equals(Constants.ACTION_FOREGROUND_SERVICE_REMOTE_UPDATE)) {
                String stepText = prevBundle.getString("text");
                stepText = stepText.replaceAll("[^0-9]","");  // or you can also use [0-9]
                int step = Integer.parseInt(stepText);
                prevBundle.remove("text");
                prevBundle.putString("text",(step + 1)  + " (보)");
                Notification updateNotification = NotificationHelper.getInstance(getApplicationContext())
                        .buildNotification(getApplicationContext(), prevBundle, NotificationHelper.NotificationType.BACKGROUND);
                NotificationHelper.getInstance(getApplicationContext()).updateNotification((int) prevBundle.getDouble("id"),updateNotification);
            }
        }
        return START_REDELIVER_INTENT;
    }


    private class CallbackHandler  extends Handler {
        @Override
        public void handleMessage( Message msg ){
            switch( msg.what ){
                case MSG_CLIENT_CONNECT:
                    Log.d(TAG, "Received MSG_CLIENT_CONNECT message from client");
                    mClientCallbacks.add(msg.replyTo);
                    break;
                case MSG_CLIENT_DISCONNECT:
                    Log.d(TAG, "Received MSG_CLIENT_DISCONNECT message from client");
                    mClientCallbacks.remove(msg.replyTo);
                    break;
                case MSG_ADD_VALUE:
                    Log.d(TAG, "Received message from client: MSG_ADD_VALUE");
//                    for (int i = mClientCallbacks.size() - 1; i >= 0; i--) {
//                        try{
//                            Log.d(TAG, "Send MSG_ADDED_VALUE message to client");
//                            Message added_msg = Message.obtain(
//                                    null, MSG_ADD_VALUE);
//                            added_msg.arg1 = mValue;
//                            mClientCallbacks.get(i).send(added_msg);
//                        }
//                        catch(RemoteException e){
//                            mClientCallbacks.remove( i );
//                        }
//                    }
                    break;
                case MSG_APP_DESTROY:
                    Log.d(TAG, "Received MSG_APP_DESTROY message from client");
                    LTSensorListner ltSensorListner = new LTSensorListner(getApplicationContext(),prevBundle);
                    ltSensorListner.start(1000);
                    break;
            }
        }
    }

    private void setTimeout(Runnable runnable, int delay){
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            }
            catch (Exception e){
                System.err.println(e);
            }
        }).start();
    }


}
