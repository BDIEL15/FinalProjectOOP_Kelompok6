import { test, expect, type Page } from '@playwright/test';

const ready = async (page: Page) => { await expect(page.locator('#run')).toBeEnabled({ timeout: 65000 }); };
const runCode = async (page: Page, code: string) => {
  await page.locator('#source').fill(code);
  await page.locator('#run').click();
};

test('real Pyodide starter, speeds, accessible layout and persistence', async ({ page, browser }, info) => {
  const errors: string[] = [];
  page.on('pageerror', error => errors.push(error.message));
  const runtime: { url: string; mime: string | undefined; cors: string | undefined }[] = [];
  page.context().on('response', response => {
    if (response.url().includes('/v0.29.3/full/')) runtime.push({ url: response.url(), mime: response.headers()['content-type'], cors: response.headers()['access-control-allow-origin'] });
  });
  await page.goto('/'); await ready(page);
  console.log(`${info.project.name} ${browser.version()} runtime assets: ${JSON.stringify(runtime)}`);
  const starter = await page.locator('#source').inputValue();
  await page.screenshot({ path: `.impeccable/review/${info.project.name}-desktop.png`, fullPage: true });
  for (const speed of [1, 4]) {
    await page.locator(`[data-speed="${speed}"]`).click();
    await page.locator('#run').click();
    await expect(page.locator('#source')).toHaveAttribute('readonly', '');
    await expect(page.locator('#status')).toHaveText('Selesai! 36/36 tanaman dipanen dalam 112 tick.', { timeout: 25000 });
    await expect(page.locator('#position')).toHaveText('(0, 5)');
    await expect(page.locator('#tick')).toHaveText('112');
    await expect(page.locator('#action-feedback')).toContainText('35 gerakan · 36 aksi panen · 5 wait');
    await expect(page.locator('#best')).toHaveText('112 tick');
  }
  await page.screenshot({ path: `.impeccable/review/${info.project.name}-success.png`, fullPage: true });
  await page.locator('#reset').click();
  await expect(page.locator('#tick')).toHaveText('0');
  await expect(page.locator('#best')).toHaveText('112 tick');
  await expect(page.locator('#source')).toHaveValue(starter);
  await page.reload(); await ready(page);
  await expect(page.locator('#best')).toHaveText('112 tick');
  await expect(page.locator('#source')).toHaveValue(starter);
  for (const [width, height] of [[1280, 720], [390, 844]]) {
    await page.setViewportSize({ width, height });
    await page.locator('#source').fill('# ' + 'x'.repeat(2000) + '\nprint("<img src=x onerror=alert(1)>")');
    await page.locator('#run').click();
    await expect(page.locator('#status')).toContainText('Masih ada 36 tanaman');
    await expect(page.locator('#output')).toContainText('<img src=x onerror=alert(1)>');
    await expect(page.locator('#output img')).toHaveCount(0);
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true);
    await page.screenshot({ path: `.impeccable/review/${info.project.name}-${width}.png`, fullPage: true });
  }
  await page.locator('#source').focus();
  await page.keyboard.press('Tab');
  await expect(page.locator('#source')).not.toBeFocused();
  await page.locator('.world-details summary').click();
  await expect(page.locator('#world-table tbody tr')).toHaveCount(6);
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await page.locator('#source').fill(starter); await page.locator('#run').click();
  await expect(page.locator('#status')).toContainText('36/36 tanaman dipanen dalam 112 tick', { timeout: 25000 });
  expect(errors).toEqual([]);
});

test('Stop, Reset, errors, output bounds, timeout and recovery', async ({ page }) => {
  await page.goto('/'); await ready(page);
  await page.locator('[data-speed="4"]').click();
  const starter = await page.locator('#source').inputValue();
  await runCode(page, 'while True: pass');
  await page.locator('#stop').click();
  await expect(page.locator('#status')).toContainText('Percobaan dihentikan');
  await expect(page.locator('#source')).toHaveValue('while True: pass');
  await page.locator('#retry').click(); await ready(page);
  await runCode(page, 'print("pulih")');
  await expect(page.locator('#output')).toHaveText('pulih\n');
  await expect(page.locator('#status')).toContainText('Masih ada 36 tanaman');
  await runCode(page, starter);
  await expect(page.locator('#phase-label')).toHaveText('Memutar gerakan');
  await page.locator('#stop').click();
  await expect(page.locator('#best')).toHaveText('Belum ada');
  const frozen = await page.locator('#tick').textContent();
  await page.waitForTimeout(300);
  await expect(page.locator('#tick')).toHaveText(frozen!);
  await page.locator('#retry').click(); await ready(page);
  await runCode(page, 'while True: pass');
  await page.locator('#reset').click(); await ready(page);
  await expect(page.locator('#source')).toHaveValue('while True: pass');
  await expect(page.locator('#tick')).toHaveText('0');
  for (const [code, expected] of [
    ['if True\n    wait()', 'player.py baris 1'],
    ['wait()\n1/0', 'player.py baris 2'],
    ['move("North")', 'ValueError'],
  ]) {
    await runCode(page, code);
    await expect(page.locator('#status')).toContainText(expected);
    await expect(page.locator('#source')).toHaveValue(code);
  }
  await runCode(page, 'for _ in range(10000):\n    print("<b>tanaman</b>")');
  await expect(page.locator('#status')).toContainText('Masih ada 36 tanaman');
  await expect(page.locator('#output')).toContainText('Output dipotong');
  expect((await page.locator('#output').textContent())!.length).toBeLessThan(17000);
  await runCode(page, 'while True:\n    wait()');
  await expect(page.locator('#status')).toContainText('Batas 200 tick', { timeout: 20000 });
  await expect(page.locator('#tick')).toHaveText('200');
  await runCode(page, 'while True:\n    print("loop")');
  await expect(page.locator('#status')).toContainText('Program melewati batas waktu', { timeout: 15000 });
  await expect(page.locator('#source')).toHaveValue('while True:\n    print("loop")');
  await page.locator('#retry').click(); await ready(page);
  await runCode(page, 'harvest()');
  await expect(page.locator('#status')).toContainText('Masih ada 35 tanaman');
  await expect(page.locator('#tick')).toHaveText('2');
});

test('runtime network failure offers retry without losing code', async ({ page, context }) => {
  await context.route('**/pyodide.mjs', route => route.abort());
  await page.goto('/');
  await page.locator('#source').fill('print("tetap ada")');
  await expect(page.locator('#retry')).toBeVisible({ timeout: 65000 });
  await expect(page.locator('#run')).toBeDisabled();
  await context.unroute('**/pyodide.mjs');
  await page.locator('#retry').click(); await ready(page);
  await expect(page.locator('#source')).toHaveValue('print("tetap ada")');
  await page.locator('#run').click();
  await expect(page.locator('#output')).toHaveText('tetap ada\n');
});
