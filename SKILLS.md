# SKILLS — Indeks Paket Skill Bersama (Codex & Antigravity)

Dokumen ini mencatat indeks paket skill tingkat proyek di `.agents/skills/` agar **Codex** dan **Antigravity** memakai instruksi serta resource pendukung yang sama tanpa instalasi global baru.

> **Status pemeriksaan:** Pemasangan/penyalinan file (**A**) dan pemeriksaan resource lokal di folder skill (**B**) sudah dilakukan pada **12 November 2026**. Penemuan di aplikasi baru pada sesi reload (**C**) dan pengujian menjalankan tugas (**D**) belum diuji.

## Konteks clone tim — 13 November 2026

Folder skill dan lockfile sekarang dibundel pada repo libGDX. Path C:\Users dalam tabel sumber adalah asal historis, bukan path yang perlu dibuat di laptop teman. Ketersediaan file tidak membuktikan tool/dependency eksternal terpasang atau discovery aplikasi teman lulus. Buka root clone di Codex/Antigravity dan baca skill yang relevan; tidak perlu menginstal ulang seluruh koleksi atau menjalankan script skill saat onboarding.

## 1. Ringkasan Artefak & Installer

- **Lokasi bersama:** `.agents/skills/` (total **25 skill**: 5 dipasang lewat `npx skills add --copy`, 20 disalin dari folder lokal).
- **Lockfile installer:** [`skills-lock.json`](skills-lock.json) dibuat oleh `npx skills add` (versi `2`, `installMode: "copy"`) dan mencatat 5 skill dari GitHub (`doc-coauthoring`, `codebase-design`, `diagnosing-bugs`, `domain-modeling`, `tdd`).
- **Catatan penamaan folder:** `design-taste-frontend` berasal dari folder lokal `C:\Users\abdie\.codex\skills\taste-skill` yang memiliki `name: design-taste-frontend` di `SKILL.md`, sehingga disalin ke `.agents/skills/design-taste-frontend/` agar nama folder cocok dengan identitas `SKILL.md`.
- **Skill belum tersedia:** `mars-libgdx` dan `mars-backend` belum dibuat/tersedia (sengaja tidak dibuat pada tugas pemasangan ini).

## 2. Daftar Skill Terpasang & Disalin

### A. Paket dari Sumber Terdaftar (`npx skills add`)

| Nama skill | Fungsi utama | Sumber | Tujuan | Versi / hash sumber | Status |
| --- | --- | --- | --- | --- | --- |
| `doc-coauthoring` | Alur kolaboratif penulisan PRD, proposal, spec, dan uji baca. | `anthropics/skills` (`skills/doc-coauthoring/SKILL.md`) | `.agents/skills/doc-coauthoring` | Hash `skills-lock.json`: `d32facc16a25b47a21a90e79c1e6c072d342a9e41b595b58b86f869ff33ccc26` (commit tidak dicatat CLI) | Dipasang (`--copy`) |
| `domain-modeling` | Mempertajam istilah domain, `GLOSSARY.md`, dan ADR. | `mattpocock/skills` (`skills/engineering/domain-modeling/SKILL.md`) | `.agents/skills/domain-modeling` | Hash `skills-lock.json`: `b78689833404148c769077355e12dbe5ad996a158a1e21e58ddf169e439d9d8c` (commit tidak dicatat CLI) | Dipasang (`--copy`) |
| `codebase-design` | Desain modul dalam (*deep modules*), *seam*, dan keterujian antarmuka. | `mattpocock/skills` (`skills/engineering/codebase-design/SKILL.md`) | `.agents/skills/codebase-design` | Hash `skills-lock.json`: `d40f025dc1b0f2020663260f38f2bcb8d6ed3523afb11e5fda624b01238677e8` (commit tidak dicatat CLI) | Dipasang (`--copy`) |
| `diagnosing-bugs` | Loop reproduksi deterministik, minimasi, hipotesis falsifiable, dan regresi. | `mattpocock/skills` (`skills/engineering/diagnosing-bugs/SKILL.md`) | `.agents/skills/diagnosing-bugs` | Hash `skills-lock.json`: `87c78ae966e591f9ceb177789cc5cc9de69277cd26679ffd36ca5300f430d513` (commit tidak dicatat CLI) | Dipasang (`--copy`) |
| `tdd` | Siklus *red-green-refactor* pada *seam* publik yang disepakati. | `mattpocock/skills` (`skills/engineering/tdd/SKILL.md`) | `.agents/skills/tdd` | Hash `skills-lock.json`: `e3e0f78ff1c2d52ffefa0378baa1e80e129d359288cf9a56f4e59afcd2aac86c` (commit tidak dicatat CLI) | Dipasang (`--copy`) |

