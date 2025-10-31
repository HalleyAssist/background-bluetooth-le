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
  StartStopResult,
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

  async startForegroundService(): Promise<StartStopResult> {
    await Preferences.set({ key: RUNNING_KEY, value: 'true' });
    return { result: 'started' };
  }

  async stopForegroundService(): Promise<StartStopResult> {
    await Preferences.set({ key: RUNNING_KEY, value: 'false' });
    return { result: 'stopped' };
  }

  async isRunning(): Promise<RunningResult> {
    const running = await Preferences.get({ key: RUNNING_KEY });
    return { running: running.value === 'true' };
  }

  async didUserStop(): Promise<UserStoppedResult> {
    const stopped = await Preferences.get({ key: STOPPED_KEY });
    return { userStopped: stopped.value === 'true' };
  }

  async getActiveDevice(): Promise<{ device: Device | null }> {
    const device = await Preferences.get({ key: 'backgroundble.activeDevice' });
    if (device && device.value) {
      return { device: JSON.parse(device.value) as Device };
    }
    return { device: null };
  }

  async setActiveDevice(_device: Device | null): Promise<void> {
    await Preferences.set({ key: 'backgroundble.activeDevice', value: JSON.stringify(_device) });
  }

  async setConfig(options: SetConfigOptions): Promise<Config> {
    await Preferences.set({ key: CONFIG_KEY, value: JSON.stringify(options.config) });
    return { config: options.config as ScanConfig };
  }

  async getConfig(): Promise<Config> {
    const storedConfig = await Preferences.get({ key: CONFIG_KEY });
    const config = storedConfig.value ? (JSON.parse(storedConfig.value) as ScanConfig) : undefined;
    if (config) {
      return { config };
    } else {
      return this.setConfig({
        config: {
          mode: ScanMode.OPPORTUNISTIC,
          debug: false,
          deviceTimeout: 30000,
          threshold: -100,
          persistent: true,
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
