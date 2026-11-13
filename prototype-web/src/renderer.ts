import type { Snapshot, Action } from './protocol';

export function createRenderer(canvas: HTMLCanvasElement) {
  const context = canvas.getContext('2d')!;
  let snapshot: Snapshot | null = null;
  let previous: Snapshot | null = null;
  let began = 0;
  let frame = 0;
  const reduced = matchMedia('(prefers-reduced-motion: reduce)');
  function draw(now = performance.now()) {
    cancelAnimationFrame(frame);
    const size = canvas.clientWidth;
    if (!size) return;
    const dpr = devicePixelRatio || 1;
    if (canvas.width !== Math.round(size * dpr)) {
      canvas.width = Math.round(size * dpr); canvas.height = canvas.width;
    }
    context.setTransform(dpr, 0, 0, dpr, 0, 0);
    context.clearRect(0, 0, size, size);
    const margin = size * .085, cell = (size - margin * 2) / 6;
    context.font = `500 ${Math.max(10, size * .027)}px Consolas, monospace`;
    context.textAlign = 'center'; context.textBaseline = 'middle';
    for (let i = 0; i < 6; i++) {
      context.fillStyle = '#536258';
      context.fillText(String(i), margin + cell * (i + .5), size - margin * .45);
      context.fillText(String(i), margin * .42, margin + cell * (5 - i + .5));
    }
    for (let y = 0; y < 6; y++) for (let x = 0; x < 6; x++) {
      const cx = margin + (x + .5) * cell, cy = margin + (5 - y + .5) * cell;
      const state = snapshot?.cells[y * 6 + x];
      context.fillStyle = state === 'empty' ? '#dcc9aa' : '#eee5d2';
      context.fillRect(cx - cell / 2 + 1.5, cy - cell / 2 + 1.5, cell - 3, cell - 3);
      context.strokeStyle = '#c8b38f'; context.lineWidth = .7;
      context.strokeRect(cx - cell / 2 + 1.5, cy - cell / 2 + 1.5, cell - 3, cell - 3);
      if (state === 'young' || state === 'ripe') {
        const scale = state === 'ripe' ? 1 : .7;
        context.save(); context.translate(cx, cy + cell * .06); context.scale(scale, scale);
        context.strokeStyle = '#496c46'; context.lineWidth = cell * .045; context.lineCap = 'round';
        context.beginPath(); context.moveTo(0, cell * .19); context.lineTo(0, -cell * .17); context.stroke();
        context.fillStyle = '#496c46';
        for (const side of [-1, 1]) {
          context.beginPath(); context.ellipse(side * cell * .095, -cell * .015, cell * .12, cell * .055, side * -.65, 0, Math.PI * 2); context.fill();
        }
        if (state === 'ripe') {
          context.fillStyle = '#ae7010';
          for (let j = 0; j < 3; j++) {
            context.beginPath(); context.ellipse((j - 1) * cell * .095, -cell * .17 + Math.abs(j - 1) * cell * .045, cell * .065, cell * .09, (j - 1) * .45, 0, Math.PI * 2); context.fill();
          }
          context.fillStyle = '#fff8dc'; context.fillRect(-cell * .025, -cell * .23, cell * .025, cell * .07);
        }
        context.restore();
      } else if (state === 'empty') {
        context.strokeStyle = '#aa906a'; context.lineWidth = 1;
        context.beginPath(); context.moveTo(cx - cell * .12, cy); context.lineTo(cx + cell * .12, cy); context.stroke();
      }
    }
    if (!snapshot) return;
    let t = reduced.matches ? 1 : Math.min(1, (now - began) / 100);
    t = 1 - (1 - t) ** 3;
    const dx = previous ? previous.x + (snapshot.x - previous.x) * t : snapshot.x;
    const dy = previous ? previous.y + (snapshot.y - previous.y) * t : snapshot.y;
    const cx = margin + (dx + .5) * cell, cy = margin + (5 - dy + .5) * cell;
    context.save(); context.translate(cx, cy);
    context.strokeStyle = '#204d34'; context.fillStyle = '#fff'; context.lineWidth = 2;
    context.beginPath(); context.moveTo(-cell * .21, -cell * .21); context.lineTo(cell * .21, cell * .21);
    context.moveTo(-cell * .21, cell * .21); context.lineTo(cell * .21, -cell * .21); context.stroke();
    for (const px of [-1, 1]) for (const py of [-1, 1]) {
      context.beginPath(); context.arc(px * cell * .22, py * cell * .22, cell * .095, 0, Math.PI * 2); context.fill(); context.stroke();
    }
    context.beginPath(); context.roundRect(-cell * .14, -cell * .14, cell * .28, cell * .28, 4); context.fill(); context.stroke();
    context.fillStyle = '#245c3c'; context.beginPath(); context.arc(0, 0, cell * .045, 0, Math.PI * 2); context.fill();
    context.restore();
    if (t < 1) frame = requestAnimationFrame(draw);
  }
  new ResizeObserver(() => draw()).observe(canvas);
  reduced.addEventListener('change', () => draw());
  return { show(next: Snapshot, action?: Action) {
    previous = action?.action === 'move' && action.ok ? snapshot : null;
    snapshot = next; began = performance.now(); draw();
  } };
}
