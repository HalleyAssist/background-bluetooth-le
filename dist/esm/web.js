import { WebPlugin } from '@capacitor/core';
import { ScanMode } from './definitions';
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
  async getDevices() {
    return { devices: [] };
  }
  async setDevices(_options) {
    return { devices: [] };
  }
  async clearDevices() {
    return { devices: [] };
  }
  async startForegroundService() {
    return { result: 'not supported' };
  }
  async stopForegroundService() {
    return;
  }
  async isRunning() {
    return { running: false };
  }
  async setConfig(options) {
    return { config: options.config };
  }
  async getConfig() {
    return {
      config: {
        mode: ScanMode.OPPORTUNISTIC,
        debug: false,
        deviceTimeout: 30000,
      },
    };
  }
}
//# sourceMappingURL=web.js.map
