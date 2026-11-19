# TASKS — mars_reboot.py

Status 13 November 2026: Per 13 November 2026, kandidat puzzle 1 panen terhubung ke terminal Python nyata, robot/replay/hasil, Run/Stop/Reset dan progres sesi. Paket Windows x64 menyertakan CPython 3.15.2; Java 21+ masih diperlukan. Backend, puzzle 2–9, save lintas restart dan komputer kedua belum terverifikasi. Lihat handoff akhir dan VERIFICATION; checkbox product gate tidak otomatis selesai karena integrasi satu kandidat lulus.

## 1. Pembagian kerja dan aturan integrasi

| Peran | Ownership utama | Sambungan yang harus disepakati |
| --- | --- | --- |
| 1 Simulasi/runtime | Domain aksi/sensor, runtime adapter, coordinator, pembatalan | Dengan 2: fixture/evaluator; dengan 3: snapshot/lifecycle. |
| 2 Puzzle/evaluasi | Konten, fixture, tujuan, skor, latihan konsep, reference test | Dengan 1: kontrak state; dengan 3: petunjuk; dengan 4: metrik skor. |
| 3 Dunia/UI | Flow Figma/wireframe, astronaut, Scene2D/editor, render, aset/audio | Mengikuti state Java dan kontrak HTTP, tidak menyalin aturan. |
| 4 Backend/distribusi | Akun/sesi, skor, klien HTTP, persistensi, build/panduan | Kontrak skor dengan 2; input/sesi dengan 3; packaging dengan 1. |

Nama orang dan koordinator integrasi belum ditetapkan. Mulai sesi berurutan; jika paralel gunakan clone/worktree terpisah, satu pemilik tiap file/kontrak. Siapkan fixture/mock sama sebelum paralel. Satu tugas → perubahan kecil → bukti fokus → review → gabungkan sesuai otorisasi. Branch/commit/push tidak dibuat otomatis oleh dokumen ini. Semua orang memahami dan dapat menjelaskan kode kontribusinya.

## 2. Dokumen dan keputusan

- [x] DOC-01 Penataan dokumen root Mars, arsip prototype, tautan dan konsistensi terverifikasi. Bukti sesi di VERIFICATION; bukan bukti implementasi atau persetujuan seluruh usulan.
- [ ] M-01 Konfirmasi Python dengan aslab, pilih prioritas usulan produk dan catat di PROJECT. Pemilik: tim; dependensi: DOC-01. Selesai: bahasa, login/mode lokal, bantuan/progres dan pemilik peran berstatus jelas; keputusan yang belum perlu tidak menghalangi eksperimen terpisah.
- [ ] M-02 Pilih lokasi modul/build, JDK serta versi dependency kompatibel. Pemilik: 1+4; dependensi: DOC-01. Selesai: keputusan tertulis, command run/test/build nyata tersedia setelah M-03; tidak menganggap package.json sebagai build Mars.
- [ ] M-03 Bootstrap minimal domain/desktop/backend sesuai lokasi terpilih. Pemilik: 1+4; dependensi: M-02. Bukti: window terbuka, test Java dasar, backend dapat dijalankan; versi pin dan wrapper.

## 3. Eksperimen sebelum komitmen arsitektur

| Status / ID | Deliverable | Pemilik | Dependensi | Kriteria selesai dan bukti |
| --- | --- | --- | --- | --- |
| [ ] M-04 | EXP-01 runtime Python | 1 | M-03, konfirmasi bahasa M-01 sebelum pilihan final | Eksperimen CPython bundel p1 telah teruji (33 assertion nyata); konfirmasi bahasa/penerimaan final masih terbuka. Aksi/sensor, Stop loop, error, timeout, Run ulang; RUN-01–04. Pilih runtime dari hasil, bukan dari mock. |
| [ ] M-05 | EXP-02 editor dan layout kecil | 3 | M-03 | Multiline/indentasi/seleksi/scroll/fokus pada ukuran kandidat; UI-02–04, rekaman/screenshot aktual. |
| [ ] M-06 | EXP-03 paket awal | 4+1 | M-04, M-05 | ZIP Windows + Python sudah lulus uji folder spasi tanpa Python PATH; komputer kedua belum diperiksa. Komputer kedua menjalankan satu aksi dan Stop; prasyarat runtime dan cara backend dicatat. |
| [ ] M-07 | Fixture/API/batas/skor kandidat dipilih | 2+1 | M-04 | RULES ditinjau, referensi p1–p4 serta negatif terhitung; angka berstatus dipilih atau tetap eksperimen, tidak tertukar. |
| [ ] M-08 | EXP-05 latihan loop/fungsi | 2+1 | M-04 | EDU-01 sah/invalid/dead code teruji; keputusan metode sebelum mengunci progres latihan. |
| [ ] M-09 | Kontrak Java/runtime/UI dan HTTP | 1+4, reviewer 2+3 | M-04, M-07 | DTO/contoh sukses/gagal disepakati; OpenAPI autentikasi/skor, sesi/CSRF dan validasi field dipilih; PROTO/API punya acuan. |

Gerbang A: build dasar + runtime/editor terbukti. Kegagalan eksperimen dicatat dengan langkah pembeda berikutnya; jangan melanjutkan massal aset/konten di atas runtime yang belum layak.

## 4. Satu alur lengkap

| Status / ID | Deliverable | Pemilik | Dependensi | Kriteria selesai dan bukti |
| --- | --- | --- | --- | --- |
| [ ] M-10 | Domain grid/command/sensor | 1 | M-07, M-09 | Subset Java kandidat p1-a: grid, gerak, panen, sensor, reset/batas/snapshot teruji 81 assertion (13 Nov). DOM-04 siram dan kontrak penuh belum selesai; checkbox tetap terbuka. |
| [ ] M-11 | Coordinator/runtime/replay | 1 | M-10, M-04, M-09 | Subset p1 Run/Stop/Reset/protokol/replay terimplementasi dan teruji; generalisasi multi-skenario belum dibuat. LIFE-01–04, PROTO-01, RUN-01–05; stale result dan antrean dibatasi. |
| [ ] M-12 | Registrasi/login/sesi backend | 4 | M-03, M-09 | API-01 dengan data nyata; hash password dan akses akun terisolasi. |
| [ ] M-13 | Simpan/ambil skor backend | 4 | M-12, M-07 | API-02/03; validasi, persistensi restart, transaksi dan idempotensi retry. |
| [ ] M-14 | Habitat kecil dan interaksi | 3 | M-05, M-09 | Subset lokal dunia/kontrol dasar (Codex) + visual adegan representatif 2D top-down 3/4, panel inspeksi, dan floating terminal sementara (Antigravity) diimplementasikan dan diuji 12 Nov (handoff akhir); integrasi EXP-02/kontrak penuh belum selesai. Astronaut dapat ke panel/terminal; input gerak berhenti ketika editor fokus. |
| [ ] M-15 | Terminal dan panel hasil | 3 | M-05, M-09 | Terminal p1 berisi editor/API/output/Run/Stop/Reset dengan runtime nyata; penerimaan editor penuh masih terbuka. Tujuan/API/source/output dan kontrol semua state; mock diberi label, kemudian dihubungkan M-11. |
| [ ] M-16 | Puzzle 1 dan evaluator | 2 | M-10, M-07 | Kandidat p1-a reference/negatif telah diuji melalui terminal Python–Java–desktop, replay dan progres sesi. Review fixture/penerimaan tim belum selesai; checkbox tetap terbuka terhadap gate produk. |
| [ ] M-17 | Integrasi klien–runtime–backend | 1–4, koordinator ditetapkan tim | M-11–16 | Login → panel → terminal → aksi/hasil → skor tersimpan dan terbaca setelah buka ulang. UI-01, RUN-01, API-03/04; bukan hanya mock. |

Gerbang B: setiap anggota dapat menjalankan build integrasi yang sama, mencoba satu alur, dan menjelaskan kontribusinya. Review sambungan sebelum memperluas puzzle.

## 5. Demo puzzle 1–4 dan stabilisasi

| Status / ID | Deliverable | Pemilik | Dependensi | Kriteria selesai dan bukti |
| --- | --- | --- | --- | --- |
| [ ] M-18 | Puzzle 2 dan latihan loop | 2+3 | M-17, M-08 | Semua target/syarat latihan dan pesan kegagalan diuji; DOM-05/06, EDU-01. |
| [ ] M-19 | Puzzle 3 sensor/young | 2+3 | M-18 | Panen young ditolak evaluator; solusi sensor lulus, source naif gagal. |
| [ ] M-20 | Puzzle 4 irigasi dua skenario | 2+3 | M-19 | Satu source lulus semua, hardcode gagal, air tepat; target tetap 9 puzzle. |
| [ ] M-21 | Progres/pemulihan/kode per puzzle | 1+3 | M-17, keputusan fitur M-01 | Habitat berubah setelah replay sah; gagal tidak merusak; perpindahan tidak menimpa source; SAVE-02. |
| [ ] M-22 | Save lokal dan retry skor bila dipilih | 4+1 | M-21, M-13, keputusan M-01 | SAVE-01, API-04; data rusak dan sesi habis ditangani; fitur tertunda tidak dicentang selesai. |
| [ ] M-23 | Review OOP dan enam pattern | Semua | M-11, M-13, M-18 | [OOP_AUDIT](docs/mars/OOP_AUDIT.md): Command/Strategy terimplementasi dan teruji lokal; empat kandidat lain belum. Lengkapi kelas/relasi/test/demo dan cocokkan materi/aslab sebelum tenggat SOP. |
| [ ] M-24 | UX/visual satu adegan dan aset tahap 1 | 3 | M-17, M-05 | Flow/wireframe → satu arah visual ditinjau → manifest aset/lisensi → render aktual; UI-03–05, ASSET-01. |
| [ ] M-25 | Playtest demo dan perbaikan prioritas | Semua, pemilik catatan 2 | M-20, M-21, M-24 | PLAY-01 dengan manusia; catat bantuan/kebingungan/bug, perbaiki masalah inti, uji ulang yang terdampak. |

Gerbang C: puzzle 1–4 berurutan, lifecycle stabil, backend nyata, progres sesuai fitur dipilih, dan bukti OOP tersedia. Empat puzzle bukan penyelesaian game lengkap.

## 6. Konten lengkap dan distribusi

| Status / ID | Deliverable | Pemilik | Dependensi | Kriteria selesai dan bukti |
| --- | --- | --- | --- | --- |
| [ ] M-26 | Aturan/fixture/API p5–p9 | 2+1 | M-20, M-08 | Lengkapi RULES sebelum implementasi tiap puzzle; reference/negatif dan batas jelas. |
| [ ] M-27 | Puzzle 5–6 | 2+3 | M-26, M-21 | Irigasi generalisasi dan fungsi lulus seluruh skenario/latihan; CONTENT-01, EDU-01. |
| [ ] M-28 | Puzzle 7–8 | 2+1+3 | M-27 | Checkpoint/peta/kerangka navigasi tersedia dan jalur buntu teruji. |
| [ ] M-29 | Puzzle 9 dan penutup | 2+1+3 | M-28 | Aktivasi pompa pada semua skenario, strategi alternatif sah, habitat pulih; AC-18. |
| [ ] M-30 | Aset/audio dan UX lengkap | 3 | M-29, M-24 | Seluruh state dan misi konsisten, lisensi tercatat, reduced motion/resize lulus. |
| [ ] M-31 | Test integrasi lengkap dan playtest ulang | Semua | M-29, M-30, M-22 jika dipilih | TEST_PLAN sesuai scope; FAIL/menunggu keputusan tidak disamarkan PASS; hasil manusia aktual. |
| [ ] M-32 | Paket rilis komputer kedua | 4 | M-31, M-06 | BUILD-01 pada artefak final; endpoint/backend/runtime jelas, tanpa secret. |
| [ ] M-33 | Luaran SOP dan halaman draft | Semua, editor 2 | DOC-01; video final setelah M-32 | Proposal/PMP/GDD/laporan/kontribusi/video/kredit sesuai sumber dan tenggat; PRD bukan pengganti GDD. |
| [ ] M-34 | Publikasi dan pemeriksaan link | 4, otorisasi pengguna | M-32, M-33 | Publikasi diotorisasi, tautan nyata diperiksa dari komputer lain; jangan centang hanya karena ada paket lokal. |

