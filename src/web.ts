import { PluginListenerHandle, WebPlugin } from '@capacitor/core';

import { Preferences } from '@capacitor/preferences';
import {
  AddDevicesOptions,
  BackgroundBLEPlugin,
  Config,
  Device,
  Devices,
  DevicesChangedListener,
  PermissionStatus,
  RunningResult,
  ScanConfig,
  ScanMode,
  SetConfigOptions,
  StartResult,
  UserStoppedResult,
} from './definitions';

const DEVICES_KEY = 'backgroundble.devices';
const CONFIG_KEY = 'backgroundble.config';
const RUNNING_KEY = 'backgroundble.running';
const STOPPED_KEY = 'backgroundble.stopped';

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

  async enable(): Promise<void> {
    return;
  }

  async getDevices(): Promise<Devices> {
    const devices = await Preferences.get({ key: DEVICES_KEY });
    if (devices && devices.value) {
      return { devices: JSON.parse(devices.value) };
    }
    return { devices: [] };
  }

  async setDevices(_options: AddDevicesOptions): Promise<Devices> {
    await Preferences.set({ key: DEVICES_KEY, value: JSON.stringify(_options.devices) });
    return { devices: _options.devices as Device[] };
  }

  async clearDevices(): Promise<Devices> {
    return this.setDevices({ devices: [] });
  }

  async startForegroundService(): Promise<StartResult> {
    await Preferences.set({ key: RUNNING_KEY, value: 'true' });
    return { result: 'started' };
  }

  async stopForegroundService(): Promise<void> {
    await Preferences.set({ key: RUNNING_KEY, value: 'false' });
  }

  async isRunning(): Promise<RunningResult> {
    const running = await Preferences.get({ key: RUNNING_KEY }).then((res) => res.value === 'true');
    return { running };
  }

  async didUserStop(): Promise<UserStoppedResult> {
    const stopped = await Preferences.get({ key: STOPPED_KEY }).then((res) => res.value === 'true');
    return { userStopped: stopped };
  }

  async setConfig(options: SetConfigOptions): Promise<Config> {
    await Preferences.set({ key: CONFIG_KEY, value: JSON.stringify(options.config) });
    return { config: options.config as ScanConfig };
  }

  async getConfig(): Promise<Config> {
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
          threshold: -100,
        },
      });
    }
  }

  addListener(
    _eventName: 'devicesChanged' | 'closeDevicesChanged',
    _event: DevicesChangedListener,
  ): Promise<PluginListenerHandle> {
    return Promise.resolve({
      remove: () => Promise.resolve(),
    });
  }
}
