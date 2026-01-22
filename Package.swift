// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "BackgroundBluetoothLe",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "BackgroundBluetoothLe",
            targets: ["BackgroundBLEPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0")
    ],
    targets: [
        .target(
            name: "BackgroundBLEPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/BackgroundBLEPlugin"),
        .testTarget(
            name: "BackgroundBLEPluginTests",
            dependencies: ["BackgroundBLEPlugin"],
            path: "ios/Tests/BackgroundBLEPluginTests")
    ]
)