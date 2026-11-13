import simulation from './python/simulation.py?raw';
import runner from './python/runner.py?raw';
import { PROTOCOL, RULES, RUNTIME, type InMessage, type Action } from './protocol';

// One pinned CDN supplies loader, WASM and standard library together.
const indexURL = `https://cdn.jsdelivr.net/pyodide/v${RUNTIME}/full/`;
type Python = { FS: { writeFile(path: string, text: string): void }; runPython(code: string): unknown;
  globals: { set(name: string, value: unknown): void } };
let python: Python | undefined;
const send = (message: object) => postMessage({ protocolVersion: PROTOCOL, ...message });

onmessage = async ({ data }: MessageEvent<InMessage>) => {
  if (data.protocolVersion !== PROTOCOL || data.rulesVersion !== RULES) return;
  if (data.type === 'init') {
    try {
      const { loadPyodide } = await import(/* @vite-ignore */ `${indexURL}pyodide.mjs`);
      python = await loadPyodide({ indexURL });
      python!.FS.writeFile('/home/pyodide/simulation.py', simulation);
      python!.FS.writeFile('/home/pyodide/runner.py', runner);
      const initial = python!.runPython('import json\nfrom simulation import FarmSimulation\nfrom runner import run_source\njson.dumps(FarmSimulation().snapshot())');
      send({ type: 'ready', runtimeVersion: RUNTIME, rulesVersion: RULES, snapshot: JSON.parse(String(initial)) });
    } catch (error) {
      send({ type: 'init_error', message: String(error).slice(0, 2000) });
    }
    return;
  }
  if (!python) return;
  const runId = data.runId;
  let batch = 0;
  let events: Action[] = [];
  const flush = () => {
    if (events.length) send({ type: 'events', runId, batch: batch++, events });
    events = [];
  };
  python.globals.set('_source', data.source);
  python.globals.set('_event_json', (value: string) => {
    events.push(JSON.parse(value));
    if (events.length === 16) flush();
  });
  python.globals.set('_output', (text: string) => send({ type: 'stdout', runId, text }));
  send({ type: 'started', runId });
  try {
    const result = python.runPython('json.dumps(run_source(_source, lambda e: _event_json(json.dumps(e)), _output))');
    flush();
    send({ type: 'finished', runId, ...JSON.parse(String(result)) });
  } catch (error) {
    throw new Error(`Worker Python gagal: ${String(error)}`);
  }
};
