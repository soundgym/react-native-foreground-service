declare type Importance = 1 | 2 | 3 | 4 | 5;
declare type Priority = 0 | -1 | -2 | 1 | 2;
export declare enum NotificationType {
    BACKGROUND = "BACKGROUND",
    FOREGROUND = "FOREGROUND"
}
export interface IChannelConfig {
    id: string;
    name: string;
    description: string;
    enableVibration?: boolean;
    importance?: Importance;
}
export interface INotificationConfig {
    channelId?: string;
    id: number;
    title: string;
    text: string;
    icon: string;
    priority?: Priority;
    ongoing?: boolean;
    notificationType?: NotificationType;
}
export interface IBackgroundConfig {
    taskName?: string;
    delay?: number;
    channelId?: string;
    id: number;
    title: string;
    text: string;
    icon: string;
    priority?: Priority;
    ongoing?: boolean;
    uid?: string;
    userToken?: string;
}
export interface IBlockChannelConfig {
    channelId?: string;
}
export interface ILTForegroundService {
    createNotificationChannel(channelConfig: IChannelConfig): Promise<void>;
    startService(notificationConfig: INotificationConfig): Promise<void>;
    stopService(): Promise<void>;
    updateService(notificationConfig: INotificationConfig): Promise<void>;
    backgroundStartService(task: (taskData?: IBackgroundConfig) => Promise<void>, backgroundConfig: IBackgroundConfig): Promise<void>;
    backgroundStopService(): Promise<void>;
    startRemoteService(notificationConfig: INotificationConfig): Promise<void>;
    stopRemoteService(): Promise<void>;
    updateRemoteService(notificationConfig: INotificationConfig): Promise<void>;
    blockNotificationChannel(channelConfig: IBlockChannelConfig): Promise<void>;
    getIsBackgroundRunning(): boolean;
    getIsRunning(): boolean;
}
declare const LTForegroundService: ILTForegroundService;
export default LTForegroundService;
