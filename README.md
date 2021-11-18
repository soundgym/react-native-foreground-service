# react-native-foreground-service-android

A foreground service performs some operation that is noticeable to the user.
For example, an audio app would use a foreground service to play an audio track.
Foreground services must display a notification.
Foreground services continue running even when the user isn't interacting with the app.

See [the Android official documentation](https://developer.android.com/guide/components/services) for details on the concept.

### This app is a library created by forking [@voximplant/react-native-foreground-service][libray-link].

## Getting started

`$ npm install react-native-foreground-service-android --save`

OR

`$ yarn add react-native-foreground-service-android`

### Automatic installation (Android only)

- React Native 0.60+

  CLI autolink feature links the module while building the app.

  1. Add the FOREGROUND_SERVICE permission to the application's `AndroidManifest.xml`:
     ```
     <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
     ```
  2. Add VIForegroundService as a service to the application's `AndroidManifest.xml`:
     ```
     <service android:name="com.voximplant.foregroundservice.VIForegroundService"
     android:exported="true" <- If you want it to be maintained even after the app is turned off, default : "false"
     />
     ```

`android:exported`

- Indicates whether components of other applications can call or interact with the service. If a call or interaction is possible, it is `"true"`, otherwise `"false"`. If this value is `"false"`, only applications with the same application component or the same user ID can start or bind to the service.
  The default value depends on whether the service contains an intent filter. If there is no filter, the service can be called only when the exact class name is specified. This suggests that the service is designed only for internal application purposes (because others do not know the class name). Therefore, in this case, the default value is `"false"`, whereas if there is more than one filter, it implies that the service is designed for external purposes, so the default value is `"true"`.
  In addition to this characteristic, there is a way to limit services from being exposed to other applications. You can also use permission to restrict external entities that can interact with the service (see permission attributes).

- React Native <= 0.59

  `$ react-native link @voximplant/react-native-foreground-service`

  1. Add the FOREGROUND_SERVICE permission to the application's `AndroidManifest.xml`:
     ```
     <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
     ```
  2. Add VIForegroundService as a service to the application's `AndroidManifest.xml`:
     ```
     <service android:name="com.voximplant.foregroundservice.VIForegroundService"> </service>
     ```

### Manual installation (Android only, React Native <= 0.59)

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
   - Add `import com.voximplant.foregroundservice.VIForegroundServicePackage;` to the imports at the top of the file
   - Add `new VIForegroundServicePackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
   ```
   include ':@voximplant_react-native-foreground-service'
   project(':@voximplant_react-native-foreground-service').projectDir = new File(rootProject.projectDir, '../node_modules/@voximplant/react-native-foreground-service/android')
   ```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
   ```
   implementation project(':@voximplant_react-native-foreground-service')
   ```
4. Add the FOREGROUND_SERVICE permission to the application's `AndroidManifest.xml`:
   ```
   <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
   ```
5. Add VIForegroundService as a service to the application's `AndroidManifest.xml`:
   ```
   <service android:name="com.voximplant.foregroundservice.VIForegroundService"> </service>
   ```

## Demo project

Demo application: none

## Usage

### Import module

```ts
import VIForegroundService, {
  IChannelConfig,
  INotificationConfig,
} from "react-native-foreground-service-android";
```

### Create notification channel (Android 8+)

Since the foreground service must display a notification, for Android 8+ it is required to create a notification
channel first:

```ts
const channelConfig: IChannelConfig = {
  id: "channelId",
  name: "Channel name",
  description: "Channel description",
  enableVibration: false,
};
VIForegroundService.createNotificationChannel(channelConfig);
```

### Update foreground service

```javascript
async startForegroundService() {
    const notificationConfig: INotificationConfig = {
        channelId: 'channelId', // you must same channelId and id
        id: 3456,
        title: 'Title',
        text: 'Some text',
        icon: 'ic_icon',
        ongoing: true // default false
    };
    try {
        await VIForegroundService.updateService(notificationConfig);
    } catch (e) {
        console.error(e);
    }
}
```

### Start foreground service

```javascript
async startForegroundService() {
    const notificationConfig = {
        channelId: 'channelId',
        id: 3456,
        title: 'Title',
        text: 'Some text',
        icon: 'ic_icon',
        ongoing: true // default false
    };
    try {
        await VIForegroundService.startService(notificationConfig);
    } catch (e) {
        console.error(e);
    }
}
```

### Stop foreground service

```javascript
VIForegroundService.stopService();
```

## Reference

### Methods

```javascript
static async startService(notificationConfig)
```

Starts the foreground service and displays a notification with the defined configuration

---

```javascript
static async stopService()
```

Stops the foreground service

---

```javascript
static async createNotificationChannel(channelConfig)
```

Creates a notification channel for the foreground service.
For Android 8+ the notification channel should be created before starting the foreground service

---

```javascript
static async updateService(notificationConfig)
```

It is used to update the foreground service.

- `NOTE` : When using the update function, the `channel ID` must be the same.

### Configs

```javascript
NotificationChannelConfig;
```

| Property name   | Description                                                                                                                                                    | Required |
| --------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- |
| id              | Unique channel id                                                                                                                                              | yes      |
| name            | Notification channel name                                                                                                                                      | yes      |
| description     | Notification channel description                                                                                                                               | no       |
| importance      | Notification channel importance. One of:<ul><li>1 – 'min'</li> <li>2 – 'low' (by default)</li><li>3 – 'default'</li><li>4 – 'high'</li><li>5 – 'max'</li></ul> | no       |
| enableVibration | Sets whether notification posted to this channel should vibrate. False by default.                                                                             | no       |

```javascript
NotificationConfig;
```

| Property name | Description                                                                                                                                                                                                         | Required              |
| ------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------- |
| channelId     | Notification channel id to display the notification                                                                                                                                                                 | yes (Android 8+ only) |
| id            | Unique notification id                                                                                                                                                                                              | yes                   |
| title         | Notification title                                                                                                                                                                                                  | yes                   |
| text          | Notification text                                                                                                                                                                                                   | yes                   |
| icon          | Icon name                                                                                                                                                                                                           | yes                   |
| priority      | Priority of this notification. One of: <ul><li>&nbsp;0 – PRIORITY_DEFAULT (by default)</li><li>-1 – PRIORITY_LOW</li><li>-2 – PRIORITY_MIN</li><li>&nbsp;1 – PRIORITY_HIGH</li><li>&nbsp;2 – PRIORITY_MAX</li></ul> | no                    |
| ongoing       | To keep the notification or prevent the user from erasing the notification even if the user shuts down the app,                                                                                                     | no                    |

[libray-link]: https://github.com/voximplant/react-native-foreground-service