### B. Engineering (Disalin dari Lokal)

| Nama skill | Fungsi utama | Sumber | Tujuan | Versi / hash sumber | Status |
| --- | --- | --- | --- | --- | --- |
| `karpathy-guidelines` | Disiplin berpikir sebelum coding, kesederhanaan, dan perubahan terarah. | `C:\Users\abdie\.codex\skills\karpathy-guidelines` | `.agents/skills/karpathy-guidelines` | Tidak diketahui | Disalin |
| `lean-build` | Membangun irisan fitur terkecil yang utuh tanpa overbuilding. | `C:\Users\abdie\.codex\skills\lean-build` | `.agents/skills/lean-build` | Tidak diketahui | Disalin |
| `surgical-patch` | Perbaikan bug pada lapisan sempit dengan bukti regresi. | `C:\Users\abdie\.codex\skills\surgical-patch` | `.agents/skills/surgical-patch` | Tidak diketahui | Disalin |
| `verify-and-stop` | Validasi kondisi terima minimal lalu berhenti bersih. | `C:\Users\abdie\.codex\skills\verify-and-stop` | `.agents/skills/verify-and-stop` | Tidak diketahui | Disalin |

### C. Game (Disalin dari Lokal)

Seluruh skill game di bawah berasal dari cache lokal `gamedev-skills/awesome-gamedev-agent-skills` (tercatat di `C:\Users\abdie\.agents\.skill-lock.json`, `updatedAt: 2026-11-11T12:32:31Z`; commit Git tidak dicatat).

| Nama skill | Fungsi utama | Sumber | Tujuan | `skillFolderHash` (lock global) | Status |
| --- | --- | --- | --- | --- | --- |
| `router` | Dispatcher skill gamedev berdasar deteksi engine dan tugas. | `C:\Users\abdie\.agents\skills\router` | `.agents/skills/router` | `c69bf767ed677fc2d73937251f0b249c5a014e4a` | Disalin |
| `game-ui-ux` | Arsitektur HUD, menu stack, fokus keyboard/controller, dan update berbasis event. | `C:\Users\abdie\.agents\skills\game-ui-ux` | `.agents/skills/game-ui-ux` | `3e8070ce80182953433e387cd4603907d32ea03b` | Disalin |
| `level-design` | Alur *blockout -> test -> iterate -> dress*, metrik gerak, dan pacing. | `C:\Users\abdie\.agents\skills\level-design` | `.agents/skills/level-design` | `3ea42436d51a120e2afdbd4827e517c89db32018` | Disalin |
| `input-systems` | Pemetaan aksi (*action mapping*), rebinding, multi-device, dan aksesibilitas input. | `C:\Users\abdie\.agents\skills\input-systems` | `.agents/skills/input-systems` | `3365732f423d3a0390fb99eb3db35d0f14fc5e30` | Disalin |
| `save-systems` | Skema penyimpanan berversi, migrasi, dan penulisan atomik. | `C:\Users\abdie\.agents\skills\save-systems` | `.agents/skills/save-systems` | `46cac963bf1074c6e865e655d8b93d4d065eb1d0` | Disalin |
| `create-game-assets` | *Art direction brief*, manifest aset, lisensi/provenance, dan inspeksi sprite/raster. | `C:\Users\abdie\.agents\skills\create-game-assets` | `.agents/skills/create-game-assets` | `75c20076a125f07ba5763c4d9c1be32f2ad470bf` | Disalin |
| `game-feel` | *Juice* dan umpan balik visual/audio di atas simulasi tanpa mengubah mekanik inti. | `C:\Users\abdie\.agents\skills\game-feel` | `.agents/skills/game-feel` | `7a1eb41bdf2fce7adeadb4b97efa8241fb9c7f98` | Disalin |
| `camera-systems` | Kamera 2D *follow*, *deadzone*, *bounds clamping*, dan hook guncangan. | `C:\Users\abdie\.agents\skills\camera-systems` | `.agents/skills/camera-systems` | `8d91a4b822661f89678dbf57e43047c670f7fd11` | Disalin |
| `audio-design` | Arsitektur bus/mixer, ducking, variasi SFX, dan musik adaptif. | `C:\Users\abdie\.agents\skills\audio-design` | `.agents/skills/audio-design` | `8bc16322f9805cb6f0aeccec5c566776ab02b073` | Disalin |
| `performance-optimization` | Disiplin *profile -> bottleneck -> fix -> re-measure*, batching, dan kontrol alokasi/GC. | `C:\Users\abdie\.agents\skills\performance-optimization` | `.agents/skills/performance-optimization` | `d3d1489d83520bdd89a10665815a082fad5aa5ca` | Disalin |

