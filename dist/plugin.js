var capacitorBackgroundBLE = (function (exports, core) {
    'use strict';

    exports.ScanMode = void 0;
    (function (ScanMode) {
        ScanMode[ScanMode["OPPORTUNISTIC"] = -1] = "OPPORTUNISTIC";
        ScanMode[ScanMode["LOW_POWER"] = 0] = "LOW_POWER";
        ScanMode[ScanMode["BALANCED"] = 1] = "BALANCED";
        ScanMode[ScanMode["LOW_LATENCY"] = 2] = "LOW_LATENCY";
    })(exports.ScanMode || (exports.ScanMode = {}));

    const BackgroundBLE = core.registerPlugin('BackgroundBLE', {
        web: () => Promise.resolve().then(function () { return web; }).then((m) => new m.BackgroundBLEWeb()),
    });

    class BackgroundBLEWeb extends core.WebPlugin {
        async checkPermissions() {
            throw this.unimplemented('Not implemented on web.');
        }
        async requestPermissions() {
            throw this.unimplemented('Not implemented on web.');
        }
        async addDevice(_options) {
            throw this.unimplemented('Not implemented on web.');
        }
        async addDevices(_options) {
            throw this.unimplemented('Not implemented on web.');
        }
        async removeDevice(_options) {
            throw this.unimplemented('Not implemented on web.');
        }
        async clearDevices() {
            throw this.unimplemented('Not implemented on web.');
        }
        async startForegroundService() {
            throw this.unimplemented('Not implemented on web.');
        }
        async stopForegroundService() {
            throw this.unimplemented('Not implemented on web.');
        }
        async isRunning() {
            throw this.unimplemented('Not implemented on web.');
        }
        async setScanMode(_options) {
            throw this.unimplemented('Not implemented on web.');
        }
    }

    var web = /*#__PURE__*/Object.freeze({
        __proto__: null,
        BackgroundBLEWeb: BackgroundBLEWeb
    });

    exports.BackgroundBLE = BackgroundBLE;

    return exports;

})({}, capacitorExports);
//# sourceMappingURL=plugin.js.map
