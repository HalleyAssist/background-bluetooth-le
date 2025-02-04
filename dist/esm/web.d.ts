import { WebPlugin } from '@capacitor/core';
import type { BackgroundBLEPlugin, AddDeviceOptions, RemoveDeviceOptions, PermissionStatus, AddDevicesOptions, AddDeviceResult, AddDevicesResult, RemoveDeviceResult, ClearDevicesResult, IsRunningResult } from './definitions';
export declare class BackgroundBLEWeb extends WebPlugin implements BackgroundBLEPlugin {
    checkPermissions(): Promise<PermissionStatus>;
    requestPermissions(): Promise<PermissionStatus>;
    addDevice(_options: AddDeviceOptions): Promise<AddDeviceResult>;
    addDevices(_options: AddDevicesOptions): Promise<AddDevicesResult>;
    removeDevice(_options: RemoveDeviceOptions): Promise<RemoveDeviceResult>;
    clearDevices(): Promise<ClearDevicesResult>;
    startForegroundService(): Promise<void>;
    stopForegroundService(): Promise<void>;
    isRunning(): Promise<IsRunningResult>;
    setScanMode(_options: {
        mode: number;
    }): Promise<void>;
}
