import { isMessage, PROTOCOL, RULES, type Snapshot, type Result, type Action, type InMessage } from './protocol';

export type Phase = 'loading' | 'init_error' | 'ready' | 'running' | 'replaying' | Result['outcome'];
export type WorkerPort = { postMessage(message: InMessage): void; terminate(): void;
  onmessage: ((event: MessageEvent) => void) | null; onerror: ((event: ErrorEvent) => void) | null };
type Hooks = { phase(phase: Phase, detail?: string): void; snapshot(snapshot: Snapshot, event?: Action): void;
  output(text: string): void; clear(): void; complete(result: Result): void };

export class Controller {
  phase: Phase = 'loading';
  snapshot: Snapshot | null = null;
  speed = 1;
  private worker: WorkerPort | null = null;
  private generation = 0;
  private runId = 0;
  private ready = false;
  private queue: Action[] = [];
  private pending: Result | null = null;
  private batch = 0;
  private count = 0;
  private lastTick = 0;
  private started = false;
  private outputBytes = 0;
  private outputLines = 0;
  private timer?: ReturnType<typeof setTimeout>;
  private replayTimer?: ReturnType<typeof setTimeout>;
  private initial: Snapshot | null = null;

  constructor(private makeWorker: () => WorkerPort, private hooks: Hooks) {}
  get active() { return this.phase === 'running' || this.phase === 'replaying'; }
  get canRun() { return this.ready && !this.active; }
  private setPhase(phase: Phase, detail?: string) { this.phase = phase; this.hooks.phase(phase, detail); }
  private dispose() {
    this.generation++;
    this.worker?.terminate(); this.worker = null; this.ready = false;
    clearTimeout(this.timer); clearTimeout(this.replayTimer);
    this.replayTimer = undefined;
    this.queue = []; this.pending = null;
  }
  init() {
    this.dispose(); this.setPhase('loading');
    const generation = this.generation;
    try {
      const worker = this.worker = this.makeWorker();
      this.timer = setTimeout(() => this.fail('init_error', 'Pemuatan Python melewati 60 detik.'), 60000);
      worker.onerror = () => { if (generation === this.generation) this.fail(this.active ? 'error' : 'init_error', 'Worker berhenti. Muat ulang Python untuk mencoba lagi.'); };
      worker.onmessage = ({ data }) => {
        if (generation !== this.generation) return;
        if (data && typeof data === 'object' && 'runId' in data && data.runId !== this.runId) return;
        if (!isMessage(data)) { this.fail(this.active ? 'error' : 'init_error', 'Pesan runtime tidak valid. Muat ulang Python.'); return; }
        if ('runId' in data && data.runId !== this.runId) return;
        if (data.type === 'ready') {
          if (this.ready || this.phase !== 'loading') return;
          clearTimeout(this.timer); this.ready = true; this.initial = data.snapshot;
          if (!this.snapshot) this.show(data.snapshot);
          this.setPhase('ready'); return;
        }
        if (data.type === 'init_error') { this.fail('init_error', data.message); return; }
        if (!this.active || this.pending) return;
        if (data.type === 'started') {
          if (this.started) { this.fail('error', 'Pesan started berulang.'); return; }
          this.started = true;
          clearTimeout(this.timer);
          this.timer = setTimeout(() => this.fail('timeout'), 10000);
        } else if (!this.started) {
          this.fail('error', 'Urutan pesan runtime tidak valid.');
        } else if (data.type === 'events') {
          if (data.batch !== this.batch++ || this.count + data.events.length > 200) { this.fail('error', 'Batas atau urutan event tidak valid.'); return; }
          for (const event of data.events) {
            if (event.sequence !== ++this.count || event.before !== this.lastTick) { this.fail('error', 'Urutan aksi tidak valid.'); return; }
            this.lastTick = event.after;
          }
          this.queue.push(...data.events); this.replay();
        } else if (data.type === 'stdout') {
          this.outputBytes += new TextEncoder().encode(data.text).length;
          this.outputLines += (data.text.match(/\n/g) ?? []).length;
          if (this.outputBytes > 17000 || this.outputLines > 203) { this.fail('error', 'Output runtime melewati batas.'); return; }
          this.hooks.output(data.text);
        } else if (data.type === 'finished') {
          clearTimeout(this.timer);
          if (data.snapshot.tick !== this.lastTick) { this.fail('error', 'Snapshot akhir tidak sesuai event.'); return; }
          this.pending = data;
          this.setPhase('replaying'); this.finishReplay();
        }
      };
      worker.postMessage({ protocolVersion: PROTOCOL, type: 'init', rulesVersion: RULES });
    } catch (error) { this.fail('init_error', String(error)); }
  }
  run(source: string): boolean {
    if (!this.canRun || !this.initial) return false;
    if (new TextEncoder().encode(source).length > 32768) { this.setPhase('error', 'Kode melebihi 32 KiB. Persingkat kode lalu jalankan lagi.'); return false; }
    this.runId++; this.batch = this.count = this.lastTick = this.outputBytes = this.outputLines = 0;
    this.started = false; this.pending = null; this.queue = [];
    this.hooks.clear(); this.show(this.initial); this.setPhase('running');
    this.timer = setTimeout(() => this.fail('error', 'Runtime tidak merespons Run. Muat ulang Python.'), 10000);
    this.worker!.postMessage({ protocolVersion: PROTOCOL, type: 'run', runId: this.runId, rulesVersion: RULES, source });
    return true;
  }
  private show(snapshot: Snapshot, event?: Action) { this.snapshot = snapshot; this.hooks.snapshot(snapshot, event); }
  private replay() {
    if (this.replayTimer || !this.queue.length) return;
    const event = this.queue.shift()!;
    this.replayTimer = setTimeout(() => {
      this.replayTimer = undefined;
      this.show(event.snapshot, event); this.replay(); this.finishReplay();
    }, (event.after - event.before) * 160 / this.speed);
  }
  private finishReplay() {
    if (!this.pending || this.queue.length || this.replayTimer) return;
    const result = this.pending; this.pending = null;
    this.show(result.snapshot); this.setPhase(result.outcome); this.hooks.complete(result);
  }
  private fail(phase: 'init_error' | 'timeout' | 'error', detail?: string) {
    this.dispose(); this.setPhase(phase, detail);
  }
  stop() { if (this.active) { this.dispose(); this.setPhase('stopped'); } }
  reset() {
    if (this.initial) this.show(this.initial);
    this.hooks.clear();
    if (this.active || !this.ready) this.init(); else this.setPhase('ready');
  }
  destroy() { this.dispose(); }
}
