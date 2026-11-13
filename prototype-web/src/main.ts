import './styles.css';
import starter from './python/starter.py?raw';
import { Controller, type Phase } from './controller';
import { createRenderer } from './renderer';
import { createStorage } from './storage';
import type { Snapshot, Result } from './protocol';

const element = <T extends HTMLElement>(id: string) => document.getElementById(id) as T;
const source = element<HTMLTextAreaElement>('source');
const run = element<HTMLButtonElement>('run');
const stop = element<HTMLButtonElement>('stop');
const retry = element<HTMLButtonElement>('retry');
const output = element<HTMLPreElement>('output');
const renderer = createRenderer(element<HTMLCanvasElement>('farm'));
const storage = createStorage({ getItem: key => localStorage.getItem(key), setItem: (key, value) => localStorage.setItem(key, value) }, () => { element('storage-notice').hidden = false; });
let best = storage.best();
let hasOutput = false;
let saveTimer: ReturnType<typeof setTimeout>;
source.value = storage.source() ?? starter;
function bestLabel() { element('best').textContent = best === null ? 'Belum ada' : `${best} tick`; }
bestLabel();
const labels: Record<Phase, string> = { loading: 'Memuat Python', init_error: 'Python belum siap', ready: 'Siap dijalankan', running: 'Menjalankan', replaying: 'Memutar gerakan', success: 'Panen selesai', incomplete: 'Belum tuntas', budget_exceeded: 'Batas tick', error: 'Ada kesalahan', timeout: 'Batas waktu', stopped: 'Dihentikan' };
function message(phase: Phase, detail?: string) {
  if (phase === 'init_error') return 'Python belum berhasil dimuat. Periksa koneksi lalu coba lagi.';
  if (detail) return detail;
  switch (phase) {
    case 'loading': return 'Menyiapkan Python… pemuatan pertama bisa lebih lama.';
    case 'ready': return 'Siap. Jalankan contoh atau ubah kodenya.';
    case 'running': return 'Menjalankan Python…';
    case 'replaying': return 'Menampilkan hasil gerakan…';
    case 'timeout': return 'Program melewati batas waktu. Periksa loop yang tidak selesai.';
    case 'stopped': return 'Percobaan dihentikan. Kode kamu tetap tersimpan.';
    case 'budget_exceeded': return 'Batas 200 tick tercapai atau aksi berikutnya akan melewatinya.';
    default: return labels[phase];
  }
}
function showWorld(snapshot: Snapshot) {
  element('harvested').textContent = String(snapshot.harvested);
  element('tick').textContent = String(snapshot.tick);
  element('position').textContent = `(${snapshot.x}, ${snapshot.y})`;
  element('world-summary').textContent = `Drone di (${snapshot.x}, ${snapshot.y}). Panen ${snapshot.harvested} dari 36 tanaman; ${snapshot.tick} dari 200 tick.`;
  const tbody = element<HTMLTableElement>('world-table').tBodies[0];
  tbody.replaceChildren();
  const names = { empty: 'Kosong', ripe: 'Matang', young: 'Tunas' };
  for (let y = 5; y >= 0; y--) {
    const row = tbody.insertRow();
    const heading = document.createElement('th'); heading.scope = 'row'; heading.textContent = String(y); row.append(heading);
    for (let x = 0; x < 6; x++) {
      const cell = row.insertCell(); cell.textContent = names[snapshot.cells[y * 6 + x]];
      if (snapshot.x === x && snapshot.y === y) { cell.dataset.drone = 'true'; cell.textContent += ' · Drone'; }
    }
  }
}
const controller = new Controller(() => new Worker(new URL('./python.worker.ts', import.meta.url), { type: 'module' }), {
  phase(phase, detail) {
    run.disabled = !controller.canRun || new TextEncoder().encode(source.value).length > 32768;
    stop.disabled = !controller.active;
    source.readOnly = controller.active;
    retry.hidden = !['init_error', 'stopped', 'timeout', 'error'].includes(phase) || controller.canRun;
    element('restore').toggleAttribute('disabled', controller.active);
    element('status').textContent = message(phase, detail);
    element('phase-label').textContent = labels[phase];
    element('result-title').parentElement!.parentElement!.dataset.phase = phase;
  },
  snapshot(snapshot, event) {
    renderer.show(snapshot, event); showWorld(snapshot);
    if (event) {
      const name = event.action === 'move' ? `move("${event.argument}")` : `${event.action}()`;
      element('action-feedback').textContent = `Aksi ${event.sequence}: ${name} · ${event.ok ? 'selesai' : 'gagal, tick tetap terpakai'} · tick ${event.before} → ${event.after}`;
    }
  },
  clear() { output.textContent = 'Output print() akan muncul di sini.'; hasOutput = false; element('action-feedback').textContent = 'Kebun dimulai dari kondisi awal yang sama.'; },
  output(text) { if (!hasOutput) { output.textContent = ''; hasOutput = true; } output.append(document.createTextNode(text)); output.scrollTop = output.scrollHeight; },
  complete(result: Result) {
    if (result.outcome === 'success') {
      const previousBest = best;
      if (best === null || result.snapshot.tick < best) { best = result.snapshot.tick; storage.saveBest(best); bestLabel(); }
      element('status').textContent = `Selesai! 36/36 tanaman dipanen dalam ${result.snapshot.tick} tick.`;
      element('action-feedback').textContent = `${result.stats.move} gerakan · ${result.stats.harvest} aksi panen · ${result.stats.wait} wait${previousBest !== null ? ` · Rekor sebelumnya ${previousBest} tick` : ' · Rekor pertama kamu'}`;
    } else if (result.outcome === 'incomplete') {
      element('status').textContent = `Program selesai. Masih ada ${36 - result.snapshot.harvested} tanaman.`;
    } else if (result.error) {
      element('status').textContent = `Ada kesalahan di kode${result.error.line ? `, player.py baris ${result.error.line}` : ''}: ${result.error.message}`;
    }
  },
});
function sourceChanged() {
  const bytes = new TextEncoder().encode(source.value).length;
  element('source-size').textContent = `${(bytes / 1024).toFixed(1)} / 32 KiB${bytes > 32768 ? ' · Terlalu panjang' : ''}`;
  run.disabled = !controller.canRun || bytes > 32768;
  const numbers = element('line-numbers');
  numbers.textContent = Array.from({ length: source.value.split('\n').length }, (_, i) => i + 1).join('\n');
  numbers.scrollTop = source.scrollTop;
}
sourceChanged();
source.addEventListener('input', () => { sourceChanged(); clearTimeout(saveTimer); saveTimer = setTimeout(() => storage.saveSource(source.value), 300); });
source.addEventListener('scroll', () => { element('line-numbers').scrollTop = source.scrollTop; });
window.addEventListener('pagehide', () => storage.saveSource(source.value));
function start() { storage.saveSource(source.value); controller.run(source.value); }
run.addEventListener('click', start);
stop.addEventListener('click', () => controller.stop());
retry.addEventListener('click', () => controller.init());
element('reset').addEventListener('click', () => controller.reset());
element('restore').addEventListener('click', () => {
  if (source.value !== starter && !confirm('Ganti kode kamu dengan contoh awal? Kode saat ini akan diganti.')) return;
  source.value = starter; storage.saveSource(starter); sourceChanged();
});
document.addEventListener('keydown', event => {
  if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') { event.preventDefault(); if (!run.disabled) start(); }
});
document.querySelectorAll<HTMLButtonElement>('[data-speed]').forEach(button => {
  button.addEventListener('click', () => {
    controller.speed = Number(button.dataset.speed);
    document.querySelectorAll('[data-speed]').forEach(item => item.setAttribute('aria-pressed', String(item === button)));
  });
});
controller.init();
