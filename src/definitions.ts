import type { PermissionState } from '@capacitor/core';

/**
 * The permission state
 *
 * The permission state is a string that can be one of the following:
 * - 'granted': The permission is granted
 * - 'denied': The permission is denied
 * - 'prompt': The permission is prompt
 * - 'unavailable': The permission is unavailable
 *
 * @since 1.0.0
 */
export interface PermissionStatus {
  bluetooth: PermissionState;
  notifications: PermissionState;
}

/**
 * A device
 *
 * @since 1.0.0
 */
export interface Device {
  /**
   * The serial of the device
   *
   * @since 1.0.0
   */
  serial: string;
  /**
   * The display name of the device
   *
   * @since 1.0.0
   */
  name: string;
  /**
   * The RSSI of the device
   *
   * 0 = device is not in range
   *
   * @since 1.0.0
   */
  rssi: number;
  /**
   * The TX power of the device
   *
   * -127 = unknown TX power
   *
   * @since 1.0.0
   */
  txPower: number;
  /**
   * The last time the device was updated
   *
   * in milliseconds since epoch
   *
   * @since 1.0.0
   */
  lastUpdated: number;
}

/**
 * The options to add a device
 *
 * Only requires the serial and name of the device
 *
 * @since 1.0.0
 */
export interface AddDeviceOptions {
  /**
   * The serial of the device
   *
   * @since 1.0.0
   */
  serial: string;
  /**
   * The display name of the device
   *
   * @since 1.0.0
   */
  name: string;
}

/**
 * The options to add multiple devices
 *
 * @since 1.0.0
 */
export interface AddDevicesOptions {
  /**
   * The devices to add to the list of devices to scan for
   *
   * @since 1.0.0
   */
  devices: AddDeviceOptions[];
}

/**
 * The options to remove a device
 *
 * Only requires the serial of the device
 *
 * @since 1.0.0
 */
export interface RemoveDeviceOptions {
  /**
   * The serial of the device
   *
   * @since 1.0.0
   */
  serial: string;
}

/**
 * The result type is used to define the result of a function
 *
 * @default { result: string }
 *
 * @since 1.0.0
 */
export type Result<Key extends string = 'result', T = string> = {
  [key in Key]: T;
};

/**
 * The scan mode, taken from the Android API
 */
export enum ScanMode {
  /**
   * A special Bluetooth LE scan mode.
   * Applications using this scan mode will passively listen for other scan results without starting BLE scans themselves.
   */
  OPPORTUNISTIC = -1,
  /**
   * Perform Bluetooth LE scan in low power mode.
   * This is the default scan mode as it consumes the least power.
   */
  LOW_POWER = 0,
  /**
   * Perform Bluetooth LE scan in balanced power mode.
   * Scan results are returned at a rate that provides a good trade-off between scan frequency and power consumption.
   */
  BALANCED = 1,
  /**
   * Scan for Bluetooth LE devices using a high duty cycle.
   * It's recommended to only use this mode when the application is running in the foreground.
   */
  LOW_LATENCY = 2,
}

/**
 * The options to set the scan mode
 */
export interface SetScanModeOptions {
  /**
   * The scan mode to set
   *
   * @see ScanMode
   *
   * @default LOW_POWER
   *
   * @since 1.0.0
   */
  mode: ScanMode;
}

export interface BackgroundBLEPlugin {
  checkPermissions(): Promise<PermissionStatus>;
  requestPermissions(): Promise<PermissionStatus>;
  /**
   * Initialise the background scanner
   *
   * @param options The options to initialise the background scanner
   *
   * @since 1.0.0
   */
  initialise(): Promise<void>;
  /**
   * Add a device to the list of devices to scan for
   *
   * @param options The options to add a device
   *
   * @since 1.0.0
   */
  addDevice(options: AddDeviceOptions): Promise<Result>;
  /**
   * Add multiple devices to the list of devices to scan for
   *
   * @param options The options to add multiple devices
   *
   * @since 1.0.0
   */
  addDevices(options: AddDevicesOptions): Promise<Result>;
  /**
   * Remove a device from the list of devices to scan for
   *
   * @param options The options to remove a device
   *
   * @since 1.0.0
   */
  removeDevice(options: RemoveDeviceOptions): Promise<Result>;
  /**
   * Clear the list of devices to scan for
   *
   * @since 1.0.0
   */
  clearDevices(): Promise<Result>;
  /**
   * Start the background scanner
   *
   * @since 1.0.0
   */
  startForegroundService(): Promise<void>;
  /**
   * Stop the background scanner
   *
   * @since 1.0.0
   */
  stopForegroundService(): Promise<void>;
  /**
   * Is the background scanner running
   *
   * @since 1.0.0
   */
  isRunning(): Promise<Result<'running', boolean>>;
  /**
   * Set the scan mode
   *
   * The scan mode can be one of the following:
   * - OPPORTUNISTIC: A special Bluetooth LE scan mode.
   *                  Applications using this scan mode will passively listen for other scan results without starting BLE scans themselves.
   * - LOW_POWER:     Perform Bluetooth LE scan in low power mode.
   *                  This is the default scan mode as it consumes the least power.
   * - BALANCED:      Perform Bluetooth LE scan in balanced power mode.
   *                  Scan results are returned at a rate that provides a good trade-off between scan frequency and power consumption.
   * - LOW_LATENCY:   Scan for Bluetooth LE devices using a high duty cycle.
   *                  It's recommended to only use this mode when the application is running in the foreground.
   *
   * @param options The options to set the scan mode
   *
   * @since 1.0.0
   */
  setScanMode(options: SetScanModeOptions): Promise<void>;
  /**
   * Get the current list of devices
   *
   * @since 1.0.0
   */
  getDevices(): Promise<Result<'devices', Device[]>>;
}
