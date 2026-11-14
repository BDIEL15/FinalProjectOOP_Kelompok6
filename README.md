# mars_reboot.py — MarsReboot

![Banner animasi mars_reboot.py](docs/mars/design-refs/mars_reboot_banner.gif)

Repository utama tim: game Java/libGDX, dokumentasi, aset, dan skill Codex–Antigravity berada dalam satu clone. Mulai dari **[TEAM_GUIDE.md](TEAM_GUIDE.md)** untuk memahami game, struktur, pembagian empat orang, urutan pengerjaan dan workflow review. Gunakan **[TEAM_PROMPTS.md](TEAM_PROMPTS.md)** untuk prompt onboarding dan tugas tiap peran.

## Status dan target

Target lengkap 9 puzzle dalam 3 misi, tahap awal puzzle 1–4. Habitat/kontrol dan visual dasar tersedia. Per 13 November 2026, kandidat puzzle 1 panen terhubung ke terminal Python nyata, robot/replay/hasil, Run/Stop/Reset dan progres sesi. Paket Windows x64 menyertakan CPython 3.15.2; Java 21+ masih diperlukan. Backend, puzzle 2–9, save lintas restart dan komputer kedua belum terverifikasi. Kandidat aturan tetap eksperimen, bukan persetujuan aslab atau penerimaan seluruh game.

## Progres pengerjaan

Pembaruan: **13 November 2026**. Ringkasan mengikuti [backlog dan handoff](TASKS.md) serta [bukti aktual](VERIFICATION.md). Status selesai hanya berlaku pada hasil yang disebut di baris tersebut, bukan otomatis seluruh milestone terkait.

**Legenda:** ✅ selesai untuk cakupan tertulis · 🟡 sebagian · ⬜ belum selesai/menunggu keputusan · ⛔ terhambat.

