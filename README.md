# mars_reboot.py — MarsReboot

![Banner animasi mars_reboot.py](docs/mars/design-refs/mars_reboot_banner.gif)

Repository utama tim: game Java/libGDX, dokumentasi, aset, dan skill Codex–Antigravity berada dalam satu clone. Mulai dari **[TEAM_GUIDE.md](TEAM_GUIDE.md)** untuk memahami game, struktur, pembagian empat orang, urutan pengerjaan dan workflow review. Gunakan **[TEAM_PROMPTS.md](TEAM_PROMPTS.md)** untuk prompt onboarding dan tugas tiap peran.

## Status dan target

Target lengkap 9 puzzle dalam 3 misi, tahap awal puzzle 1–4. Habitat/kontrol astronaut serta visual/terminal sementara sudah tersedia berdasarkan bukti 12 November 2026. Robot/runtime Python, evaluator puzzle, autentikasi/skor backend serta progres lengkap belum dibuktikan. Layar akun/hasil/Before–After masih bisa berupa preview. Konsolidasi 13 November tidak menambahkan gameplay atau backend.

## Progres pengerjaan

Pembaruan: **13 November 2026**. Ringkasan mengikuti [backlog dan handoff](TASKS.md) serta [bukti aktual](VERIFICATION.md). Status selesai hanya berlaku pada hasil yang disebut di baris tersebut, bukan otomatis seluruh milestone terkait.

**Legenda:** ✅ selesai untuk cakupan tertulis · 🟡 sebagian · ⬜ belum selesai/menunggu keputusan · ⛔ terhambat.

| Status | Pekerjaan / hasil yang dituju | Penanggung jawab | Bukti saat ini / yang masih perlu dilakukan |
| --- | --- | --- | --- |
| ✅ | Dokumen perencanaan, panduan tim, prompt, dan paket skill tersedia dalam repo | Semua; satu editor dokumen | Konteks telah dikonsolidasikan; keputusan teknis bertanda Usulan tetap perlu dipilih. Discovery skill pada laptop anggota belum diuji. |
| ✅ | Arah visual dan referensi layar | 3 — Dunia/UI | Referensi desain tersedia di docs/mars/design-refs; bukan persetujuan seluruh aset produksi. |
| 🟡 | Fondasi proyek libGDX dan toolchain tim | 1 + 4 | Modul core/lwjgl3 serta launcher tersedia; keberhasilan Wrapper/JDK pada seluruh komputer belum dibuktikan. |
| ✅ | Habitat, gerak astronaut, collision, dan interaksi dasar dalam cakupan test existing | 3 — Dunia/UI | Catatan 40 assertion kontrol dan 29 assertion desktop lulus; bukan klaim seluruh pengujian keyboard/mouse manual selesai. |
| ✅ | Panel inspeksi dan terminal editor sementara | 3 — Dunia/UI | Buka/tutup, fokus dan draf sesi telah diperiksa; terminal belum mengeksekusi kode pemain. |
| 🟡 | UI outpost, HUD, log, peta misi, bantuan dan pengaturan | 3 — Dunia/UI | Layar/preview tersedia; perlu dihubungkan ke state gameplay nyata dan pemeriksaan manual lengkap. |
| ⛔ | Build dan test melalui Gradle Wrapper | 1 + 4 | Handoff terakhir mencatat unduhan distribusi timeout. Kompilasi langsung tidak menggantikan bukti Wrapper; cek kembali pada mesin tim. |
| ⬜ | Konfirmasi bahasa pemain dan keputusan runtime | Tim + aslab; pemilik 1 | Python masih perlu konfirmasi; pilihan runtime ditetapkan setelah eksperimen. M-01/M-04. |
| 🟡 | Kontrak API robot, fixture, batas, skor, dan HTTP | 1 + 2 + 4 | Draft tersedia; finalisasi dan validasi bersama pemilik UI sebelum implementasi paralel. M-07/M-09. |
| ⬜ | Simulasi robot Java: grid, gerak, panen, sensor dan siram | 1 — Simulasi/runtime | Implementasikan aturan terpilih dan test domain. M-10. |
| ⬜ | Runtime Python serta Run/Stop/Reset/error/timeout | 1 — Simulasi/runtime | Buktikan aksi/sensor nyata, loop tanpa akhir, recovery, dan penolakan hasil lama. M-04/M-11. |
| ⬜ | Puzzle 1 dan evaluator berhasil/gagal | 2 — Puzzle/evaluasi | Solusi referensi serta kasus negatif harus lulus melalui implementasi. M-16. |
| ⬜ | Registrasi, login dan sesi backend | 4 — Backend/distribusi | Implementasi Spring Boot dan test autentikasi nyata. M-12. |
| ⬜ | Penyimpanan/pengambilan skor dan rekor | 4 — Backend/distribusi | Uji isolasi akun, persistensi, validasi dan retry tanpa duplikasi. M-13. |
| ⬜ | Satu alur terintegrasi dari login sampai skor tersimpan | Semua | Login → panel → terminal → robot → evaluasi → simpan/ambil skor; bukan mock/preview. M-17. |
| ⬜ | Puzzle 2–4 dan latihan konsep yang dipilih | 2 + 3, integrasi 1 | Lengkapi loop, sensor panen, irigasi dan skenario pembeda. M-18–20. |
| ⬜ | Progres dan pemulihan habitat dari hasil puzzle nyata | 1 + 2 + 3 | Terapkan setelah hasil sah dan replay selesai; Before/After preview bukan bukti progres. M-21. |
| ⬜ | Save kode/progres lintas sesi dan retry skor, jika dipilih | 1 + 4 | Pilih kebijakan dahulu, lalu uji buka ulang, data rusak, profil dan jaringan. M-22. |
| ⬜ | Empat pilar OOP dan minimal enam pattern pada kode/demo | Semua | Kandidat rancangan belum membuktikan implementasi sesuai materi/aslab. M-23. |
| 🟡 | Aset, audio, aksesibilitas dan UX produksi | 3 — Dunia/UI | Placeholder dan visual dasar ada; font ukuran kecil, audio serta validasi lengkap masih terbuka. M-24/M-30. |
| ⬜ | Puzzle 5–9, misi pompa, dan penutup | 2 + 1 + 3 | Rinci kontrak/fixture lanjutan lalu uji seluruh skenario. Target tetap 9 puzzle. M-26–29. |
| ⬜ | Playtest manusia dan perbaikan berdasarkan observasi | Semua; pencatat 2 | Belum ada hasil playtest nyata. M-25/M-31. |
| ⬜ | Paket rilis pada komputer anggota lain | 4, didukung semua | Uji artefak final, runtime dan koneksi backend dengan panduan yang dapat diikuti. M-06/M-32. |
| ⬜ | Kelengkapan dan pengumpulan luaran SOP | Semua; editor 2 | Proposal/PMP/GDD, laporan, kontribusi, video dan kredit; status pengumpulan belum dibuktikan. M-33. |
| ⬜ | Publikasi game dan pemeriksaan tautan rilis | 4 + otorisasi pengguna | Bedakan paket lokal, push source GitHub, dan rilis game yang benar-benar dapat dimainkan. M-34. |

