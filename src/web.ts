import { WebPlugin } from '@capacitor/core';

import type {
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

export class BackgroundBLEWeb extends WebPlugin implements BackgroundBLEPlugin {
  async checkPermissions(): Promise<PermissionStatus> {
    throw this.unimplemented('Not implemented on web.');
  }

  async requestPermissions(): Promise<PermissionStatus> {
    throw this.unimplemented('Not implemented on web.');
  }

  async initialise(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async getDevices(): Promise<Result<'devices', Device[]>> {
    throw this.unimplemented('Not implemented on web.');
  }

  async addDevice(_options: AddDeviceOptions): Promise<Result<'devices', Device[]>> {
    throw this.unimplemented('Not implemented on web.');
  }

  async addDevices(_options: AddDevicesOptions): Promise<Result<'devices', Device[]>> {
    throw this.unimplemented('Not implemented on web.');
  }

  async removeDevice(_options: RemoveDeviceOptions): Promise<Result<'devices', Device[]>> {
    throw this.unimplemented('Not implemented on web.');
  }

  async clearDevices(): Promise<Result<'devices', Device[]>> {
    throw this.unimplemented('Not implemented on web.');
  }

  async startForegroundService(): Promise<Result<'result', string>> {
    throw this.unimplemented('Not implemented on web.');
  }

  async stopForegroundService(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async isRunning(): Promise<Result<'running', boolean>> {
    throw this.unimplemented('Not implemented on web.');
  }

  async setScanMode(_options: SetScanModeOptions): Promise<Result<'result', ScanMode>> {
    throw this.unimplemented('Not implemented on web.');
  }
}
