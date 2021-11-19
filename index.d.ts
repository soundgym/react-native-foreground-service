declare type Importance = 1 | 2 | 3 | 4 | 5;
declare type Priority = 0 | -1 | -2 | 1 | 2;
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
}
export interface IBackgroundConfig {
    delay?: number;
    channelId?: string;
    id: number;
    title: string;
    text: string;
    icon: string;
    priority?: Priority;
    ongoing?: boolean;
}
export interface ILTForegroundService {
    createNotificationChannel(channelConfig: IChannelConfig): Promise<void>;
    startService(notificationConfig: INotificationConfig): Promise<void>;
    stopService(): Promise<void>;
    updateService(notificationConfig: INotificationConfig): Promise<void>;
    backgroundStartService(task: (taskData?: IBackgroundConfig) => Promise<void>, backgroundConfig: IBackgroundConfig): Promise<void>;
    backgroundStopService(): Promise<void>;
}
declare const LTForegroundService: ILTForegroundService;
export default LTForegroundService;