**Prioritas berikutnya:** tutup kendala build/kontrol yang relevan, sepakati kontrak, lalu buat simulasi robot dan puzzle 1 benar-benar berjalan; integrasikan backend sebelum memperluas puzzle 2–4. Ikuti dependensi lengkap pada TASKS.

**Cara memperbarui:** ubah simbol dan catatan setelah ada deliverable serta bukti; perbarui TASKS/VERIFICATION dalam perubahan yang sama. Jangan mencentang pekerjaan hanya karena mockup, preview, atau rencana tersedia. Checklist rinci tetap di TASKS; tabel ini ringkasan yang terbaca di halaman GitHub, tanpa persentase kemajuan rekaan. Penanggung jawab masih nomor peran sampai nama anggota ditetapkan.

## Menjalankan game

Buka root repository ini di IntelliJ sebagai proyek Gradle. Gunakan **JDK 21** sesuai source/target dan toolchain existing. Versi existing: libGDX 1.14.2, LWJGL 3.4.3, Wrapper 9.8.0, app version 1.0.0; jangan menurunkan/mengupgrade versi sepihak.

Dari root clone pada Windows:

```powershell
.\gradlew.bat :core:controlTest
.\gradlew.bat :lwjgl3:run
# Memerlukan desktop/OpenGL:
.\gradlew.bat :lwjgl3:desktopSmokeTest
# Build setelah dependency tersedia:
.\gradlew.bat :lwjgl3:jar
```

Di IntelliJ jalankan task lwjgl3/application/run atau Lwjgl3Launcher dengan working directory assets. Sinkronisasi pertama memerlukan jaringan untuk Wrapper/toolchain/dependency; cache pribadi tidak ikut clone. Wrapper sebelumnya terhambat unduhan. Bukti kompilasi langsung dengan JDK 25 --release 21 dan assertion kontrol/desktop tidak sama dengan keberhasilan build Wrapper pada komputer teman. Lihat [VERIFICATION](VERIFICATION.md) dan arsip [catatan README libGDX](docs/setup/LIBGDX_README_BEFORE_CONTEXT.md). Sesi konsolidasi ini tidak menjalankan build/test game baru.

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
