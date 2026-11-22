# AGENTS — Instruksi kerja proyek

## Tujuan dan status

Repository ini adalah sumber utama **mars_reboot.py**: source libGDX core/lwjgl3, backend Spring, assets, dokumentasi dan skill berada pada root yang sama. Per 22 November 2026, kandidat puzzle 1 panen terhubung ke terminal Python nyata, robot/replay/hasil, Run/Stop/Reset dan progres sesi; Home/login/tamu, setelan, akun/skor lokal dan scoreboard P1 juga ada. Java 21 langsung mengompilasi core/backend, tetapi Gradle Wrapper gagal start karena `native-platform.dll`, sehingga UI terbaru belum diverifikasi. Puzzle 2–9, save lintas restart, server bersama dan komputer kedua belum terverifikasi. Baca TEAM_GUIDE, TEAM_PROMPTS, [kontrak runtime](docs/mars/PYTHON_RUNTIME.md), serta handoff terbaru TASKS sebelum melanjutkan. Prototype web di prototype-web/ hanya referensi; jangan menjalankan generator ulang atau mencampur aturan kedua target.

Utamakan pekerjaan yang diminta pengguna pada giliran aktif. Keberadaan TASKS.md bukan izin untuk otomatis mengimplementasikan semua tugas, memasang software global, atau memublikasikan proyek.

## Baca sebelum mengubah

0. TEAM_GUIDE.md untuk onboarding; TEAM_PROMPTS.md untuk prompt peran. PROJECT.md bagian 2 dan 12 serta handoff terbaru di TASKS.md untuk target, workflow AI, dan konteks lintas alat. Untuk Mars, baca juga bagian 5–7 dan keputusan bagian 11.
1. PRD.md untuk tujuan, cakupan, FR/NFR, dan kriteria penerimaan Mars.
2. RULES.md untuk kandidat fixture, API, biaya, dan skor Mars.
3. ARCHITECTURE.md untuk rancangan runtime, protokol, backend, dan lifecycle Mars.
4. DESIGN.md untuk UX, antarmuka, dan aksesibilitas Mars.
5. TEST_PLAN.md untuk skenario pemeriksaan; TASKS.md untuk pekerjaan terbuka dan dependensinya; VERIFICATION.md untuk bukti aktual Mars.
6. Untuk tugas prototype, baca PROTOTYPE.md dan dokumen domain di docs/prototype/. Aturan/angka prototype tidak otomatis berlaku bagi Mars; jangan mengubah source web mengikuti aturan Mars tanpa permintaan migrasi kode.

Dokumen memiliki tanggung jawab per domain, bukan urutan yang membenarkan konflik. Jika perilaku berubah, perbarui dokumen pemilik keputusan beserta referensi/test terkait. Ikuti instruksi pengguna terbaru dan catat perubahan spesifikasi yang material. Jangan menyebut asumsi baru sebagai keputusan yang sudah disetujui.

## Batas implementasi

Batas berikut khusus prototype web. Pekerjaan Mars mengikuti PROJECT.md dan dokumen root, dengan status Dipilih/Wajib SOP dibedakan dari Rancangan kerja/Usulan. Permintaan dokumentasi tidak otomatis mengotorisasi bootstrap atau migrasi stack.

- Gunakan TypeScript, Vite, Canvas 2D, dan Pyodide di module Web Worker untuk v0.1.
- Python simulasi adalah sumber state permainan; jangan menduplikasi aturan otoritatif di renderer TS.
- Backend, akun, database server, multiplayer, engine 3D, interpreter khusus, dan paket UI besar di luar cakupan awal.
- Implementasikan perubahan terkecil yang memenuhi kebutuhan; jangan menambah plugin system, ECS, repository layer, atau abstraksi migrasi tanpa kebutuhan nyata.
- Dependency dipasang lokal proyek dan versinya dikunci. Periksa dokumentasi resmi sebelum memakai API runtime yang belum dikenal.
- Gunakan aset buatan proyek atau berlisensi sesuai; jangan mengambil aset/source game referensi.

## Invarian yang tidak boleh rusak

Daftar di bawah menjaga kode prototype yang masih ada. Untuk Mars, pertahankan determinisme, source saat pembatalan, dan penolakan hasil lama sesuai kontrak yang dipilih; aturan otoritatifnya Java, dengan pengganti mekanisme Worker yang harus dibuktikan pada runtime desktop. Kandidat angka/fixture/protokol di dokumen root belum keputusan final hanya karena sudah ditulis.

