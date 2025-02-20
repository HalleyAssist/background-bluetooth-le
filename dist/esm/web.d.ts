import { WebPlugin } from '@capacitor/core';
import type {
  AddDeviceOptions,
  AddDevicesOptions,
  BackgroundBLEPlugin,
  Device,
  IsRunningResult,
  PermissionStatus,
  RemoveDeviceOptions,
  Result,
  SetScanModeOptions,
} from './definitions';
export declare class BackgroundBLEWeb extends WebPlugin implements BackgroundBLEPlugin {
  checkPermissions(): Promise<PermissionStatus>;
  requestPermissions(): Promise<PermissionStatus>;
  initialise(): Promise<void>;
  addDevice(_options: AddDeviceOptions): Promise<Result>;
  addDevices(_options: AddDevicesOptions): Promise<Result>;
  removeDevice(_options: RemoveDeviceOptions): Promise<Result>;
  clearDevices(): Promise<Result>;
  startForegroundService(): Promise<void>;
  stopForegroundService(): Promise<void>;
  isRunning(): Promise<IsRunningResult>;
  setScanMode(_options: SetScanModeOptions): Promise<void>;
  getDevices(): Promise<Device[]>;
}
