"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const react_native_1 = require("react-native");
const ForegroundServiceModule = react_native_1.NativeModules.VIForegroundService;
const VIForegroundService = {
    createNotificationChannel: async (channelConfig) => {
        return await ForegroundServiceModule.createNotificationChannel(channelConfig);
    },
    startService: async (notificationConfig) => {
        return await ForegroundServiceModule.startService(notificationConfig);
    },
    stopService: async () => {
        return await ForegroundServiceModule.stopService();
    },
};
exports.default = VIForegroundService;
//# sourceMappingURL=index.js.map