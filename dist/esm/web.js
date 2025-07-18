import { WebPlugin } from '@capacitor/core';
import { Preferences } from '@capacitor/preferences';
import { ScanMode } from './definitions';
const DEVICES_KEY = 'backgroundble.devices';
const CONFIG_KEY = 'backgroundble.config';
const RUNNING_KEY = 'backgroundble.running';
const STOPPED_KEY = 'backgroundble.stopped';
export class BackgroundBLEWeb extends WebPlugin {
  async checkPermissions() {
    return { bluetooth: 'denied', notifications: 'denied' };
  }
  async requestPermissions() {
    return { bluetooth: 'denied', notifications: 'denied' };
  }
  async initialise() {
    return;
  }
  async enable() {
    return;
  }
  async getDevices() {
    const devices = await Preferences.get({ key: DEVICES_KEY });
    if (devices && devices.value) {
      return { devices: JSON.parse(devices.value) };
    }
    return { devices: [] };
  }
  async setDevices(_options) {
    await Preferences.set({ key: DEVICES_KEY, value: JSON.stringify(_options.devices) });
    return { devices: _options.devices };
  }
  async clearDevices() {
    return this.setDevices({ devices: [] });
  }
  async startForegroundService() {
    await Preferences.set({ key: RUNNING_KEY, value: 'true' });
    return { result: 'started' };
  }
  async stopForegroundService() {
    await Preferences.set({ key: RUNNING_KEY, value: 'false' });
    return { result: 'stopped' };
  }
  async isRunning() {
    const running = await Preferences.get({ key: RUNNING_KEY }).then((res) => res.value === 'true');
    return { running };
  }
  async didUserStop() {
    const stopped = await Preferences.get({ key: STOPPED_KEY }).then((res) => res.value === 'true');
    return { userStopped: stopped };
  }
  async setConfig(options) {
    await Preferences.set({ key: CONFIG_KEY, value: JSON.stringify(options.config) });
    return { config: options.config };
  }
  async getConfig() {
    const config = await Preferences.get({ key: CONFIG_KEY }).then((res) =>
      res.value ? JSON.parse(res.value) : undefined,
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
  addListener(_eventName, _event) {
    return Promise.resolve({
      remove: () => Promise.resolve(),
    });
  }
}
//# sourceMappingURL=web.js.map
