# TASKS — Rencana implementasi prototype

Status per 2 November 2026. Checkbox menunjukkan pekerjaan yang sudah selesai, bukan prioritas. Implementasi lokal M1–M4 telah diverifikasi; distribusi publik dan playtest masih terbuka.

## M0 — Spesifikasi

- [x] Buat PRD.md: tujuan, pengguna, cakupan, dan rencana playtest.
- [x] Buat RULES.md: fixture 6×6, API, tick, hasil, dan baseline.
- [x] Buat ARCHITECTURE.md: Worker/Pyodide, simulasi, protokol, lifecycle, dan pemulihan.
- [x] Buat DESIGN.md: spesifikasi layar, state, token, dan aksesibilitas.
- [x] Buat AGENTS.md: batas kontribusi dan verifikasi.
- [x] Buat TASKS.md: milestone, dependensi, dan bukti.

Spesifikasi awal belum berarti prototype atau arah visual final sudah diimplementasikan/disetujui.

## M1 — Bootstrap dan bukti Python di browser

Dependensi: M0. Tujuan: buktikan jalur runtime pada browser sebelum membangun permainan lengkap.

- [x] T-01 Bootstrap Vite + TypeScript tanpa framework; tambah .gitignore dan lockfile.
- [x] T-02 Sediakan script dev/typecheck/test:unit/test:python/build/preview yang dijelaskan AGENTS.md; dokumentasikan prasyarat.
- [x] T-03 Pilih dan pin satu versi Pyodide beserta semua asset runtime yang cocok.
- [x] T-04 Buat module Worker yang menjalankan Python sederhana dan mengirim output ke halaman.
- [x] T-05 Buktikan loop `while True: pass` dapat dihentikan dari UI dengan terminate, lalu runtime dapat dipakai lagi.
- [x] T-06 Periksa runtime pada build produksi yang disajikan HTTP; verifikasi path, MIME, dan CORS.

Selesai jika: loading/ready/error dapat ditampilkan, Python nyata berjalan di Worker, Stop/recovery terbukti, dan build produksi memuat runtime. Catat versi/browser dan hasil; jangan menyamakan keberhasilan dev server dengan deployment.

## M2 — Simulasi dan runner Python

Dependensi: T-04. Dapat dikembangkan mandiri dari tampilan Canvas.

- [x] T-07 Implementasikan fixture, koordinat, kematangan absolut, dan state tanaman sesuai RULES.md.
- [x] T-08 Implementasikan kelima API beserta validasi argumen dan urutan biaya aksi.
- [x] T-09 Implementasikan success, incomplete, budget_exceeded, dan error; tambahkan kontrol penghentian runner.
- [x] T-10 Muat starter.py persis sesuai RULES.md; buktikan 36 panen, 35 gerakan, 5 wait, 112 tick, posisi `(0, 5)`.
- [x] T-11 Tambah unit test untuk tepi, sensor, panen gagal/kosong, kematangan, dan batas tick 200.
- [x] T-12 Buktikan runner yang sama bekerja pada Pyodide, bukan hanya Python lokal.

Selesai jika: aturan dapat diuji tanpa DOM dan hasil baseline konsisten di runtime browser.

## M3 — Protokol, lifecycle, dan pemulihan

Dependensi: M1 dan M2.

- [x] T-13 Definisikan pesan/snapshot bertipe dan validasi protocolVersion, generation, runId, serta urutan batch.
- [x] T-14 Implementasikan event aksi, batching maksimal 16 aksi, serta batas antrean 200 aksi.
- [x] T-15 Implementasikan controller loading/ready/running/replaying/outcome beserta kontrol Run/Stop/Reset.
- [x] T-16 Tambah watchdog komputasi 10 detik dan startup 60 detik; buktikan stdout tidak memperpanjang timeout.
- [x] T-17 Batasi source 32 KiB dan output 200 baris/16 KiB; render sebagai teks dan gabungkan fragmen.
- [x] T-18 Uji pesan lama setelah Stop/Reset/Run baru, worker crash, output flood, dan pemulihan tanpa kehilangan source.

