"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.NotificationType = void 0;
const react_native_1 = require("react-native");
const ForegroundServiceModule = react_native_1.NativeModules.LTForegroundService;
var NotificationType;
(function (NotificationType) {
    NotificationType["BACKGROUND"] = "BACKGROUND";
    NotificationType["FOREGROUND"] = "FOREGROUND";
})(NotificationType = exports.NotificationType || (exports.NotificationType = {}));
let stopTask = (_) => { };
let isRunning = false;
let isBackgroundRunning = false;
const generateTask = (task, parameters) => {
    return async () => {
        await new Promise((resolve) => {
            stopTask = resolve;
            task(parameters).then(() => stopTask({}));
        });
    };
};
const LTForegroundService = {
    createNotificationChannel: async (channelConfig) => {
        return await ForegroundServiceModule.createNotificationChannel(channelConfig);
    },
    startService: async (notificationConfig) => {
        await ForegroundServiceModule.startService(notificationConfig);
        isRunning = true;
    },
    stopService: async () => {
        await ForegroundServiceModule.stopService();
        isRunning = false;
    },
    updateService: async (notificationConfig) => {
        await ForegroundServiceModule.updateService(notificationConfig);
    },
    backgroundStartService: async (task, backgroundConfig) => {
        try {
            const finalTask = generateTask(task, backgroundConfig);
            const taskName = backgroundConfig.taskName ?? "BackgroundTask";
            react_native_1.AppRegistry.registerHeadlessTask(taskName, () => finalTask);
            if (isRunning) {
                await ForegroundServiceModule.stopService();
            }
            await ForegroundServiceModule.backgroundStartService(backgroundConfig);
            isBackgroundRunning = true;
        }
        catch (err) {
            console.error("backgroundStartService error");
            console.error(err);
        }
    },
    backgroundStopService: async () => {
        await stopTask({});
        await ForegroundServiceModule.backgroundStopService();
        isBackgroundRunning = false;
        return;
    },
    startRemoteService: async (notificationConfig) => {
        await ForegroundServiceModule.startRemoteService(notificationConfig);
        isRunning = true;
    },
    stopRemoteService: async () => {
        await ForegroundServiceModule.stopRemoteService();
        isRunning = false;
    },
    updateRemoteService: async (notificationConfig) => {
        await ForegroundServiceModule.updateRemoteService(notificationConfig);
    },
    blockNotificationChannel: async (channelConfig) => {
        await ForegroundServiceModule.blockNotificationChannel(channelConfig);
    },
    getIsBackgroundRunning: () => {
        return isBackgroundRunning;
    },
    getIsRunning: () => {
        return isRunning;
    },
};
exports.default = LTForegroundService;
//# sourceMappingURL=index.js.map