### D. Desain, Pendukung, dan Opsional (Disalin dari Lokal)

| Nama skill | Fungsi utama | Sumber | Tujuan | Versi / hash sumber | Status |
| --- | --- | --- | --- | --- | --- |
| `impeccable` | Audit, kritik, polish, dan desain UX/UI frontend/native. | `C:\Users\abdie\.agents\skills\impeccable` | `.agents/skills/impeccable` | Skill `4.8.0` (`SKILL.md`), engine `0.1.18` (`scripts/VERSION`) | Disalin |
| `playwright-cli` | Otomasi browser dan inspeksi web lewat `playwright-cli`. | `C:\Users\abdie\.codex\skills\playwright-cli` | `.agents/skills/playwright-cli` | Tidak diketahui | Disalin |
| `caveman` | Gaya jawaban ringkas (*terse*) opsional. | `C:\Users\abdie\.codex\skills\caveman` | `.agents/skills/caveman` | Tidak diketahui | Disalin |
| `design-taste-frontend` | Art direction & inspeksi UI website/app screen anti-template (opsional). | `C:\Users\abdie\.codex\skills\taste-skill` | `.agents/skills/design-taste-frontend` | Tidak diketahui | Disalin (opsional) |
| `puzzle` | Pola state papan diskret, resolusi aturan, dan progresi level puzzle (opsional). | `C:\Users\abdie\.agents\skills\puzzle` | `.agents/skills/puzzle` | Hash lock global `386ed8e88f63ad538d6758fbfd1765dca6d61b80` | Disalin (opsional) |
| `itch-publish` | Upload build dan channel itch.io via `butler push` (opsional). | `C:\Users\abdie\.agents\skills\itch-publish` | `.agents/skills/itch-publish` | Hash lock global `f53fd74ad346f2bce278bc633f46852fe268f4d4` | Disalin (opsional) |

## 3. Dependensi & Keterbatasan yang Ditemukan

Pemeriksaan lingkungan dilakukan pada **12 November 2026** terhadap tool yang dirujuk oleh skill:

1. **`create-game-assets` (`scripts/asset_report.py`, `scripts/build_preview_sheet.py`):**
   - `scripts/requirements.txt` meminta `Pillow>=10,<14`.
   - Pada mesin ini `python` tersedia (`Python315`), tetapi modul `PIL` (`Pillow`) **belum terpasang** (`ModuleNotFoundError: No module named 'PIL'`). Script inspeksi/contact-sheet Python belum siap pakai sebelum `Pillow` dipasang.
2. **`itch-publish`:**
   - Membutuhkan CLI `butler` (`butler push`, `butler login`). Saat diperiksa, `butler` **tidak ditemukan** di `PATH`.
3. **`impeccable`:**
   - Menyertakan binary Windows x64 (`scripts/bin/windows-x64/impeccable.exe`) dan launcher `scripts/impeccable.cmd` (khusus Windows; `scripts/impeccable` untuk shell POSIX).
   - Folder `agents/*.toml` mengacu ke definisi subagent gaya Codex dan fallback `reference/degraded/*.md`. Karena proyek melarang delegasi subagent otomatis tanpa izin sesi, gunakan fallback manual/konteks langsung bila launcher atau subagent tidak dipakai.
4. **`router`:**
   - Daftar deteksi bawaan (`references/engine-detection.md`) tidak memuat `libGDX` dan merujuk ke skill engine eksternal (`godot-*`, `unity-*`, `steam-publish`, dll.) yang sengaja **tidak disalin** ke proyek ini.
   - Untuk target **Mars**, gunakan jalur fallback `references/custom-engines.md` dan petakan ke dokumentasi resmi **libGDX** + kontrak Java/Spring Boot.
5. **`diagnosing-bugs` & `tdd` & `codebase-design` & `doc-coauthoring`:**
   - `diagnosing-bugs/scripts/hitl-loop.template.sh` adalah script bash (memerlukan bash/Git Bash di Windows jika dijalankan).
   - `tdd` menyebut skill `code-review` (tidak terpasang di `.agents/skills/`) dan mewajibkan kesepakatan *seam* sebelum menulis test.
   - `codebase-design/DESIGN-IT-TWICE.md` dan `doc-coauthoring` Stage 3 menyebut pemakaian subagent paralel; di proyek ini ikuti aturan `AGENTS.md` (jangan membuat subagent/delegasi otomatis tanpa izin sesi).
