package com.leetaehong.foregroundservice;

import static com.leetaehong.foregroundservice.Constants.MSG_ADDED_VALUE;
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
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

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

    private ArrayList<Messenger> mClientCallbacks = new ArrayList<>();
    final Messenger mMessenger = new Messenger(new CallbackHandler());
    private SharedPreferences sharedPref;
    private LTSensorListner ltSensorListner;

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case Constants.ACTION_FOREGROUND_SERVICE_START:
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
                            changeStepCount(notificationConfig, true);
                            callScheduleApi(true);
                        }
                    }
                    break;
                case Constants.ACTION_FOREGROUND_SERVICE_STOP:
                    // remote service 종료
                    stopSelf();
                    stopForeground(true);
                    // notification 띄워둔것 삭제
                    NotificationHelper.getInstance(getApplicationContext()).cancelNotification((int) prevBundle.getDouble("id"));
                    NotificationHelper.getInstance(getApplicationContext()).cancelAllNotification();
                    // 리스너 삭제
                    if(ltSensorListner == null) {
                        ltSensorListner = new LTSensorListner(getApplicationContext());
                    }
                    ltSensorListner.stop();
                    break;
                case Constants.ACTION_FOREGROUND_SERVICE_UPDATE:
                    Bundle notificationConfig = intent.getExtras().getBundle(NOTIFICATION_CONFIG);
                    // 최근 데이터 저장
                    prevBundle = notificationConfig;
                    // 걸음수 변수에 저장
                    changeStepCount(notificationConfig, false);
                    saveStep(false);
                    Notification updateNotification = NotificationHelper.getInstance(getApplicationContext())
                            .buildNotification(getApplicationContext(), notificationConfig, NotificationHelper.NotificationType.BACKGROUND);
                    NotificationHelper.getInstance(getApplicationContext()).updateNotification((int) notificationConfig.getDouble("id"), updateNotification);
                    break;
                case Constants.ACTION_FOREGROUND_SERVICE_REMOTE_UPDATE:
                    changeStepCount(prevBundle, false);
                    saveStep(false);
                    prevBundle.remove("text");
                    prevBundle.putString("text", currentStep + " 걸음");
                    Notification updateNotificationTwo = NotificationHelper.getInstance(getApplicationContext())
                            .buildNotification(getApplicationContext(), prevBundle, NotificationHelper.NotificationType.BACKGROUND);
                    NotificationHelper.getInstance(getApplicationContext()).updateNotification((int) prevBundle.getDouble("id"), updateNotificationTwo);
                    break;
                default:
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(ltSensorListner == null) {
            ltSensorListner = new LTSensorListner(getApplicationContext());
        }
        ltSensorListner.stop();
    }


    private class CallbackHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if(msg != null) {
                switch (msg.what) {
                    case MSG_CLIENT_CONNECT:
                        mClientCallbacks.add(msg.replyTo);
                        break;
                    case MSG_CLIENT_DISCONNECT:
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
                        if(ltSensorListner == null) {
                            ltSensorListner = new LTSensorListner(getApplicationContext());
                        }
                        Log.d(TAG, "Received MSG_APP_DESTROY message from client");
                        //채널 차단여부 확인
                        if(prevBundle != null) {
                            Boolean enabled =  NotificationHelper.getInstance(getApplicationContext()).isNotificationChannelEnabled(getApplicationContext(),prevBundle.getString("channelId"));
                            if(enabled) {
                                ltSensorListner.start(1000);
                            }
                        }
                        break;
                }
            }
        }
    }

    private int changeStepCount(Bundle bundle, Boolean isFirst) {
        if(bundle != null) {
            String stepText = bundle.getString("text");
            stepText = stepText.replaceAll("[^0-9]", "");  // or you can also use [0-9]
            int step = Integer.parseInt(stepText);
            currentStep = step + 1;
            if (isFirst) {
                sendStep = step + 1;
            }
            return currentStep;
        } else {
            return 0;
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

    private void callScheduleApi(boolean forceCall) {
        AsyncTask.execute(() -> {
            // All your networking logic
            // should be here
            try {
                if (forceCall || currentStep - sendStep > 0) {
                    ApplicationInfo appInfo = getApplicationContext().getApplicationInfo();
                    String title = getApplicationContext().getPackageManager().getApplicationLabel(appInfo).toString();
                    String apiPath = "https://api.dev.soundgym.kr/app/user/health/steps";
                    if (!title.contains("dev")) {
                        apiPath = "https://api.soundgym.kr/app/user/health/steps";
                    }
                    soundgymAPI = new URL(apiPath);
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
                    if (currentStep - sendStep > 0) {
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
                            saveStep(true);
                            setTimeout(() -> callScheduleApi(false), 60000 * 20);
                        }
                    } else if (forceCall) {
                        int prevStep = getStep();
                        if (prevStep > 0) {
                            // 서버로 전달할 Json객체 생성
                            JSONObject json = new JSONObject();
                            json.put("stepCount", prevStep);
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
                                currentStep += prevStep;
                                sendStep += prevStep;
                                saveStep(true);
                                for (int i = mClientCallbacks.size() - 1; i >= 0; i--) {
                                    try {
                                        Message added_msg = Message.obtain(
                                                null, MSG_ADDED_VALUE);
                                        added_msg.arg1 = 1;
                                        mClientCallbacks.get(i).send(added_msg);
                                    } catch (RemoteException e) {
                                        mClientCallbacks.remove(i);
                                    }
                                }
                                setTimeout(() -> callScheduleApi(false), 60000 * 20);
                            }
                        }
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void saveStep(boolean init) {
        int step = getStep();
        if(init) {
            sharedPref.edit().remove("stepCount");
            sharedPref.edit().putString("stepCount", "0").apply();
        } else {
            sharedPref.edit().putString("stepCount", String.valueOf(step + 1)).apply();
        }

    }

    private int getStep() {
        sharedPref = getApplicationContext().getSharedPreferences("soundgymStep", Context.MODE_PRIVATE);
        String count = sharedPref.getString("stepCount", "0");
        return Integer.parseInt(count);
    }


}
