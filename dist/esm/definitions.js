/**
 * The scan mode, taken from the Android API
 */
export var ScanMode;
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
})(ScanMode || (ScanMode = {}));
//# sourceMappingURL=definitions.js.map
