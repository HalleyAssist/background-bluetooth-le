import { WebPlugin } from '@capacitor/core';
import {
  AddDevicesOptions,
  BackgroundBLEPlugin,
  Device,
  PermissionStatus,
  Result,
  ScanConfig,
  SetConfigOptions,
} from './definitions';
export declare class BackgroundBLEWeb extends WebPlugin implements BackgroundBLEPlugin {
  checkPermissions(): Promise<PermissionStatus>;
  requestPermissions(): Promise<PermissionStatus>;
  initialise(): Promise<void>;
  getDevices(): Promise<Result<'devices', Device[]>>;
  setDevices(_options: AddDevicesOptions): Promise<Result<'devices', Device[]>>;
  clearDevices(): Promise<Result<'devices', Device[]>>;
  startForegroundService(): Promise<Result<'result', string>>;
  stopForegroundService(): Promise<void>;
  isRunning(): Promise<Result<'running', boolean>>;
  setConfig(options: SetConfigOptions): Promise<Result<'config', ScanConfig>>;
  getConfig(): Promise<Result<'config', ScanConfig>>;
}
