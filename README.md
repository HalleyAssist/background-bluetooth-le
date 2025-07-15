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
  <!-- Bluetooth Scan Reciver -->
  <receiver android:name="com.halleyassist.backgroundble.BackgroundBLEReceiver"></receiver>
</application>
```

A drawable resource is also required for the notification icon. this should use the name `ic_notification` .

## API

<docgen-index>

* [`checkPermissions()`](#checkpermissions)
* [`requestPermissions()`](#requestpermissions)
* [`initialise()`](#initialise)
* [`enable()`](#enable)
* [`getDevices()`](#getdevices)
* [`setDevices(...)`](#setdevices)
* [`clearDevices()`](#cleardevices)
* [`startForegroundService()`](#startforegroundservice)
* [`stopForegroundService()`](#stopforegroundservice)
* [`isRunning()`](#isrunning)
* [`didUserStop()`](#diduserstop)
* [`getConfig()`](#getconfig)
* [`setConfig(...)`](#setconfig)
* [`addListener('devicesChanged', ...)`](#addlistenerdeviceschanged-)
* [`addListener('closeDevicesChanged', ...)`](#addlistenerclosedeviceschanged-)
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

--------------------


### enable()

```typescript
enable() => Promise<void>
```

Enable bluetooth

--------------------


### getDevices()

```typescript
getDevices() => Promise<Devices>
```

Get the current list of devices

**Returns:** <code>Promise&lt;<a href="#devices">Devices</a>&gt;</code>

--------------------


### setDevices(...)

```typescript
setDevices(options: AddDevicesOptions) => Promise<Devices>
```

Set the list of devices to scan for

| Param         | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`options`** | <code><a href="#adddevicesoptions">AddDevicesOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#devices">Devices</a>&gt;</code>

--------------------


### clearDevices()

```typescript
clearDevices() => Promise<Devices>
```

Clear the list of devices to scan for

**Returns:** <code>Promise&lt;<a href="#devices">Devices</a>&gt;</code>

--------------------


### startForegroundService()

```typescript
startForegroundService() => Promise<StartResult>
```

Start the background scanner

**Returns:** <code>Promise&lt;<a href="#startresult">StartResult</a>&gt;</code>

--------------------


### stopForegroundService()

```typescript
stopForegroundService() => Promise<void>
```

Stop the background scanner

--------------------


### isRunning()

```typescript
isRunning() => Promise<RunningResult>
```

Is the background scanner running

**Returns:** <code>Promise&lt;<a href="#runningresult">RunningResult</a>&gt;</code>

--------------------


### didUserStop()

```typescript
didUserStop() => Promise<UserStoppedResult>
```

Did the user stop the background scanner from the notification

**Returns:** <code>Promise&lt;<a href="#userstoppedresult">UserStoppedResult</a>&gt;</code>

--------------------


### getConfig()

```typescript
getConfig() => Promise<Config>
```

Get the scanner configuration

**Returns:** <code>Promise&lt;<a href="#config">Config</a>&gt;</code>

--------------------


### setConfig(...)

```typescript
setConfig(options: SetConfigOptions) => Promise<Config>
```

Set the scanner configuration

| Param         | Type                                                          | Description                                  |
| ------------- | ------------------------------------------------------------- | -------------------------------------------- |
| **`options`** | <code><a href="#setconfigoptions">SetConfigOptions</a></code> | The options to set the scanner configuration |

**Returns:** <code>Promise&lt;<a href="#config">Config</a>&gt;</code>

--------------------


### addListener('devicesChanged', ...)

```typescript
addListener(eventName: 'devicesChanged', event: DevicesChangedListener) => Promise<PluginListenerHandle>
```

Add a listener for when the list of devices changes

| Param           | Type                                                                      | Description                                                    |
| --------------- | ------------------------------------------------------------------------- | -------------------------------------------------------------- |
| **`eventName`** | <code>'devicesChanged'</code>                                             |                                                                |
| **`event`**     | <code><a href="#deviceschangedlistener">DevicesChangedListener</a></code> | The listener function to call when the list of devices changes |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### addListener('closeDevicesChanged', ...)

```typescript
addListener(eventName: 'closeDevicesChanged', event: DevicesChangedListener) => Promise<PluginListenerHandle>
```

Add a listener for when the list of close devices changes

This list only includes devices that are within the configured RSSI threshold.

| Param           | Type                                                                      | Description                                                          |
| --------------- | ------------------------------------------------------------------------- | -------------------------------------------------------------------- |
| **`eventName`** | <code>'closeDevicesChanged'</code>                                        |                                                                      |
| **`event`**     | <code><a href="#deviceschangedlistener">DevicesChangedListener</a></code> | The listener function to call when the list of close devices changes |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

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


#### Devices

The list of devices

| Prop          | Type                  | Description         |
| ------------- | --------------------- | ------------------- |
| **`devices`** | <code>Device[]</code> | The list of devices |


#### Device

A device

| Prop              | Type                | Description                                                      |
| ----------------- | ------------------- | ---------------------------------------------------------------- |
| **`serial`**      | <code>string</code> | The serial of the device                                         |
| **`name`**        | <code>string</code> | The display name of the device                                   |
| **`rssi`**        | <code>number</code> | The RSSI of the device 0 = device is not in range                |
| **`txPower`**     | <code>number</code> | The TX power of the device -127 = unknown TX power               |
| **`lastUpdated`** | <code>number</code> | The last time the device was updated in milliseconds since epoch |


#### AddDevicesOptions

The options to add multiple devices

| Prop          | Type                            | Description                                           |
| ------------- | ------------------------------- | ----------------------------------------------------- |
| **`devices`** | <code>AddDeviceOptions[]</code> | The devices to add to the list of devices to scan for |


#### AddDeviceOptions

The options to add a device

Only requires the serial and name of the device

| Prop         | Type                | Description                    |
| ------------ | ------------------- | ------------------------------ |
| **`serial`** | <code>string</code> | The serial of the device       |
| **`name`**   | <code>string</code> | The display name of the device |


#### StartResult

The result of starting the background scanner

| Prop         | Type                | Description                                   |
| ------------ | ------------------- | --------------------------------------------- |
| **`result`** | <code>string</code> | The result of starting the background scanner |


#### RunningResult

The result of checking if the background scanner is running

| Prop          | Type                 | Description                                                 |
| ------------- | -------------------- | ----------------------------------------------------------- |
| **`running`** | <code>boolean</code> | The result of checking if the background scanner is running |


#### UserStoppedResult

The result of checking if the user stopped the background scanner

| Prop              | Type                 | Description                                                       |
| ----------------- | -------------------- | ----------------------------------------------------------------- |
| **`userStopped`** | <code>boolean</code> | The result of checking if the user stopped the background scanner |


#### Config

The current configuration of the scanner

| Prop         | Type                                              | Description                                     |
| ------------ | ------------------------------------------------- | ----------------------------------------------- |
| **`config`** | <code><a href="#scanconfig">ScanConfig</a></code> | The result of getting the scanner configuration |


#### ScanConfig

The scan configuration

| Prop                | Type                                          | Description                                                                                                                                                                   | Default                         |
| ------------------- | --------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------- |
| **`mode`**          | <code><a href="#scanmode">ScanMode</a></code> | The scan mode                                                                                                                                                                 | <code>ScanMode.LOW_POWER</code> |
| **`debug`**         | <code>boolean</code>                          | The debug mode                                                                                                                                                                | <code>false</code>              |
| **`deviceTimeout`** | <code>number</code>                           | The device timeout in milliseconds If a device has not had a scan result for this amount of time, it will be assumed to be out of range and will be pushed down the list      | <code>30000</code>              |
| **`threshold`**     | <code>number</code>                           | The minimum RSSI to consider a device in range If a device has an RSSI below this value, it will be considered out of range The value is clamped to the range of -10 to -100. | <code>-100</code>               |


#### SetConfigOptions

The options to set the configuration

| Prop         | Type                                                                                    | Description              |
| ------------ | --------------------------------------------------------------------------------------- | ------------------------ |
| **`config`** | <code><a href="#partial">Partial</a>&lt;<a href="#scanconfig">ScanConfig</a>&gt;</code> | The configuration to set |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


### Type Aliases


#### PermissionState

<code>'prompt' | 'prompt-with-rationale' | 'granted' | 'denied'</code>


#### Partial

Make all properties in T optional

<code>{ [P in keyof T]?: T[P]; }</code>


#### DevicesChangedListener

A listener that is called when the list of devices changes

<code>(devices: <a href="#devices">Devices</a>): void</code>


### Enums


#### ScanMode

| Members             | Value           | Description                                                                                                                                                        |
| ------------------- | --------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **`OPPORTUNISTIC`** | <code>-1</code> | A special Bluetooth LE scan mode. Applications using this scan mode will passively listen for other scan results without starting BLE scans themselves.            |
| **`LOW_POWER`**     | <code>0</code>  | Perform Bluetooth LE scan in low power mode. This is the default scan mode as it consumes the least power.                                                         |
| **`BALANCED`**      | <code>1</code>  | Perform Bluetooth LE scan in balanced power mode. Scan results are returned at a rate that provides a good trade-off between scan frequency and power consumption. |
| **`LOW_LATENCY`**   | <code>2</code>  | Scan for Bluetooth LE devices using a high duty cycle. It's recommended to only use this mode when the application is running in the foreground.                   |

</docgen-api>
