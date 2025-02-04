import { WebPlugin } from '@capacitor/core';
export class BackgroundBLEWeb extends WebPlugin {
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
//# sourceMappingURL=web.js.map