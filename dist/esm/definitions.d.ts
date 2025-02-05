import type { PermissionState } from '@capacitor/core';
export interface PermissionStatus {
  bluetooth: PermissionState;
  notifications: PermissionState;
}
export interface AddDeviceOptions {
  name: string;
  displayName: string;
}
export interface AddDevicesOptions {
  devices: AddDeviceOptions[];
}
export interface RemoveDeviceOptions {
  name: string;
}
export interface AddDeviceResult {
  result: string;
}
export interface AddDevicesResult {
  result: string;
}
export interface RemoveDeviceResult {
  result: string;
}
export interface ClearDevicesResult {
  result: string;
}
export interface IsRunningResult {
  running: boolean;
}
export declare enum ScanMode {
  OPPORTUNISTIC = -1,
  LOW_POWER = 0,
  BALANCED = 1,
  LOW_LATENCY = 2,
}
export interface SetScanModeOptions {
  mode: ScanMode;
}
export interface BackgroundBLEPlugin {
  checkPermissions(): Promise<PermissionStatus>;
  requestPermissions(): Promise<PermissionStatus>;
  /**
   * Add a device to the list of devices to scan for
   *
   * @param options The options to add a device
   */
  addDevice(options: AddDeviceOptions): Promise<AddDeviceResult>;
  /**
   * Add multiple devices to the list of devices to scan for
   *
   * @param options The options to add multiple devices
   */
  addDevices(options: AddDevicesOptions): Promise<AddDevicesResult>;
  /**
   * Remove a device from the list of devices to scan for
   *
   * @param options The options to remove a device
   */
  removeDevice(options: RemoveDeviceOptions): Promise<RemoveDeviceResult>;
  /**
   * Clear the list of devices to scan for
   */
  clearDevices(): Promise<ClearDevicesResult>;
  /**
   * Start the background scanner
   */
  startForegroundService(): Promise<void>;
  /**
   * Stop the background scanner
   */
  stopForegroundService(): Promise<void>;
  /**
   * is the background scanner running
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
   */
  setScanMode(options: SetScanModeOptions): Promise<void>;
}
