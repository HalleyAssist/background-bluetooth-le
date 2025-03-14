import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(BackgroundBLEPlugin)
public class BackgroundBLEPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "BackgroundBLEPlugin"
    public let jsName = "BackgroundBLE"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "initialise", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "addDevice", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "addDevices", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "removeDevice", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "startForegroundService", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "stopForegroundService", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "isRunning", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getDevices", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setScanMode", returnType: CAPPluginReturnPromise),
    ]
    private let implementation = BackgroundBLE()

    @objc public func initialise(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS")
    }

    @objc public func getDevices(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS")
    }

    @objc public func addDevice(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS")
    }

    @objc public func addDevices(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS")
    }

    @objc public func removeDevice(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS")
    }

    @objc public func startForegroundService(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS")
    }

    @objc public func stopForegroundService(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS")
    }

    @objc public func isRunning(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS")
    }

    @objc public func setScanMode(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS")
    }

}
