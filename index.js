"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const react_native_1 = require("react-native");
const ForegroundServiceModule = react_native_1.NativeModules.LTForegroundService;
let stopTask = (_) => { };
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
        return await ForegroundServiceModule.startService(notificationConfig);
    },
    stopService: async () => {
        await stopTask({});
        return await ForegroundServiceModule.stopService();
    },
    updateService: async (notificationConfig) => {
        await ForegroundServiceModule.updateService(notificationConfig);
    },
    backgroundStartService: async (task, backgroundConfig) => {
        const finalTask = generateTask(task, backgroundConfig);
        react_native_1.AppRegistry.registerHeadlessTask(backgroundConfig.taskName, () => finalTask);
    },
};
exports.default = LTForegroundService;
//# sourceMappingURL=index.js.map