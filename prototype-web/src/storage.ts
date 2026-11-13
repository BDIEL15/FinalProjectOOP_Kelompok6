import { RULES } from './protocol';
export function createStorage(storage: Pick<Storage, 'getItem' | 'setItem'>, onError: () => void) {
  const read = (key: string) => { try { return storage.getItem(key); } catch { onError(); return null; } };
  const write = (key: string, value: string) => { try { storage.setItem(key, value); } catch { onError(); } };
  return {
    source: () => read('farm-code-lab:v1:source'),
    saveSource: (source: string) => write('farm-code-lab:v1:source', source),
    best: (): number | null => {
      try {
        const value = JSON.parse(read('farm-code-lab:rules-v1:best') ?? 'null');
        return value?.rulesVersion === RULES && Number.isInteger(value.tick) && value.tick >= 72 && value.tick <= 200 ? value.tick : null;
      } catch { return null; }
    },
    saveBest: (tick: number) => write('farm-code-lab:rules-v1:best', JSON.stringify({ rulesVersion: RULES, tick })),
  };
}