- Fixture sama pada setiap Run; tidak ada random/wall clock/FPS dalam mekanik.
- Kecepatan animasi tidak memengaruhi skor atau kematangan.
- Batas aksi, output, source, antrean, dan watchdog tetap diberlakukan.
- UI tetap dapat menghentikan loop Python tanpa menunggu Worker merespons pesan Stop.
- Pesan Worker lama tidak boleh mengubah dunia atau hasil percobaan baru.
- Stop/Reset/error tidak menghapus kode; Reset tidak menghapus rekor.
- Source/output tidak dikirim ke server dan output tidak dirender sebagai HTML.
- Pyodide/Worker tidak boleh dipasarkan sebagai sandbox aman untuk script bermusuhan.
- Tidak ada secret/token di origin prototype atau file yang dipublikasikan.

## Cara bekerja

- Periksa status git dan file terkait sebelum mengedit; pertahankan perubahan kontributor lain.
- Jangan melakukan reset/revert/cleanup destruktif untuk mendapatkan keadaan bersih.
- Jangan membuat commit, push, atau deployment kecuali termasuk permintaan/otorisasi pengguna.
- Jangan membuat subagent atau delegasi otomatis berdasarkan file ini; ikuti otorisasi sesi yang berlaku.
- Komunikasikan temuan, perubahan cakupan, dan blocker secara ringkas dalam bahasa Indonesia.
- Jika belum ada tool/dependency, nyatakan itu; jangan mencantumkan pemeriksaan sebagai lulus hanya karena direncanakan.

## Verifikasi sesuai perubahan

Untuk dokumentasi: periksa tautan relatif, konsistensi angka/API/state, dan status tugas. Tidak perlu memasang seluruh stack untuk edit teks.

Untuk mekanik prototype: tambah/perbarui test Python yang memeriksa aturan dan baseline starter. Untuk lifecycle/protokol prototype: test TypeScript dengan Worker tiruan, termasuk pesan terlambat. Untuk perubahan visual/interaksi prototype: jalankan browser pada ukuran target dan cek state yang berubah. Untuk Mars setelah bootstrap: test domain/controller Java, integrasi runtime dan Spring Boot, serta UI pada build desktop sesuai TEST_PLAN.md. Jangan memakai browser test sebagai bukti desktop libGDX.

Target script package setelah bootstrap:

```text
npm run dev          # server pengembangan
npm run typecheck    # pemeriksaan TypeScript tanpa emit
npm run test:unit    # unit test TypeScript sekali jalan
npm run test:python  # unittest simulasi/runner; Python lokal untuk pengembangan
npm run build        # build produksi
npm run preview      # menyajikan build produksi
```

Script npm di atas tersedia di prototype-web/package.json; jalankan hanya dari prototype-web/. Mars memakai Wrapper root: .\gradlew.bat :lwjgl3:run, :core:controlTest, :core:robotTest, :lwjgl3:desktopSmokeTest, dan build. Versi serta batas bukti Wrapper/JDK ada di README dan VERIFICATION; jangan mengklaim build lulus sebelum dijalankan. Hasil historis prototype di docs/prototype/VERIFICATION.md tidak menggantikan verifikasi perubahan saat ini. Pemain browser tidak perlu Python lokal. Bila lingkungan pengembangan tidak memiliki Python, laporkan test Python belum dijalankan dan gunakan pengujian Pyodide sebagai bukti tambahan; jangan menginstal Python global tanpa kebutuhan/otorisasi yang sesuai.

Setelah checks yang relevan lulus, jangan mengulang test luas tanpa perubahan atau alasan baru. Laporkan apa yang diubah, bukti verifikasi, dan batas yang masih ada.

## Pengelolaan TASKS.md

- Centang tugas hanya setelah deliverable dan verifikasinya ada.
- Bedakan pekerjaan kode selesai dari integrasi teruji, deployment berhasil, dan playtest selesai.
- Jangan mengisi hasil playtest, angka performa, atau persetujuan teman dengan data rekaan.
- Bukti sederhana cukup: nama test dan hasil, versi browser, catatan manual, atau link deployment yang benar-benar diperiksa.
- Jika suatu tugas terhalang, catat alasan dan langkah berikutnya tanpa menandainya selesai.

## Handoff lintas alat

Ikuti PROJECT.md bagian 12 untuk pembagian AI, kontrak, file ownership, dan review. Pada awal sesi, baca handoff TASKS.md dan periksa branch serta perubahan lokal. Pada akhir sesi atau pergantian alat, perbarui handoff dengan hasil, keputusan baru, file pemilik, verifikasi aktual, blocker, serta langkah berikutnya. Bedakan usulan dari keputusan pengguna dan pekerjaan selesai dari rencana. Ringkasan repository menjadi konteks bersama; chat di alat lain perlu diringkas secara eksplisit. Untuk pekerjaan paralel, dokumen bersama memiliki satu editor dan disinkronkan sebelum sesi lain melanjutkan.