| Status | Pekerjaan / hasil yang dituju | Penanggung jawab | Bukti saat ini / yang masih perlu dilakukan |
| --- | --- | --- | --- |
| ✅ | Dokumen perencanaan, panduan tim, prompt, dan paket skill tersedia dalam repo | Semua; satu editor dokumen | Konteks telah dikonsolidasikan; keputusan teknis bertanda Usulan tetap perlu dipilih. Discovery skill pada laptop anggota belum diuji. |
| ✅ | Arah visual dan referensi layar | 3 — Dunia/UI | Referensi desain tersedia di docs/mars/design-refs; bukan persetujuan seluruh aset produksi. |
| 🟡 | Fondasi proyek libGDX dan toolchain tim | 1 + 4 | Modul core/lwjgl3 serta launcher tersedia; keberhasilan Wrapper/JDK pada seluruh komputer belum dibuktikan. |
| ✅ | Habitat, gerak astronaut, collision, dan interaksi dasar dalam cakupan test existing | 3 — Dunia/UI | Catatan 40 assertion kontrol dan 29 assertion desktop lulus; bukan klaim seluruh pengujian keyboard/mouse manual selesai. |
| ✅ | Panel inspeksi dan terminal editor sementara | 3 — Dunia/UI | Buka/tutup, fokus dan draf sesi telah diperiksa; terminal kini mengeksekusi Python nyata pada p1; editor penuh masih perlu penerimaan manusia. |
| 🟡 | UI outpost, HUD, log, peta misi, bantuan dan pengaturan | 3 — Dunia/UI | Layar/preview tersedia; perlu dihubungkan ke state gameplay nyata dan pemeriksaan manual lengkap. |
| ✅ | Test fokus dan paket desktop melalui Gradle Wrapper | 1 + 4 | 13 Nov: robotTest 81 assertion, controlTest 40 assertion, compileJava desktop dan JAR lulus offline dengan cache mesin ini; belum bukti komputer kedua. |
| 🟡 | Konfirmasi bahasa pemain dan keputusan runtime | Tim + aslab; pemilik 1 | CPython bundel dipilih/teruji untuk eksperimen p1; konfirmasi bahasa ke aslab masih terbuka. M-01/M-04. |
| 🟡 | Kontrak API robot, fixture, batas, skor, dan HTTP | 1 + 2 + 4 | Draft tersedia; finalisasi dan validasi bersama pemilik UI sebelum implementasi paralel. M-07/M-09. |
| 🟡 | Simulasi robot Java: grid, gerak, panen, sensor dan siram | 1 — Simulasi/runtime | Grid/gerak/panen/sensor/reset/batas serta adapter Python p1 teruji; siram belum dibuat. M-10. |
| ✅ | Runtime Python p1 serta Run/Stop/Reset/error/timeout | 1 — Simulasi/runtime | 33 assertion controller/protokol + 33 integrasi Python nyata; 14 desktop puzzle. Terbatas eksperimen p1 Windows x64. M-04/M-11 tetap gate lebih luas. |
| 🟡 | Puzzle 1 dan evaluator berhasil/gagal | 2 — Puzzle/evaluasi | Kandidat p1-a berjalan melalui terminal Python, replay dan progres sesi; review fixture/penerimaan tim belum selesai. M-16. |
| ⬜ | Registrasi, login dan sesi backend | 4 — Backend/distribusi | Implementasi Spring Boot dan test autentikasi nyata. M-12. |
| ⬜ | Penyimpanan/pengambilan skor dan rekor | 4 — Backend/distribusi | Uji isolasi akun, persistensi, validasi dan retry tanpa duplikasi. M-13. |
| ⬜ | Satu alur terintegrasi dari login sampai skor tersimpan | Semua | Login → panel → terminal → robot → evaluasi → simpan/ambil skor; bukan mock/preview. M-17. |
| ⬜ | Puzzle 2–4 dan latihan konsep yang dipilih | 2 + 3, integrasi 1 | Lengkapi loop, sensor panen, irigasi dan skenario pembeda. M-18–20. |
| 🟡 | Progres dan pemulihan habitat dari hasil puzzle nyata | 1 + 2 + 3 | P1 selesai selama sesi setelah replay sah; seluruh habitat/misi belum terhubung. Before/After tetap preview. M-21. |
| ⬜ | Save kode/progres lintas sesi dan retry skor, jika dipilih | 1 + 4 | Pilih kebijakan dahulu, lalu uji buka ulang, data rusak, profil dan jaringan. M-22. |
| ⬜ | Empat pilar OOP dan minimal enam pattern pada kode/demo | Semua | Kandidat rancangan belum membuktikan implementasi sesuai materi/aslab. M-23. |
| 🟡 | Aset, audio, aksesibilitas dan UX produksi | 3 — Dunia/UI | Placeholder dan visual dasar ada; font ukuran kecil, audio serta validasi lengkap masih terbuka. M-24/M-30. |
| ⬜ | Puzzle 5–9, misi pompa, dan penutup | 2 + 1 + 3 | Rinci kontrak/fixture lanjutan lalu uji seluruh skenario. Target tetap 9 puzzle. M-26–29. |
| ⬜ | Playtest manusia dan perbaikan berdasarkan observasi | Semua; pencatat 2 | Belum ada hasil playtest nyata. M-25/M-31. |
| ⬜ | Paket rilis pada komputer anggota lain | 4, didukung semua | Uji artefak final, runtime dan koneksi backend dengan panduan yang dapat diikuti. M-06/M-32. |
| ⬜ | Kelengkapan dan pengumpulan luaran SOP | Semua; editor 2 | Proposal/PMP/GDD, laporan, kontribusi, video dan kredit; status pengumpulan belum dibuktikan. M-33. |
| ⬜ | Publikasi game dan pemeriksaan tautan rilis | 4 + otorisasi pengguna | Bedakan paket lokal, push source GitHub, dan rilis game yang benar-benar dapat dimainkan. M-34. |

**Prioritas berikutnya:** tim mencoba paket puzzle 1, meninjau fixture/keterbacaan editor dan mengonfirmasi bahasa ke aslab. Lanjutkan kontrak/backend serta domain siram sebelum memperluas puzzle 2–4. Ikuti TASKS; alur login/skor lengkap belum selesai.

**Cara memperbarui:** ubah simbol dan catatan setelah ada deliverable serta bukti; perbarui TASKS/VERIFICATION dalam perubahan yang sama. Jangan mencentang pekerjaan hanya karena mockup, preview, atau rencana tersedia. Checklist rinci tetap di TASKS; tabel ini ringkasan yang terbaca di halaman GitHub, tanpa persentase kemajuan rekaan. Penanggung jawab masih nomor peran sampai nama anggota ditetapkan.

## Menjalankan game

Buka root repository ini di IntelliJ sebagai proyek Gradle. Gunakan **JDK 21** sesuai source/target dan toolchain existing. Versi existing: libGDX 1.14.2, LWJGL 3.4.3, Wrapper 9.8.0, app version 1.0.0; jangan menurunkan/mengupgrade versi sepihak.

Dari root clone pada Windows:

