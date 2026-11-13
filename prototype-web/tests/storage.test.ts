import { expect, it, vi } from 'vitest';
import { createStorage } from '../src/storage';

it('preserves source including empty text and validates versioned scores', () => {
  const data = new Map<string, string>();
  const storage = createStorage({ getItem: k => data.get(k) ?? null, setItem: (k, v) => { data.set(k, v); } }, vi.fn());
  expect(storage.source()).toBeNull(); storage.saveSource(''); expect(storage.source()).toBe('');
  storage.saveBest(112); expect(storage.best()).toBe(112);
  for (const invalid of ['bad', 'null', '{"rulesVersion":2,"tick":100}', '{"rulesVersion":1,"tick":201}', '{"rulesVersion":1,"tick":1}', '{"rulesVersion":1,"tick":100.5}']) {
    data.set('farm-code-lab:rules-v1:best', invalid); expect(storage.best()).toBeNull();
  }
});
it('continues when storage access or writes throw', () => {
  const onError = vi.fn(); const unavailable = () => { throw new Error('quota'); };
  const storage = createStorage({ getItem: unavailable, setItem: unavailable }, onError);
  expect(storage.source()).toBeNull(); expect(storage.best()).toBeNull();
  storage.saveSource('my code'); storage.saveBest(112); expect(onError).toHaveBeenCalledTimes(4);
});
