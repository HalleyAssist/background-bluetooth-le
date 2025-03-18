import { WebPlugin } from '@capacitor/core';

import {
  AddDevicesOptions,
  BackgroundBLEPlugin,
  Device,
  PermissionStatus,
  Result,
  ScanConfig,
  ScanMode,
  SetConfigOptions,
} from './definitions';

export class BackgroundBLEWeb extends WebPlugin implements BackgroundBLEPlugin {
  async checkPermissions(): Promise<PermissionStatus> {
    return { bluetooth: 'denied', notifications: 'denied' };
  }

  async requestPermissions(): Promise<PermissionStatus> {
    return { bluetooth: 'denied', notifications: 'denied' };
  }

  async initialise(): Promise<void> {
    return;
  }

  async getDevices(): Promise<Result<'devices', Device[]>> {
    return { devices: [] };
  }

  async setDevices(_options: AddDevicesOptions): Promise<Result<'devices', Device[]>> {
    return { devices: [] };
  }

  async clearDevices(): Promise<Result<'devices', Device[]>> {
    return { devices: [] };
  }

  async startForegroundService(): Promise<Result<'result', string>> {
    return { result: 'not supported' };
  }

  async stopForegroundService(): Promise<void> {
    return;
  }

  async isRunning(): Promise<Result<'running', boolean>> {
    return { running: false };
  }

  async setConfig(options: SetConfigOptions): Promise<Result<'config', ScanConfig>> {
    return { config: options.config as ScanConfig };
  }

  async getConfig(): Promise<Result<'config', ScanConfig>> {
    return {
      config: {
        mode: ScanMode.OPPORTUNISTIC,
        debug: false,
        deviceTimeout: 30000,
      },
    };
  }
}