Tenggat dari SOP: 14 Nov 2026 kelengkapan/revisi/proposal-PMP; 22 Nov enam pattern diimplementasikan; 6 Des 2026 23.59 WIB build/itch.io. Status pengumpulan belum dibuktikan. M-33 dimulai sejak awal dan M-23 disiapkan sejak desain; tidak menunggu seluruh game selesai untuk kewajiban yang lebih awal.

## 7. Cara menjalankan satu sesi tim/AI

1. Baca PROJECT, dokumen domain, dan handoff terbaru; periksa branch/perubahan lokal.
2. Pilih satu M-ID, pemilik/file, hasil, dependensi, dan bukti selesai.
3. Implementasi satu alur, gunakan mock hanya dengan kontrak yang sama.
4. Jalankan test fokus dan pemeriksaan UI aktual bila relevan; anggota lain review.
5. Perbarui tugas serta handoff. Commit/push/merge/deployment sesuai otorisasi sesi, bukan otomatis.

## Handoff lintas alat

Catatan sebelum penataan ini bersifat historis: lokasi dokumen root saat itu masih prototype. Makna klaim lama tidak berubah ketika nama file root sekarang menjadi Mars; lihat [arsip lengkap TASKS prototype](docs/prototype/TASKS.md). Semua entri lama dipertahankan di bawah.


