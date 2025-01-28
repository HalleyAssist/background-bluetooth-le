import { registerPlugin } from '@capacitor/core';

import type { BackgroundBLEPlugin } from './definitions';

const BackgroundBLE = registerPlugin<BackgroundBLEPlugin>('BackgroundBLE', {
  web: () => import('./web').then((m) => new m.BackgroundBLEWeb()),
});

export * from './definitions';
export { BackgroundBLE };
