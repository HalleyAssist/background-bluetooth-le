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

  async addDevice(_options: AddDeviceOptions): Promise<Result> {
    throw this.unimplemented('Not implemented on web.');
  }

  async addDevices(_options: AddDevicesOptions): Promise<Result> {
    throw this.unimplemented('Not implemented on web.');
  }

  async removeDevice(_options: RemoveDeviceOptions): Promise<Result> {
    throw this.unimplemented('Not implemented on web.');
  }

  async clearDevices(): Promise<Result> {
    throw this.unimplemented('Not implemented on web.');
  }

  async startForegroundService(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async stopForegroundService(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async isRunning(): Promise<IsRunningResult> {
    throw this.unimplemented('Not implemented on web.');
  }

  async setScanMode(_options: SetScanModeOptions): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async getDevices(): Promise<Result<'devices', Device[]>> {
    throw this.unimplemented('Not implemented on web.');
  }
}