Bagian ini lintas target; checkbox milestone M0–M6 di atas tetap khusus prototype. Perbarui ringkasan berikut saat sesi berakhir atau alat berganti. Untuk sesi paralel, tiap tugas mencantumkan branch/worktree dan satu pemilik file; satu editor menggabungkan ringkasan bersama. Panduan: [PROJECT.md bagian 12](PROJECT.md#12-ai-development-strategy--workflow).

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
- **Target dan tugas aktif:** lintas target (persiapan workspace bersama Codex & Antigravity). Menyiapkan 25 skill tingkat proyek di `.agents/skills/`, membuat indeks [SKILLS.md](SKILLS.md), dan memverifikasi integritas file serta dependensi tanpa menulis PRD/arsitektur/desain atau implementasi game.
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

- **Target/hasil:** memperbarui draft [PRD Mars](docs/mars/PRD.md) yang sudah ada; mempertahankan FR-001–024, NFR-001–011, AC-01–11, dan KT-01–05 serta menambah AC-12–20. Cakupan tetap 9 puzzle dalam 3 misi, tahap awal puzzle 1–4. Status keputusan, rancangan kerja, usulan, dan bukti implementasi dipisahkan; tautan ke dokumen Mars yang belum dibuat diganti catatan pekerjaan berikutnya.
- **Ownership/status:** hanya `docs/mars/PRD.md` dan tambahan handoff `TASKS.md` ini; branch `master` belum memiliki commit dan file repo belum dilacak. Dokumen prototype, PROJECT.md, README Mars, skill, kode, dan dependency dipertahankan. Tidak ada milestone implementasi yang dicentang atau commit/push/deployment dibuat.
- **Keputusan/batas:** pengguna meminta PRD dikerjakan Codex secara bertahap. Tidak ada keputusan produk baru; Python/persetujuan aslab, runtime, aturan/skor/latihan konsep, database/deployment, paket/mesin target, dan detail progres/UI tetap terbuka. Lokasi kode terpisah yang disebut pasti oleh README Mars belum ditetapkan PROJECT.md; dicatat sebagai usulan tanpa mengedit README.
- **Verifikasi aktual:** AGENTS.md, seluruh PROJECT.md, handoff, SKILLS.md, draft PRD/README Mars, header dokumen prototype, daftar file, dan package.json dibaca. Lima render lokal SOP (`tmp/pdfs/assignment-2026-1.png` sampai `-5.png`) diperiksa visual; tidak ditemukan PDF SOP asli (PDF proposal bukan SOP). Pemeriksaan dokumen melalui Python stdlib lulus: 24 FR, 11 NFR, 20 AC, 5 KT unik/berurutan, seluruh FR/NFR terpetakan ke AC, referensi KT terdefinisi, 11 tautan relatif beserta anchor valid, tabel/code fence serta cakupan konsisten. Pemeriksaan baca langsung dilakukan tanpa subagent; tidak ada uji pembaca independen, render Mermaid, atau test aplikasi yang dijalankan.
- **Langkah berikutnya:** susun `docs/mars/RULES.md` dari PRD dan PROJECT.md; tetapkan fixture, API, skenario wajib, biaya/batas, serta evaluasi konsep dengan status usulan sebelum dipilih. Dokumen arsitektur/desain/test plan/backlog Mars menyusul; tautan dokumen tersebut di README Mars yang sudah ada belum tersedia dan berada di luar ownership sesi ini.


### 12 November 2026 — Rewrite dokumentasi utama menjadi Mars (Codex)

- **Keputusan pengguna:** dokumen root ditulis ulang untuk Mars; referensi prototype dipisahkan. Keputusan organisasi dokumentasi dicatat PROJECT §11; tidak berarti semua usulan gameplay/teknis disetujui atau migrasi kode diotorisasi.
- **Hasil/ownership:** PRD Mars dipromosikan ke root dengan ID kebutuhan dipertahankan; RULES, ARCHITECTURE, DESIGN, TEST_PLAN, TASKS, VERIFICATION, dan README kini Mars. PROTOTYPE.md menjelaskan Farm Code Lab; tujuh dokumen lamanya tersimpan di docs/prototype/. Dua file docs/mars menjadi penunjuk. AGENTS/PROJECT diselaraskan; handoff lama tetap ada. Sesi ini satu editor tanpa subagent.
- **Branch/status aktual:** master belum memiliki commit; file repository belum dilacak. Tidak ada source, test aplikasi, dependency, konfigurasi, atau skill diubah; tidak ada file dihapus, instalasi, commit/push, atau deployment.
- **Verifikasi aktual:** link/anchor relatif, code fence, ID dan pemetaan diperiksa dengan Python stdlib; PRD 24 FR/11 NFR/20 AC/5 KT, TEST_PLAN 36 kasus. Arsip dibandingkan sebelum/sesudah dan sama selain tujuan tautan. Model ilustrasi dokumentasi memeriksa 5 fixture kandidat serta 2 contoh negatif; bukan test produk Java/runtime. Hash memastikan hanya Markdown berubah. Sumber resmi libGDX lifecycle/Scene2D, Java Process/ProcessBuilder, dan Spring Security Password Storage dibaca. Tidak ada render Mermaid, Figma, build desktop/backend, atau test/playtest aplikasi baru.
- **Terbuka:** Python/aslab, pilihan runtime dan lokasi modul, angka/fixture/skor kandidat, verifikasi latihan konsep, database/deployment/sesi/CSRF, penyimpanan progres, paket, serta arah visual. Semua rincian baru berlabel Usulan; kandidat fixture 5–9 masih perlu dirinci pada fase konten lanjutan.
- **Langkah berikutnya:** M-01–03 untuk keputusan dan fondasi, lalu M-04/05 eksperimen runtime/editor. Sepakati kontrak M-09 sebelum integrasi/paralel. Centang hanya DOC-01; implementasi dan distribusi tetap belum selesai.

### 12 November 2026 — Penjelasan integrasi backend ke gameplay (Codex)

- **Target/hasil:** menjelaskan rancangan ARCHITECTURE §7: klien desktop mengakses Spring Boot melalui HTTP/JSON untuk akun, sesi, kirim hasil sukses, dan mengambil rekor sendiri. Simulasi/robot/Python/audio tetap di klien; source/output tidak dikirim. Cloud save progres dan leaderboard belum disepakati.
- **Status/keputusan:** konsultasi, tanpa keputusan teknis baru atau implementasi. Sesi cookie, endpoint, H2 file, dan retry adalah kandidat dalam dokumen, bukan kontrak final. Validasi payload server belum membuktikan kebenaran permainan klien atau menyediakan anti-cheat.
- **Ownership/branch:** hanya tambahan handoff TASKS.md oleh Codex pada master; file belum dilacak. Tidak mengubah kode, dependency, milestone, atau kontrak; tidak ada commit/push/deployment.
- **Verifikasi:** membaca PROJECT, ARCHITECTURE, instruksi dan handoff terbaru; panduan REST Spring dan Password Storage Spring Security diperiksa. Code fence dan tautan lokal handoff diperiksa; tidak ada backend/build/test aplikasi dijalankan.
- **Langkah berikutnya:** pilih database, hosting dan metode sesi/CSRF; finalisasi kontrak M-09, lalu buktikan login → satu puzzle → simpan skor → buka ulang dan ambil rekor. Kegagalan simpan skor tidak membatalkan keberhasilan puzzle lokal sesuai rancangan.

### 12 November 2026 — Desain UI/UX & Referensi MarsReboot di Figma (Antigravity)

- **Tanggal / alat / model aktual:** 12 November 2026 / Antigravity + Figma MCP Plugin / Gemini 3.8 Flash (Medium).
- **Target dan tugas aktif:** target Mars (`mars_reboot.py`), desain UI/UX di Figma dan penguncian keputusan arah visual serta tata letak terminal ke `PROJECT.md` dan `DESIGN.md`. Tidak ada kode aplikasi (prototype web maupun Java/libGDX) yang diubah.
- **Branch / worktree / commit dasar / edit belum di-commit:** branch `master` (belum ada commit). Perubahan file repo: `PROJECT.md` (§2 & §11), `DESIGN.md` (header, §1, §4, §8, §9), dan handoff `TASKS.md`. Artefak Figma di akun `abdiel deandra` (`abdiel deandra's team`):
  - Figma Design: `https://www.figma.com/design/z5Q44g0NLnulWXFMaA4Hlz` (`MarsReboot — UI/UX & Top-Down 2D`, 8 board vector termasuk `05c_Terminal_Floating_TFWR_Left` dan `05d_Terminal_Floating_TFWR_Dragged`).
  - FigJam Flow: `https://www.figma.com/board/1cSWTDnhTvzMJjk8jb15vI` (`MarsReboot — User Flow & Siklus UI`).
- **Keputusan pengguna + alasan / usulan yang belum dipilih:** pengguna mengunci arah visual **crisp pixel art 2D top-down 3/4** (referensi *MewnBase* untuk struktur modul/space survival, *The Martian* untuk premis terdampar, dan *Pathway* untuk kualitas shading + pencahayaan 2D ringan dengan `TextureFilter.Nearest` tanpa membangun *custom voxel lighting engine* penuh) serta tata letak **floating draggable terminal** (*The Farmer Was Replaced* style: IDE di kiri/bebas digeser, dunia puzzle robot di kanan). Yang masih terbuka: skala tile pixel (`16×16`/`32×32`), palette terukur, opsi Mode Lokal di Login, konfirmasi Python aslab (`M-01`), dan bootstrap Java/libGDX (`M-02`/`M-03`).
- **Pemilik file aktif dan kontrak yang terdampak:** peran 3 (Dunia/UI); `M-24` dan `M-05` tetap belum dicentang karena implementasi dan uji desktop libGDX belum dibuat.
- **Verifikasi aktual (lulus / gagal / belum dijalankan + bukti):**
  - Seluruh board Figma (`01`–`06` plus `05c`/`05d`) diverifikasi visual via `get_screenshot` (`1024px`).
  - Konsistensi `PROJECT.md` §2/§11, `DESIGN.md`, dan tautan relatif `TASKS.md` diperiksa setelah edit. Test aplikasi (`Java/libGDX` maupun prototype web) tidak relevan/tidak dijalankan karena hanya dokumen & Figma yang berubah.
- **Blocker / risiko / pertanyaan terbuka:** belum ada bootstrap Gradle/libGDX (`M-02`/`M-03`); produksi aset pixel art tetap membutuhkan tool pixel art (mis. LibreSprite/Aseprite) atau aset berlisensi yang dicatat di manifest (`NFR-011`).
- **Langkah berikutnya dan kriteria selesai:** selesaikan keputusan `M-01` & lokasi modul/JDK `M-02`, lalu lanjut ke `M-03` (bootstrap minimal) dan `M-05` (uji editor `TextArea` + `Scene2D Window` draggable di desktop libGDX nyata).

### 12 November 2026 — Dunia dan kontrol dasar pada libGDX existing (Codex)

- **Target/otorisasi:** subset habitat `M-14` pada proyek Java yang sudah ada; satu area placeholder, astronaut, collider, inspeksi dan terminal sementara. Tidak menjalankan gdx-liftoff, membuat proyek/modul pengganti, mengedit source/arsip web, menambah dependency/ECS/physics engine, backend, robot/Python atau evaluator. Tidak memakai subagent, membuat commit/push/deployment, atau memasang tool global.
- **Alat/model:** Codex; nomor model aktual tidak tersedia dari sesi, tidak ditebak. Satu editor untuk source dan dokumen bersama.
- **Root dan identitas nyata:** `C:\Users\abdie\OneDrive\Desktop\Folder_Abdiel\PRAKTIKUM\FINDIH\MarsReboot`. `settings.gradle` memuat `core`/`lwjgl3`; package `com.marsreboot`, main `MarsRebootGame`, launcher `com.marsreboot.lwjgl3.Lwjgl3Launcher`. Java source/target 21, libGDX 1.14.2, LWJGL 3.4.3, projectVersion 1.0.0, Wrapper Gradle 9.8.0 dipertahankan. `assets/ui/uiskin.json`/atlas/font adalah aset generator existing; tidak ditimpa. Tidak ada AGENTS tambahan di root Java yang ditemukan.
- **Git/status awal:** root dokumentasi `FinalProjectOOP` di `master`, belum ada commit dan seluruh file belum dilacak. Root Java bukan repository Git (tidak ada `.git` pada proyek/parent yang dikenali). Karena itu tidak ada diff Git/baseline commit Java yang dapat diklaim; file existing dibaca sebelum perubahan, launcher/konfigurasi generator dipertahankan dan diedit seperlunya. Jangan menginisialisasi Git atau menimpa edit dari IDE secara otomatis.
- **Command proyek (jalankan dari root Java):**

  ```powershell
  .\gradlew.bat :lwjgl3:run
  .\gradlew.bat :lwjgl3:jar
  .\gradlew.bat :core:controlTest
  .\gradlew.bat :lwjgl3:desktopSmokeTest
  .\gradlew.bat build
  ```

  Task run existing menggunakan working directory `assets`. `controlTest` ditambahkan ke `check`, tanpa framework/dependency test baru; smoke desktop adalah task opt-in. **Eksekusi task melalui Wrapper belum lulus:** cache pengguna mula-mula ditolak sandbox; percobaan berizin kemudian timeout mengunduh distribusi. Cache lokal `.gradle-local/` dan curl resmi juga belum menyelesaikan zip. Versi/URL/timeout Wrapper dikembalikan persis existing; tidak menurunkan versi untuk mendapat build hijau. JDK 21 belum ditemukan lokal; file daemon existing meminta 21 dan bisa memerlukan unduhan toolchain setelah jaringan tersedia.
- **Pemilik file sesi / perubahan source Java (relatif root Java):**
  - Codex: `core/src/main/java/com/marsreboot/MarsRebootGame.java` beralih dari logo ke `Game`/`HabitatScreen`; launcher `lwjgl3/src/main/java/com/marsreboot/lwjgl3/Lwjgl3Launcher.java` judul Mars dan ukuran awal 1280×720.
  - Codex: lima kelas baru `core/src/main/java/com/marsreboot/habitat/`: `HabitatWorld`, `HabitatInput`, `HabitatRenderer`, `HabitatScreen`, `HabitatUi`. State dunia/input terpisah dari visual, bukan domain robot.
  - Codex: `core/src/test/java/com/marsreboot/habitat/HabitatControlTest.java`, `lwjgl3/src/test/java/com/marsreboot/habitat/HabitatDesktopSmokeTest.java`; tambahan task pada `core/build.gradle` dan `lwjgl3/build.gradle`; `README.md` Java dan `.gitignore` untuk cache lokal.
  - Dokumen bersama yang diselaraskan Codex di root dokumentasi: PROJECT, PRD, ARCHITECTURE, DESIGN, TEST_PLAN, README, TASKS dan VERIFICATION. RULES, source web, dependency npm dan arsip prototype dipertahankan.
- **Perilaku yang bekerja:** dunia 960×600 dengan dinding/batas, kebun dan fasilitas solid; astronaut 24×28 mulai (160,200), gerak 180 unit/detik, diagonal dinormalisasi, axis sweep mencegah tembus dinding bahkan saat delta besar. Panel air dan terminal dapat didekati lewat jalur terbuka; hint hanya pada jarak pusat-ke-bounds ≤60. Angka ini parameter placeholder astronaut, bukan aturan puzzle/robot. Mode `EXPLORING`, `INSPECTION`, `TERMINAL` memblokir gerak ketika modal. E hanya beraksi pada penekanan baru, Esc mendahului Stage/editor, karakter E pembuka ditahan. Kontrol kembali setelah keluar, source draf TextArea tetap pada sesi layar; tidak disimpan lintas restart.
- **Kontrol/keputusan:** WASD/panah, E dan Esc memakai fallback yang diminta pengguna, tetap **usulan awal**, bukan shortcut final yang disetujui tim. Terminal draggable di kiri mengikuti arah visual terpilih, tetapi seluruh bentuk/warna/font/teks masih placeholder. Label jelas “Kode belum dieksekusi”; tidak ada Run palsu, runtime, hasil atau skor.
- **Verifikasi aktual:** kompilasi seluruh main/test `core`+`lwjgl3` lulus dengan Oracle JDK 25.0.4.1, `javac --release 21`, jar libGDX 1.14.2/LWJGL 3.4.3 dari Maven Central sesuai metadata/constraint existing. Eksekusi Java langsung: **40 assertion kontrol lulus**, **29 assertion desktop OpenGL/Scene2D lulus**. Launcher existing juga dibuka terpisah, habitat dilihat lewat Computer Use lalu ditutup normal. Smoke memeriksa Screen/input/Stage, hint, inspeksi, gerak modal, fokus TextArea/pengetikan, Esc, reopen/draf, resize 1440×900/900×600/640×480 dan pelepasan processor. Screenshot 1280×720 inspeksi/terminal serta resize di `lwjgl3/build/reports/habitat/`; inspeksi dan terminal 1440×900/640×480 dilihat langsung. Detail command alternatif di VERIFICATION. Bukan bukti build Gradle, JDK 21 runtime, browser, backend atau puzzle.
- **Batas bukti:** input smoke dikirim melalui processor antar-frame, bukan tahan tombol OS; tap Computer Use tidak memberi bukti gerak yang cukup. Keyboard fisik tahan/diagonal, drag manual, tombol kembali via mouse, clipboard, seleksi/scroll panjang, perpindahan fokus OS, kontras dan keterbacaan minimum belum lulus manual. Pada 640×480 teks mengecil karena letterbox; ukuran minimum belum dipilih. Belum ada playtest manusia/mesin kedua. EXP-02/M-05 dan M-14 integrasi penuh tetap terbuka; M-02/M-03/backend/puzzle/M-24 tidak dicentang.
- **Serah visual ke Antigravity:** edit `HabitatRenderer.java` untuk bentuk/sprite/layer/label, `HabitatUi.java` untuk widget/layout/skin, dan aset baru di `assets/` dengan asal/lisensi. `HabitatScreen.java` pemilik lifecycle dan dua viewport; koordinasikan perubahan wiring. Hindari mengubah `HabitatWorld.java`/`HabitatInput.java` atau test perilaku hanya demi mencocokkan gambar. Pertahankan collider/state sebagai otoritas, gerak berdelta/normalisasi, jangkauan/hint, latch E, Esc sebelum editor, penguncian gerak sepanjang modal, draf ketika tutup, pengembalian kontrol, resize serta dispose/input cleanup. Gerak/waktu astronaut tidak boleh menjadi biaya/skor robot. Jika peta/collider perlu berubah, catat dan perbarui test terkait secara eksplisit.
- **Langkah berikutnya:** selesaikan unduhan Wrapper/daemon JDK 21 dan jalankan task build/test nyata tanpa upgrade. Lalu lakukan pemeriksaan keyboard/mouse/editor manual di desktop, pilih ukuran minimum, dan lanjutkan visual terpisah dari aturan kontrol. Integrasi puzzle/runtime/backend memerlukan tugas serta kontrak berikutnya, tidak otomatis diotorisasi handoff ini.

### 12 November 2026 — Visual adegan habitat representatif, panel inspeksi, dan floating terminal pada libGDX existing (Antigravity)

- **Tanggal / alat / model aktual:** 12 November 2026 / Antigravity / Gemini 4 Argon.
- **Target (prototype web atau Mars) dan tugas aktif:** Target Mars (`mars_reboot.py`), kelanjutan subset visual `M-14` / fondasi adegan contoh `M-24` pada proyek Java/libGDX existing (`C:\Users\abdie\OneDrive\Desktop\Folder_Abdiel\PRAKTIKUM\FINDIH\MarsReboot`). Merancang dan mengimplementasikan visual satu adegan habitat representatif beserta panel inspeksi dan terminal sementara di atas baseline kontrol Codex tanpa menambah gameplay, runtime Python, backend, akun, puzzle baru, atau sistem survival.
- **Branch / worktree / commit dasar / edit belum di-commit:**
  - Repo dokumentasi (`C:\Users\abdie\OneDrive\Documents\ChatGPT\FinalProjectOOP`): branch `master` (belum ada commit). File yang diubah: `DESIGN.md`, `VERIFICATION.md`, dan `TASKS.md`. Source prototype web tidak disentuh.
  - Proyek Java (`C:\Users\abdie\OneDrive\Desktop\Folder_Abdiel\PRAKTIKUM\FINDIH\MarsReboot`): bukan repository Git. Tidak ada commit/push/deployment atau instalasi global dilakukan.
- **Hasil dan file yang berubah:**
  - `MarsReboot/core/src/main/java/com/marsreboot/habitat/HabitatRenderer.java`: mengganti blockout persegi panjang datar menjadi adegan 2D top-down 3/4 dengan `SpriteBatch` (`TextureFilter.Nearest`) + `ShapeRenderer`. Menampilkan tanah regolith Mars di luar modul, dinding hull 3/4 titanium di sekeliling `FLOOR` (`40,40,880,520`), ubin lantai interior `32×32`, pipa saluran air/hidroponik lantai yang menghubungkan Panel Air → Terminal → Kebun, efek pencahayaan 2D lembut ala *Pathway* (`glBlendFuncSeparate` agar alpha framebuffer tetap `1.0`), bracket sudut interaktif pada fasilitas (`PANEL AIR [E]`, `TERMINAL ROBOT [E]`), serta penempatan badge dunia yang adaptif sehingga tidak pernah bertabrakan dengan badge `ASTRONAUT`.
  - `MarsReboot/core/src/main/java/com/marsreboot/habitat/HabitatUi.java`: membangun HUD atas (`y=566..600`) dan bawah (`y=0..34`) di luar lantai `FLOOR` sehingga 100% area jalan tidak tertutup; menyusun ulang `Window` inspeksi (`590×384`) dengan 3 kartu informasi terstruktur, badge fokus keyboard, dan tombol `Kembali ke habitat [Esc]`; menyusun ulang `Window` floating draggable terminal (`556×492` di kiri `x=36, y=54`) dengan NinePatch tajam, badge `[!] Kode belum dieksekusi`, `Robot/Python belum aktif`, `FOKUS: EDITOR AKTIF`, ringkasan temuan + kandidat API, gutter nomor baris `1..8`, `TextArea` berbingkai fokus cyan `2px` (`#67C5D6`), baris status konsol pasif, dan tombol `Kembali ke habitat [Esc]`. Posisi kursor awal draf diletakkan di akhir teks agar pengetikan baru tidak merusak baris komentar pertama.
  - Aset pixel-art placeholder konsisten baru di `MarsReboot/assets/habitat/` (dibuat prosedural oleh script internal proyek `scratch/generate_habitat_assets.py` tanpa aset pihak ketiga, lisensi internal proyek):
    - `mars_soil_tile.png` (`32×32`), `floor_tile.png` (`32×32`), `partition_wall.png` (`32×200`), `water_panel.png` (`100×40`), `robot_terminal.png` (`100×52`), `hydro_garden.png` (`180×100` dengan indikator non-warna `[!]` retak kering, tetesan air, tunas muda, tanaman matang), `astronaut_eva.png` (`24×28`), serta `asset-manifest.json` dan daftar di `MarsReboot/assets/assets.txt`.
  - `MarsReboot/README.md` dan `MarsReboot/lwjgl3/build/reports/habitat/source-sha256.txt` diperbarui.
- **Keputusan pengguna + alasan / usulan yang belum dipilih:**
  - **Dipilih (diikuti):** arah visual crisp pixel art 2D top-down 3/4 (`TextureFilter.Nearest`, referensi *MewnBase* / *The Martian* / *Pathway*) dan tata letak floating draggable terminal di sisi kiri (*The Farmer Was Replaced* style) sesuai `DESIGN.md` dan referensi Figma (`z5Q44g0NLnulWXFMaA4Hlz` board `03`, `04`, `05c`).
  - **Usulan (diterapkan sebagai kandidat adegan representatif, belum disetujui final):** skala grid `32×32`, nilai hex & kontras terukur di `DESIGN.md` §8, aset berlabel `placeholder_consistent` di `assets/habitat/`, serta penempatan HUD top/bottom bar di luar `FLOOR`.
- **Pemilik file aktif dan kontrak yang terdampak:**
  - Peran 3 (Dunia/UI) mengedit `HabitatRenderer.java`, `HabitatUi.java`, dan `assets/habitat/`.
  - **Kontrak milik peran 1/Codex tidak disentuh (SHA-256 tetap identik):** `HabitatWorld.java`, `HabitatInput.java`, `HabitatScreen.java`, `MarsRebootGame.java`, `Lwjgl3Launcher.java`, `HabitatControlTest.java`, dan `HabitatDesktopSmokeTest.java`. Seluruh dimensi collider, posisi fasilitas, `INTERACT_MARGIN = 18f`, field refleksi (`inspection`, `terminal`, `editor`), dan ukuran kanvas virtual `960×600` dipertahankan 100%.
- **Verifikasi aktual (lulus / gagal / belum dijalankan + bukti):**
  - Kompilasi `core` + `lwjgl3` (`javac --release 21 -encoding UTF-8 -Xlint:deprecation`): **PASS** (0 error, 0 warning pada kelas yang diedit).
  - `com.marsreboot.habitat.HabitatControlTest`: **PASS (40 assertion)**.
  - `com.marsreboot.habitat.HabitatDesktopSmokeTest`: **PASS (29 assertion)** pada window OpenGL/LWJGL3 nyata.
  - Bukti visual desktop aktual di `MarsReboot/lwjgl3/build/reports/habitat/`: `exploring.png` (`1280×720`), `inspection.png` (`1280×720`), `terminal.png` (`1280×720`), `terminal-1440x900.png` (`1440×900`), `terminal-900x600.png` (`900×600`), dan `terminal-640x480.png` (`640×480`) diperiksa langsung. Tidak ada kontrol atau teks yang terpotong pada `1280×720`, `1440×900`, dan `900×600`; kebun hidroponik dan astronaut di sisi kanan tetap terlihat jelas saat terminal terbuka di kiri.
- **Blocker / risiko / pertanyaan terbuka:**
  - Distribusi Gradle Wrapper 9.8.0 (`.\gradlew.bat`) masih timeout saat mengunduh dari jaringan lingkungan sehingga kompilasi/test tetap dijalankan langsung via JDK 25 (`--release 21`) dan jar lokal `.gradle-local/manual/*`.
  - Pada ukuran sangat kecil (`640×480`), seluruh window dan tombol tetap berada di dalam kanvas virtual dan berfungsi, tetapi `BitmapFont` bawaan (`default.fnt`) mengalami artefak downscaling; perlu penggantian font monospace/UI beresolusi lebih tajam dan penetapan ukuran minimum window pada `M-05`.
  - Pengujian tahan tombol keyboard fisik OS jangka panjang, drag window manual dengan mouse oleh manusia, serta seleksi/clipboard/scroll teks panjang belum diverifikasi secara manual.
- **Bagian yang perlu direview Codex / langkah berikutnya:**
  1. Review `HabitatRenderer.java` dan `HabitatUi.java` untuk memastikan cara pembersihan resource (`dispose()` untuk tekstur sprite dan NinePatch prosedural) serta clamping posisi `terminal` (`draw()` & `resize()`) selaras dengan rencana integrasi `EXP-02` / `M-11` / `M-15`.
  2. Konfirmasi apakah penempatan kursor awal `editor.setCursorPosition(editor.getText().length())` di `HabitatUi` diinginkan saat draf dimuat ulang pada integrasi runtime nanti.
  3. Jalankan verifikasi manual di mesin pengguna (`.\gradlew.bat :lwjgl3:run` setelah jaringan/distribusi tersedia, atau command Java langsung di `VERIFICATION.md`) untuk menguji tahan tombol fisik dan drag mouse manual sebelum melanjutkan ke `M-04`/`M-05`/`M-10`.

### 12 November 2026 — Upgrade Desain Outpost Permukaan Mars, Layar UI Lengkap, & Sinkronisasi PNG ke `docs/mars/design-refs/` (Antigravity)

- **Tanggal / alat / model aktual:** 12 November 2026 / Antigravity + Figma MCP + Image Generator / Gemini 3.8 Flash.
- **Target dan tugas aktif:** Target Mars (`mars_reboot.py`). Menjawab evaluasi UI sebelumnya dengan merancang konsep **Outpost Permukaan Mars** (astronaut berinteraksi di luar ruangan antara `Hab Utama` ↔ `Bio-Dome Base` terbuka ↔ `Stasiun Pompa`), melengkapi layar UI/UX yang belum ada, membuat target *Art Direction* pixel art, dan menyinkronkan seluruh `.png`/`.jpg` ke `docs/mars/design-refs/` agar terbaca langsung oleh Codex. Tidak ada kode aplikasi Java/libGDX maupun prototype web yang diubah pada sesi ini.
- **Branch / perubahan repo:** `master` (belum ada commit). File yang ditambah/diubah di `FinalProjectOOP`:
  - `docs/mars/design-refs/`: 14 file visual (`00_art_direction_mars_outpost.jpg`, `01`–`06` + `05c`/`05d`, dan `07`–`11`).
  - `DESIGN.md` (§10), `PROJECT.md` (§11), dan handoff `TASKS.md`.
- **Artefak Figma (`z5Q44g0NLnulWXFMaA4Hlz`):** ditambahkan node `9:2` (`00_Art_Direction_Mars_Outpost.jpg`), `9:3` (`07_Screen_Outpost_Before_After`), `9:170` (`08_Screen_SciFi_Floating_Terminal_HUD`), `9:321` (`09_Screen_Terminal_Error_And_Help_Drawer`), `10:2` (`10_Screen_Field_Log_And_Mission_Map_v2`), dan `9:536` (`11_Screen_Settings_Modal`).
- **Verifikasi aktual:**
  - Seluruh 5 layar baru (`07`–`11`) dan *Art Direction* (`00`) diverifikasi visual melalui `get_screenshot` (`1024×576`) dan `read_file`; cacat entity/wrapping pada `10` diperbaiki di `10:2` dan diverifikasi ulang.
  - Seluruh 14 file di `docs/mars/design-refs/` terkonfirmasi ada dan ditautkan di `DESIGN.md` §10. Test aplikasi Java/web tidak dijalankan karena tidak ada perubahan kode.
- **Langkah berikutnya:**
  1. Review referensi `docs/mars/design-refs/00_art_direction_mars_outpost.jpg` dan `07`–`11` sebagai acuan iterasi visual berikutnya di `MarsReboot` (`HabitatRenderer` / `HabitatUi`) maupun integrasi `M-05`/`M-14`/`M-24`.
  2. Lanjutkan keputusan teknis `M-01` (Python/runtime) & `M-09` (kontrak Java–Spring Boot) sebelum membuka milestone implementasi terkait.

### 12 November 2026 — Implementasi Desain Outpost Permukaan Mars & Layar UI Lengkap ke `MarsReboot` libGDX (Antigravity)

- **Tanggal / alat / model aktual:** 12 November 2026 / Antigravity / Gemini 4 Argon.
- **Target dan tugas aktif:** Target Mars (`mars_reboot.py`). Menerapkan rencana apply_mars_outpost_plan.md (catatan lokal historis, tidak dibundel) dan desain `docs/mars/design-refs/` (`00`, `07`–`11`) ke proyek Java/libGDX `MarsReboot` (`C:\Users\abdie\OneDrive\Desktop\Folder_Abdiel\PRAKTIKUM\FINDIH\MarsReboot`) dengan menjaga 100% kompatibilitas kontrak `HabitatWorld`, `HabitatInput`, `HabitatScreen`, `HabitatControlTest`, dan `HabitatDesktopSmokeTest`.
- **Hasil dan file yang berubah:**
  - `MarsReboot/core/src/main/java/com/marsreboot/habitat/HabitatRenderer.java`: mengubah tampilan ruangan tertutup menjadi **Outpost Permukaan Mars** terbuka (regolith berdebu, kawah, batu Mars dengan bayangan searah ala *Pathway*, jejak ban rover, **Hab Airlock + Panel Surya**, **Spine Pipa**, **Open Hydroponic Bio-Dome Platform 4×4** dengan kubah transparan parsial + tangki air + **Mars Rover**, **Stasiun Pompa Utama Misi 3**, serta pipa yang menyala cyan/hijau saat mode *After*).
  - `MarsReboot/core/src/main/java/com/marsreboot/habitat/HabitatUi.java`: menambahkan Top/Bottom HUD Sci-Fi dengan tombol & pintasan `[B] BEFORE/AFTER`, `[J] Log` (*Field Log Astronaut*), `[M] Misi` (*Peta Misi & Rekor 1–9*), `[O] Setelan` (*Modal Pengaturan Audio & Reduced Motion UI-05*), serta memperbarui *Floating Draggable Terminal* dengan chip API, tombol `? BANTUAN (3)` (*Drawer Bantuan Bertahap 3 Langkah · FR-020*), dan preview state *Normal / Error / Timeout / Stopped* (kode editor tetap aman).
  - `MarsReboot/assets/habitat/`: 11 PNG pixel art (`mars_soil_tile.png`, `floor_tile.png`, `partition_wall.png`, `water_panel.png`, `robot_terminal.png`, `hydro_garden.png`, `hydro_garden_restored.png`, `hab_airlock.png`, `pump_station.png`, `mars_rover.png`, `astronaut_eva.png`), `asset-manifest.json`, dan `MarsReboot/assets/assets.txt`.
  - Kontrak milik Codex tidak disentuh (`HabitatWorld.java`, `HabitatInput.java`, `HabitatScreen.java`, `HabitatControlTest.java`, `HabitatDesktopSmokeTest.java` memiliki SHA-256 identik di `lwjgl3/build/reports/habitat/source-sha256.txt`).
- **Verifikasi aktual:**
  - `javac --release 21 -encoding UTF-8 -Xlint:deprecation`: **PASS** (0 error, 0 warning).
  - `com.marsreboot.habitat.HabitatControlTest`: **PASS (40 assertion)**.
  - `com.marsreboot.habitat.HabitatDesktopSmokeTest`: **PASS (29 assertion)**.
  - Screenshot desktop OpenGL `1280×720` di `MarsReboot/lwjgl3/build/reports/habitat/` (`exploring.png`, `exploring-after.png`, `inspection.png`, `terminal.png`, `terminal-error-help.png`, `field-log.png`, `mission-map.png`, `settings.png`) serta ukuran resize (`1440×900`, `900×600`, `640×480`) diperiksa langsung tanpa ada teks HUD/terminal yang terpotong.
- **Langkah berikutnya:**
  1. Jalankan `Lwjgl3Launcher` secara interaktif untuk menguji klik/shortcut `[B]`, `[J]`, `[M]`, `[O]`, dan `? BANTUAN (3)` secara langsung.
  2. Lanjutkan keputusan kontrak `M-01` (eksekusi Python CPython NDJSON) dan `M-09` (integrasi backend Spring Boot) bersama Codex.


### 13 November 2026 — Panduan handoff backend untuk anggota tim (Codex)

- **Permintaan/hasil:** pengguna membahas pembagian backend dan penyelarasan konteks/skill. Dibuat BACKEND_HANDOFF.md dan tautan README; backend dimaknai Spring Boot untuk akun/skor, terpisah dari logika game Java/runtime. Handoff lama tetap dipertahankan.
- **Usulan, belum keputusan pembagian:** satu developer backend utama dan reviewer bergilir; bila dua developer, pisahkan autentikasi dan skor setelah kontrak/skema bersama. Nama anggota, ownership nyata, lokasi backend dan commit dasar belum ditetapkan. Tidak ada keputusan runtime/database/sesi baru.
- **Skill/konteks:** gunakan paket .agents/skills beserta resource, SKILLS.md dan skills-lock.json yang sama; utamakan kontrak ARCHITECTURE dan contoh/test bersama. Tidak ada skill dipasang/disalin atau penemuan aplikasi teman yang diuji pada sesi ini.
- **Ownership/status/verifikasi:** hanya BACKEND_HANDOFF.md, README.md, dan tambahan TASKS.md; branch master belum memiliki commit. Panduan dan tautan lokal diperiksa; tidak menjalankan test aplikasi atau bootstrap, mengubah source Java/web, membuat commit/push, atau deployment. Hasil pemeriksaan source/handoff sebelumnya bukan test baru.
- **Langkah berikutnya:** pilih pemilik/reviewer dan lokasi backend, isi satu tugas pada prompt panduan; selesaikan kontrak M-09 sebelum implementasi M-12/13. Kontrak draft, metode autentikasi/CSRF, versi toolchain/database serta integrasi nyata tetap perlu ditutup/dibuktikan.


### 13 November 2026 — Konsolidasi konteks tim ke repository libGDX (Codex)

- **Keputusan pengguna:** dokumentasi, konteks, skill dan panduan/prompt tim disatukan dengan source libGDX agar satu clone memberi konteks yang sama. Root repo ini menjadi sumber utama; workspace FinalProjectOOP lama cadangan, bukan tempat mengedit spesifikasi paralel.
- **Hasil/ownership:** dokumen root, docs/referensi visual/SOP, skill lengkap dan lockfile dikonsolidasikan; README libGDX digabung dan versi sebelumnya diarsipkan pada docs/setup. TEAM_GUIDE dan TEAM_PROMPTS menjelaskan planning, empat peran, urutan tugas, Git/review serta prompt umum/peran/reviewer. Referensi source web di prototype-web terpisah tanpa cache/node_modules. AGENTS dan tautan disesuaikan; nama orang belum diisi, usulan teknis tidak otomatis dipilih.
- **Batas:** tidak mengubah source Java/aset game/dependency Gradle, menyalin .git, cache/credential, menjalankan build game, menginstal tool, atau membuat commit/push/deployment. File lama dipertahankan untuk rollback; setelah publikasi konteks lewat Git barulah clone teman mendapat file.
- **Verifikasi:** manifest SHA-256 snapshot konteks disiapkan; pemeriksaan link/anchor, ID dan resource skill serta hash source/konfigurasi existing dilakukan pada proses transfer. Hasil aktual dan batas ada pada VERIFICATION. Discovery skill pada laptop teman, build Wrapper serta backend belum diuji oleh sesi ini.
- **Langkah berikutnya:** koordinator meninjau diff dan pembagian nama/peran, memberi otorisasi publikasi Git bila siap; teman membaca TEAM_GUIDE dan memakai TEAM_PROMPTS di root clone. Mulai audit status/source lalu satu M-ID, bukan seluruh backlog. Riwayat path absolut pada entri lama hanya bukti historis, bukan prasyarat komputer anggota.


### 13 November 2026 — Tabel progres pada README GitHub (Codex)

- **Permintaan/hasil:** pengguna meminta daftar pekerjaan dan status langsung pada README. Ditambahkan tabel ringkasan dengan simbol selesai/sebagian/belum/terhambat, peran pemilik, ID backlog dan bukti/sisa pekerjaan; diposisikan sebelum petunjuk menjalankan game.
- **Dasar status:** TASKS dan VERIFICATION existing; hasil sempit habitat/terminal sementara dibedakan dari penerimaan milestone penuh. Preview akun/hasil/Before–After tidak dianggap runtime, backend atau progres nyata. Tidak ada perubahan checkbox milestone atau persetujuan usulan teknis.
- **Ownership/verifikasi:** hanya README.md dan tambahan handoff TASKS.md; tautan/anchor Markdown, susunan kolom, status dan referensi M-ID diperiksa. Hasil historis tidak dijalankan ulang; tidak ada render GitHub, test aplikasi, perubahan source/dependency, commit/push/deployment.
- **Langkah berikutnya:** pemilik tiap tugas memperbarui ringkasan bersama bukti pada TASKS/VERIFICATION. Tabel terlihat di repo GitHub setelah perubahan lokal dipublikasikan melalui Git sesuai otorisasi; bukan klaim sudah di-push oleh sesi ini.


### 13 November 2026 — GIF banner README (Codex)

- **Permintaan/hasil:** memasang mars_reboot_banner.gif milik pengguna di bawah judul README. GIF ditemukan pada workspace dokumentasi lama dan disalin tanpa edit ke docs/mars/design-refs/mars_reboot_banner.gif pada repo utama; referensi README memakai path relatif agar ikut clone/GitHub.
- **Ownership/verifikasi:** README.md, tambahan TASKS.md, dan satu GIF; signature/dimensi GIF serta SHA-256 salinan diperiksa, referensi lokal tersedia, dan isi README lainnya dipertahankan persis. Tidak mengubah gameplay, tabel status, dependency, atau aset lain. Tidak mengklaim render/animasi GitHub telah dilihat; tidak menjalankan test aplikasi.
- **Langkah berikutnya:** review lalu commit/push sesuai otorisasi untuk menampilkan banner di GitHub. Tidak ada commit/push/deployment dilakukan sesi ini; file sumber lama tetap disimpan.

### 13 November 2026 — Implementasi Tahap 1 (Hi-DPI UI) & Tahap 2 (Grafis Outpost 2× Pixel Art) (Antigravity)

- **Tanggal / alat / tugas aktif:** 13 November 2026 / Antigravity / Menjalankan rencana peningkatan grafis & ketajaman UI (`ui_graphics_polish_roadmap.md`) di repository terpadu `MarsReboot` sambil menjaga 100% kontrak `HabitatWorld`, `HabitatInput`, `HabitatScreen`, `HabitatControlTest`, dan `HabitatDesktopSmokeTest`.
- **Hasil dan file yang berubah:**
  - `assets/ui/ui_sans.fnt/.png`, `assets/ui/ui_sans_bold.fnt/.png`, `assets/ui/ui_mono.fnt/.png`: baked BMFont anti-aliased `30px` (`Segoe UI`, `Segoe UI Semibold`, `Consolas`, ASCII + simbol UI `· • → ← ↑ ↓ × — – ■ ▶ … °`) dimuat dengan `TextureFilter.MipMapLinearLinear` & scaling sub-pixel.
  - `core/src/main/java/com/marsreboot/habitat/HabitatUi.java`: mengganti `BitmapFont` `15px` bawaan dengan `uiFont`, `uiBoldFont`, dan `monoFont` (editor + nomor baris + chip API), mengganti box `12×12` kasar menjadi `NinePatch` 3× supersampled mipmapped (`scale 1/3`), tanpa mengubah ukuran virtual stage `960×600` yang disyaratkan smoke test.
  - `assets/habitat/` (30 PNG `2×` crisp pixel art + `light_glow.png` + `asset-manifest.json`) & `assets/assets.txt`: regolith Mars 4 varian (`mars_soil_0..3.png`), lantai terpadat (`floor_tile.png`), kawah/batu/peti kargo/walkway, `hab_airlock.png`, `solar_array_dusty/clean.png`, `water_panel.png`, `robot_terminal.png`, `pump_station.png`, `biodome_deck(_restored).png`, `biodome_canopy.png`, `hydro_garden(_restored).png`, `mars_rover.png`, dan sprite sheet astronaut 4 arah × 4 frame (`astronaut_eva_sheet.png`).
  - `core/src/main/java/com/marsreboot/habitat/HabitatRenderer.java`: merender tile regolith multivarian, properti permukaan, walkway kisi, jalur pipa berbayang + aliran animasi saat mode pulih, sprite struktur/Bio-Dome `2×` selaras collider, animasi jalan astronaut 4 arah, pencahayaan radial aditif dengan reset warna `SpriteBatch` yang aman, dan badge dunia menggunakan `ui_sans_bold`.
  - Seluruh 8 file kontrak non-visual milik Codex memiliki SHA-256 identik dengan baseline (`lwjgl3/build/reports/habitat/source-sha256.txt`).
- **Verifikasi aktual:**
  - `javac --release 21 -encoding UTF-8 -Xlint:deprecation`: **PASS** (0 error, 0 warning pada file proyek).
  - `HabitatControlTest`: **PASS (40 assertion)**.
  - `HabitatDesktopSmokeTest`: **PASS (29 assertion)** pada window OpenGL/LWJGL3 nyata (`1280×720`, `1440×900`, `900×600`, `640×480`).
  - Screenshot OpenGL di `lwjgl3/build/reports/habitat/` (`exploring.png`, `inspection.png`, `terminal.png`, `terminal-1440x900.png`, `terminal-900x600.png`, `terminal-640x480.png`) diverifikasi langsung secara visual.
- **Sisa pekerjaan / blocker:**
  - `.\gradlew.bat` tetap belum diuji karena unduhan Gradle 9.8.0 offline terhalang; verifikasi interaktif mouse drag, scroll editor panjang, dan penekanan keyboard fisik OS jangka panjang perlu dicek manual (`Lwjgl3Launcher`).
  - Lanjut koordinasi kontrak `M-01` (runtime Python CPython) dan `M-09` (Spring Boot) sebelum implementasi gameplay robot/puzzle.

### 13 November 2026 — Codex: domain robot dan kandidat puzzle 1

- **Lingkup:** melanjutkan implementasi game dengan satu potongan domain Java, tanpa mengganti visual/kontrol Antigravity. Usulan p1-a dipakai sebagai eksperimen, bukan keputusan final aturan atau klaim Python disetujui aslab. Target tetap 9 puzzle/3 misi, tahap awal 1–4.
- **File pemilik:** baru `core/src/main/java/com/marsreboot/robot/RobotSimulation.java` dan `core/src/test/java/com/marsreboot/robot/RobotSimulationTest.java`; `core/build.gradle` menambah task robotTest pada check tanpa dependency baru. Dokumen status, aturan, arsitektur, rencana uji dan panduan diselaraskan. Source habitat/launcher, aset dan versi tetap identik dengan baseline awal sesi.
- **Hasil:** gerak empat arah/tepi/penghalang, panen/sensor, immutable fixture/snapshot, biaya aksi/API, reset deterministik dan evaluasi akhir normal untuk p1-a. Tidak otomatis sukses saat target tercapai; error/limit tetap gagal. Belum ada water, runtime, replay, skor, persistensi atau integrasi terminal.
- **Bukti aktual:** test awal gagal karena kelas belum ada; test tambahan batas gagal sebelum guard ditambahkan, lalu lulus. `javac --release 21 -encoding UTF-8 -Xlint:all` + test langsung lulus. `.\gradlew.bat --offline :core:robotTest :core:controlTest :lwjgl3:compileJava --console=plain` exit 0: **81 assertion robot + 40 kontrol**. `.\gradlew.bat --offline :lwjgl3:jar --console=plain` exit 0; JAR berisi RobotSimulation. Wrapper 9.8.0 sekarang tersedia dari cache; laporan timeout sebelumnya tetap historis. Launcher JVM Oracle 25.0.4.1, source/target 21 dan konfigurasi daemon existing tidak diubah. Rincian di VERIFICATION §7.
- **Batas:** belum menjalankan ulang desktopSmokeTest/window, komputer kedua, Python/backend atau playtest; menjalankan game masih membuka habitat existing. M-10/M-16 tetap belum dicentang penuh. Tidak ada commit/push/deployment.
- **Berikutnya:** peran 1+2 meninjau kandidat fixture/kontrak snapshot; peran 3 menghubungkan presentasi robot/hasil ke snapshot tanpa menyalin aturan. Pilih dan buktikan runtime lewat M-04 sebelum Run terminal mengeksekusi source pemain; baru hubungkan coordinator/Stop/replay M-11. Jangan memakai urutan Java hardcode sebagai bukti Python atau penerimaan alur penuh.

### 13 November 2026 — Codex: integrasi puzzle 1, CPython dan paket Windows

- **Permintaan/keputusan pengguna:** implementasikan rencana penuh puzzle 1 sampai kode Python nyata; bundel CPython pada game Windows x64. Python 3.15.2 dipin URL/SHA-256 tanpa instalasi global, Java 21+ tetap prasyarat. Konfirmasi aslab tidak direka; backend/puzzle 2–9/save lintas restart di luar tugas ini.
- **Ownership dan file:** package runtime baru (RunCoordinator, RuntimeProtocol, PythonRuntime), assets/python/runner.py, manifest runtime, tiga test baru, task Gradle preparePython/runtimeTest/pythonIntegrationTest/puzzleDesktopTest/bundleWindows, distribusi Windows. HabitatScreen/Ui/Renderer disambungkan tanpa mengubah domain robot, kontrol/collider, launcher atau aset visual lama. Dokumentasi aktual diselaraskan; rincian kontrak di docs/mars/PYTHON_RUNTIME.md.
- **Hasil:** source snapshot → proses Python → request tervalidasi → simulasi Java → replay/robot/panen/hasil → selesai sesi. Stop/Reset menolak hasil lama, draf dipertahankan, Esc tidak membatalkan Run. API hanya move/harvest/can_harvest/get_pos. Preview visual dan rancangan peta tidak memberi progres/rekor palsu.
- **Verifikasi:** awal test gagal karena runtime belum diimplementasikan, kemudian referensi Python lulus. Robot 81, kontrol 40, controller/protokol 33, integrasi Python 33, desktop existing 29 dan desktop puzzle 14 assertion lulus. Uji baru menemukan callback resize reentrant pada harness; urutan fase test diperbaiki dan lulus. Kasus nonzero exit setelah finished, startup deadline dan resolusi executable langsung IntelliJ turut diuji.
- **Paket:** Wrapper offline berhasil membuat ZIP. Ekstraksi ke folder spasi; JAR paket + CPython paket lulus kembali 33 integrasi dan 14 desktop assertion dengan PATH C:\Windows\System32. JDK proses pemeriksaan paket adalah Eclipse Adoptium 21 dari cache Gradle. Hash/hasil/screenshot di VERIFICATION §8 dan lwjgl3/build/reports/puzzle/package-smoke.json.
- **Batas:** komputer kedua, playtest manusia, persetujuan aslab, penerimaan editor penuh, runtime untuk script bermusuhan, backend dan puzzle lain belum selesai. 640x480 tidak clipping tetapi teks kecil; review keterbacaan masih perlu. Tidak ada commit/push/deployment. Task M-04/06/10/11/15/16 tetap parsial terhadap gate produk yang lebih luas.
- **Berikutnya:** tim coba ZIP dan review fixture/UX p1; konfirmasi bahasa ke aslab. Pemilik 1 lanjut siram/multi-skenario, 2 menyiapkan konten/test p2–4, 3 memperbaiki UX berdasar observasi, 4 mengerjakan backend sesuai kontrak. Baca handoff ini sebelum mengubah runtime/terminal; jangan bootstrap ulang atau mengembalikan UI ke contoh hardcode.

### 14 November 2026 — Penyempurnaan UI, keterbacaan `18` unit UI, dan alur Puzzle 1 (Antigravity)

- **Ruang lingkup & kontrak:** Hanya menyentuh lapisan visual/UI (`HabitatUi`, `HabitatRenderer`, font BMFont `assets/ui/`, dan `PuzzleDesktopSmokeTest` serta dokumen handoff). `RunCoordinator.View`, `RobotSimulation`, `PythonRuntime`, protokol NDJSON, `HabitatWorld`, `HabitatInput`, `HabitatScreen`, `HabitatControlTest`, `HabitatDesktopSmokeTest`, dan `prototype-web/` tidak diubah.
- **Perubahan utama:**
  - Font BMFont `ui_sans`/`ui_sans_bold` (`36px`, skala `0.50f`) dan `ui_mono` (`34px`, skala `0.53f`) dipangkas leading atasnya (~`18` unit UI / `~12px` di `640×480`) dan seluruh `Label`/`Table` pada `inspection` (`680×424`), `terminal` (`556×518` di `(12,42)`), `helpDrawer`, `fieldLogWindow`, `missionMapWindow`, serta `settingsWindow` diberi batas lebar/wrap agar bebas clipping.
  - Alur P1 tersambung penuh: `inspection` menjelaskan dua tanaman matang `(0,0)` & `(1,0)`, start `(0,0)`, tujuan panen, serta batas 6 aksi tanpa teleportasi saat tutup; terminal berurutan tujuan/status → API 2 kolom → editor scroll (`setPrefRows(4)` + gutter baris terlihat `getFirstLineShowing()`/`getLinesShowing()`) → `Run`/`Stop`/`Reset` → output scroll berbatas (`76` unit) → ringkasan sukses (`Coba lagi` mereset dengan draf & pencapaian sesi utuh, `Kembali ke habitat`); pesan gagal/batas aksi/timeout/Stop dibedakan dan tetap menampilkan pesan/baris runtime.
  - `MissionMap` menampilkan status nyata P1 dari `RunCoordinator.View` (`TERSEDIA` / `SEDANG DIJALANKAN` / `SELESAI`, tetap `SELESAI` saat ulang atau gagal setelah lulus) serta P2–9 jujur `BELUM TERSEDIA`. `Preview Visual` (Before/After) dipindahkan dari HUD ke `[O] Setelan`; Bio-Dome menampilkan petak panen `2×1` (`74×64`), `roverTexture` vs tanaman matang/kosong, koordinat `(0,0)`/`(1,0)`, dan penanda lokal `PANEN P1 SELESAI (SESI)` tanpa menutupi rover/tanaman.
- **Verifikasi aktual (14 Nov 2026):** `.\gradlew.bat --offline :core:controlTest :core:robotTest :core:runtimeTest :core:pythonIntegrationTest :lwjgl3:desktopSmokeTest :lwjgl3:puzzleDesktopTest --console=plain` **lulus seluruhnya** (`40` kontrol, `81` robot, `33` runtime, `33` integrasi Python bundel, `29` desktop smoke, `41` desktop puzzle + pemeriksaan visual PNG `reports/habitat/` & `reports/puzzle/` termasuk `1440×900`, `900×600`, `640×480`, kode/output panjang, help/dragged, error, Mission Map before/after, dan Setelan preview). Lihat `VERIFICATION.md` §9.
- **Batas & berikutnya:** `:lwjgl3:bundleWindows` belum dibangun ulang dengan perubahan UI 14 Nov; playtest manusia (drag mouse/hold tombol OS), komputer kedua, save lintas restart, backend Spring Boot, serta puzzle 2–9 masih terbuka. Tidak ada commit, push, atau deployment.

### 15 November 2026 — Desain Ulang Outpost 1680×1040, FollowCamera, Klik Rover Langsung & Terminal Resizable Non-Kotak (Antigravity)

- **Permintaan & keputusan:** menindaklanjuti persetujuan pengguna atas `implementation_plan.md` untuk menghapus kesan kotak-kotak/terpisah: memperluas dunia agar seluruh peta tidak tampil sekaligus, menyatukan interaksi terminal langsung lewat klik Rover di Bio-Dome, membuat UI non-kotak lebih halus + terminal ringkas yang bisa di-resize, serta memperhalus gerak pemain dan animasi rover.
- **File pemilik & perubahan:**
  - `assets/habitat/*.png` (30 aset vektor halus 2D anti-aliased) & `assets/habitat/asset-manifest.json` (`smooth_organic_vector_2d_v3`): memperbarui regolith, konservatori Bio-Dome organik `960×640`, `mars_rover.png` `128×128`, `astronaut_eva_sheet.png` `256×256` 4 arah.
  - `core/src/main/java/com/marsreboot/habitat/HabitatWorld.java` & `FollowCamera.java` (baru): dunia `1680×1040`, kamera `720×450` dengan deadzone + look-ahead + exponential damping + clamping + *smart terminal framing*, gerak deterministik sub-step (`1/120s`) berakselerasi/deselerasi halus, penggabungan terminal ke klik/interaksi Rover & Panel, serta hit-test koordinat Rover/petak bersama.
  - `core/src/main/java/com/marsreboot/habitat/HabitatInput.java` & `HabitatScreen.java`: pemisahan `ExtendViewport(720,450)` (dunia) dan `ExtendViewport(960,600)` (UI), pencegahan tembus klik UI Scene2D ke klik dunia, klik langsung Rover/Panel, dan isolasi input keyboard.
  - `core/src/main/java/com/marsreboot/habitat/HabitatRenderer.java` & `HabitatUi.java`: tepi kapsul/rounded anti-aliasing seam-free (`FillRoundedRect`), jejak debu langkah EVA, interpolasi gerak/rotasi haluan rover, laser pemindai panen, efek spora + `+1 PANEN`, HUD kapsul mengambang, terminal ringkas yang bisa digeser & di-resize sudut/tepi (`setResizable(true)`, `min 350×280`), popover `[?] API`, `[Log]` terlipat saat kosong, serta perbaikan padding label `SELESAI` & kontras tombol `Stop`.
  - `RobotSimulation`, `RunCoordinator`, `PythonRuntime`, `RuntimeProtocol`, dan `prototype-web/` **tidak diubah**. Test diperbarui di `HabitatControlTest`, `HabitatDesktopSmokeTest`, `PuzzleDesktopSmokeTest`.
- **Verifikasi aktual (15 Nov 2026):** `.\gradlew.bat --offline :core:controlTest :core:robotTest :core:runtimeTest :core:pythonIntegrationTest :lwjgl3:desktopSmokeTest :lwjgl3:puzzleDesktopTest --console=plain` **lulus (`exit 0`)**: `46` control, `81` robot, `33` runtime, `33` Python integration, `30` desktop smoke, dan `45` Python puzzle desktop (termasuk klik Rover langsung di dunia & resize drag pointer terminal), serta pemeriksaan visual PNG (`VERIFICATION.md` §10).
- **Batas & berikutnya:** `:lwjgl3:bundleWindows` belum dibangun ulang pasca-ubah 15 Nov; playtest manual di mesin desktop (drag/ketik berkelanjutan), komputer kedua, save lintas restart, backend Spring Boot, serta puzzle 2–9 masih terbuka. Tidak ada commit, push, atau deployment.

### 15 November 2026 — Refocus Visual Dunia ke 2× Crisp Pixel Art (Pertahankan Camera/UX Baru) (Antigravity)

- **Permintaan & keputusan:** pasca menyimpan snapshot desain vektor ke branch `backup/vector-redesign` (`ac058b5`), pengguna meminta fokus visual dunia dikembalikan ke **Crisp Pixel Art** (*MewnBase × Pathway*), sambil tetap mempertahankan `FollowCamera`, peta `1680×1040`, gerak astronaut halus, klik langsung Rover, UI non-kotak, dan terminal ringkas *resizable*.
- **File pemilik & perubahan:**
  - `assets/habitat/*.png`, `assets/assets.txt`, & `assets/habitat/asset-manifest.json` (`crisp_2x_pixel_art_outpost_v4`): memulihkan sprite 2× pixel art `b97eb55` untuk regolith, kawah, batu, walkway, Airlock, Solar Wing, Conduit Spine, Pump Station, Water Panel, dan astronaut EVA 4 arah (`192×224`, frame `48×56`), serta membake ulang `biodome_deck(_restored).png` (`600×390`), `biodome_canopy.png` (`600×390`), `mars_rover.png` (`72×64` hadap Timur), dan strip sprite `hydro_crops.png` (`192×64`) secara *nearest-neighbor* 2×.
  - `core/src/main/java/com/marsreboot/habitat/HabitatRenderer.java`: beralih ke `loadPixel(...)` (`TextureFilter.Nearest`, kecuali `light_glow.png` tetap `Linear`), merender tile `64×64`, pipa industri berflensa, bed hidroponik oktagonal/bevel, crop piksel (`RIPE`/`YOUNG`/`EMPTY`), braket L-corner hover/dekat, partikel debu/spora piksel persegi, dan sinar pemindai panen piksel—tanpa mengubah `HabitatWorld`, `FollowCamera`, `HabitatInput`, `HabitatScreen`, `HabitatUi`, runtime Python, simulasi robot, maupun test kontrol.
  - `DESIGN.md`, `VERIFICATION.md`, dan `TASKS.md` diselaraskan ke status terbaru.
- **Verifikasi aktual (15 Nov 2026 pasca pixel-refocus):** `.\gradlew.bat --offline :core:controlTest :core:robotTest :core:runtimeTest :core:pythonIntegrationTest :lwjgl3:desktopSmokeTest :lwjgl3:puzzleDesktopTest --console=plain` **lulus (`exit 0`)**: `46` control, `81` robot, `33` runtime, `33` Python integration, `30` desktop smoke, `45` Python puzzle desktop, serta verifikasi visual PNG desktop OpenGL (`VERIFICATION.md` §11).
- **Batas & berikutnya:** `:lwjgl3:bundleWindows` belum dibangun ulang pasca pixel-refocus; playtest manual di desktop, uji komputer kedua, save lintas restart, backend Spring Boot, serta puzzle 2–9 masih terbuka. Tidak ada commit, push, atau deployment yang dibuat oleh asisten.

### 19 November 2026 — Codex: pemilihan puzzle dan integrasi p2-a

- **Lingkup/hasil:** melanjutkan gameplay di atas desain pixel art/follow camera/klik rover/terminal resizable Antigravity. P1 membuka P2 setelah replay sukses; P2 empat tanaman/12 aksi, referensi for menghasilkan 7 aksi. Pemilih terminal, tujuan/hasil dinamis, peta misi, empat petak/hit test dan draf terpisah per puzzle sudah terhubung. Pencapaian tetap selama sesi; reset/pindah puzzle tidak menghapus pencapaian/draf. Tidak ada backend/save/puzzle 3–9 baru.
- **Kontrak:** PuzzleDefinition.ONE/TWO memuat fixture/starter/reference; RunCoordinator.selectPuzzle/isCompleted dan View.puzzle memberi sumber konten/progres. Bridge.forPuzzle menjaga pemanggil P1 existing; PythonRuntime/RuntimeProtocol memakai skenario p1-a/p2-a. Pemilihan diblokir saat Run/replay/cleanup. Runtime tetap Java otoritatif, frame lintas skenario ditolak. Loop adalah latihan, bukan syarat yang sudah diverifikasi; EDU-01 dan gate M-18 tetap terbuka.
- **File:** runtime/PuzzleDefinition.java baru; RunCoordinator, PythonRuntime, RuntimeProtocol disesuaikan; HabitatUi/Renderer/Input hanya disambungkan ke konten/kolom aktif. Tidak mengganti aset, kamera, HabitatWorld, launcher atau dependency. PuzzleProgressTest/task baru dan PuzzleDesktopSmokeTest diperluas. Perubahan visual lokal sebelumnya dipertahankan, tidak direvert.
- **Bukti:** test Gradle awal gagal karena PuzzleDefinition belum ada; sesudah implementasi referensi P2 lulus. Test domain robot 81, kontrol 46, runtime/protokol 33, integrasi Python existing 33, desktop existing 30, desktop puzzle kini 57 pemeriksaan lulus. PuzzleProgressTest lulus referensi P2 serta 12 kasus progres/negatif dengan CPython nyata. Pemeriksaan screenshot menemukan ringkasan 2 panen dan posisi socket hardcode P1; keduanya diperbaiki, test/visual diulang dan lulus. Screenshot p2-success.png diperiksa langsung.
- **Paket:** bundleWindows dibangun ulang dengan visual terbaru dan P2. JAR dari ZIP + Python paket diuji dari folder spasi dengan PATH hanya System32: test progres dan 57 desktop checks lulus. Laporan/hash di VERIFICATION §12 dan lwjgl3/build/reports/puzzle/p2-package-smoke.json. Komputer kedua belum diuji; tidak mengklaim playtest manusia.
- **Berikutnya:** tim mencoba P1→P2 dan review konten/UX; putuskan verifikator konsep melalui EXP-05 sebelum mengklaim latihan for dinilai. Lanjut domain sensor/young untuk P3 lalu siram P4; backend berjalan terpisah sesuai kontrak. Audit kewajiban OOP/pattern tetap perlu. Tidak ada commit/push/deployment.

### 19 November 2026 — Codex: puzzle 3 sensor dan larangan panen young

- **Lingkup:** melanjutkan P3 sesuai permintaan pengguna, mempertahankan desain Antigravity dan dukungan P1/P2. P3 terbuka setelah P2; empat petak ripe x=0,2 / young x=1,3, dua target, batas 12 aksi. Referensi sensor: empat pembacaan, dua panen + tiga gerak = 5 aksi/9 request. Draf dan pencapaian tiga puzzle terpisah selama sesi.
- **Implementasi:** PuzzleDefinition.THREE dengan targetCount terpisah dari columns, instruksi/starter/reference. RobotSimulation.finish(boolean) memeriksa invalidHarvestCount pada akhir normal; finish() existing tetap kompatibel. Mencoba panen muda dihitung sebagai aksi dan membuat INCOMPLETE meski target ripe habis, tanpa mengubah young atau memberi progres. PythonRuntime memakai kebijakan dari konten; scenarioId p3-a. UI memiliki pemilih/status P3, target dinamis dan pesan pelanggaran khusus. Tidak menambah API/dependency/aset atau mengganti kamera/terminal.
- **Bukti aktual:** test awal Gradle gagal pada THREE yang belum tersedia, lalu lulus implementasi. PuzzleProgressTest kini reference P1/P2/P3 + 23 pemeriksaan progres/sensor/negatif, robot 81, kontrol 46, runtime/protokol 33, integrasi Python existing 33, desktop existing 30. Test desktop puzzle diperluas dan lulus 71 pemeriksaan termasuk program naif gagal, reference sukses dan draf/progres P2/P3. Screenshot p3-success/p3-young-violation diperiksa; judul hasil pelanggaran diperbaiki agar tidak menyebut target belum habis ketika panen 2/2.
- **Paket:** bundleWindows dibangun ulang. JAR/CPython dari ZIP diuji di folder spasi, PATH hanya System32: test progres dan 71 desktop checks lulus. Bukti/hash di VERIFICATION §13 dan lwjgl3/build/reports/puzzle/p3-package-smoke.json. Komputer kedua dan playtest manusia belum dilakukan.
- **Batas/berikutnya:** P3 menilai hasil dunia/larangan young, bukan verifikator source yang memaksa pemakaian sensor/loop. EDU-01 tetap terbuka; keputusan fixture/penerimaan tim/aslab tidak direka. Lanjut domain water/needs_water, persediaan air serta multi-skenario P4; backend dan audit OOP/pattern terpisah. Tidak ada commit/push/deployment. M-19 memerlukan penerimaan tim terhadap kandidat, tidak mengklaim seluruh game selesai.

### 19 November 2026 — Codex: bantuan pengetikan IDE game

- **Permintaan/hasil:** editor Python game melengkapi (), [], {}, kutip, membungkus seleksi, melewati closer yang sudah ada, autoindent Enter setelah colon blok, inherited/continuation indentation, Tab ke kelipatan empat spasi, Tab/Shift+Tab per baris terpilih serta smart Backspace. Tidak ada pelengkapan keyword/LSP atau perubahan aturan/runtime/puzzle.
- **File:** PythonEditing.java baru (transformasi teks murni), PythonCodeArea.java baru (subclass TextArea) dan satu penggantian konstruksi editor di HabitatUi. Tampilan/fonts/layout, mouse scroll, gutter, Run/Stop/Reset dan shortcut game tetap existing. Native Undo/clipboard/navigation dipertahankan; edit banyak baris hanya mengubah whitespace. TextArea tetap tipe field editor sehingga kontrak test existing berlaku.
- **Verifikasi:** test awal gagal karena transformasi belum tersedia; implementasi lulus 25 pemeriksaan transformasi. Integrasi widget menemukan perbedaan setSelection(c,c) terhadap setCursorPosition, dikoreksi lalu lulus. Test desktop kini 90 (71 existing + 19 editor/typed-program) melalui Stage/InputMultiplexer nyata, termasuk Undo, Tab/ShiftTab, pasangan/seleksi, blok 100 baris, fokus, dan source for/if yang diketik tanpa indent manual lalu benar-benar dijalankan pada P2. Desktop existing 30 dan kontrol 46 juga lulus. Modifier Ctrl/Shift pada harness disimulasikan; tidak mengklaim uji keyboard fisik manusia.
- **Paket:** ZIP Windows dibangun ulang. JAR dan Python paket dari folder spasi, PATH hanya System32, lulus 25 editor dan 90 desktop checks. Bukti/hash di VERIFICATION §14 dan lwjgl3/build/reports/puzzle/editor-package-smoke.json. README/DESIGN/TEST_PLAN memuat perilaku dan shortcut.
- **Batas/berikutnya:** ini bantuan lokal berbasis konteks sederhana, bukan parser/validator/autocomplete Python penuh. Pengujian komputer kedua dan playtest manusia masih terbuka. Tidak memasang dependency/mengubah IntelliJ, melakukan commit/push/deployment, atau mengganti pekerjaan visual Antigravity. Tugas gameplay berikutnya tetap P4 water/needs_water dan multi-skenario, bukan bagian patch editor ini.

### 19 November 2026 — Codex: P4 irigasi dan replay dua skenario

- **Lingkup/hasil:** melanjutkan gameplay P4 setelah editor/P3. P3 selesai membuka P4; source snapshot yang sama diuji pada p4-a (dry 0,2) dan p4-b (dry 1,3), proses/namespace/fixture/air baru per skenario. Masing-masing 2 air, 12 aksi, 1.024 request. Referensi needs_water/water: 5 aksi/2 air per skenario, total 10 aksi/4 air. Hardcode posisi 0,2 lulus A tetapi gagal B tanpa progres P4.
- **Domain/runtime:** Fixture/Snapshot menambah target/state air dengan constructor harvest existing tetap tersedia. needsWater adalah sensor; water selalu memakai aksi dan memakai air bila tersedia, termasuk membuang air pada petak lembap, tanpa angka negatif. Panen tidak memenuhi goal irigasi. Execution.scenarios memuat hasil/trace; output Run disatukan dengan batas global dan tanpa log duplikat. Coordinator replay tiap skenario, menampilkan frame akhir A selama satu langkah sebelum reset B, menghitung total, dan baru commit setelah semua lulus/replay terakhir. Stop/reset/error/timeout dan frame lintas skenario tidak memberi progres baru.
- **UI/ownership:** PuzzleDefinition.FOUR, pemilih/status P4, sisa air/nomor skenario/total hasil, warna dan teks KERING/LEMBAP. Renderer membersihkan FX panen saat ganti identitas skenario. API popover dibuat scrollable dan menambah dua fungsi P4. Desain/aset pixel art, FollowCamera, editor autoindent serta kontrol existing dipertahankan. PETUNJUK paket diperbarui untuk P1–P4 dan shortcut editor.
- **Verifikasi aktual:** test awal gagal karena FOUR belum tersedia, kemudian referensi/negatif lulus. :core:check lulus (46 kontrol, 25 editor, 81 robot, 33 controller/protokol, 21 domain air). Integrasi Python existing 33, integrasi P4/progres 33, desktop existing 30 dan desktop puzzle 109 lulus. Kasus P4 mencakup air habis/pemborosan, tujuan parsial, first-pass/second-fail, fresh namespace, Stop/timeout proses kedua, error setelah target, replay final-frame dan output global bounded. Screenshot p4-dry-start/p4-hardcode-failure/p4-success diperiksa; ringkasan dipadatkan agar tidak clipping di window kecil.
- **Kendala build yang diperbaiki:** core:test awalnya gagal karena tidak menemukan JUnit (suite repo adalah main JavaExec). failOnNoDiscoveredTests=false di core saja, sementara suite JavaExec tetap wajib melalui check dan kegagalannya tidak diabaikan. Sesudah konfigurasi ini, core:check benar-benar lulus. Tidak mengklaim full root build/backend/semua platform.
- **Paket:** bundleWindows dibuat; JAR/CPython dari ZIP pada folder spasi, PATH hanya System32, lulus ulang 33 P4 + 25 editor + 109 desktop checks. Sesudahnya hanya PETUNJUK.txt direvisi dan ZIP dikemas ulang; seluruh 43 file lain, termasuk JAR/runtime/lisensi, dibandingkan hash identik dengan ekstraksi yang teruji. Report/hash akhir di VERIFICATION §15 dan lwjgl3/build/reports/puzzle/p4-package-smoke.json.
- **Batas/berikutnya:** kode lokal untuk kandidat P1–P4 terhubung, bukan game lengkap atau penerimaan gate C. Loop/sensor source belum diverifikasi semantik, fixture masih perlu review tim/aslab, backend/login/skor/save/komputer kedua/playtest dan kewajiban OOP/pattern belum dibuktikan lengkap. Utamakan uji demo tim, audit OOP/pattern dan integrasi backend sebelum memperluas P5–P9. Tidak ada commit/push/deployment.

### 19 November 2026 — Codex: gate demo P1–P4 dan audit OOP

- **Hasil/ownership:** root build.gradle menambah verifyDemo dan verifyDesktopDemo. lwjgl3/build.gradle menerima suite main JavaExec tanpa menuntut penemuan JUnit; suite dan exit gagal tetap aktif. Source gameplay/desain/aset anggota tidak diubah pada sesi ini.
- **Verifikasi aktual:** build awal gagal di lwjgl3:test (tidak ada JUnit), lalu `.\gradlew.bat --offline build verifyDesktopDemo :lwjgl3:bundleWindows --console=plain` exit 0: 46 kontrol, 25 editor, 81 robot, 33 runtime, 21 air; referensi P1–P3 + 23 progres/sensor/negatif; 33 integrasi Python, 33 P4, 30 desktop dan 109 desktop puzzle/editor. ZIP identik hash dengan artefak teruji §15. Rincian VERIFICATION §16.
- **Dokumen:** docs/mars/OOP_AUDIT.md memetakan bukti empat pilar, kekurangan tiap kandidat dan usulan ownership/langkah lanjut. README memperbarui perintah gate/prioritas serta mengoreksi petunjuk P1/P2 yang tertinggal. ARCHITECTURE menautkan audit dan membedakan rancangan dari implementasi. M-23 belum dicentang: enam kandidat belum terpenuhi; penerimaan materi/aslab belum ada.
- **Berikutnya:** peran 1–2 mulai perubahan Command/Strategy yang teruji berdasarkan batas domain existing; koordinasikan State/Observer dengan peran 3, cocokkan Factory Method/Template Method dengan kebutuhan misi dan materi kelas menjelang SOP 22 Nov. Peran 4 meneruskan backend melalui BACKEND_HANDOFF setelah repo/ownership/kontrak dikonfirmasi tim. Playtest tim/komputer kedua tetap perlu. Tidak ada commit/push/deployment atau klaim CI berjalan.

### 19 November 2026 — Codex: implementasi Command dan Strategy

- **Hasil:** package robot menambah RobotCommand dengan Move/Harvest/WaterCommand; runtime memvalidasi request lalu mengeksekusi lewat interface. Sensor tetap query tanpa trace. EvaluationStrategy/Evaluation dengan HarvestEvaluation, MatureHarvestEvaluation dan IrrigationEvaluation dipilih PuzzleDefinition dan diterapkan RobotSimulation.finish(strategy) hanya saat ACTIVE. Overload finish lama kompatibel; sukses tetap menunggu proses normal dan replay existing. Tidak mengubah desain/aset/fixture/protokol/dependency.
- **Verifikasi aktual:** test slice command/strategy gagal sebelum kelas tersedia lalu lulus. patternTest terhubung core:check, 64 pemeriksaan. verifyDemo lalu verifyDesktopDemo + bundleWindows exit 0: kontrol 46, editor 25, pattern 64, robot 81, runtime 33, air 21; referensi P1–P3 + 23 progres, Python 33, P4 33, desktop 30/109. ZIP baru diuji dari folder spasi, hanya fat JAR paket sebagai kelas aplikasi, Java 21 absolut, PATH hanya System32 dan Python paket: pattern 64, Python 33, referensi/progres P1–P3, P4 33, desktop 109 lulus. Report/hash di VERIFICATION §17.
- **Dokumen:** audit OOP mencatat dua implementasi dan diagram relasi, empat kandidat tetap terbuka. README/ARCHITECTURE/TEST_PLAN/M-23 disinkronkan. M-23 tetap tidak selesai untuk keseluruhan enam pattern; tidak ada klaim persetujuan aslab.
- **Berikutnya:** review tim dua pattern ini, rencanakan State/Observer bersama pemilik runtime/UI, dan Factory Method/Template Method sesuai variasi misi nyata serta materi kelas. Backend tetap melalui pemilik peran 4; komputer kedua/playtest manusia terbuka. Tidak ada commit/push/deployment.
