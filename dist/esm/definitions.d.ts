import type { PermissionState, Plugin, PluginListenerHandle } from '@capacitor/core';
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
   */
  serial: string;
  /**
   * The display name of the device
   */
  name: string;
  /**
   * The RSSI of the device
   *
   * 0 = device is not in range
   */
  rssi: number;
  /**
   * The TX power of the device
   *
   * -127 = unknown TX power
   */
  txPower: number;
  /**
   * The last time the device was updated
   *
   * in milliseconds since epoch
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
   */
  serial: string;
  /**
   * The display name of the device
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
   */
  serial: string;
}
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
   */
  mode: ScanMode;
  /**
   * The debug mode
   *
   * @default false
   */
  debug: boolean;
  /**
   * The device timeout in milliseconds
   *
   * If a device has not had a scan result for this amount of time, it will be assumed to be out of range and will be pushed down the list
   *
   * @default 30000
   */
  deviceTimeout: number;
  /**
   * The minimum RSSI to consider a device in range
   *
   * If a device has an RSSI below this value, it will be considered out of range
   *
   * The value is clamped to the range of -10 to -100.
   *
   * @default -100
   */
  threshold: number;
}
/**
 * The options to set the configuration
 */
export interface SetConfigOptions {
  /**
   * The configuration to set
   */
  config: Partial<ScanConfig>;
}
/**
 * The list of devices
 */
export interface Devices {
  /**
   * The list of devices
   */
  devices: Device[];
}
/**
 * The result of starting the background scanner
 */
export interface StartResult {
  /**
   * The result of starting the background scanner
   */
  result: string;
}
/**
 * The result of checking if the background scanner is running
 */
export interface RunningResult {
  /**
   * The result of checking if the background scanner is running
   */
  running: boolean;
}
/**
 * The result of checking if the user stopped the background scanner
 */
export interface UserStoppedResult {
  /**
   * The result of checking if the user stopped the background scanner
   */
  userStopped: boolean;
}
/**
 * The current configuration of the scanner
 */
export interface Config {
  /**
   * The result of getting the scanner configuration
   */
  config: ScanConfig;
}
/**
 * A listener that is called when the list of devices changes
 */
export type DevicesChangedListener = (devices: Devices) => void;
/**
 * The background BLE plugin
 */
export interface BackgroundBLEPlugin extends Plugin {
  checkPermissions(): Promise<PermissionStatus>;
  requestPermissions(): Promise<PermissionStatus>;
  /**
   * Initialise the background scanner
   *
   * @param options The options to initialise the background scanner
   */
  initialise(): Promise<void>;
  /**
   * Get the current list of devices
   */
  getDevices(): Promise<Devices>;
  /**
   * Set the list of devices to scan for
   *
   * @param devices The devices to scan for
   * @returns The new list of devices
   * @since 1.0.0
   */
  setDevices(options: AddDevicesOptions): Promise<Devices>;
  /**
   * Clear the list of devices to scan for
   *
   * @returns The list of devices
   */
  clearDevices(): Promise<Devices>;
  /**
   * Start the background scanner
   *
   * @returns The result of starting the background scanner
   */
  startForegroundService(): Promise<StartResult>;
  /**
   * Stop the background scanner
   */
  stopForegroundService(): Promise<void>;
  /**
   * Is the background scanner running
   *
   * @returns The result of whether the background scanner is running
   */
  isRunning(): Promise<RunningResult>;
  /**
   * Did the user stop the background scanner from the notification
   *
   * @returns The result of whether the user stopped the background scanner
   */
  didUserStop(): Promise<UserStoppedResult>;
  /**
   * Get the scanner configuration
   *
   * @returns The scanner configuration
   */
  getConfig(): Promise<Config>;
  /**
   * Set the scanner configuration
   *
   * @param options The options to set the scanner configuration
   * @returns The new scanner configuration
   */
  setConfig(options: SetConfigOptions): Promise<Config>;
  /**
   * Add a listener for when the list of devices changes
   *
   * @param event The listener function to call when the list of devices changes
   * @returns A handle to remove the listener
   */
  addListener(eventName: 'devicesChanged', event: DevicesChangedListener): Promise<PluginListenerHandle>;
}