```powershell
.\gradlew.bat :core:robotTest :core:controlTest :core:runtimeTest :core:pythonIntegrationTest
.\gradlew.bat :lwjgl3:run
# Memerlukan desktop/OpenGL:
.\gradlew.bat :lwjgl3:desktopSmokeTest
# Integrasi puzzle desktop dan paket Windows + Python:
.\gradlew.bat :lwjgl3:puzzleDesktopTest
.\gradlew.bat :lwjgl3:bundleWindows
```

Di IntelliJ jalankan task lwjgl3/application/run; task ini menyiapkan Python bundel otomatis. Untuk Run launcher langsung, jalankan `:lwjgl3:preparePython` sekali dahulu; runtime dicari dari lokasi kelas, tanpa PATH. Sinkronisasi pertama membutuhkan jaringan, cache tidak ikut clone. Paket uji ada di `lwjgl3/build/distributions/MarsReboot-1.0.0-windows-x64.zip`: ekstrak seluruhnya dan buka run.bat (Java 21+). Test paket dengan PATH tanpa Python sistem sudah lulus pada mesin ini; komputer kedua belum diuji. Detail: [kontrak runtime](docs/mars/PYTHON_RUNTIME.md) dan [VERIFICATION](VERIFICATION.md).

Kontrol awal: WASD/panah, E interaksi, Esc menutup panel/editor; source draf dipertahankan pada sesi layar, bukan otomatis tersimpan lintas restart. Periksa fitur/shortcut terbaru di DESIGN dan source existing.

## Peta dokumentasi

| Dokumen | Acuan |
| --- | --- |
| [AGENTS](AGENTS.md) | Instruksi kerja manusia/AI dan batas otorisasi. |
| [TEAM_GUIDE](TEAM_GUIDE.md) / [TEAM_PROMPTS](TEAM_PROMPTS.md) | Onboarding tim, urutan milestone, ownership, kontribusi dan prompt. |
| [PROJECT](PROJECT.md) | Keputusan pengguna, SOP dan strategi kolaborasi. |
| [PRD](PRD.md) / [RULES](RULES.md) | Kebutuhan serta aturan/fixture kandidat. |
| [ARCHITECTURE](ARCHITECTURE.md) / [DESIGN](DESIGN.md) | Kontrak teknis dan UX/visual. |
| [TEST_PLAN](TEST_PLAN.md) / [VERIFICATION](VERIFICATION.md) | Rencana pemeriksaan versus bukti aktual. |
| [TASKS](TASKS.md) | Backlog, dependensi, dan handoff terbaru. |
| [BACKEND_HANDOFF](BACKEND_HANDOFF.md) | Batas akun/skor, pembagian backend, prompt tugas. |
| [SKILLS](SKILLS.md) / [skills-lock.json](skills-lock.json) | Paket skill bersama; dependencies/discovery aplikasi masih perlu diperiksa. |
| [PROTOTYPE](PROTOTYPE.md) | Referensi Farm Code Lab, source opsional di prototype-web/. |

Referensi visual berada di docs/mars/design-refs/. Arsip prototype di docs/prototype/, salinan SOP lokal di docs/references/sop/. Dokumen docs/mars lain hanya penunjuk, bukan sumber spesifikasi kedua. Source prototype tidak berada di root core/; npm dijalankan hanya dari prototype-web/ bila memang mengerjakan referensi web.

## Kerja bersama

Tetapkan satu tugas kecil per anggota, pemilik file, reviewer dan kontrak yang sama. Pakai clone/worktree terpisah, branch per tugas, test fokus, review silang, lalu integrasi/demo. Setiap anggota memahami kodenya dan melakukan kontribusi Git dengan identitas sendiri sesuai otorisasi; jumlah push bukan ukuran tunggal pemerataan. Tidak ada commit/push/merge/deployment yang dilakukan oleh sesi konsolidasi.

Buka Codex/Antigravity pada root clone agar AGENTS.md dan .agents/skills terbaca. Jangan menginstal ulang seluruh koleksi, menjalankan script skill otomatis, atau berbagi credential. Folder build/.gradle/.idea bukan konteks tim. Bahasa/runtime, database/deployment serta usulan gameplay tetap mengikuti keputusan dokumen pemilik.

### Mencoba puzzle 1

Dekati terminal kanan lalu tekan E. Starter baru berisi satu harvest; lengkapi dengan `move("east")` dan `harvest()` berikutnya, lalu klik Run. Robot bergerak pada dua petak kebun. Stop menghentikan program, Reset memulihkan petak tanpa menghapus draf, Esc menutup terminal dan Run tetap berjalan. Kelulusan hanya dicatat selama sesi; preview Before/After tidak memengaruhi progres. Folder runtime dan semua build diabaikan Git; jangan hanya membagikan JAR tanpa folder runtime.
