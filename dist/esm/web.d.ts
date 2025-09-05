import { PluginListenerHandle, WebPlugin } from '@capacitor/core';
import {
  AddDevicesOptions,
  BackgroundBLEPlugin,
  Config,
  Device,
  Devices,
  DevicesChangedListener,
  PermissionStatus,
  RunningResult,
  SetConfigOptions,
  StartStopResult,
  UserStoppedResult,
} from './definitions';
export declare class BackgroundBLEWeb extends WebPlugin implements BackgroundBLEPlugin {
  checkPermissions(): Promise<PermissionStatus>;
  requestPermissions(): Promise<PermissionStatus>;
  initialise(): Promise<void>;
  enable(): Promise<void>;
  getDevices(): Promise<Devices>;
  setDevices(_options: AddDevicesOptions): Promise<Devices>;
  clearDevices(): Promise<Devices>;
  startForegroundService(): Promise<StartStopResult>;
  stopForegroundService(): Promise<StartStopResult>;
  isRunning(): Promise<RunningResult>;
  didUserStop(): Promise<UserStoppedResult>;
  getActiveDevice(): Promise<{
    device: Device | null;
  }>;
  setActiveDevice(_device: Device | null): Promise<void>;
  setConfig(options: SetConfigOptions): Promise<Config>;
  getConfig(): Promise<Config>;
  addListener(
    _eventName: 'devicesChanged' | 'closeDevicesChanged',
    _event: DevicesChangedListener,
  ): Promise<PluginListenerHandle>;
}
