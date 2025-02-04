import { WebPlugin } from '@capacitor/core';

import type {
  BackgroundBLEPlugin,
  AddDeviceOptions,
  RemoveDeviceOptions,
  PermissionStatus,
  AddDevicesOptions,
  AddDeviceResult,
  AddDevicesResult,
  RemoveDeviceResult,
  ClearDevicesResult,
  IsRunningResult,
} from './definitions';

export class BackgroundBLEWeb extends WebPlugin implements BackgroundBLEPlugin {
  async checkPermissions(): Promise<PermissionStatus> {
    throw this.unimplemented('Not implemented on web.');
  }

  async requestPermissions(): Promise<PermissionStatus> {
    throw this.unimplemented('Not implemented on web.');
  }

  async addDevice(_options: AddDeviceOptions): Promise<AddDeviceResult> {
    throw this.unimplemented('Not implemented on web.');
  }

  async addDevices(_options: AddDevicesOptions): Promise<AddDevicesResult> {
    throw this.unimplemented('Not implemented on web.');
  }

  async removeDevice(_options: RemoveDeviceOptions): Promise<RemoveDeviceResult> {
    throw this.unimplemented('Not implemented on web.');
  }

  async clearDevices(): Promise<ClearDevicesResult> {
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

  async setScanMode(_options: { mode: number }): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }
}