Selesai jika: pembatalan tetap berfungsi untuk loop tanpa aksi, Run baru bersih, dan hasil percobaan lama tidak bocor ke percobaan berikutnya.

## M4 — Antarmuka yang dapat dimainkan

Dependensi: M3; layout statis dapat dikerjakan setelah M1 tanpa mengubah kontrak mekanik.

- [x] T-19 Implementasikan layout DESIGN.md dengan editor native, tujuan, bantuan API, dan kontrol berlabel.
- [x] T-20 Implementasikan Canvas HiDPI, koordinat, drone, tiga keadaan petak, dan legenda.
- [x] T-21 Implementasikan replay 1×/2×/4×, tick/progres sesuai snapshot terlihat, serta reduced motion.
- [x] T-22 Tampilkan semua outcome dan lokasi error player.py jika tersedia; jangan tampilkan sukses sebelum replay selesai.
- [x] T-23 Tambah ringkasan DOM, data petak yang dapat diakses, fokus terlihat, dan navigasi keyboard.
- [x] T-24 Simpan source dan rekor per versi aturan; validasi data storage dan tangani storage unavailable.
- [x] T-25 Periksa tampilan 1440×900, 1280×720, dan 390 px; periksa kontras dan kondisi teks/output panjang.

Selesai jika: orang lain dapat menjalankan, mengubah kode, memahami hasil, dan mencoba ulang tanpa alat developer.

## M5 — Verifikasi terpadu dan distribusi

Dependensi: M4.

- [x] T-26 Jalankan typecheck, unit test TS, test Python, serta build produksi; catat hasil aktual.
- [x] T-27 Pada build produksi, jalankan starter di 1× dan 4×; buktikan skor/state sama.
- [x] T-28 Uji Stop saat running dan replaying; Reset saat aktif; syntax error; runtime error; incomplete; budget; timeout; runtime load failure.
- [ ] T-29 Periksa Chrome dan Edge desktop; catat hasil Firefox tanpa mengklaim dukungan yang belum diuji.
- [x] T-30 Siapkan artefak hosting statis; publikasikan hanya bila termasuk instruksi/otorisasi pengguna.
- [ ] T-31 Setelah deployment diotorisasi, periksa link dari sesi browser bersih: runtime, starter, Stop, dan persistensi.
- [x] T-32 Catat batas versi ini, langkah menjalankan lokal, dan known issues berdasarkan bukti.

Selesai jika: pemeriksaan relevan lulus dan link yang benar-benar diuji tersedia untuk playtest. Jika hosting belum diotorisasi, implementasi lokal dapat selesai tetapi milestone distribusi tetap terbuka.

## M6 — Playtest dan keputusan tim

Dependensi: link teruji dari M5. Tahap ini memerlukan keterlibatan pengguna/teman; tidak dapat diselesaikan agen dengan data buatan.

- [ ] T-33 Siapkan panduan sesi: tujuan uji, instruksi netral, dan lembar catatan sederhana tanpa identitas sensitif.
- [ ] T-34 Uji ke 3–5 teman; catat pengalaman coding, bantuan, kebingungan, dan percobaan optimasi.
- [ ] T-35 Ringkas hasil aktual terhadap hipotesis PRD; bedakan komentar, perilaku, dan dugaan penyebab.
- [ ] T-36 Bersama tim putuskan lanjut/perbaiki/ubah mekanik; catat keputusan dan alasan.
- [ ] T-37 Perbarui cakupan berikutnya hanya berdasarkan keputusan tersebut.

## Bukti dan blocker

