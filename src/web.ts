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
import { Preferences } from '@capacitor/preferences';

const DEVICES_KEY = 'backgroundble.devices';
const CONFIG_KEY = 'backgroundble.config';
const RUNNING_KEY = 'backgroundble.running';

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
    const devices = await Preferences.get({ key: DEVICES_KEY });
    if (devices && devices.value) {
      return { devices: JSON.parse(devices.value) };
    }
    return { devices: [] };
  }

  async setDevices(_options: AddDevicesOptions): Promise<Result<'devices', Device[]>> {
    await Preferences.set({ key: DEVICES_KEY, value: JSON.stringify(_options.devices) });
    return { devices: _options.devices as Device[] };
  }

  async clearDevices(): Promise<Result<'devices', Device[]>> {
    return this.setDevices({ devices: [] });
  }

  async startForegroundService(): Promise<Result<'result', string>> {
    await Preferences.set({ key: RUNNING_KEY, value: 'true' });
    return { result: 'started' };
  }

  async stopForegroundService(): Promise<void> {
    await Preferences.set({ key: RUNNING_KEY, value: 'false' });
  }

  async isRunning(): Promise<Result<'running', boolean>> {
    const running = await Preferences.get({ key: RUNNING_KEY }).then((res) => res.value === 'true');
    return { running };
  }

  async setConfig(options: SetConfigOptions): Promise<Result<'config', ScanConfig>> {
    await Preferences.set({ key: CONFIG_KEY, value: JSON.stringify(options.config) });
    return { config: options.config as ScanConfig };
  }

  async getConfig(): Promise<Result<'config', ScanConfig>> {
    const config = await Preferences.get({ key: CONFIG_KEY }).then((res) =>
      res.value ? (JSON.parse(res.value) as ScanConfig) : undefined,
    );
    if (config) {
      return { config };
    } else {
      return this.setConfig({
        config: {
          mode: ScanMode.OPPORTUNISTIC,
          debug: false,
          deviceTimeout: 30000,
        },
      });
    }
  }
}