6. **`design-taste-frontend`:**
   - Script `scripts/*.mjs` membutuhkan Node.js 22+ (terpasang `v24.21.0`) serta browser Chrome/Edge/Chromium; `scripts/assets.mjs` membutuhkan akses jaringan. Fokus utamanya adalah halaman web/app screen HTML/CSS, bukan renderer desktop libGDX.
7. **`playwright-cli`:**
   - Binary `playwright-cli` terdeteksi di `C:\Users\abdie\AppData\Roaming\npm\playwright-cli.ps1`, dan proyek prototype juga memakai `@playwright/test` (`1.56.1`).
8. **Toolchain Target Mars (di luar paket skill):**
   - `java` & `javac` terdeteksi, sedangkan `gradle` dan `mvn` **tidak ditemukan** di `PATH`; nama skill khusus mars-libgdx/mars-backend belum tersedia; kode libGDX existing sekarang berada di core/ dan lwjgl3/. Kebutuhan Gradle proyek memakai Wrapper, bukan Gradle global.

## 4. Kapan Digunakan & Batas Khusus Proyek

- **Aturan tertinggi:** Instruksi pengguna dan [`AGENTS.md`](AGENTS.md) selalu mengatasi default bawaan skill. Memasang skill tidak berarti semuanya aktif bersamaan pada setiap tugas.
- **Dokumentasi & arsitektur Mars:**
  - `doc-coauthoring`, `domain-modeling`, `codebase-design`: dipakai saat menyusun dokumen Mars, `GLOSSARY.md`, ADR, dan merancang *deep module* serta kontrak HTTP/Java–Python.
- **Implementasi & debugging (Prototype maupun Mars):**
  - `karpathy-guidelines`, `lean-build`, `surgical-patch`, `verify-and-stop`, `tdd`, `diagnosing-bugs`: dipakai untuk menjaga perubahan minimal, teruji di *seam* publik, dan verifikasi fokus.
- **Khusus Prototype Web (`Farm Code Lab`):**
  - `playwright-cli`: hanya untuk inspeksi/pengujian browser prototype web; **tidak membuktikan** window desktop libGDX berjalan.
  - `impeccable` & `design-taste-frontend` (opsional): untuk review/perbaikan UI web; `design-taste-frontend` tidak aktif otomatis dan bukan fondasi renderer game.
- **Khusus Game Mars (`Java/libGDX` + `Spring Boot`):**
  - `router`, `game-ui-ux`, `level-design`, `input-systems`, `save-systems`, `create-game-assets`, `game-feel`, `camera-systems`, `audio-design`, `performance-optimization`: **wajib diadaptasi ke dokumentasi resmi libGDX** (Scene2D UI, `OrthographicCamera`, `AssetManager`, dll.). Jangan mengganti engine ke Godot/Unity karena contoh kode di skill.
  - `game-feel`: animasi dan *juice* **tidak boleh** mengubah hasil simulasi, kematangan, jatah aksi, atau skor.
  - `puzzle` (opsional, tidak aktif otomatis): hanya referensi pola papan diskret; **tidak mengotorisasi** fitur undo, cascade, atau match-3 pada Mars.
- **Lainnya:**
  - `caveman`: gaya jawaban opsional; **tidak diaktifkan** kecuali diminta pengguna.
  - `itch-publish` (opsional): **tidak mengotorisasi** publikasi/upload; butuh izin eksplisit pengguna dan CLI `butler`.

## 5. Cara Memeriksa Pemuatan Skill di Codex & Antigravity

### Antigravity
1. Tutup sesi chat lama atau buka jendela/percakapan **baru** pada root clone MarsReboot (folder dengan settings.gradle dan AGENTS.md) (Antigravity memindai `.agents/skills/` dengan *progressive disclosure* saat sesi dimulai; hanya `name` dan `description` yang dimuat di awal, isi lengkap dimuat saat skill dipanggil).
2. Pada percakapan baru, tanyakan skill workspace apa saja yang terdeteksi dari `.agents/skills/` dan minta agen membaca satu skill (mis. `domain-modeling` atau `lean-build`) untuk memastikan kontennya dapat diakses.

### Codex
1. Buka sesi Codex **baru** dengan working directory di root clone MarsReboot (atau jalankan reload/sesi baru agar indeks skill proyek dibaca ulang).
2. Periksa daftar skill terdeteksi lewat pemilih skill (`$` / `/skills` sesuai antarmuka Codex yang dipakai) atau jalankan `npx skills list` di root proyek untuk memastikan seluruh 25 skill terbaca sebagai *Project Skills* untuk `Codex` dan `Antigravity`.
3. Uji pemanggilan satu skill (mis. meminta ringkasan aturan dari `$lean-build`) pada sesi baru sebelum mulai mengerjakan dokumen atau kode Mars.
