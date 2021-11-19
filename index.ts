//@ts-ignore
import { AppRegistry, NativeModules } from "react-native";

const ForegroundServiceModule = NativeModules.LTForegroundService;

type Importance = 1 | 2 | 3 | 4 | 5;

type Priority = 0 | -1 | -2 | 1 | 2;

/**
 * @property {string} id - Unique channel ID
 * @property {string} name - Notification channel name
 * @property {string} [description] - Notification channel description
 * @property {number} [importance] - Notification channel importance. One of:
 *                                   1 - 'min',
 *                                   2 - 'low' (by default),
 *                                   3 - 'default',
 *                                   4 - 'high',
 *                                   5 - 'max'.
 * @property {boolean} [enableVibration] - Sets whether notification posted to this channel should vibrate. False by default.
 */
export interface IChannelConfig {
  id: string;
  name: string;
  description: string;
  enableVibration?: boolean;
  importance?: Importance;
}

/**
 * @property {string} channelId - Notification channel id to display notification
 * @property {number} id - Unique notification id
 * @property {string} title - Notification title
 * @property {string} text - Notification text
 * @property {string} icon - Small icon name
 * @property {number} [priority] - Priority of this notification. One of:
 *                              0 - PRIORITY_DEFAULT (by default),
 *                              -1 - PRIORITY_LOW,
 *                              -2 - PRIORITY_MIN,
 *                              1 - PRIORITY_HIGH,
 *                              2- PRIORITY_MAX
 */
export interface INotificationConfig {
  channelId?: string;
  id: number;
  title: string;
  text: string;
  icon: string;
  priority?: Priority;
  ongoing?: boolean;
}

/**
 * @property {string} taskName - background Task Name
 */
export interface IBackgroundConfig {
  taskName: string;
  delay?: number;
}

export interface IVIForegroundService {
  createNotificationChannel(channelConfig: IChannelConfig): Promise<void>;

  startService(notificationConfig: INotificationConfig): Promise<void>;

  stopService(): Promise<void>;

  updateService(notificationConfig: INotificationConfig): Promise<void>;

  backgroundStartService(
    task: (taskData?: IBackgroundConfig) => Promise<void>,
    backgroundConfig: IBackgroundConfig
  ): Promise<void>;
}

let stopTask = (_: unknown) => {};

const generateTask = (
  task: (taskData?: IBackgroundConfig) => Promise<void>,
  parameters: IBackgroundConfig
) => {
  return async () => {
    await new Promise((resolve) => {
      stopTask = resolve;
      task(parameters).then(() => stopTask({}));
    });
  };
};

const VIForegroundService: IVIForegroundService = {
  /**
   * Create notification channel for foreground service
   *
   * @param {IChannelConfig} channelConfig - Notification channel configuration
   * @return Promise
   */
  createNotificationChannel: async (channelConfig) => {
    return await ForegroundServiceModule.createNotificationChannel(
      channelConfig
    );
  },

  /**
   * Start foreground service
   * @param {INotificationConfig} notificationConfig - Notification config
   * @return Promise
   */
  startService: async (notificationConfig) => {
    return await ForegroundServiceModule.startService(notificationConfig);
  },

  /**
   * Stop foreground service
   *
   * @return Promise
   */
  stopService: async () => {
    await stopTask({});
    return await ForegroundServiceModule.stopService();
  },

  /**
   * Update foreground service
   * @param {INotificationConfig} notificationConfig - Notification config
   * @return void
   */
  updateService: async (notificationConfig) => {
    await ForegroundServiceModule.updateService(notificationConfig);
  },

  /**
   * You can start background service
   * @param {IBackgroundConfig} backgroundConfig - Background config
   * @param {(taskData?: IBackgroundConfig) => Promise<void>} task - Background task
   * @return void
   */
  backgroundStartService: async (task, backgroundConfig) => {
    const finalTask = generateTask(task, backgroundConfig);
    AppRegistry.registerHeadlessTask(
      backgroundConfig.taskName,
      () => finalTask
    );
  },
};

export default VIForegroundService;
