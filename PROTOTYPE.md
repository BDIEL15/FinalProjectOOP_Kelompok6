# PROTOTYPE — Farm Code Lab sebagai referensi Mars

Status: prototype web v0.1 sudah memiliki implementasi dan bukti historis lokal 2 November 2026. Dokumen ini memisahkan rujukan prototype dari rencana utama mars_reboot.py. Penataan awal 12 November 2026 hanya mengubah dokumentasi; konsolidasi 13 November menyalin referensi web ke subfolder; source, test, dependency, dan konfigurasi prototype disalin utuh ke prototype-web/ tanpa cache atau dependency terunduh.

## 1. Apa yang sudah ada

Farm Code Lab adalah satu layar web: pemain menulis Python untuk drone pada kebun 6×6 berisi 36 tanaman. Target panen selesai paling banyak 200 tick; starter historis menyelesaikan dalam 112 tick. Stack: TypeScript/Vite, Canvas 2D, Pyodide 0.29.3 pada module Web Worker. Aturan otoritatif berada di Python; renderer memakai snapshot. Prototype tidak memiliki backend/akun.

Source ada di prototype-web/src/, test di prototype-web/tests/, manifest/lockfile di prototype-web/. Script npm tetap milik prototype. Tidak ada kode Java yang dibuat oleh penataan dokumentasi. Nilai grid, API wait, biaya harvest, tick, dan timeout prototype tidak menjadi keputusan Mars.

## 2. Menjalankan kode yang sudah ada

Jalankan dari subfolder prototype-web/; prasyarat historis Node.js 22.12+ atau 20.19+, npm, dan jaringan untuk Pyodide CDN awal. Python lokal hanya untuk test pengembangan; pemain browser tidak memerlukannya.

```sh
cd prototype-web
npm ci
npm run dev
```

Ikuti URL terminal. Tunggu Python siap, Run starter, ubah kode dan coba ulang. Build produksi menggunakan npm run build lalu npm run preview; sajikan HTTP/HTTPS, bukan file URL. Perintah ini petunjuk, tidak dijalankan pada sesi rewrite dokumentasi.

| Perintah tersedia | Fungsi |
| --- | --- |
| npm run typecheck | TypeScript tanpa emit. |
| npm run test:unit | Test TS sekali jalan. |
| npm run test:python | unittest simulasi/runner dengan Python lokal. |
| npm run build | Build web produksi. |
| npm run test:browser | Test Playwright Chrome/Edge sesuai konfigurasi. |

## 3. Arsip spesifikasi dan bukti

Arsip mempertahankan isi sebelum root dialihkan ke Mars; tautan relatif disesuaikan agar tetap menunjuk target yang benar.

- [README lama](docs/prototype/README.md): penggunaan dan batas runtime.
- [PRD lama](docs/prototype/PRD.md): tujuan dan hipotesis prototype.
- [RULES lama](docs/prototype/RULES.md): fixture, API, tick, starter dan baseline.
- [ARCHITECTURE lama](docs/prototype/ARCHITECTURE.md): Worker/protokol/lifecycle.
- [DESIGN lama](docs/prototype/DESIGN.md): layout dan aksesibilitas web.
- [TASKS lama](docs/prototype/TASKS.md): checklist historis prototype dan handoff.
- [VERIFICATION lama](docs/prototype/VERIFICATION.md): hasil aktual historis dan batas pemeriksaannya.

Bukti historis menyebut typecheck/build, 14 test TS, 16 test Python, dan 6 skenario browser Chrome/Edge lulus. Tidak dijalankan ulang pada penataan ini. Firefox, deployment publik, dan playtest manusia belum dibuktikan; screenshot lokal bukan persetujuan visual pengguna. Jangan mencentang pekerjaan Mars berdasarkan hasil ini.

## 4. Pelajaran yang dipakai dan yang berubah

| Dipakai sebagai pelajaran | Harus dirancang ulang untuk Mars |
| --- | --- |
| Fixture tetap, replay tidak mengubah skor | Simulasi otoritatif Java, bukan Python prototype. |
| Stop dapat membatalkan loop tanpa API | Adapter/proses Python dan lifecycle desktop, bukan Worker. |
| RunId/generation menolak hasil lama | DTO/protokol Java dan identitas percobaan Mars. |
| Source tetap saat Stop/Reset/error | Save lokal per puzzle/profil, bukan localStorage web. |
| Output teks, batas antrean/source/output | Angka dan mekanisme dibuktikan pada runtime Mars. |
| Umpan balik aksi, keyboard, indikator nonwarna | Habitat/inspeksi/terminal libGDX dan validasi desktop. |
| Test aturan dan integrasi nyata terpisah | Autentikasi/skor Spring Boot serta 9 puzzle. |

Pyodide/Worker bukan sandbox aman untuk script bermusuhan; tidak ada secret pada origin prototype. Source/output tidak dikirim backend aplikasi, tetapi loader runtime awal berasal dari CDN. Untuk perubahan kode prototype, baca arsip domain terkait dan pertahankan invarian/test yang relevan. Acuan game akhir tetap [PROJECT](PROJECT.md) dan dokumen root Mars.
