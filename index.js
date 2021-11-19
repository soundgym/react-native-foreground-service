"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const react_native_1 = require("react-native");
const ForegroundServiceModule = react_native_1.NativeModules.LTForegroundService;
let stopTask = (_) => { };
let isRunning = false;
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
            react_native_1.AppRegistry.registerHeadlessTask(backgroundConfig.title, () => finalTask);
            if (isRunning) {
                await ForegroundServiceModule.stopService();
            }
            await ForegroundServiceModule.backgroundStartService(backgroundConfig);
        }
        catch (err) {
            console.error("backgroundStartService error");
            console.error(err);
        }
    },
    backgroundStopService: async () => {
        await stopTask({});
        return await ForegroundServiceModule.backgroundStopService();
    },
};
exports.default = LTForegroundService;
//# sourceMappingURL=index.js.map