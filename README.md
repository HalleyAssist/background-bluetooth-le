# Background Bluetooth Low Energy

Run a BLE scan in the background, and display seen devices in a notification.

**Android only**

## Install

```bash
yarn add background-bluetooth-le
npx cap sync
```

## API

<docgen-index>

* [`checkPermissions()`](#checkpermissions)
* [`requestPermissions()`](#requestpermissions)
* [`addDevice(...)`](#adddevice)
* [`addDevices(...)`](#adddevices)
* [`removeDevice(...)`](#removedevice)
* [`clearDevices()`](#cleardevices)
* [`startForegroundService()`](#startforegroundservice)
* [`stopForegroundService()`](#stopforegroundservice)
* [`isRunning()`](#isrunning)
* [`setScanMode(...)`](#setscanmode)
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


### addDevice(...)

```typescript
addDevice(options: AddDeviceOptions) => Promise<AddDeviceResult>
```

Add a device to the list of devices to scan for

| Param         | Type                                                          | Description                 |
| ------------- | ------------------------------------------------------------- | --------------------------- |
| **`options`** | <code><a href="#adddeviceoptions">AddDeviceOptions</a></code> | The options to add a device |

**Returns:** <code>Promise&lt;<a href="#adddeviceresult">AddDeviceResult</a>&gt;</code>

--------------------


### addDevices(...)

```typescript
addDevices(options: AddDevicesOptions) => Promise<AddDevicesResult>
```

Add multiple devices to the list of devices to scan for

| Param         | Type                                                            | Description                         |
| ------------- | --------------------------------------------------------------- | ----------------------------------- |
| **`options`** | <code><a href="#adddevicesoptions">AddDevicesOptions</a></code> | The options to add multiple devices |

**Returns:** <code>Promise&lt;<a href="#adddevicesresult">AddDevicesResult</a>&gt;</code>

--------------------


### removeDevice(...)

```typescript
removeDevice(options: RemoveDeviceOptions) => Promise<RemoveDeviceResult>
```

Remove a device from the list of devices to scan for

| Param         | Type                                                                | Description                    |
| ------------- | ------------------------------------------------------------------- | ------------------------------ |
| **`options`** | <code><a href="#removedeviceoptions">RemoveDeviceOptions</a></code> | The options to remove a device |

**Returns:** <code>Promise&lt;<a href="#removedeviceresult">RemoveDeviceResult</a>&gt;</code>

--------------------


### clearDevices()

```typescript
clearDevices() => Promise<ClearDevicesResult>
```

Clear the list of devices to scan for

**Returns:** <code>Promise&lt;<a href="#cleardevicesresult">ClearDevicesResult</a>&gt;</code>

--------------------


### startForegroundService()

```typescript
startForegroundService() => Promise<void>
```

Start the background scanner

--------------------


### stopForegroundService()

```typescript
stopForegroundService() => Promise<void>
```

Stop the background scanner

--------------------


### isRunning()

```typescript
isRunning() => Promise<IsRunningResult>
```

is the background scanner running

**Returns:** <code>Promise&lt;<a href="#isrunningresult">IsRunningResult</a>&gt;</code>

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

--------------------


### Interfaces


#### PermissionStatus

| Prop                | Type                                                        |
| ------------------- | ----------------------------------------------------------- |
| **`bluetooth`**     | <code><a href="#permissionstate">PermissionState</a></code> |
| **`notifications`** | <code><a href="#permissionstate">PermissionState</a></code> |


#### AddDeviceResult

| Prop         | Type                |
| ------------ | ------------------- |
| **`result`** | <code>string</code> |


#### AddDeviceOptions

| Prop       | Type                |
| ---------- | ------------------- |
| **`name`** | <code>string</code> |
| **`id`**   | <code>string</code> |


#### AddDevicesResult

| Prop         | Type                |
| ------------ | ------------------- |
| **`result`** | <code>string</code> |


#### AddDevicesOptions

| Prop          | Type                            |
| ------------- | ------------------------------- |
| **`devices`** | <code>AddDeviceOptions[]</code> |


#### RemoveDeviceResult

| Prop         | Type                |
| ------------ | ------------------- |
| **`result`** | <code>string</code> |


#### RemoveDeviceOptions

| Prop     | Type                |
| -------- | ------------------- |
| **`id`** | <code>string</code> |


#### ClearDevicesResult

| Prop         | Type                |
| ------------ | ------------------- |
| **`result`** | <code>string</code> |


#### IsRunningResult

| Prop          | Type                 |
| ------------- | -------------------- |
| **`running`** | <code>boolean</code> |


#### SetScanModeOptions

| Prop       | Type                                          |
| ---------- | --------------------------------------------- |
| **`mode`** | <code><a href="#scanmode">ScanMode</a></code> |


### Type Aliases


#### PermissionState

<code>'prompt' | 'prompt-with-rationale' | 'granted' | 'denied'</code>


### Enums


#### ScanMode

| Members             | Value           |
| ------------------- | --------------- |
| **`OPPORTUNISTIC`** | <code>-1</code> |
| **`LOW_POWER`**     | <code>0</code>  |
| **`BALANCED`**      | <code>1</code>  |
| **`LOW_LATENCY`**   | <code>2</code>  |

</docgen-api>
