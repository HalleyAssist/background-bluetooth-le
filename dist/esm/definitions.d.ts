import type { PermissionState } from '@capacitor/core';
export interface PermissionStatus {
  bluetooth: PermissionState;
  notifications: PermissionState;
}
export interface AddDeviceOptions {
  /**
   * The name of the device to scan for
   *
   * This is the name that the device advertises itself as, used for filtering devices
   *
   * @since 1.0.0
   */
  name: string;
  /**
   * The display name of the device
   *
   * This is the name that will be displayed to the user when the device is found
   *
   * @since 1.0.0
   */
  displayName: string;
}
export interface AddDevicesOptions {
  /**
   * The devices to add to the list of devices to scan for
   *
   * @since 1.0.0
   */
  devices: AddDeviceOptions[];
}
export interface RemoveDeviceOptions {
  /**
   * The name of the device to remove from the list of devices to scan for
   *
   * @since 1.0.0
   */
  name: string;
}
export interface AddDeviceResult {
  /**
   * The result of adding the device
   *
   * @since 1.0.0
   */
  result: string;
}
export interface AddDevicesResult {
  /**
   * The result of adding the devices
   *
   * @since 1.0.0
   */
  result: string;
}
export interface RemoveDeviceResult {
  /**
   * The result of removing the device
   *
   * @since 1.0.0
   */
  result: string;
}
export interface ClearDevicesResult {
  /**
   * The result of clearing the devices
   *
   * @since 1.0.0
   */
  result: string;
}
export interface IsRunningResult {
  /**
   * Whether the background scanner is running
   *
   * @since 1.0.0
   */
  running: boolean;
}
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
  addDevice(options: AddDeviceOptions): Promise<AddDeviceResult>;
  /**
   * Add multiple devices to the list of devices to scan for
   *
   * @param options The options to add multiple devices
   *
   * @since 1.0.0
   */
  addDevices(options: AddDevicesOptions): Promise<AddDevicesResult>;
  /**
   * Remove a device from the list of devices to scan for
   *
   * @param options The options to remove a device
   *
   * @since 1.0.0
   */
  removeDevice(options: RemoveDeviceOptions): Promise<RemoveDeviceResult>;
  /**
   * Clear the list of devices to scan for
   *
   * @since 1.0.0
   */
  clearDevices(): Promise<ClearDevicesResult>;
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
  isRunning(): Promise<IsRunningResult>;
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
}
