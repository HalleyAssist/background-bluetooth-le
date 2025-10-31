var capacitorBackgroundBLE = (function (exports, core, preferences) {
  'use strict';

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
    async enable() {
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
      return { result: 'stopped' };
    }
    async isRunning() {
      const running = await preferences.Preferences.get({ key: RUNNING_KEY });
      return { running: running.value === 'true' };
    }
    async didUserStop() {
      const stopped = await preferences.Preferences.get({ key: STOPPED_KEY });
      return { userStopped: stopped.value === 'true' };
    }
    async getActiveDevice() {
      const device = await preferences.Preferences.get({ key: 'backgroundble.activeDevice' });
      if (device && device.value) {
        return { device: JSON.parse(device.value) };
      }
      return { device: null };
    }
    async setActiveDevice(_device) {
      await preferences.Preferences.set({ key: 'backgroundble.activeDevice', value: JSON.stringify(_device) });
    }
    async setConfig(options) {
      await preferences.Preferences.set({ key: CONFIG_KEY, value: JSON.stringify(options.config) });
      return { config: options.config };
    }
    async getConfig() {
      const storedConfig = await preferences.Preferences.get({ key: CONFIG_KEY });
      const config = storedConfig.value ? JSON.parse(storedConfig.value) : undefined;
      if (config) {
        return { config };
      } else {
        return this.setConfig({
          config: {
            mode: exports.ScanMode.OPPORTUNISTIC,
            debug: false,
            deviceTimeout: 30000,
            threshold: -100,
            persistent: true,
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

  return exports;
})({}, capacitorExports, capacitorPreferencesExports);
//# sourceMappingURL=plugin.js.map
