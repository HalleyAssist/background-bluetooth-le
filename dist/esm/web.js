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
  async setScanMode(_options) {
    return { result: ScanMode.OPPORTUNISTIC };
  }
  async setDebugMode(options) {
    return { debug: options.debug };
  }
}
//# sourceMappingURL=web.js.map
