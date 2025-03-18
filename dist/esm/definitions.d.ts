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
export declare enum ScanMode {
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
 * The scan configuration
 */
export interface ScanConfig {
  /**
   * The scan mode
   *
   * @see ScanMode
   *
   * @default ScanMode.LOW_POWER
   *
   * @since 1.0.0
   */
  mode: ScanMode;
  /**
   * The debug mode
   *
   * @default false
   *
   * @since 1.0.0
   */
  debug: boolean;
  /**
   * The device timeout in milliseconds
   *
   * If a device has not had a scan result for this amount of time, it will be assumed to be out of range and will be pushed down the list
   *
   * @default 30000
   *
   * @since 1.0.0
   */
  deviceTimeout: number;
}
/**
 * The options to set the configuration
 */
export interface SetConfigOptions {
  /**
   * The configuration to set
   *
   * @since 1.0.0
   */
  config: Partial<ScanConfig>;
}
/**
 * The background BLE plugin
 */
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
   * Get the current list of devices
   *
   * @since 1.0.0
   */
  getDevices(): Promise<Result<'devices', Device[]>>;
  /**
   * Set the list of devices to scan for
   *
   * @param devices The devices to scan for
   * @returns The new list of devices
   * @since 1.0.0
   */
  setDevices(options: AddDevicesOptions): Promise<Result<'devices', Device[]>>;
  /**
   * Clear the list of devices to scan for
   *
   * @returns The list of devices
   *
   * @since 1.0.0
   */
  clearDevices(): Promise<Result<'devices', Device[]>>;
  /**
   * Start the background scanner
   *
   * @returns The result of starting the background scanner
   *
   * @since 1.0.0
   */
  startForegroundService(): Promise<Result<'result', string>>;
  /**
   * Stop the background scanner
   *
   * @since 1.0.0
   */
  stopForegroundService(): Promise<void>;
  /**
   * Is the background scanner running
   *
   * @returns The result of whether the background scanner is running
   *
   * @since 1.0.0
   */
  isRunning(): Promise<Result<'running', boolean>>;
  /**
   * Get the scanner configuration
   *
   * @returns The scanner configuration
   *
   * @since 1.0.0
   */
  getConfig(): Promise<Result<'config', ScanConfig>>;
  /**
   * Set the scanner configuration
   *
   * @param options The options to set the scanner configuration
   * @returns The new scanner configuration
   *
   * @since 1.0.0
   */
  setConfig(options: SetConfigOptions): Promise<Result<'config', ScanConfig>>;
}
