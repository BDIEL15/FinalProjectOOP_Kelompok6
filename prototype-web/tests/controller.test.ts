import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { Controller, type WorkerPort } from '../src/controller';
import { isMessage, type Snapshot, type OutMessage } from '../src/protocol';

const initial = (): Snapshot => ({ tick: 0, x: 0, y: 0, harvested: 0, cells: Array(36).fill('ripe') });
class FakeWorker implements WorkerPort {
  onmessage: WorkerPort['onmessage'] = null;
  onerror: WorkerPort['onerror'] = null;
  postMessage = vi.fn(); terminate = vi.fn();
  send(message: object) { this.onmessage?.({ data: { protocolVersion: 1, ...message } } as MessageEvent); }
  ready() { this.send({ type: 'ready', rulesVersion: 1, runtimeVersion: 'test', snapshot: initial() }); }
  started(runId = 1) { this.send({ type: 'started', runId }); }
}
function setup() {
  const workers: FakeWorker[] = [];
  const hooks = { phase: vi.fn(), snapshot: vi.fn(), output: vi.fn(), clear: vi.fn(), complete: vi.fn() };
  const controller = new Controller(() => { const w = new FakeWorker(); workers.push(w); return w; }, hooks);
  controller.init(); workers[0].ready();
  return { controller, workers, hooks, worker: workers[0] };
}
function event(sequence: number, before: number) {
  return { sequence, action: 'wait', argument: null, ok: true, before, after: before + 1, snapshot: { ...initial(), tick: before + 1 } };
}
function finished(snapshot = initial(), outcome = 'incomplete') {
  return { type: 'finished', runId: 1, outcome, snapshot, stats: { move: 0, harvest: 0, wait: 0 }, error: null };
}
beforeEach(() => vi.useFakeTimers());
afterEach(() => vi.useRealTimers());

