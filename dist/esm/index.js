import { registerPlugin } from '@capacitor/core';
const BackgroundBLE = registerPlugin('BackgroundBLE', {
    web: () => import('./web').then((m) => new m.BackgroundBLEWeb()),
});
export * from './definitions';
export { BackgroundBLE };
//# sourceMappingURL=index.js.map