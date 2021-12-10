/*
 * Copyright (c) 2011-2019, Zingaya, Inc. All rights reserved.
 */

package com.leetaehong.foregroundservice;

public class Constants {
    static final String ACTION_FOREGROUND_SERVICE_START = "com.leetaehong.foregroundservice.service_start";
    static final String ACTION_FOREGROUND_SERVICE_STOP = "com.leetaehong.foregroundservice.service_stop";
    static final String ACTION_FOREGROUND_SERVICE_UPDATE = "com.leetaehong.foregroundservice.service_update";

    static final String NOTIFICATION_CONFIG = "com.leetaehong.foregroundservice.notif_config";
    static final String BACKGROUND_CONFIG = "com.leetaehong.foregroundservice.background_config";

    static final String ERROR_INVALID_CONFIG = "ERROR_INVALID_CONFIG";
    static final String ERROR_SERVICE_ERROR = "ERROR_SERVICE_ERROR";
    static final String ERROR_ANDROID_VERSION = "ERROR_ANDROID_VERSION";

    static final int MSG_CLIENT_CONNECT = 1;
    static final int MSG_CLIENT_DISCONNECT = 2;
    static final int MSG_ADD_VALUE = 3;
    static final int MSG_ADDED_VALUE = 4;
    static final int MSG_APP_DESTROY = 5;
}
