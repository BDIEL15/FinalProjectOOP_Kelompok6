# Farm Code Lab

Prototype web untuk menulis Python, mengendalikan drone, dan memanen kebun 6×6. Seluruh 36 tanaman harus dipanen dalam paling banyak 200 tick; skor lebih kecil lebih baik. Starter menyelesaikan kebun dalam **112 tick**.

## Menjalankan lokal

Prasyarat: Node.js 22.12+ (atau 20.19+), npm, dan koneksi internet untuk unduhan Pyodide pertama. Python lokal hanya diperlukan untuk test pengembangan, bukan untuk pemain.

```sh
npm ci
npm run dev
```

Buka URL yang ditampilkan terminal, biasanya http://127.0.0.1:5173. Tunggu Python siap lalu klik **Run**. Ctrl/Cmd+Enter juga menjalankan kode. Tab pada editor memindahkan fokus seperti textarea biasa; gunakan empat spasi untuk indentasi.

Build produksi:

```sh
npm run build
npm run preview
```

Preview biasanya tersedia di http://127.0.0.1:4173. Sajikan folder `dist/` melalui hosting statis HTTP/HTTPS. Membuka `index.html` melalui `file://` tidak didukung. Tidak ada deployment publik yang dibuat.

## Penggunaan

- **Run** selalu memulai fixture baru; tidak melanjutkan percobaan sebelumnya.
- **Stop** menghentikan Worker, membuang replay tertunda, dan mempertahankan state terakhir yang ditampilkan. Klik **Muat ulang Python** untuk menyiapkan runtime lagi; kode tetap ada.
- **Reset** memulihkan kebun dan output tanpa menghapus kode atau rekor. Jika Worker dibatalkan, runtime dimuat ulang.
- **1× / 2× / 4×** hanya mengubah kecepatan animasi, bukan skor atau kematangan.
- Source dan rekor disimpan di browser jika storage tersedia. Kode contoh hanya mengganti kode setelah konfirmasi.
- Koordinat (0, 0) berada di kiri bawah. Buka panduan perintah dan tabel data petak untuk detail yang juga dapat diakses tanpa membaca Canvas.

## Pemeriksaan

```sh
npm run typecheck
npm run test:unit
npm run test:python
npm run build
npm run test:browser
```

Test browser menggunakan Chrome dan Edge yang sudah terpasang; tidak memasang browser global. Konfigurasi Playwright menyajikan build `dist/` pada port 4173. Tutup preview lama bila ingin memastikan server test memulai sendiri.

Bukti verifikasi 2 November 2026 tercatat di [VERIFICATION.md](VERIFICATION.md). Aturan: [RULES.md](RULES.md). Rancangan dan lifecycle: [ARCHITECTURE.md](ARCHITECTURE.md). Status pekerjaan: [TASKS.md](TASKS.md).

## Runtime dan batas versi ini

TypeScript + Vite, Canvas 2D, dan **Pyodide 0.29.3** dalam module Web Worker. Loader, WASM, lock runtime, serta standard library semuanya memakai satu distribusi CDN `https://cdn.jsdelivr.net/pyodide/v0.29.3/full/`. Dependency pengembangan dipin dalam `package.json` dan `package-lock.json`.

Python adalah satu-satunya sumber aturan. Renderer menerima snapshot Python; tidak menghitung kematangan atau skor sendiri. Setiap Run mendapat namespace dan simulasi baru. Protokol menolak generation, runId, urutan, versi, serta domain snapshot yang tidak valid.

Batas: source 32 KiB UTF-8, output maksimal 200 baris/16 KiB termasuk pemberitahuan pemotongan, 200 event aksi, startup 60 detik, komputasi 10 detik. Watchdog komputasi dimulai saat `started`; output tidak memperpanjangnya. Rekor hanya ditulis setelah replay sukses selesai.

Kode/output tidak dikirim ke server aplikasi. Pyodide/Worker **bukan sandbox aman untuk script bermusuhan**; jangan menyimpan secret di origin prototype atau menjalankan script pihak lain secara otomatis. Batas waktu browser bersifat best effort, dan terminasi Worker bukan batas memori keras.

Belum mendukung offline, backend, akun, atau leaderboard. Firefox belum diuji. Mobile dapat membuka tampilan bertumpuk, tetapi editing Python ditujukan untuk keyboard desktop. Deployment publik dan playtest teman belum dilakukan.

## Proyek akhir dan kolaborasi AI

Arah game Java/libGDX `mars_reboot.py` dan backend Spring Boot dijelaskan di [PROJECT.md](../../PROJECT.md); petunjuk menjalankan di atas tetap untuk prototype web. Lihat [AI Development Strategy & Workflow](../../PROJECT.md#12-ai-development-strategy--workflow) untuk pembagian Codex, Antigravity/Gemini, dan Claude, kontrak integrasi, serta review. Mulai pergantian alat dengan membaca [AGENTS.md](../../AGENTS.md) dan [handoff terbaru](TASKS.md#handoff-lintas-alat) agar konteks keputusan dan progres terbawa.
