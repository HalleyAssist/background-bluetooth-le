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
        CAPPluginMethod(name: "getDevices", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setDevices", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "clearDevices", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "startForegroundService", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "stopForegroundService", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "isRunning", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "didUserStop", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getConfig", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setConfig", returnType: CAPPluginReturnPromise),
    ]
    private let implementation = BackgroundBLE()

    @objc public func initialise(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS")
    }

    @objc public func getDevices(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS")
    }

    @objc public func setDevices(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS")
    }

    @objc public func clearDevices(_ call: CAPPluginCall) {
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

    @objc public func didUserStop(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS")
    }

    @objc public func getConfig(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS")
    }

    @objc public func setConfig(_ call: CAPPluginCall) {
        call.unimplemented("Not implemented on iOS")
    }

}