| Area | Bukti saat ini | Status |
| --- | --- | --- |
| Spesifikasi | Enam dokumen root dibuat; tautan dokumen lokal, pasangan code fence, angka/API/state diperiksa. | Selesai untuk draft v0.1. |
| Runtime/dependency | Vite 7.2.2, TypeScript 5.9.3, Pyodide 0.29.3; dependency lokal dan lockfile. Runtime produksi dimuat pada Chrome/Edge. | Lulus lokal. |
| Mekanik/baseline | Runner aplikasi: 16 test Python; starter 36 panen, 35 gerakan, 36 aksi panen, 5 wait, 112 tick, `(0, 5)`. Pyodide Chrome/Edge pada 1×/4× dan reduced motion sama. | Lulus. |
| UI/browser | 14 test TS dan 6 skenario browser produksi; screenshot 1440/1280/390 px dibuka dan diperiksa. Detail di VERIFICATION.md. | Chrome/Edge lulus; Firefox belum diuji. |
| Hosting | Belum ada deployment. | Belum dilakukan. |
| Playtest/persetujuan tim | Belum ada sesi atau keputusan. | Menunggu prototype dan partisipan. |

Saat tugas selesai, tambahkan bukti singkat yang sesuai. Jangan mengubah daftar rencana menjadi laporan keberhasilan tanpa menjalankan pemeriksaannya.

## Catatan implementasi lokal

- Bukti terperinci, versi browser, nama suite, dan batas pemeriksaan: [VERIFICATION.md](VERIFICATION.md). Langkah menjalankan: [README.md](README.md).
- T-29 tetap terbuka untuk Firefox. Chrome 156.0.8078.76 dan Edge 156.0.4314.58 sudah diperiksa.
- T-30 selesai pada artefak statis `dist/`; tidak ada publikasi. T-31 menunggu otorisasi dan target hosting. M5 distribusi belum selesai.
- Stop/timeout mempertahankan hasil terakhir dan menawarkan pemuatan runtime eksplisit; Reset aktif menyiapkan Worker baru. Pilihan pemulihan ini dicatat di ARCHITECTURE.md dan belum divalidasi lewat playtest.
- M6 tetap terbuka; tidak ada hasil teman atau persetujuan tim yang direka.

## Handoff lintas alat

