import { WebPlugin } from '@capacitor/core';
import {
  AddDeviceOptions,
  AddDevicesOptions,
  BackgroundBLEPlugin,
  Device,
  PermissionStatus,
  RemoveDeviceOptions,
  Result,
  ScanMode,
  SetScanModeOptions,
} from './definitions';
export declare class BackgroundBLEWeb extends WebPlugin implements BackgroundBLEPlugin {
  checkPermissions(): Promise<PermissionStatus>;
  requestPermissions(): Promise<PermissionStatus>;
  initialise(): Promise<void>;
  getDevices(): Promise<Result<'devices', Device[]>>;
  setDevices(_options: AddDeviceOptions): Promise<Result<'devices', Device[]>>;
  addDevice(_options: AddDeviceOptions): Promise<Result<'devices', Device[]>>;
  addDevices(_options: AddDevicesOptions): Promise<Result<'devices', Device[]>>;
  removeDevice(_options: RemoveDeviceOptions): Promise<Result<'devices', Device[]>>;
  clearDevices(): Promise<Result<'devices', Device[]>>;
  startForegroundService(): Promise<Result<'result', string>>;
  stopForegroundService(): Promise<void>;
  isRunning(): Promise<Result<'running', boolean>>;
  setScanMode(_options: SetScanModeOptions): Promise<Result<'result', ScanMode>>;
}
