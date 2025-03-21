var capacitorBackgroundBLE = (function (exports, core) {
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
          mode: exports.ScanMode.OPPORTUNISTIC,
          debug: false,
          deviceTimeout: 30000,
        },
      };
    }
  }

  var web = /*#__PURE__*/ Object.freeze({
    __proto__: null,
    BackgroundBLEWeb: BackgroundBLEWeb,
  });

  exports.BackgroundBLE = BackgroundBLE;

  return exports;
})({}, capacitorExports);
//# sourceMappingURL=plugin.js.map
