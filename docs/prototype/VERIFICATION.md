# Verifikasi prototype v0.1

Tanggal: 2 November 2026. Pengujian lokal pada build produksi yang disajikan melalui HTTP; ini bukan bukti deployment publik atau playtest.

## Lingkungan

| Komponen | Versi |
| --- | --- |
| Node.js | 24.21.0 |
| npm | 11.19.0 |
| Python lokal | 3.15.1 |
| TypeScript | 5.9.3 |
| Vite | 7.2.2 |
| Vitest | 3.2.4 |
| Playwright | 1.56.1 |
| Pyodide | 0.29.3 |
| Chrome | 156.0.8078.76 |
| Edge | 156.0.4314.58 |

## Pemeriksaan kode dan aturan

- `npm run typecheck`: lulus.
- `npm run test:unit`: 14 test lulus, mencakup lifecycle, watchdog, replay, kecepatan, Stop, Reset, generation/runId lama, worker crash, pesan tidak valid, batas event/output/source, dan storage rusak/tidak tersedia.
- `npm run test:python`: 16 test lulus, mencakup fixture, empat arah, tepi, argumen API, biaya panen gagal/kosong, urutan kematangan, sensor, wait, batas 200, aksi over-budget tanpa mutasi, runner, lokasi error, namespace baru, batas output Unicode, dan starter.
- `npm run build`: lulus; `dist/` memuat HTML/CSS/JS dan module Worker. Seluruh aset Python aplikasi dimasukkan ke bundle Worker.
- Baseline runner: **36 panen, 35 gerakan, 36 aksi panen, 5 wait, 112 tick, posisi (0, 5)**. Hasil Run berulang identik.

## Pengujian browser produksi

Suite [prototype.spec.ts](../../prototype-web/tests/browser/prototype.spec.ts) memuat tiga skenario pada masing-masing Chrome dan Edge, total enam test:

1. Pyodide nyata berjalan; starter pada 1× dan 4× sama-sama sukses dalam 112 tick pada (0, 5). Rekor/source tetap ada setelah Reset/reload. Reduced motion menghasilkan state/skor yang sama. Layout 1440×900, 1280×720, dan 390×844 tidak memiliki overflow horizontal global. Tabel petak tersedia; Tab dapat keluar dari editor. Source/output panjang dan teks menyerupai HTML tidak menjadi elemen HTML. Tidak ada `pageerror` pada alur starter.
2. Stop memutus loop tanpa aksi dan membatalkan replay sukses sebelum rekor disimpan. State ditahan setelah Stop. Reset saat aktif menyiapkan runtime baru dan mempertahankan kode. Syntax error/runtime error menampilkan baris `player.py`. Output flood dipotong; budget berhenti pada tick 200; loop yang terus mencetak tetap timeout. Runtime bisa dimuat ulang dan digunakan lagi.
3. Unduhan loader digagalkan lewat network interception: Run disabled, pemulihan tersedia, source tetap ada. Setelah jaringan dipulihkan, Python dapat digunakan lagi.

Distribusi yang benar-benar dimuat: loader `pyodide.mjs`, `pyodide.asm.js`, `pyodide.asm.wasm`, `python_stdlib.zip`, serta `pyodide-lock.json`, semuanya dari path versi 0.29.3 yang sama. Respons WASM memiliki MIME `application/wasm`; CDN mengirim `Access-Control-Allow-Origin: *`.

Screenshot aktual tersedia pada `.impeccable/review/`: `chrome-desktop.png`, `chrome-success.png`, `chrome-1280.png`, `chrome-390.png`, beserta pasangan Edge. File screenshot adalah bukti lokal dan diabaikan git. Laporan Playwright di `test-results/browser-results.json` dibuat ulang setiap suite dijalankan.

## Tinjauan visual dan aksesibilitas

Tinjauan dilakukan langsung dalam chat ini tanpa subagent, mengikuti otorisasi sesi. Screenshot desktop/mobile dibuka dan diperiksa: editor, kontrol, grid, legenda, output, dan bantuan terlihat pada susunan yang ditentukan DESIGN.md; teks panjang tetap berada dalam panelnya.

Kontras yang dihitung dari warna aktual: teks utama terhadap halaman **13.20:1**; teks sekunder terhadap halaman **5.81:1**, terhadap putih **6.45:1**; label Run putih terhadap hijau **7.86:1**. Amber tanaman dipergelap dari token awal agar kontras bagian tanaman terhadap petak melewati 3:1. Keadaan petak juga dibedakan oleh bentuk dan tabel DOM. Fokus biru native, kontrol HTML semantik, output text-only, serta `aria-live` hanya pada fase/hasil.

Pemeriksaan ini bukan audit aksesibilitas dengan pembaca layar manusia. Waktu komputasi/loading bukan benchmark performa perangkat.

Triage hook setelah implementasi: fallback Inter dihapus agar stack UI langsung memakai Segoe UI/system sans sesuai DESIGN.md. Temuan `flat-type-hierarchy` merupakan false positive pada `index.html`: computed style menunjukkan h1 27 px, body 15 px, pengantar 14 px; mobile h1 24 px. Pengecualian hanya untuk pasangan rule/nilai tersebut pada `index.html`, tersimpan di `.impeccable/config.json`. Hierarki visual dipertahankan.

## Batas bukti

- Firefox tidak tersedia dalam pengujian ini; belum diklaim kompatibel.
- Belum ada link deployment publik atau pemeriksaan hosting dari sesi bersih.
- Belum ada playtest teman, data kepuasan, atau keputusan pengembangan lanjutan.
- Asset runtime masih diunduh dari CDN; koneksi internet diperlukan.
