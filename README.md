# Background Bluetooth Low Energy

Run a BLE scan in the background, and display seen devices in a notification.

**Android only**

## Install

```bash
yarn add background-bluetooth-le
npx cap sync
```

## Permissions

On Android, the following permissions are required:

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE" />

<uses-permission android:name="android.permission.ACCESS_COURSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission
  android:name="android.permission.BLUETOOTH"
  android:maxSdkVersion="30" />
<uses-permission
  android:name="android.permission.BLUETOOTH_SCAN"
  android:usesPermissionFlags="neverForLocation"
  tools:targetApi="31" />
<uses-permission
  android:name="android.permission.BLUETOOTH_CONNECT"
  tools:targetApi="31" />

<uses-feature
  android:name="android.hardware.bluetooth_le"
  android:required="false" />
```

To use the foreground service, you must also add the following to your `AndroidManifest.xml` :

```xml
<application>
  <!-- Other application details -->
  <service
    android:name="com.halleyassist.backgroundble.BackgroundBLEService"
    android:foregroundServiceType="connectedDevice" />
</application>
```

A drawable resource is also required for the notification icon. this should use the name `ic_notification` .

## API

<docgen-index>

* [`checkPermissions()`](#checkpermissions)
* [`requestPermissions()`](#requestpermissions)
* [`initialise()`](#initialise)
* [`addDevice(...)`](#adddevice)
* [`addDevices(...)`](#adddevices)
* [`removeDevice(...)`](#removedevice)
* [`clearDevices()`](#cleardevices)
* [`startForegroundService()`](#startforegroundservice)
* [`stopForegroundService()`](#stopforegroundservice)
* [`isRunning()`](#isrunning)
* [`setScanMode(...)`](#setscanmode)
* [`getDevices()`](#getdevices)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)
* [Enums](#enums)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### checkPermissions()

```typescript
checkPermissions() => Promise<PermissionStatus>
```

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

--------------------


### requestPermissions()

```typescript
requestPermissions() => Promise<PermissionStatus>
```

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

--------------------


### initialise()

```typescript
initialise() => Promise<void>
```

Initialise the background scanner

**Since:** 1.0.0

--------------------


### addDevice(...)

```typescript
addDevice(options: AddDeviceOptions) => Promise<Result>
```

Add a device to the list of devices to scan for

| Param         | Type                                                          | Description                 |
| ------------- | ------------------------------------------------------------- | --------------------------- |
| **`options`** | <code><a href="#adddeviceoptions">AddDeviceOptions</a></code> | The options to add a device |

**Returns:** <code>Promise&lt;<a href="#result">Result</a>&lt;'result', string&gt;&gt;</code>

**Since:** 1.0.0

--------------------


### addDevices(...)

```typescript
addDevices(options: AddDevicesOptions) => Promise<Result>
```

Add multiple devices to the list of devices to scan for

| Param         | Type                                                            | Description                         |
| ------------- | --------------------------------------------------------------- | ----------------------------------- |
| **`options`** | <code><a href="#adddevicesoptions">AddDevicesOptions</a></code> | The options to add multiple devices |

**Returns:** <code>Promise&lt;<a href="#result">Result</a>&lt;'result', string&gt;&gt;</code>

**Since:** 1.0.0

--------------------


### removeDevice(...)

```typescript
removeDevice(options: RemoveDeviceOptions) => Promise<Result>
```

Remove a device from the list of devices to scan for

| Param         | Type                                                                | Description                    |
| ------------- | ------------------------------------------------------------------- | ------------------------------ |
| **`options`** | <code><a href="#removedeviceoptions">RemoveDeviceOptions</a></code> | The options to remove a device |

**Returns:** <code>Promise&lt;<a href="#result">Result</a>&lt;'result', string&gt;&gt;</code>

**Since:** 1.0.0

--------------------


### clearDevices()

```typescript
clearDevices() => Promise<Result>
```

Clear the list of devices to scan for

**Returns:** <code>Promise&lt;<a href="#result">Result</a>&lt;'result', string&gt;&gt;</code>

**Since:** 1.0.0

--------------------


### startForegroundService()

```typescript
startForegroundService() => Promise<void>
```

Start the background scanner

**Since:** 1.0.0

--------------------


### stopForegroundService()

```typescript
stopForegroundService() => Promise<void>
```

Stop the background scanner

**Since:** 1.0.0

--------------------


### isRunning()

```typescript
isRunning() => Promise<Result<'running', boolean>>
```

Is the background scanner running

**Returns:** <code>Promise&lt;<a href="#result">Result</a>&lt;'running', boolean&gt;&gt;</code>

**Since:** 1.0.0

--------------------


### setScanMode(...)

```typescript
setScanMode(options: SetScanModeOptions) => Promise<void>
```

Set the scan mode

The scan mode can be one of the following:
- OPPORTUNISTIC: A special Bluetooth LE scan mode.
                 Applications using this scan mode will passively listen for other scan results without starting BLE scans themselves.
- LOW_POWER:     Perform Bluetooth LE scan in low power mode.
                 This is the default scan mode as it consumes the least power.
- BALANCED:      Perform Bluetooth LE scan in balanced power mode.
                 Scan results are returned at a rate that provides a good trade-off between scan frequency and power consumption.
- LOW_LATENCY:   Scan for Bluetooth LE devices using a high duty cycle.
                 It's recommended to only use this mode when the application is running in the foreground.

| Param         | Type                                                              | Description                      |
| ------------- | ----------------------------------------------------------------- | -------------------------------- |
| **`options`** | <code><a href="#setscanmodeoptions">SetScanModeOptions</a></code> | The options to set the scan mode |

**Since:** 1.0.0

--------------------


### getDevices()

```typescript
getDevices() => Promise<Result<'devices', Device[]>>
```

Get the current list of devices

**Returns:** <code>Promise&lt;<a href="#result">Result</a>&lt;'devices', Device[]&gt;&gt;</code>

**Since:** 1.0.0

--------------------


### Interfaces


#### PermissionStatus

The permission state

The permission state is a string that can be one of the following:
- 'granted': The permission is granted
- 'denied': The permission is denied
- 'prompt': The permission is prompt
- 'unavailable': The permission is unavailable

| Prop                | Type                                                        |
| ------------------- | ----------------------------------------------------------- |
| **`bluetooth`**     | <code><a href="#permissionstate">PermissionState</a></code> |
| **`notifications`** | <code><a href="#permissionstate">PermissionState</a></code> |


#### AddDeviceOptions

The options to add a device

Only requires the serial and name of the device

| Prop         | Type                | Description                    | Since |
| ------------ | ------------------- | ------------------------------ | ----- |
| **`serial`** | <code>string</code> | The serial of the device       | 1.0.0 |
| **`name`**   | <code>string</code> | The display name of the device | 1.0.0 |


#### AddDevicesOptions

The options to add multiple devices

| Prop          | Type                            | Description                                           | Since |
| ------------- | ------------------------------- | ----------------------------------------------------- | ----- |
| **`devices`** | <code>AddDeviceOptions[]</code> | The devices to add to the list of devices to scan for | 1.0.0 |


#### RemoveDeviceOptions

The options to remove a device

Only requires the serial of the device

| Prop         | Type                | Description              | Since |
| ------------ | ------------------- | ------------------------ | ----- |
| **`serial`** | <code>string</code> | The serial of the device | 1.0.0 |


#### SetScanModeOptions

The options to set the scan mode

| Prop       | Type                                          | Description          | Default                | Since |
| ---------- | --------------------------------------------- | -------------------- | ---------------------- | ----- |
| **`mode`** | <code><a href="#scanmode">ScanMode</a></code> | The scan mode to set | <code>LOW_POWER</code> | 1.0.0 |


#### Device

A device

| Prop              | Type                | Description                                                      | Since |
| ----------------- | ------------------- | ---------------------------------------------------------------- | ----- |
| **`serial`**      | <code>string</code> | The serial of the device                                         | 1.0.0 |
| **`name`**        | <code>string</code> | The display name of the device                                   | 1.0.0 |
| **`rssi`**        | <code>number</code> | The RSSI of the device 0 = device is not in range                | 1.0.0 |
| **`txPower`**     | <code>number</code> | The TX power of the device -127 = unknown TX power               | 1.0.0 |
| **`lastUpdated`** | <code>number</code> | The last time the device was updated in milliseconds since epoch | 1.0.0 |


### Type Aliases


#### PermissionState

<code>'prompt' | 'prompt-with-rationale' | 'granted' | 'denied'</code>


#### Result

The result type is used to define the result of a function

<code>{ [key in Key]: T; }</code>


### Enums


#### ScanMode

| Members             | Value           | Description                                                                                                                                                        |
| ------------------- | --------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **`OPPORTUNISTIC`** | <code>-1</code> | A special Bluetooth LE scan mode. Applications using this scan mode will passively listen for other scan results without starting BLE scans themselves.            |
| **`LOW_POWER`**     | <code>0</code>  | Perform Bluetooth LE scan in low power mode. This is the default scan mode as it consumes the least power.                                                         |
| **`BALANCED`**      | <code>1</code>  | Perform Bluetooth LE scan in balanced power mode. Scan results are returned at a rate that provides a good trade-off between scan frequency and power consumption. |
| **`LOW_LATENCY`**   | <code>2</code>  | Scan for Bluetooth LE devices using a high duty cycle. It's recommended to only use this mode when the application is running in the foreground.                   |

</docgen-api>