describe('controller lifecycle', () => {
  it('disables duplicate Run, starts with fixture and checks UTF-8 source size', () => {
    const { controller } = setup();
    expect(controller.run('🌱'.repeat(9000))).toBe(false);
    expect(controller.canRun).toBe(true);
    expect(controller.run('wait()')).toBe(true);
    expect(controller.run('wait()')).toBe(false);
    expect(controller.snapshot).toEqual(initial());
  });
  it('replays in order and does not complete or save a success early', () => {
    const { controller, worker, hooks } = setup();
    controller.run('wait()'); worker.started();
    worker.send({ type: 'events', runId: 1, batch: 0, events: [event(1, 0), event(2, 1)] });
    worker.send(finished({ ...initial(), tick: 2 }));
    expect(controller.phase).toBe('replaying'); expect(hooks.complete).not.toHaveBeenCalled();
    vi.advanceTimersByTime(160); expect(controller.snapshot?.tick).toBe(1);
    vi.advanceTimersByTime(160); expect(controller.phase).toBe('incomplete');
    expect(hooks.complete).toHaveBeenCalledOnce();
  });
  it.each([1, 4])('gives identical state at speed %s', speed => {
    const { controller, worker } = setup(); controller.speed = speed;
    controller.run('wait()'); worker.started();
    worker.send({ type: 'events', runId: 1, batch: 0, events: [event(1, 0)] });
    worker.send(finished({ ...initial(), tick: 1 }));
    vi.advanceTimersByTime(160 / speed);
    expect(controller.snapshot?.tick).toBe(1); expect(controller.phase).toBe('incomplete');
  });
  it('Stop terminates immediately, freezes displayed state, discards pending success', () => {
    const { controller, worker, hooks } = setup();
    controller.run('wait()'); worker.started();
    const end = { ...initial(), tick: 1, harvested: 36, cells: Array(36).fill('empty') } as Snapshot;
    worker.send({ type: 'events', runId: 1, batch: 0, events: [{ ...event(1, 0), snapshot: end }] });
    worker.send(finished(end, 'success')); controller.stop();
    vi.advanceTimersByTime(50000);
    expect(worker.terminate).toHaveBeenCalledOnce(); expect(controller.phase).toBe('stopped');
    expect(controller.snapshot?.tick).toBe(0); expect(hooks.complete).not.toHaveBeenCalled();
  });
  it('ignores old generation ready/errors/events after Reset and supports new replay', () => {
    const { controller, workers, worker } = setup();
    controller.run('wait()'); worker.started();
    worker.send({ type: 'events', runId: 1, batch: 0, events: [event(1, 0)] });
    controller.reset();
    worker.ready(); worker.send(finished()); worker.onerror?.({} as ErrorEvent);
    expect(controller.phase).toBe('loading');
    workers[1].ready(); controller.run('wait()'); workers[1].started(2);
    workers[1].send({ type: 'events', runId: 2, batch: 0, events: [event(1, 0)] });
    workers[1].send({ ...finished({ ...initial(), tick: 1 }), runId: 2 });
    vi.advanceTimersByTime(160);
    expect(controller.phase).toBe('incomplete'); expect(controller.snapshot?.tick).toBe(1);
  });
  it('ignores old runId on reused worker', () => {
    const { controller, worker } = setup();
    controller.run(''); worker.started(); worker.send(finished());
    controller.run(''); worker.started(2); worker.send(finished());
    expect(controller.phase).toBe('running');
  });
  it('times out at 10 seconds even if stdout keeps arriving', () => {
    const { controller, worker } = setup(); controller.run('while True: pass'); worker.started();
    vi.advanceTimersByTime(9999); worker.send({ type: 'stdout', runId: 1, text: 'still running' });
    vi.advanceTimersByTime(1); expect(controller.phase).toBe('timeout'); expect(worker.terminate).toHaveBeenCalledOnce();
    controller.init(); expect(controller.phase).toBe('loading');
  });
  it('fails startup at 60 seconds and recovers from init_error', () => {
    const { controller, workers } = setup(); controller.init();
    vi.advanceTimersByTime(60000); expect(controller.phase).toBe('init_error');
    controller.init(); workers.at(-1)!.ready(); expect(controller.phase).toBe('ready');
  });
  it('handles worker crash, invalid messages and output flood', () => {
    for (const mode of ['crash', 'protocol', 'flood']) {
      const { controller, worker } = setup(); controller.run(''); worker.started();
      if (mode === 'crash') worker.onerror?.({} as ErrorEvent);
      if (mode === 'protocol') worker.send({ type: 'events', runId: 1, batch: 1, events: [event(1, 0)] });
      if (mode === 'flood') for (let i = 0; i < 20; i++) worker.send({ type: 'stdout', runId: 1, text: 'x'.repeat(1024) });
      expect(controller.phase).toBe('error'); expect(worker.terminate).toHaveBeenCalledOnce();
    }
  });
  it('enforces 200 total actions, not only current queue size', () => {
    const { controller, worker } = setup(); controller.run(''); worker.started();
    controller.speed = 4;
    for (let i = 0; i < 200; i++) {
      worker.send({ type: 'events', runId: 1, batch: i, events: [event(i + 1, i)] });
      vi.advanceTimersByTime(40);
    }
    // This sequence is outside the valid numeric domain as well as the queue limit.
    worker.send({ type: 'events', runId: 1, batch: 200, events: [event(201, 199)] });
    expect(controller.phase).toBe('error');
  });
});

it('rejects malformed snapshots, versions, outcomes and oversized batches', () => {
  const valid = { protocolVersion: 1, ...finished() } as OutMessage;
  expect(isMessage(valid)).toBe(true);
  expect(isMessage({ ...valid, protocolVersion: 2 })).toBe(false);
  expect(isMessage({ ...valid, snapshot: { ...initial(), x: -1 } })).toBe(false);
  expect(isMessage({ ...valid, outcome: 'success' })).toBe(false);
  expect(isMessage({ protocolVersion: 1, type: 'events', runId: 1, batch: 0, events: Array(17).fill(event(1, 0)) })).toBe(false);
});
