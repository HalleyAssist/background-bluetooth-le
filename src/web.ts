import { WebPlugin } from '@capacitor/core';

import {
  AddDevicesOptions,
  BackgroundBLEPlugin,
  Device,
  PermissionStatus,
  Result,
  ScanMode,
  SetScanModeOptions,
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

  async setScanMode(_options: SetScanModeOptions): Promise<Result<'result', ScanMode>> {
    return { result: ScanMode.OPPORTUNISTIC };
  }
}