Bagian ini lintas target; checkbox milestone M0–M6 di atas tetap khusus prototype. Perbarui ringkasan berikut saat sesi berakhir atau alat berganti. Untuk sesi paralel, tiap tugas mencantumkan branch/worktree dan satu pemilik file; satu editor menggabungkan ringkasan bersama. Panduan: [PROJECT.md bagian 12](../../PROJECT.md#12-ai-development-strategy--workflow).

### 11 November 2026 — Integrasi workflow AI (dokumentasi)

- **Target dan hasil:** dokumentasi workflow untuk prototype web dan target Mars. PROJECT.md menjelaskan pembagian AI, shared contracts, worktree, ownership, CI, review, dan prompt awal lintas alat. README.md memberi pintu masuk; AGENTS.md mengarahkan sesi berikutnya membaca konteks bersama.
- **Keputusan pengguna:** memakai Codex dan Antigravity serta meminta pembagian tugas dan kesinambungan konteks. Pembagian model adalah rekomendasi; akses akun belum diperiksa. Target game lengkap tetap 9 puzzle, tahap pertama puzzle 1–4.
- **Status aktual:** prototype web punya source dan test; bukti historis di VERIFICATION.md. Game Java/libGDX, backend, database, kontrak HTTP, dan CI belum dibuktikan tersedia. Pembaruan ini tidak mengimplementasikan komponen tersebut.
- **Branch/commit dan ownership:** sesi dokumentasi ini mengubah PROJECT.md, AGENTS.md, README.md, TASKS.md. Semua file tersebut teramati belum dilacak Git sebelum edit; tidak ada commit dibuat. Pemilik sesi berikutnya memeriksa status aktual sebelum melanjutkan.
- **Verifikasi:** konsistensi batas prototype/Mars, tautan lokal tambahan, code fence, serta penamaan model terhadap sumber resmi diperiksa. Test aplikasi tidak dijalankan ulang untuk perubahan dokumentasi ini.
- **Blocker/pertanyaan terbuka:** akses model akun, konfirmasi Python ke aslab, bootstrap Java, kontrak HTTP, schema database, serta nama pemilik peran tim masih perlu dipastikan. Chat baru perlu diringkas di dokumen agar alat lain dapat membacanya.
- **Langkah berikutnya:** buka root project di Antigravity, gunakan prompt awal PROJECT.md bagian 12, minta ringkasan pemahaman, kemudian tetapkan satu tugas aktif dan file ownership sebelum implementasi. Tidak ada pekerjaan kode baru yang dimulai oleh handoff ini.

### 11 November 2026 — Pembahasan pattern game Mars

- **Target/tugas:** menjelaskan kandidat pattern untuk Mars Java/libGDX, termasuk audio, input, UI, runtime, serta ringkasan game. Ini pembahasan desain; bukan implementasi atau persetujuan daftar pattern final.
- **Hasil pembahasan:** enam kandidat PROJECT.md bagian 8 tetap menjadi acuan awal. Adapter untuk penghubung runtime dan Facade untuk kontrol audio menjadi kandidat tambahan. Game Loop, Event Queue, dan pengelolaan aset adalah pola/layanan pendukung; tidak otomatis dihitung sebagai pattern GoF yang diakui mata kuliah. Singleton tidak wajib hanya karena audio dikelola satu objek. Object Pool dan ECS belum punya kebutuhan yang terbukti.
- **Ownership/status:** hanya TASKS.md diperbarui untuk handoff ini pada branch master; file masih belum dilacak Git. Tidak ada perubahan kode, commit, atau penetapan arsitektur baru.
- **Verifikasi:** PROJECT.md dan handoff sebelumnya dibaca; dokumentasi resmi libGDX untuk audio, AssetManager, dan lifecycle diperiksa. Pemeriksaan tautan handoff dan code fence dilakukan; test aplikasi tidak relevan untuk pembahasan ini.
- **Terbuka/langkah berikutnya:** cocokkan kandidat dengan materi pattern kelas dan struktur Java aktual; pilih penerapan yang dapat dibuktikan lewat relasi kelas serta perilaku. Audio, input, dan aset perlu ownership saat bootstrap. Target tetap 9 puzzle, tahap awal 1–4; implementasi Mars belum dibuktikan.

### Format untuk handoff berikutnya

```text
Tanggal / alat / model aktual:
Target (prototype web atau Mars) dan tugas aktif:
Branch / worktree / commit dasar / edit belum di-commit:
Hasil dan file yang berubah:
Keputusan pengguna + alasan / usulan yang belum dipilih:
Pemilik file aktif dan kontrak yang terdampak:
Verifikasi aktual (lulus / gagal / belum dijalankan + bukti):
Blocker / risiko / pertanyaan terbuka:
Langkah berikutnya dan kriteria selesai:
```

### 12 November 2026 — Pembahasan workflow visual Mars

- **Target/tugas:** konsultasi workflow karakter, UI/UX, lingkungan, dan animasi untuk Mars Java/libGDX; tidak ada implementasi atau koneksi Figma yang dilakukan.
- **Usulan:** tentukan arah visual dan skala aset, rancang alur inspeksi–terminal–hasil, buat satu komposisi adegan sebagai acuan, produksi aset kecil yang konsisten, lalu uji satu alur di libGDX sebelum memperluas konten. Figma menjadi tempat rancangan layar dan referensi; aset sprite/animasi memerlukan produksi serta pemeriksaan tersendiri.
- **Alat/model:** Antigravity mendokumentasikan MCP dan Figma Dev Mode MCP. Koneksi akun dan kemampuan baca/tulis belum diuji. Hasil pencarian resmi menyebut Gemini 4 Argon, tetapi halaman models yang berhasil dibaca menampilkan daftar berbeda; ketersediaan model harus diperiksa pada aplikasi pengguna. Tidak ada rekomendasi model yang dianggap hasil benchmark.
- **Ownership/status:** hanya TASKS.md diperbarui oleh Codex pada branch master; status awal file repo belum dilacak Git. Tidak ada kode, dependency, commit, push, atau deployment berubah.
- **Verifikasi:** PROJECT.md dan handoff dibaca; dokumentasi resmi Antigravity MCP/models dan Figma MCP dibuka. Penambahan ini tidak mengubah API, angka puzzle, atau status milestone. Test aplikasi tidak dijalankan untuk pembahasan workflow.
- **Terbuka/langkah berikutnya:** pengguna memilih arah visual dan referensi; kemudian tetapkan brief serta satu adegan habitat/terminal untuk ditinjau. Seluruh arah visual di atas masih usulan, belum keputusan pengguna.

### 12 November 2026 — Konsultasi dokumentasi Mars dan skill Antigravity

- **Target/tugas:** rekomendasi penulis PRD, arsitektur, dan desain; prompt audit konteks lalu penulisan dokumentasi Mars. Ketiga dokumen root saat ini tetap milik prototype web Farm Code Lab.
- **Usulan:** Claude di Antigravity menjadi penulis utama PRD/arsitektur, Codex meninjau kontrak dan rencana pengujian, serta arah visual ditinjau pengguna melalui contoh layar. Susun draft Mars terpisah di docs/mars/ agar dokumentasi prototype tetap berlaku. Pembagian dan lokasi ini rekomendasi, belum keputusan pengguna.
- **Skill:** doc-coauthoring dari anthropics/skills; domain-modeling dan codebase-design dari mattpocock/skills. Instruksi sumber diperiksa; belum ada pemasangan atau uji pemuatan di Antigravity. Instruksi proyek dan batas ownership tetap mengatasi default skill.
- **Status/ownership:** Codex hanya menambah handoff TASKS.md pada master yang belum memiliki commit; tidak mengubah spesifikasi, kode, dependency, atau status milestone.
- **Verifikasi aktual:** membaca PROJECT.md, dokumen prototype, dan handoff; membuka dokumentasi resmi Antigravity skills/models, sumber skill, serta dokumentasi Skills CLI. Daftar model hasil pencarian dan halaman terbuka masih berbeda; gunakan pemilih model akun, tanpa mengunci nomor versi. Tautan lokal pada tambahan ini tidak diperkenalkan; test aplikasi tidak dijalankan untuk konsultasi.
- **Langkah berikutnya:** pengguna membuka root repo di Antigravity dan menjalankan audit konteks; setelah ringkasan benar, menulis draft dokumen Mars dengan keputusan terbuka tetap ditandai. Tidak ada implementasi Mars yang diotorisasi oleh konsultasi ini.

### 12 November 2026 — Usulan paket skill bersama Codex–Antigravity

- **Permintaan:** memilih skill untuk seluruh proyek Mars, terutama Codex, dengan paket yang sama untuk Antigravity nanti.
- **Usulan inti:** doc-coauthoring, domain-modeling, codebase-design, karpathy-guidelines, tdd, diagnosing-bugs, dan verify-and-stop; game-ui-ux digunakan sesuai kebutuhan dengan adaptasi libGDX. Kandidat skill khusus Mars libGDX dan backend Spring Boot masih usulan untuk dibuat setelah kontrak dipilih, bukan paket yang sudah tersedia.
- **Kesinambungan:** gunakan satu salinan skill tingkat proyek di .agents/skills/ beserta resource pendukung dan versi sumber yang dicatat; verifikasi pemuatan pada kedua aplikasi. Skill global Codex tidak otomatis tersedia di Antigravity. Instruksi AGENTS.md dan keputusan pengguna tetap berlaku; jangan memasang paket workflow bertumpuk atau mengaktifkan delegasi otomatis.
- **Status/verifikasi:** hanya TASKS.md ditambah oleh Codex; sumber lokal skill serta dokumentasi resmi Antigravity/Skills CLI dan sumber Matt Pocock diperiksa. Beberapa default skill memerlukan persetujuan batas test atau subagent sehingga perlu diselaraskan dengan workflow sesi. Tidak ada instalasi, perubahan konfigurasi, atau pengujian pemuatan dilakukan; rekomendasi belum merupakan keputusan pengguna. Test aplikasi tidak relevan untuk konsultasi ini.
- **Langkah berikutnya:** tetapkan paket minimal bersama, periksa dependensi instruksinya, lalu pasang dan uji penemuan skill pada kedua aplikasi bila pengguna meminta penerapan.

### 12 November 2026 — Pemasangan paket skill bersama proyek (Antigravity)

- **Tanggal / alat / model aktual:** 12 November 2026 / Antigravity / Gemini 3.8 Flash (Medium).
- **Target dan tugas aktif:** lintas target (persiapan workspace bersama Codex & Antigravity). Menyiapkan 25 skill tingkat proyek di `.agents/skills/`, membuat indeks [SKILLS.md](../../SKILLS.md), dan memverifikasi integritas file serta dependensi tanpa menulis PRD/arsitektur/desain atau implementasi game.
- **Branch / worktree / commit dasar / edit belum di-commit:** branch `master` (belum ada commit). Perubahan baru/teredit oleh sesi ini: `.agents/skills/` (25 folder skill), `skills-lock.json`, `SKILLS.md`, dan handoff `TASKS.md`. File aplikasi, `package.json`, `AGENTS.md`, dan dokumen spesifikasi tidak diubah.
- **Hasil dan file yang berubah:**
  - Dipasang via `npx skills add --copy -y --agent antigravity --agent codex`: `doc-coauthoring` (`anthropics/skills`) dan `domain-modeling`, `codebase-design`, `diagnosing-bugs`, `tdd` (`mattpocock/skills`) -> tercatat di `skills-lock.json` (`version: 2`, `installMode: "copy"`).
  - Disalin penuh dari sumber lokal tanpa mengubah sumber: 7 skill dari `C:\Users\abdie\.codex\skills\` (`karpathy-guidelines`, `lean-build`, `surgical-patch`, `verify-and-stop`, `playwright-cli`, `caveman`, dan `taste-skill` ke `.agents/skills/design-taste-frontend`) serta 13 skill dari `C:\Users\abdie\.agents\skills\` (`router`, `game-ui-ux`, `level-design`, `input-systems`, `save-systems`, `create-game-assets`, `game-feel`, `camera-systems`, `audio-design`, `performance-optimization`, `impeccable`, `puzzle`, `itch-publish`).
  - Dibuat `SKILLS.md` berisi indeks, sumber, hash/versi, keterbatasan dependensi, batas pemakaian, dan cara cek pemuatan di Codex & Antigravity.
- **Keputusan & batas yang ditegaskan:** `AGENTS.md` dan keputusan pengguna tetap mengatasi default skill; `mars-libgdx` dan `mars-backend` belum dibuat; `caveman` tidak diaktifkan; skill game wajib diadaptasi ke dokumentasi resmi libGDX; test aplikasi tidak dijalankan pada tugas ini.
- **Verifikasi aktual:**
  - **A (File skill tersedia):** Lulus — 25 folder di `.agents/skills/` memiliki `SKILL.md` beridentitas sesuai tanpa duplikasi nama; `npx skills list` mendeteksi seluruh 25 *Project Skills* untuk Antigravity & Codex.
  - **B (Resource/dependensi):** Lulus untuk file lokal internal skill (lengkap tanpa file cache/credential); namun dependensi eksternal berikut **belum tersedia**: `Pillow` (`PIL`) untuk script `create-game-assets`, `butler` untuk `itch-publish`, serta `gradle`/`mvn` untuk target Mars. `router` tidak memuat `libGDX` dan merujuk skill engine yang tidak disalin (gunakan `custom-engines.md`).
  - **C (Penemuan di sesi baru Codex & Antigravity):** Belum diuji — memerlukan percakapan/reload baru di kedua aplikasi.
  - **D (Uji menjalankan tugas dengan skill):** Belum diuji.
- **Blocker / risiko / pertanyaan terbuka:** `Pillow` dan `butler` belum terpasang; `C` dan `D` perlu dicek pada sesi baru; `mars-libgdx` dan `mars-backend` masih menunggu bootstrap/kontrak.
- **Langkah berikutnya:** buka percakapan baru (atau reload workspace) pada Antigravity dan Codex di root proyek, verifikasi penemuan skill (langkah di `SKILLS.md` bagian 5), baru lanjutkan ke penulisan dokumentasi/kontrak Mars sesuai arahan pengguna.

### 12 November 2026 — PRD Mars bertahap (Codex)

- **Target/hasil:** memperbarui draft [PRD Mars](../mars/PRD.md) yang sudah ada; mempertahankan FR-001–024, NFR-001–011, AC-01–11, dan KT-01–05 serta menambah AC-12–20. Cakupan tetap 9 puzzle dalam 3 misi, tahap awal puzzle 1–4. Status keputusan, rancangan kerja, usulan, dan bukti implementasi dipisahkan; tautan ke dokumen Mars yang belum dibuat diganti catatan pekerjaan berikutnya.
- **Ownership/status:** hanya `docs/mars/PRD.md` dan tambahan handoff `TASKS.md` ini; branch `master` belum memiliki commit dan file repo belum dilacak. Dokumen prototype, PROJECT.md, README Mars, skill, kode, dan dependency dipertahankan. Tidak ada milestone implementasi yang dicentang atau commit/push/deployment dibuat.
- **Keputusan/batas:** pengguna meminta PRD dikerjakan Codex secara bertahap. Tidak ada keputusan produk baru; Python/persetujuan aslab, runtime, aturan/skor/latihan konsep, database/deployment, paket/mesin target, dan detail progres/UI tetap terbuka. Lokasi kode terpisah yang disebut pasti oleh README Mars belum ditetapkan PROJECT.md; dicatat sebagai usulan tanpa mengedit README.
- **Verifikasi aktual:** AGENTS.md, seluruh PROJECT.md, handoff, SKILLS.md, draft PRD/README Mars, header dokumen prototype, daftar file, dan package.json dibaca. Lima render lokal SOP (`tmp/pdfs/assignment-2026-1.png` sampai `-5.png`) diperiksa visual; tidak ditemukan PDF SOP asli (PDF proposal bukan SOP). Pemeriksaan dokumen melalui Python stdlib lulus: 24 FR, 11 NFR, 20 AC, 5 KT unik/berurutan, seluruh FR/NFR terpetakan ke AC, referensi KT terdefinisi, 11 tautan relatif beserta anchor valid, tabel/code fence serta cakupan konsisten. Pemeriksaan baca langsung dilakukan tanpa subagent; tidak ada uji pembaca independen, render Mermaid, atau test aplikasi yang dijalankan.
- **Langkah berikutnya:** susun `docs/mars/RULES.md` dari PRD dan PROJECT.md; tetapkan fixture, API, skenario wajib, biaya/batas, serta evaluasi konsep dengan status usulan sebelum dipilih. Dokumen arsitektur/desain/test plan/backlog Mars menyusul; tautan dokumen tersebut di README Mars yang sudah ada belum tersedia dan berada di luar ownership sesi ini.
