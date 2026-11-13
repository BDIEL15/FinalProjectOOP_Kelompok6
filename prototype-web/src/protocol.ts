export const PROTOCOL = 1;
export const RULES = 1;
export const RUNTIME = '0.29.3';
export type Cell = 'young' | 'ripe' | 'empty';
export type Snapshot = { tick: number; x: number; y: number; harvested: number; cells: Cell[] };
export type Action = {
  sequence: number; action: 'move' | 'harvest' | 'wait'; argument: string | null;
  ok: boolean; before: number; after: number; snapshot: Snapshot;
};
export type Outcome = 'success' | 'incomplete' | 'budget_exceeded' | 'error' | 'timeout' | 'stopped';
export type Result = {
  outcome: Outcome; snapshot: Snapshot;
  stats: { move: number; harvest: number; wait: number };
  error: { message: string; line: number | null } | null;
};
export type InMessage =
  | { protocolVersion: 1; type: 'init'; rulesVersion: 1 }
  | { protocolVersion: 1; type: 'run'; rulesVersion: 1; runId: number; source: string };
export type OutMessage = { protocolVersion: 1 } & (
  | { type: 'ready'; runtimeVersion: string; rulesVersion: 1; snapshot: Snapshot }
  | { type: 'init_error'; message: string }
  | { type: 'started'; runId: number }
  | { type: 'events'; runId: number; batch: number; events: Action[] }
  | { type: 'stdout'; runId: number; text: string }
  | ({ type: 'finished'; runId: number } & Result)
);

const record = (v: unknown): v is Record<string, unknown> => v !== null && typeof v === 'object';
const integer = (v: unknown, max: number) => Number.isInteger(v) && Number(v) >= 0 && Number(v) <= max;
export function isSnapshot(v: unknown): v is Snapshot {
  return record(v) && integer(v.tick, 200) && integer(v.x, 5) && integer(v.y, 5)
    && integer(v.harvested, 36) && Array.isArray(v.cells) && v.cells.length === 36
    && v.cells.every(c => ['young', 'ripe', 'empty'].includes(c))
    && v.cells.filter(c => c === 'empty').length === v.harvested;
}
export function isMessage(v: unknown): v is OutMessage {
  if (!record(v) || v.protocolVersion !== PROTOCOL) return false;
  if (v.type === 'ready') return v.rulesVersion === RULES && typeof v.runtimeVersion === 'string' && isSnapshot(v.snapshot);
  if (v.type === 'init_error') return typeof v.message === 'string' && v.message.length <= 2000;
  if (!integer(v.runId, Number.MAX_SAFE_INTEGER)) return false;
  if (v.type === 'started') return true;
  if (v.type === 'stdout') return typeof v.text === 'string' && v.text.length <= 17000;
  if (v.type === 'events') return integer(v.batch, 200) && Array.isArray(v.events)
    && v.events.length > 0 && v.events.length <= 16 && v.events.every(e => record(e)
      && integer(e.sequence, 200) && ['move', 'harvest', 'wait'].includes(String(e.action))
      && (e.argument === null || ['north', 'east', 'south', 'west'].includes(String(e.argument)))
      && typeof e.ok === 'boolean' && integer(e.before, 199) && integer(e.after, 200)
      && Number(e.after) > Number(e.before) && isSnapshot(e.snapshot) && e.snapshot.tick === e.after);
  if (v.type === 'finished') return ['success', 'incomplete', 'budget_exceeded', 'error'].includes(String(v.outcome))
    && isSnapshot(v.snapshot) && record(v.stats)
    && ['move', 'harvest', 'wait'].every(k => integer((v.stats as Record<string, unknown>)[k], 200))
    && (v.error === null || (record(v.error) && typeof v.error.message === 'string'
      && v.error.message.length <= 1000 && (v.error.line === null || integer(v.error.line, 32768))))
    && (v.outcome !== 'success' || v.snapshot.harvested === 36);
  return false;
}
