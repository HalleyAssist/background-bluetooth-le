'use strict';

var core = require('@capacitor/core');
var preferences = require('@capacitor/preferences');

/**
 * The scan mode, taken from the Android API
 */
exports.ScanMode = void 0;
(function (ScanMode) {
  /**
   * A special Bluetooth LE scan mode.
   * Applications using this scan mode will passively listen for other scan results without starting BLE scans themselves.
   */
  ScanMode[(ScanMode['OPPORTUNISTIC'] = -1)] = 'OPPORTUNISTIC';
  /**
   * Perform Bluetooth LE scan in low power mode.
   * This is the default scan mode as it consumes the least power.
   */
  ScanMode[(ScanMode['LOW_POWER'] = 0)] = 'LOW_POWER';
  /**
   * Perform Bluetooth LE scan in balanced power mode.
   * Scan results are returned at a rate that provides a good trade-off between scan frequency and power consumption.
   */
  ScanMode[(ScanMode['BALANCED'] = 1)] = 'BALANCED';
  /**
   * Scan for Bluetooth LE devices using a high duty cycle.
   * It's recommended to only use this mode when the application is running in the foreground.
   */
  ScanMode[(ScanMode['LOW_LATENCY'] = 2)] = 'LOW_LATENCY';
})(exports.ScanMode || (exports.ScanMode = {}));

const BackgroundBLE = core.registerPlugin('BackgroundBLE', {
  web: () =>
    Promise.resolve()
      .then(function () {
        return web;
      })
      .then((m) => new m.BackgroundBLEWeb()),
});

const DEVICES_KEY = 'backgroundble.devices';
const CONFIG_KEY = 'backgroundble.config';
const RUNNING_KEY = 'backgroundble.running';
const STOPPED_KEY = 'backgroundble.stopped';
class BackgroundBLEWeb extends core.WebPlugin {
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
    const devices = await preferences.Preferences.get({ key: DEVICES_KEY });
    if (devices && devices.value) {
      return { devices: JSON.parse(devices.value) };
    }
    return { devices: [] };
  }
  async setDevices(_options) {
    await preferences.Preferences.set({ key: DEVICES_KEY, value: JSON.stringify(_options.devices) });
    return { devices: _options.devices };
  }
  async clearDevices() {
    return this.setDevices({ devices: [] });
  }
  async startForegroundService() {
    await preferences.Preferences.set({ key: RUNNING_KEY, value: 'true' });
    return { result: 'started' };
  }
  async stopForegroundService() {
    await preferences.Preferences.set({ key: RUNNING_KEY, value: 'false' });
  }
  async isRunning() {
    const running = await preferences.Preferences.get({ key: RUNNING_KEY }).then((res) => res.value === 'true');
    return { running };
  }
  async didUserStop() {
    const stopped = await preferences.Preferences.get({ key: STOPPED_KEY }).then((res) => res.value === 'true');
    return { userStopped: stopped };
  }
  async setConfig(options) {
    await preferences.Preferences.set({ key: CONFIG_KEY, value: JSON.stringify(options.config) });
    return { config: options.config };
  }
  async getConfig() {
    const config = await preferences.Preferences.get({ key: CONFIG_KEY }).then((res) =>
      res.value ? JSON.parse(res.value) : undefined,
    );
    if (config) {
      return { config };
    } else {
      return this.setConfig({
        config: {
          mode: exports.ScanMode.OPPORTUNISTIC,
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

var web = /*#__PURE__*/ Object.freeze({
  __proto__: null,
  BackgroundBLEWeb: BackgroundBLEWeb,
});

exports.BackgroundBLE = BackgroundBLE;
//# sourceMappingURL=plugin.cjs.js.map
