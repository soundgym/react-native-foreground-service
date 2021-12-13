package com.leetaehong.foregroundservice;

import static com.leetaehong.foregroundservice.Constants.MSG_ADD_VALUE;
import static com.leetaehong.foregroundservice.Constants.MSG_APP_DESTROY;
import static com.leetaehong.foregroundservice.Constants.MSG_CLIENT_CONNECT;
import static com.leetaehong.foregroundservice.Constants.MSG_CLIENT_DISCONNECT;
import static com.leetaehong.foregroundservice.Constants.NOTIFICATION_CONFIG;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;

import javax.net.ssl.HttpsURLConnection;

public class LTForegroundRemoteService extends Service {
    private final String TAG = "RemoteService";

    //이전 운동데이터 저장
    private Bundle prevBundle;
    private int currentStep = 0;
    private int sendStep = 0;
    //유저정보
    private String userId;
    private String userToken;
    // Create URL
    private URL soundgymAPI;

    private ArrayList<Messenger> mClientCallbacks = new ArrayList();
    final Messenger mMessenger = new Messenger(new CallbackHandler());

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
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
                                .buildNotification(getApplicationContext(), notificationConfig, NotificationHelper.NotificationType.FOREGROUND);
                        if (notificationConfig.getBoolean("ongoing")) {
                            notification.flags |= Notification.FLAG_ONGOING_EVENT;
                            notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                        }
                        startForeground((int) notificationConfig.getDouble("id"), notification);
                        userId = notificationConfig.getString("uid");
                        userToken = notificationConfig.getString("userToken");
                        changeStepCount(notificationConfig,true);
                        callScheduleApi();
                    }
                }
            } else if (action.equals(Constants.ACTION_FOREGROUND_SERVICE_STOP)) {
                stopSelf();
            } else if (action.equals(Constants.ACTION_FOREGROUND_SERVICE_UPDATE)) {
                Bundle notificationConfig = intent.getExtras().getBundle(NOTIFICATION_CONFIG);
                // 최근 데이터 저장
                prevBundle = notificationConfig;
                changeStepCount(notificationConfig,null);
                Notification updateNotification = NotificationHelper.getInstance(getApplicationContext())
                        .buildNotification(getApplicationContext(), notificationConfig, NotificationHelper.NotificationType.BACKGROUND);
                NotificationHelper.getInstance(getApplicationContext()).updateNotification((int) notificationConfig.getDouble("id"), updateNotification);
            } else if (action.equals(Constants.ACTION_FOREGROUND_SERVICE_REMOTE_UPDATE)) {
                changeStepCount(prevBundle,null);
                prevBundle.remove("text");
                prevBundle.putString("text", currentStep + " (보)");
                Notification updateNotification = NotificationHelper.getInstance(getApplicationContext())
                        .buildNotification(getApplicationContext(), prevBundle, NotificationHelper.NotificationType.BACKGROUND);
                NotificationHelper.getInstance(getApplicationContext()).updateNotification((int) prevBundle.getDouble("id"), updateNotification);
            }
        }
        return START_REDELIVER_INTENT;
    }


    private class CallbackHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
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
                    LTSensorListner ltSensorListner = new LTSensorListner(getApplicationContext(), prevBundle);
                    ltSensorListner.start(1000);
                    break;
            }
        }
    }

    private int changeStepCount(Bundle bundle, Boolean isFirst) {
        String stepText = bundle.getString("text");
        stepText = stepText.replaceAll("[^0-9]", "");  // or you can also use [0-9]
        int step = Integer.parseInt(stepText);
        currentStep = step + 1;
        if(isFirst) {
            sendStep = step + 1;
        }
        return currentStep;
    }


    private void setTimeout(Runnable runnable, int delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            } catch (Exception e) {
                System.err.println(e);
            }
        }).start();
    }

    private void callScheduleApi() {
        AsyncTask.execute(() -> {
            // All your networking logic
            // should be here
            try {
                if(currentStep - sendStep > 0) {
                    soundgymAPI = new URL("https://api.dev.soundgym.kr/app/user/health/steps");
                    // Create connection
                    HttpsURLConnection soundgymConnection =
                            (HttpsURLConnection) soundgymAPI.openConnection();
                    // 요청방식 선택
                    soundgymConnection.setRequestMethod("POST");
                    //헤더옵션 추가
                    soundgymConnection.setRequestProperty("Authorization", userToken);
                    soundgymConnection.setRequestProperty("Content-Type", "application/json");
                    soundgymConnection.setRequestProperty("Accept", "application/json");
                    // InputStream으로 서버로 부터 응답을 받겠다는 옵션
                    soundgymConnection.setDoInput(true);
                    // OutputStream으로 Post 데이터를 넘겨주겠다는 옵션
                    soundgymConnection.setDoOutput(true);
                    // 서버로 전달할 Json객체 생성
                    JSONObject json = new JSONObject();
                    json.put("stepCount", currentStep);
                    json.put("registeredAt", System.currentTimeMillis());
                    // Request Body에 데이터를 담기위한 OutputStream 객체 생성
                    OutputStream outputStream;
                    outputStream = soundgymConnection.getOutputStream();
                    outputStream.write(json.toString().getBytes());
                    outputStream.flush();

                    // 실제 서버로 Request 요청 하는 부분 (응답 코드를 받음, 200은 성공, 나머지 에러)
                    int response = soundgymConnection.getResponseCode();
                    String responseMessage = soundgymConnection.getResponseMessage();
                    // 접속해지
                    soundgymConnection.disconnect();
                    if (response == 200) {
                        //보낸 데이터 저장
                        sendStep = currentStep;
                        setTimeout(() -> callScheduleApi(), 60000 * 20);
                    }
                }
            } catch (MalformedURLException e) {
                System.err.println(e);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println(e);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

}
