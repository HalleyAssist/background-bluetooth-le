import { WebPlugin } from '@capacitor/core';
import type {
  AddDeviceOptions,
  AddDeviceResult,
  AddDevicesOptions,
  AddDevicesResult,
  BackgroundBLEPlugin,
  ClearDevicesResult,
  IsRunningResult,
  PermissionStatus,
  RemoveDeviceOptions,
  RemoveDeviceResult,
  SetScanModeOptions,
} from './definitions';
export declare class BackgroundBLEWeb extends WebPlugin implements BackgroundBLEPlugin {
  checkPermissions(): Promise<PermissionStatus>;
  requestPermissions(): Promise<PermissionStatus>;
  initialise(): Promise<void>;
  addDevice(_options: AddDeviceOptions): Promise<AddDeviceResult>;
  addDevices(_options: AddDevicesOptions): Promise<AddDevicesResult>;
  removeDevice(_options: RemoveDeviceOptions): Promise<RemoveDeviceResult>;
  clearDevices(): Promise<ClearDevicesResult>;
  startForegroundService(): Promise<void>;
  stopForegroundService(): Promise<void>;
  isRunning(): Promise<IsRunningResult>;
  setScanMode(_options: SetScanModeOptions): Promise<void>;
}
