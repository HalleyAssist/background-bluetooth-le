# Background Bluetooth Low Energy

Run a BLE scan in the background, and display seen devices in a notification.

**Android only**

## Install

```bash
yarn add background-bluetooth-le
npx cap sync
```

## Permissions

### Android

The following permissions are required:

```xml
<uses-permission
  android:name="android.permission.ACCESS_COARSE_LOCATION"
  android:maxSdkVersion="30" />
<uses-permission
  android:name="android.permission.ACCESS_FINE_LOCATION"
  android:maxSdkVersion="30" />
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
  
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE" />

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
* [`getDevices()`](#getdevices)
* [`setDevices(...)`](#setdevices)
* [`clearDevices()`](#cleardevices)
* [`startForegroundService()`](#startforegroundservice)
* [`stopForegroundService()`](#stopforegroundservice)
* [`isRunning()`](#isrunning)
* [`getConfig()`](#getconfig)
* [`setConfig(...)`](#setconfig)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)
* [Enums](#enums)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

The background BLE plugin

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


### getDevices()

```typescript
getDevices() => Promise<Result<'devices', Device[]>>
```

Get the current list of devices

**Returns:** <code>Promise&lt;<a href="#result">Result</a>&lt;'devices', Device[]&gt;&gt;</code>

**Since:** 1.0.0

--------------------


### setDevices(...)

```typescript
setDevices(options: AddDevicesOptions) => Promise<Result<'devices', Device[]>>
```

Set the list of devices to scan for

| Param         | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`options`** | <code><a href="#adddevicesoptions">AddDevicesOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#result">Result</a>&lt;'devices', Device[]&gt;&gt;</code>

**Since:** 1.0.0

--------------------


### clearDevices()

```typescript
clearDevices() => Promise<Result<'devices', Device[]>>
```

Clear the list of devices to scan for

**Returns:** <code>Promise&lt;<a href="#result">Result</a>&lt;'devices', Device[]&gt;&gt;</code>

**Since:** 1.0.0

--------------------


### startForegroundService()

```typescript
startForegroundService() => Promise<Result<'result', string>>
```

Start the background scanner

**Returns:** <code>Promise&lt;<a href="#result">Result</a>&lt;'result', string&gt;&gt;</code>

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


### getConfig()

```typescript
getConfig() => Promise<Result<'config', ScanConfig>>
```

Get the scanner configuration

**Returns:** <code>Promise&lt;<a href="#result">Result</a>&lt;'config', <a href="#scanconfig">ScanConfig</a>&gt;&gt;</code>

**Since:** 1.0.0

--------------------


### setConfig(...)

```typescript
setConfig(options: SetConfigOptions) => Promise<Result<'config', ScanConfig>>
```

Set the scanner configuration

| Param         | Type                                                          | Description                                  |
| ------------- | ------------------------------------------------------------- | -------------------------------------------- |
| **`options`** | <code><a href="#setconfigoptions">SetConfigOptions</a></code> | The options to set the scanner configuration |

**Returns:** <code>Promise&lt;<a href="#result">Result</a>&lt;'config', <a href="#scanconfig">ScanConfig</a>&gt;&gt;</code>

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


#### Device

A device

| Prop              | Type                | Description                                                      | Since |
| ----------------- | ------------------- | ---------------------------------------------------------------- | ----- |
| **`serial`**      | <code>string</code> | The serial of the device                                         | 1.0.0 |
| **`name`**        | <code>string</code> | The display name of the device                                   | 1.0.0 |
| **`rssi`**        | <code>number</code> | The RSSI of the device 0 = device is not in range                | 1.0.0 |
| **`txPower`**     | <code>number</code> | The TX power of the device -127 = unknown TX power               | 1.0.0 |
| **`lastUpdated`** | <code>number</code> | The last time the device was updated in milliseconds since epoch | 1.0.0 |


#### AddDevicesOptions

The options to add multiple devices

| Prop          | Type                            | Description                                           | Since |
| ------------- | ------------------------------- | ----------------------------------------------------- | ----- |
| **`devices`** | <code>AddDeviceOptions[]</code> | The devices to add to the list of devices to scan for | 1.0.0 |


#### AddDeviceOptions

The options to add a device

Only requires the serial and name of the device

| Prop         | Type                | Description                    | Since |
| ------------ | ------------------- | ------------------------------ | ----- |
| **`serial`** | <code>string</code> | The serial of the device       | 1.0.0 |
| **`name`**   | <code>string</code> | The display name of the device | 1.0.0 |


#### ScanConfig

The scan configuration

| Prop                | Type                                          | Description                                                                                                                                                              | Default                         | Since |
| ------------------- | --------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------- | ----- |
| **`mode`**          | <code><a href="#scanmode">ScanMode</a></code> | The scan mode                                                                                                                                                            | <code>ScanMode.LOW_POWER</code> | 1.0.0 |
| **`debug`**         | <code>boolean</code>                          | The debug mode                                                                                                                                                           | <code>false</code>              | 1.0.0 |
| **`deviceTimeout`** | <code>number</code>                           | The device timeout in milliseconds If a device has not had a scan result for this amount of time, it will be assumed to be out of range and will be pushed down the list | <code>30000</code>              | 1.0.0 |


#### SetConfigOptions

The options to set the configuration

| Prop         | Type                                                                                    | Description              | Since |
| ------------ | --------------------------------------------------------------------------------------- | ------------------------ | ----- |
| **`config`** | <code><a href="#partial">Partial</a>&lt;<a href="#scanconfig">ScanConfig</a>&gt;</code> | The configuration to set | 1.0.0 |


### Type Aliases


#### PermissionState

<code>'prompt' | 'prompt-with-rationale' | 'granted' | 'denied'</code>


#### Result

The result type is used to define the result of a function

<code>{ [key in Key]: T; }</code>


#### Partial

Make all properties in T optional

<code>{ [P in keyof T]?: T[P]; }</code>


### Enums


#### ScanMode

| Members             | Value           | Description                                                                                                                                                        |
| ------------------- | --------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **`OPPORTUNISTIC`** | <code>-1</code> | A special Bluetooth LE scan mode. Applications using this scan mode will passively listen for other scan results without starting BLE scans themselves.            |
| **`LOW_POWER`**     | <code>0</code>  | Perform Bluetooth LE scan in low power mode. This is the default scan mode as it consumes the least power.                                                         |
| **`BALANCED`**      | <code>1</code>  | Perform Bluetooth LE scan in balanced power mode. Scan results are returned at a rate that provides a good trade-off between scan frequency and power consumption. |
| **`LOW_LATENCY`**   | <code>2</code>  | Scan for Bluetooth LE devices using a high duty cycle. It's recommended to only use this mode when the application is running in the foreground.                   |

</docgen-api>
