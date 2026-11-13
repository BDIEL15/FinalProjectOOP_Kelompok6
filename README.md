# mars_reboot.py — MarsReboot

Repository utama tim: game Java/libGDX, dokumentasi, aset, dan skill Codex–Antigravity berada dalam satu clone. Mulai dari **[TEAM_GUIDE.md](TEAM_GUIDE.md)** untuk memahami game, struktur, pembagian empat orang, urutan pengerjaan dan workflow review. Gunakan **[TEAM_PROMPTS.md](TEAM_PROMPTS.md)** untuk prompt onboarding dan tugas tiap peran.

## Status dan target

Target lengkap 9 puzzle dalam 3 misi, tahap awal puzzle 1–4. Habitat/kontrol astronaut serta visual/terminal sementara sudah tersedia berdasarkan bukti 12 November 2026. Robot/runtime Python, evaluator puzzle, autentikasi/skor backend serta progres lengkap belum dibuktikan. Layar akun/hasil/Before–After masih bisa berupa preview. Konsolidasi 13 November tidak menambahkan gameplay atau backend.

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
