# VERIFICATION — mars_reboot.py

Status diperbarui 23 November 2026. Bukti historis UI/Puzzle 1 ada di bagian 9–10. Implementasi Home/backend/scoreboard terbaru dicatat di bagian 11. Kompilasi Gradle core/lwjgl3 lulus hari ini, tetapi game dan dialog konfirmasi keluar belum diperiksa secara visual di desktop. Rencana TEST_PLAN bukan hasil uji; prototype tetap terpisah di docs/prototype/VERIFICATION.md.

## 1. Status produk

| Area | Bukti aktual | Status |
| --- | --- | --- |
| Java/libGDX habitat | Root MarsReboot; `.\gradlew.bat --offline` lulus 14 Nov 2026: 40 assertion kontrol (`:core:controlTest`) dan 29 assertion desktop/Scene2D (`:lwjgl3:desktopSmokeTest`) | Habitat teruji; input tahan tombol OS manusia penuh belum disimulasikan. |
| Runtime script dan Stop desktop | CPython bundel (`build/python/windows-x64`): 81 assertion robot (`:core:robotTest`), 33 controller/protokol (`:core:runtimeTest`), 33 integrasi Python (`:core:pythonIntegrationTest`) lulus 14 Nov 2026 | Teruji untuk p1 Windows x64; bukan sandbox atau seluruh platform. |
| Backend/auth/skor | Source Spring Boot, akun/sesi, simpan/lihat skor, papan skor P1, dan klien game tersedia; uji API PowerShell oleh pengguna terdahulu membuktikan register/login/submit/baca skor personal. | Kompilasi source langsung dan kompilasi Gradle core/lwjgl3 lulus pada 23 Nov. Ranking multiakun, UI desktop dan alur terbaru belum diuji. |
| Konfirmasi keluar desktop | Dialog modal tersedia untuk Home dan habitat; penutupan jendela dibatalkan sampai pemain memilih. | Belum diuji lewat peluncuran desktop; lihat UI-07 di TEST_PLAN. |
| UI/aset/Figma | Adegan Outpost Mars `2×` (`HabitatRenderer`) dgn petak panen 2×1 tidak tertutup dan penanda lokal `PANEN P1 SELESAI (SESI)`; font BMFont `ui_sans`/`ui_sans_bold` (`36px`, skala `0.50f`) & `ui_mono` (`34px`, skala `0.53f`) ~`18` unit UI (`~12px` di `640×480`); terminal `556×518` di kiri + editor/output scroll berbatas + footer `Coba lagi`/`Kembali ke habitat` + `[O] Setelan` Preview Visual + Peta Misi P1 berbasis `RunCoordinator.View`; screenshot `1280×720`, `1440×900`, `900×600`, `640×480` diperiksa visual 14 Nov 2026 | Teruji desktop otomatis & inspeksi PNG pada keempat resolusi; drag mouse OS manusia dan review playtest tetap terbuka. |
| Puzzle 1–9 | Kandidat p1-a melalui Python–Java–desktop (41 assertion desktop puzzle pada `:lwjgl3:puzzleDesktopTest`, 14 Nov 2026); P2–9 tampil jujur `BELUM TERSEDIA` | P1 teruji lintas alur inspeksi → terminal → Run → replay → hasil → ulang/kembali; puzzle 2–9 belum ada. |
| Distribusi/playtest/publikasi | ZIP Windows + Python diuji 13 Nov dari folder spasi tanpa Python PATH; `:lwjgl3:bundleWindows` belum dibangun ulang pasca-penyempurnaan UI 14 Nov | Paket 13 Nov teruji lokal; re-bundle 14 Nov, komputer kedua, manusia, dan publikasi belum dilakukan. |

## 12. Kompilasi dialog konfirmasi keluar — 23 November 2026

| Pemeriksaan | Hasil aktual | Batas |
| --- | --- | --- |
| `gradlew.bat :core:compileJava :lwjgl3:compileJava --offline --console=plain` | PASS, exit 0 | Core dan launcher desktop terkompilasi. Tidak menjalankan test, server, atau game desktop; dialog belum dilihat saat berjalan. |
| `git diff --check` | PASS | Pemeriksaan whitespace saja. |

## 2. Pemeriksaan dokumentasi sesi rewrite

- Dokumen utama root ditargetkan ke Mars; dokumen lama prototype diarsipkan dan dirujuk PROTOTYPE.md. Ini perubahan dokumentasi, bukan migrasi stack aplikasi.
- Pemeriksaan dokumen dengan Python stdlib: ID PRD tetap 24 FR, 11 NFR, 20 AC, dan 5 KT; seluruh FR/NFR terhubung AC, seluruh AC terpetakan ke 36 kasus uji TEST_PLAN. Tautan relatif/anchor dan pasangan code fence diperiksa lintas dokumen root, penunjuk docs/mars, serta arsip prototype. Tidak ada render Mermaid yang diklaim.
- Integritas tujuh arsip prototype dibandingkan dengan isi sebelum rewrite: sama selain tujuan tautan relatif. Handoff lama dipertahankan pada TASKS Mars sebagai catatan historis.
- Model ilustrasi sementara di memori memeriksa lima fixture kandidat dan dua contoh negatif: aksi p1-a=3, p2-a=7, p3-a=5, p4-a/p4-b=5 masing-masing dengan air 2. Semua konsisten dengan RULES. Ini pemeriksaan angka/contoh dokumentasi, **bukan** implementasi/test Java, runtime Python produk, atau bukti EDU-01.
- Perbandingan hash sebelum/sesudah menunjukkan perubahan hanya file Markdown: tidak ada source, test aplikasi, dependency, atau konfigurasi yang diubah; tidak ada file dihapus.
- Referensi teknis resmi lifecycle/Scene2D libGDX, Java Process/ProcessBuilder, dan Spring Security Password Storage dibaca untuk kelayakan rancangan. Tidak menguji implementasi API tersebut.
- Diagram Mermaid disediakan sebagai source; belum dirender atau diuji dengan renderer Mermaid.
- Tidak menjalankan test aplikasi, instalasi, build Java/backend, atau browser/desktop baru.

## 3. Format bukti berikutnya

Catat tanggal, target, versi/commit atau hash build, lingkungan, ID TEST_PLAN, command/langkah aktual, hasil PASS/FAIL/BELUM DIJALANKAN, artefak, keterbatasan, dan pemilik tindak lanjut. Jangan menyimpan credential/source pemain dalam laporan yang dibagikan. Update TASKS hanya setelah deliverable dan bukti tersedia.

## 4. Habitat/kontrol existing — 12 November 2026

Root: `C:\Users\abdie\OneDrive\Desktop\Folder_Abdiel\PRAKTIKUM\FINDIH\MarsReboot`. Windows, Oracle JDK 25.0.4.1; target bytecode Java 21. libGDX 1.14.2, LWJGL 3.4.3 dan transitif sesuai POM resmi/constraint generator, tanpa upgrade. Tidak ada commit Git dasar Java; manifest hash source sesi disimpan di `lwjgl3/build/reports/habitat/source-sha256.txt`. Data puzzle/fixture/backend tidak terlibat.

| Pemeriksaan | Hasil aktual | Bukti / batas |
| --- | --- | --- |
| Wrapper `--offline tasks --all` | GAGAL | Izin cache mula-mula ditolak sandbox; setelah izin, distribusi 9.8.0 belum tersedia dan unduhan timeout. Offline tidak menghilangkan kebutuhan distribusi Wrapper. |
| Wrapper `:core:controlTest :lwjgl3:jar --info` dengan cache pengguna/lokal | BELUM BERHASIL | Unduhan gagal/menunggu cache; percobaan lokal gagal koneksi. curl URL resmi dibatasi 180 detik juga timeout setelah sekitar 10 MiB dari 144,5 MiB. Tidak mengklaim task build/test Gradle telah berjalan. Wrapper tidak diganti; percobaan timeout 60 detik dikembalikan menjadi 10 detik existing. |
| Kompilasi seluruh source main/test `core`/`lwjgl3` | PASS | `javac --release 21 -encoding UTF-8 -Xlint:deprecation`, tanpa diagnostic pada pemeriksaan terakhir, classpath jar versi proyek. Bukan bukti Gradle atau runtime JDK 21. |
| `HabitatControlTest` | PASS, 40 assertion | Gerak 180 unit/detik, diagonal, 30/60/144 FPS, alias/oposisi, delta invalid, clear input, sweep collider/batas dari beberapa arah, panel/kebun solid, range, modal, Esc/E/hold dan kontrol kembali. Tidak menggunakan OpenGL. |
| `Lwjgl3Launcher` existing | PASS buka/render/tutup | Window habitat dibuka dengan Java langsung dari assets, screenshot Computer Use dilihat, ditutup lewat tombol window; exit 0. Tap D melalui alat tidak cukup membuktikan gerak OS. |
| `HabitatDesktopSmokeTest` | PASS, 29 assertion | OpenGL/Stage nyata, Screen wiring, input antar-frame, hint, inspeksi, penguncian gerak, TextArea menerima karakter, fokus, Esc, draf reopen, resize, hide/processor cleanup. Event input disuntik ke processor, bukan keyboard fisik. |
| Screenshot desktop | DIPERIKSA parsial | `inspection.png`, `terminal-1440x900.png`, `terminal-640x480.png` dilihat; terminal awal dan 900×600 juga dihasilkan. Panel/label/editor/tombol keluar berada dalam kanvas. 640×480 teks kecil; bukan sertifikasi keterbacaan/kontras. |
| Test UI-01–04 lengkap, EXP-02, robot/puzzle, backend, distribusi | BELUM DIJALANKAN / BELUM TERSEDIA | Hanya subset habitat dari UI-01–04. Tahan keyboard OS, drag/tombol mouse, clipboard/seleksi/scroll panjang, fokus OS, mesin kedua dan playtest perlu dilanjutkan. Tidak ada klaim AC-02/15 lengkap atau skor/determinisme robot. |

Command alternatif yang benar-benar dieksekusi (jar disiapkan lokal dari Maven Central sesuai versi proyek; cache `.gradle-local` diabaikan Git):

```powershell
# Dari root Java:
$javaSources = @(Get-ChildItem core/src/main/java,core/src/test/java,lwjgl3/src/main/java,lwjgl3/src/test/java -Recurse -Filter '*.java' | ForEach-Object FullName)
& 'C:/Program Files/Java/jdk-25.0.4.1/bin/javac.exe' --release 21 -encoding UTF-8 -Xlint:deprecation -cp '.gradle-local/manual/*' -d core/build/direct-check $javaSources
& 'C:/Program Files/Java/jdk-25.0.4.1/bin/java.exe' -cp 'core/build/direct-check;.gradle-local/manual/*' com.marsreboot.habitat.HabitatControlTest
# Dari assets (working directory launcher existing):
& 'C:/Program Files/Java/jdk-25.0.4.1/bin/java.exe' --enable-native-access=ALL-UNNAMED --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED -cp '../core/build/direct-check;../.gradle-local/manual/*;../lwjgl3/src/main/resources;.' com.marsreboot.habitat.HabitatDesktopSmokeTest
# Launcher juga dibuka dengan command terakhir dan main class com.marsreboot.lwjgl3.Lwjgl3Launcher.
```

Artefak screenshot berorientasi normal di `lwjgl3/build/reports/habitat/`. Tidak ada aset produksi atau source pemain yang dipublikasikan. Setelah unduhan Gradle tersedia, jalankan `:core:controlTest`, `:lwjgl3:desktopSmokeTest`, `:lwjgl3:jar`/`build` melalui Wrapper dan catat hasil baru; jangan mengganti bukti gagal ini dengan rencana. Detail ownership/kontrak ada pada handoff TASKS terbaru.

## 5. Implementasi visual adegan habitat, panel inspeksi, dan floating terminal — 12 November 2026

Root: `C:\Users\abdie\OneDrive\Desktop\Folder_Abdiel\PRAKTIKUM\FINDIH\MarsReboot`. Kontrak perilaku `HabitatWorld`, `HabitatInput`, `HabitatScreen`, `MarsRebootGame`, `Lwjgl3Launcher`, `HabitatControlTest`, dan `HabitatDesktopSmokeTest` dipertahankan tanpa modifikasi (hash SHA-256 identik dengan baseline di `lwjgl3/build/reports/habitat/source-sha256.txt`). Perubahan hanya pada `HabitatRenderer.java`, `HabitatUi.java`, `assets/assets.txt`, dan aset placeholder konsisten baru di `assets/habitat/`.

| Pemeriksaan | Hasil aktual | Bukti / batas |
| --- | --- | --- |
| Kompilasi source `core` + `lwjgl3` (`javac --release 21`) | PASS | `javac --release 21 -encoding UTF-8 -Xlint:deprecation -cp '.gradle-local/manual/*' -d core/build/direct-check` lulus tanpa error/warning pada `HabitatRenderer` dan `HabitatUi`. |
| `HabitatControlTest` (tanpa OpenGL) | PASS, 40 assertion | Seluruh kontrak gerak ortogonal/diagonal, collision dinding/sekat/kebun/fasilitas, jangkauan interaksi `INTERACT_MARGIN = 18f`, penguncian gerak saat modal terbuka, dan tombol `Esc`/`E` tetap lulus 100%. |
| `HabitatDesktopSmokeTest` (OpenGL/LWJGL3 & Scene2D nyata) | PASS, 29 assertion | Memverifikasi siklus penuh di window desktop: eksplorasi → dekati Panel Air → buka/tutup inspeksi (`Esc`) → dekati Terminal Robot → buka terminal → ketik ke `TextArea` (`editor` fokus aktif & gerak astronaut terkunci) → resize (`1440×900`, `900×600`, `640×480`) → tutup/buka ulang (draf bertahan) → keluar (`Esc` melepas fokus keyboard). |
| Inspeksi visual screenshot desktop (`1280×720`, `1440×900`, `900×600`, `640×480`) | PASS pada `1280×720`, `1440×900`, `900×600`; TERBACA PARSIAL pada `640×480` | `exploring.png` (`1280×720`), `inspection.png` (`1280×720`), `terminal.png` (`1280×720`), `terminal-1440x900.png` (`1440×900`), `terminal-900x600.png` (`900×600`), dan `terminal-640x480.png` (`640×480`) diperiksa langsung. Objek interaktif (`PANEL AIR [E]`, `TERMINAL ROBOT [E]`) memiliki bracket sudut, glow 2D, dan pipa penghubung yang membedakannya dari `SEKAT MODUL` dan `KEBUN: 2 Kering [!] | 2 Lembap [~]`. Tidak ada teks/tombol terpotong di dalam window; kebun dan astronaut di sisi kanan tetap terlihat saat floating terminal terbuka di kiri. Pada `640×480`, semua kontrol tetap di dalam kanvas virtual namun bitmap font bawaan mengalami artefak downscaling. |
| Keselarasan sprite pixel-art terhadap collider (`assets/habitat/`) | PASS | 11 PNG (`mars_soil_tile.png` `32×32`, `floor_tile.png` `32×32`, `partition_wall.png` `32×200`, `water_panel.png` `100×40`, `robot_terminal.png` `100×52`, `hydro_garden.png` `180×100`, `hydro_garden_restored.png` `180×100`, `hab_airlock.png` `144×112`, `pump_station.png` `96×88`, `mars_rover.png` `36×32`, `astronaut_eva.png` `24×28`) dirender dengan `TextureFilter.Nearest` tepat pada koordinat `HabitatWorld.Box` dan modul Outpost Mars. |
| Upgrade UI/UX Outpost Permukaan Mars (`07`–`11`) di `MarsReboot` | PASS (Visual & Smoke Desktop) | Toggle *Before/After* (`[B]`), *Field Log* (`[J]`), *Peta Misi 1–9* (`[M]`), *Pengaturan & Reduced Motion* (`[O]`), *Drawer Bantuan 3-Langkah* (`? BANTUAN (3)`), serta preview state *Normal / Error / Timeout / Stopped* di *Floating Terminal* diverifikasi lewat screenshot OpenGL `1280×720` (`exploring.png`, `exploring-after.png`, `inspection.png`, `terminal.png`, `terminal-error-help.png`, `field-log.png`, `mission-map.png`, `settings.png`). |
| Wrapper Gradle (`.\gradlew.bat`) & pengujian manual perangkat keras | BELUM DIJALANKAN | Distribusi Gradle 9.8.0 masih terhalang unduhan jaringan lingkungan; pengujian tahan tombol fisik OS yang lama, drag mouse manual oleh pengguna, dan clipboard/scroll panjang tetap membutuhkan verifikasi sesi interaktif manusia. |


## Konsolidasi konteks tim — 13 November 2026

Dokumentasi/skill dan referensi disiapkan untuk root repo libGDX; README lama diarsipkan, prototype web dipisahkan ke prototype-web, dan sumber awal dipertahankan. Verifikasi transfer memakai manifest hash, tautan/anchor Markdown, ID kebutuhan/pemetaan test, resource skill lengkap, serta pembandingan source Java/aset/build existing. Bukan pengujian fitur game, aplikasi AI di komputer teman, atau keberhasilan Wrapper. Catatan jumlah file/hash final berada di docs/setup/context-files.sha256.json setelah transfer; manifest adalah snapshot migrasi, bukan larangan mengedit dokumen pada sesi berikutnya. Tidak ada build/test aplikasi baru, commit/push atau deployment dari sesi ini.

## 6. Upgrade ketajaman UI (Hi-DPI) & grafis Outpost pixel art `2×` — 13 November 2026

Root: `C:\Users\abdie\OneDrive\Desktop\Folder_Abdiel\PRAKTIKUM\FINDIH\MarsReboot`. Kontrak non-visual (`HabitatWorld`, `HabitatInput`, `HabitatScreen`, `MarsRebootGame`, `Lwjgl3Launcher`, `StartupHelper`, `HabitatControlTest`, `HabitatDesktopSmokeTest`) diverifikasi tidak berubah lewat SHA-256 terhadap baseline (`lwjgl3/build/reports/habitat/source-sha256.txt`); hanya `HabitatRenderer.java`, `HabitatUi.java`, `assets/habitat/`, `assets/ui/`, dan `assets/assets.txt` yang diperbarui.

| Pemeriksaan | Hasil aktual | Bukti / batas |
| --- | --- | --- |
| Kompilasi `core` + `lwjgl3` (`javac --release 21 -encoding UTF-8 -Xlint:deprecation`) | PASS | Kompilasi penuh main + test terhadap `.gradle-local/manual/*` selesai tanpa error/warning; Wrapper `.\gradlew.bat` tidak dipakai karena unduhan Gradle 9.8.0 offline terhalang jaringan. |
| `com.marsreboot.habitat.HabitatControlTest` | PASS, 40 assertion | Kontrak gerak, collision, jarak interaksi, penguncian input, dan kontrol `Esc`/`E` lulus tanpa OpenGL. |
| `com.marsreboot.habitat.HabitatDesktopSmokeTest` | PASS, 29 assertion | Stage virtual `960×600`, fokus editor `TextArea`, `Esc` keluar modal, pertahanan draf, dan resize `1440×900`, `900×600`, `640×480` lulus di LWJGL3/OpenGL nyata. |
| Inspeksi visual screenshot (`exploring.png`, `inspection.png`, `terminal.png`, `terminal-1440x900.png`, `terminal-900x600.png`, `terminal-640x480.png`) | PASS | Font Hi-DPI (`ui_sans`, `ui_sans_bold`, `ui_mono` `30px` mipmapped) dan border NinePatch 3× supersampled terbaca tajam tanpa clipping; sprite `2×` regolith multivarian, kawah, batu, walkway, Hab Airlock, Panel Surya, Terminal, Bio-Dome + kanopi transparan + 4×4 kebun + rover, Pompa Utama, dan astronaut 4 arah selaras dengan collider dan pencahayaan radial aditif. |
| Wrapper Gradle (`.\gradlew.bat`) & pengujian input/drag manual OS | BELUM DIJALANKAN | Pengujian drag jendela/scroll mouse serta penekanan tombol fisik jangka panjang perlu verifikasi interaktif manual; Wrapper perlu diuji di lingkungan dengan distribusi Gradle tersedia. |

## 7. Domain robot dan kandidat puzzle 1 — 13 November 2026

Target: root MarsReboot, branch main dengan perubahan lokal sesi ini; Windows 11 amd64. Wrapper Gradle 9.8.0, Launcher JVM Oracle 25.0.4.1; source/target tetap 21 dan konfigurasi daemon existing meminta Java 21. Tidak mengubah versi libGDX 1.14.2/LWJGL 3.4.3/app 1.0.0 atau dependency. Distribusi/dependency tersedia dari cache lokal; offline pada mesin ini tidak menjamin clone baru dapat membangun tanpa jaringan.

| Pemeriksaan aktual | Hasil | Cakupan/batas |
| --- | --- | --- |
| Kompilasi dua file robot dengan `javac --release 21 -encoding UTF-8 -Xlint:all`, lalu main RobotSimulationTest | PASS, 81 assertion | Domain Java murni; test awal gagal sebelum implementasi dan guard batas. Bukan runtime interpreter. |
| `.\gradlew.bat --offline :core:robotTest :core:controlTest :lwjgl3:compileJava --console=plain` | PASS, exit 0 | 81 assertion robot, 40 assertion kontrol existing, source desktop berhasil dikompilasi melalui Wrapper aktual. |
| `.\gradlew.bat --offline :lwjgl3:jar --console=plain` | PASS, exit 0 | `lwjgl3/build/libs/MarsReboot-1.0.0.jar` terbentuk; ZIP memuat `com/marsreboot/robot/RobotSimulation.class`. Belum diluncurkan sebagai paket distribusi. |
| Perbandingan SHA-256 terhadap baseline awal sesi | PASS | 72 file existing Java/aset/build selain core/build.gradle identik; dua source/test robot baru dan task robotTest ditambahkan. |

SHA-256 JAR saat diperiksa: `fab3d446900dd5ea6ce4c7dee40c16ac585a86ef13af82fb9f714daca87f5ad9`. Artefak ada di build lokal (dapat diabaikan Git), bukan rilis yang dipublikasikan.

Test robot mencakup referensi p1-a (3 aksi/2 panen/1 gerak), target kosong/parsial, empat arah/tepi/penghalang, panen young/empty, sensor, reset dan independensi instance, salinan fixture/snapshot, argumen salah, tepat/melewati batas aksi/API, hasil terminal/idempotensi serta error setelah target tercapai. Ini subset DOM-01/02/03/05/06/07/08; tidak membuktikan pelanggaran puzzle 3, siram, latihan konsep, multi-skenario, skor atau produk lengkap. `finish()` hanya akhir normal skenario domain.

Tidak menjalankan ulang window/OpenGL/desktopSmokeTest, Python/Stop/coordinator, backend, komputer kedua atau playtest. Terminal dan renderer existing belum memakai domain baru, sehingga belum ada perubahan gameplay puzzle yang tampak saat Run game. M-10/M-16 tetap parsial; konfirmasi bahasa/runtime/database/distribusi dan aturan kandidat masih terbuka. Tidak ada commit/push/deployment.

## 8. Integrasi puzzle 1 dan paket Windows — 13 November 2026

Root MarsReboot, branch main dengan perubahan lokal; tidak ada commit/push/deployment. Windows 11 amd64. Wrapper 9.8.0; task JavaExec aktual memakai Eclipse Adoptium 21 di cache Gradle, Launcher JVM Oracle 25.0.4.1. CPython 3.15.2 embeddable x64 diambil dari URL resmi dan dikunci manifest SHA-256 `d012f3aa29cf07436dae8aea1fec0fd195c2612a0b059b1f915ae0afb9ad1f62`. Tidak mengubah versi Java target/libGDX/LWJGL atau memasang dependency/global Python.

| Command / pemeriksaan aktual | Hasil |
| --- | --- |
| `:core:robotTest :core:controlTest` melalui Wrapper offline | PASS: 81 robot + 40 kontrol assertion |
| `:core:runtimeTest` | PASS: 33 assertion controller/protokol, termasuk hasil lama dan replay |
| `:core:pythonIntegrationTest` | PASS: 33 assertion pada Python bundel nyata; terakhir setelah perbaikan resolusi lokasi IntelliJ/JAR |
| `:lwjgl3:desktopSmokeTest` | PASS: 29 assertion desktop existing |
| `:lwjgl3:puzzleDesktopTest` | PASS: 14 assertion pointer events, Python, source capture, Stop/Reset, error, Esc/reopen dan resize |
| `:lwjgl3:bundleWindows` | PASS: ZIP + JAR + runtime/lisensi/manifest/petunjuk dihasilkan |
| JAR dari ZIP, folder ekstraksi berisi spasi, PATH hanya `C:\Windows\System32` | PASS ulang: 33 Python + 14 desktop assertion memakai Python dalam paket dan Java 21 absolut; PYTHONHOME/PYTHONPATH tidak dipakai |

Semua task dijalankan melalui `.\gradlew.bat --offline ... --console=plain` (distribusi/dependency telah ada di cache). Sebelum implementasi, test referensi gagal kompilasi karena kelas runtime belum ada, lalu lulus. Pengujian desktop baru sempat gagal akibat callback resize reentrant pada harness, diperbaiki dengan memajukan fase sebelum native resize; pengujian ulang lulus. Ini tidak disamarkan sebagai bug gameplay.

Integrasi Python mencakup reference 3 aksi, target parsial/kosong, sensor/nilai balik, invalid argument, syntax/error baris, error setelah target, tepat/melebihi batas aksi/request/source/output UTF-8, output flood, frame palsu/berlebih, exit tanpa finished/nonzero sesudah finished, stderr flood, missing runtime, startup/compute timeout, Stop CPU loop, Run ulang serta lokasi runtime tanpa property/PATH.

Paket: `lwjgl3/build/distributions/MarsReboot-1.0.0-windows-x64.zip`, SHA-256 `addfa8c8f25e1a08ee85dc9bff4251e8c1cd72164c95af24136501097e67696d`. Report mesin dan lokasi ekstraksi: `lwjgl3/build/reports/puzzle/package-smoke.json`. Screenshot desktop dan paket di `lwjgl3/build/reports/puzzle/` dan subfolder `packaged/`: p1-success, p1-error, p1-1440x900, p1-900x600, p1-640x480. Screenshot diperiksa: robot/indikator tanaman, output error/sukses dan kontrol tidak clipping; teks 640x480 tetap kecil dengan FitViewport existing, belum klaim penerimaan keterbacaan manusia.

Batas: uji PATH pada mesin ini bukan komputer fisik kedua; test pointer Scene2D bukan penahanan keyboard/mouse OS manusia. Backend, skor/save lintas restart, multi-skenario, siram/puzzle 2–9, persetujuan aslab dan playtest tetap terbuka. Hanya progres p1 selama sesi, bukan seluruh habitat. Kontrak implementasi di [PYTHON_RUNTIME](docs/mars/PYTHON_RUNTIME.md).

## 9. Penyempurnaan UI, keterbacaan `18` unit UI, dan alur Puzzle 1 — 14 November 2026

Komando verifikasi offline dijalankan dari root `MarsReboot`:

```text
.\gradlew.bat --offline :core:controlTest :core:robotTest :core:runtimeTest :core:pythonIntegrationTest :lwjgl3:desktopSmokeTest :lwjgl3:puzzleDesktopTest --console=plain
```

Hasil teramati: seluruh tugas keluar `0`:
- `:core:controlTest`: `PASS: 40 habitat control assertions (no OpenGL).`
- `:core:robotTest`: `PASS: 81 robot simulation assertions (no libGDX/Python).`
- `:core:runtimeTest`: `PASS: 33 controller/protocol assertions.`
- `:core:pythonIntegrationTest`: `PASS: 33 bundled Python integration assertions; executable=...\MarsReboot\build\python\windows-x64`
- `:lwjgl3:desktopSmokeTest`: `PASS: 29 real desktop/Scene2D assertions; OS held keys not simulated.`
- `:lwjgl3:puzzleDesktopTest`: `PASS: 41 Python puzzle desktop assertions; pointer events, real CPython and OpenGL.`

Pemeriksaan visual PNG pada `lwjgl3/build/reports/habitat/` (`inspection.png`, `terminal.png`) serta `lwjgl3/build/reports/puzzle/` (`p1-success.png`, `p1-error.png`, `p1-1440x900.png`, `p1-900x600.png`, `p1-640x480.png`, `p1-mission-before.png`, `p1-mission-after.png`, `p1-settings-preview.png`, `p1-long-code-output.png`, `p1-help-dragged.png`) memverifikasi: teks ~`18` unit UI (`~12px` di `640×480`) tanpa pemotongan horizontal, terminal kiri `<= 560×520` menjaga Bio-Dome terlihat di kanan, gutter baris terlihat & output scroll berbatas tidak menggeser kontrol `Run`/`Stop`/`Reset`, Peta Misi P1 berbasis `RunCoordinator.View` (`TERSEDIA` → `SELESAI`, bertahan meski gagal ulang) & P2–9 jujur `BELUM TERSEDIA`, `Preview Visual` dipindahkan ke `[O] Setelan`, dan petak panen 2×1 memisahkan `roverTexture`, tanaman matang/kosong, `(0,0)`/`(1,0)`, serta penanda lokal `PANEN P1 SELESAI (SESI)` secara jelas.

Batas terbuka: paket `:lwjgl3:bundleWindows` belum dibangun ulang setelah perubahan UI 14 Nov; drag mouse/penahanan keyboard OS manusia, komputer fisik kedua, save lintas restart, backend Spring Boot, serta puzzle 2–9 belum diuji.

## 10. Desain ulang Dunia 1680×1040, Follow Camera, Interaksi Rover Langsung & Terminal Resizable — 15 November 2026

Komando verifikasi offline dijalankan dari root `MarsReboot` setelah seluruh edit kode (`HabitatWorld`, `FollowCamera`, `HabitatInput`, `HabitatRenderer`, `HabitatScreen`, `HabitatUi`, suite uji, serta baker aset):

```text
.\gradlew.bat --offline :core:controlTest :core:robotTest :core:runtimeTest :core:pythonIntegrationTest :lwjgl3:desktopSmokeTest :lwjgl3:puzzleDesktopTest --console=plain
```

Hasil teramati: seluruh tugas keluar `0`:
- `:core:controlTest`: `PASS: 46 habitat control assertions (no OpenGL).`
- `:core:robotTest`: `PASS: 81 robot simulation assertions (no libGDX/Python).`
- `:core:runtimeTest`: `PASS: 33 controller/protocol assertions.`
- `:core:pythonIntegrationTest`: `PASS: 33 bundled Python integration assertions; executable=C:\Users\abdie\OneDrive\Desktop\Folder_Abdiel\PRAKTIKUM\FINDIH\MarsReboot\build\python\windows-x64`
- `:lwjgl3:desktopSmokeTest`: `PASS: 30 real desktop/Scene2D assertions; OS held keys not simulated.`
- `:lwjgl3:puzzleDesktopTest`: `PASS: 45 Python puzzle desktop assertions; pointer events, real CPython and OpenGL.`

Pemeriksaan visual PNG di `lwjgl3/build/reports/habitat/` dan `lwjgl3/build/reports/puzzle/` (`p1-biodome-explore.png`, `p1-inspection.png`, `p1-mission-after.png`, `p1-settings-preview.png`, `p1-help-dragged.png`, `p1-long-code-output.png`, `p1-success.png`, `p1-error.png`, `p1-640x480.png`, `p1-1440x900.png`) memastikan:
- Dunia `1680×1040` tidak tampil penuh sekaligus (`FollowCamera` `ExtendViewport(720,450)` + *smart terminal framing* membingkai Bio-Dome bersih di kanan).
- HUD kapsul mengambang (`ExtendViewport(960,600)`) dan panel/terminal rounded 3× supersampled bebas sudut kotak tajam serta bebas tumpang tindih dengan objek penting.
- Klik langsung pointer pada sprite Rover di dalam dunia membuka editor Python; corner-drag pointer resize memperbesar/memperkecil `terminal` tanpa menggeser kontrol `Run`/`Stop`/`Reset`.
- Popover `[?] API` (`336×294`), output scroll berbatas, tombol `Stop` berkontras tinggi, badge `SELESAI`, penanda `PANEN BIO-DOME SELESAI`, `+1 PANEN`, sinar pemindai rover, dan efek halo status tampil utuh pada `1280×720`, `1440×900`, `900×600`; pada `640×480` kontrol tidak terpotong namun huruf font tetap halus/kecil karena downscale resolusi rendah.

Batas terbuka: paket `:lwjgl3:bundleWindows` belum dibangun ulang pasca-ubah visual 15 Nov; penahanan keyboard/mouse fisik OS manusia pada jendela interaktif, komputer fisik kedua, save lintas restart, backend Spring Boot, serta puzzle 2–9 tetap belum dijalankan.

## 11. Refocus 2× Crisp Pixel Art di Atas Peta `1680×1040`, FollowCamera & Klik Rover — 15 November 2026

Komando verifikasi offline dijalankan dari root `MarsReboot` pasca pembaruan aset pixel art (`assets/habitat/*.png`, `hydro_crops.png`, `assets/assets.txt`, `asset-manifest.json` v4) dan refactor `HabitatRenderer` (`TextureFilter.Nearest`):

```text
.\gradlew.bat --offline :core:controlTest :core:robotTest :core:runtimeTest :core:pythonIntegrationTest :lwjgl3:desktopSmokeTest :lwjgl3:puzzleDesktopTest --console=plain
```

Hasil teramati (`task-700`, `exit 0`):
- `:core:controlTest`: `PASS: 46 habitat control assertions (no OpenGL).`
- `:core:robotTest`: `PASS: 81 robot simulation assertions (no libGDX/Python).`
- `:core:runtimeTest`: `PASS: 33 controller/protocol assertions.`
- `:core:pythonIntegrationTest`: `PASS: 33 bundled Python integration assertions; executable=C:\Users\abdie\OneDrive\Desktop\Folder_Abdiel\PRAKTIKUM\FINDIH\MarsReboot\build\python\windows-x64`
- `:lwjgl3:desktopSmokeTest`: `PASS: 30 real desktop/Scene2D assertions; OS held keys not simulated.`
- `:lwjgl3:puzzleDesktopTest`: `PASS: 45 Python puzzle desktop assertions; pointer events, real CPython and OpenGL.`

Pemeriksaan visual PNG terkini (`outpost-biodome.png`, `terminal.png`, `p1-biodome-explore.png`, `p1-success.png`, `p1-error.png`, `p1-help-dragged.png`, `p1-settings-preview.png`, `p1-1440x900.png`) memastikan:
- Dunia `1680×1040` tampil tajam sebagai **2× Crisp Pixel Art** (`TextureFilter.Nearest`) dengan regolith Mars bertekstur, pipa industri berflensa, konservatori Bio-Dome oktagonal + kanopi lengkung kaca piksel, sprite tanaman `RIPE`/`YOUNG`/`EMPTY` beroutline gelap tegas, dan Mars Rover hadap Timur dengan braket L-corner saat disorot/dekat.
- `FollowCamera` (`720×450`), klik langsung sprite Rover untuk membuka editor Python, penggeseran & corner-drag resize terminal, popover `[?] API`, `[Log]` auto-collapse, animasi halus Rover, dan status `RunCoordinator.View` (`SELESAI`, `ERROR`, `+1 PANEN`) tetap bekerja identik tanpa clipping.

Batas terbuka: `:lwjgl3:bundleWindows` belum dibangun ulang dengan perubahan pixel-refocus 15 Nov; playtest interaktif manusia pada OS, komputer fisik kedua, save lintas restart, backend Spring Boot, serta puzzle 2–9 tetap belum dijalankan.

## 12. Pemilihan P1/P2 dan paket terbaru — 19 November 2026

Windows x64, Wrapper 9.8.0/Java target 21, CPython bundel 3.15.2; versi/dependency tetap. Pemeriksaan aktual melalui Wrapper offline:

| Task / pemeriksaan | Hasil aktual |
| --- | --- |
| :core:puzzleProgressTest | PASS referensi P2 (4 panen, 7 aksi) + 12 kasus progres/negatif, interpreter nyata |
| :core:robotTest / :core:controlTest | PASS 81 robot / 46 kontrol assertion |
| :core:runtimeTest / :core:pythonIntegrationTest | PASS 33 controller/protokol / 33 integrasi Python existing |
| :lwjgl3:desktopSmokeTest | PASS 30 desktop assertion |
| :lwjgl3:puzzleDesktopTest | PASS 57 pemeriksaan P1/P2 pada OpenGL, pointer events dan CPython nyata |
| :lwjgl3:bundleWindows | PASS, ZIP terbaru dibangun ulang setelah koreksi UI/socket P2 |
| Test progres dan desktop dengan JAR/CPython dari ZIP, folder spasi, PATH hanya Windows System32 | PASS progres P2 dan 57 desktop checks; tidak bergantung Python sistem |

Kasus tambahan mencakup target parsial, error setelah target P2 tercapai, melebihi batas 12 aksi, P2 terkunci sebelum P1, larangan pindah saat Run, replay membuka P2, isolasi/reset/progres per puzzle, frame P1 ditolak pada P2, pemilih terminal nyata serta pemulihan draf setelah kembali P1/P2. Test baru mula-mula gagal kompilasi pada Gradle karena tipe konten belum ada, lalu lulus implementasi. Snapshot p2-success.png memperlihatkan empat petak dan teks 4 dipanen/7 aksi; koreksi socket sempit serta ringkasan hardcode P1 diuji kembali.

ZIP SHA-256: `01269c57afbc5ebaeef5c543525913b8b70a51418ebff1f703e567949a8704c3`. Laporan mesin/ekstraksi ada di lwjgl3/build/reports/puzzle/p2-package-smoke.json. Screenshot development di lwjgl3/build/reports/puzzle/p2-success.png. Build artefact/cache tetap diabaikan Git. Desain/aset lokal Antigravity tidak direvert.

Batas: EDU-01/verifikator penggunaan loop masih belum dipilih; solusi unrolled dapat lulus tujuan dunia. Tidak ada klaim penilaian latihan for otomatis, penerimaan manusia, komputer fisik kedua, backend/save atau puzzle 3–9. Tidak ada commit/push/deployment.

## 13. P3 sensor/young dan paket Windows — 19 November 2026

Pemeriksaan aktual melalui Wrapper offline pada Windows x64, Java target 21 dan CPython bundel 3.15.2. Versi/dependency tetap. Test awal gagal kompilasi karena PuzzleDefinition.THREE belum tersedia; sesudah implementasi:

| Pemeriksaan | Hasil aktual |
| --- | --- |
| :core:puzzleProgressTest | PASS reference P1/P2/P3 + 23 pemeriksaan progres/sensor/negatif, interpreter nyata |
| :core:robotTest / :core:runtimeTest | PASS 81 domain / 33 controller-protokol |
| :core:controlTest / :core:pythonIntegrationTest | PASS 46 kontrol / 33 integrasi Python existing |
| :lwjgl3:desktopSmokeTest | PASS 30 desktop checks |
| :lwjgl3:puzzleDesktopTest | PASS 71 desktop checks P1/P2/P3; terakhir sesudah koreksi judul pelanggaran muda |
| :lwjgl3:bundleWindows | PASS, ZIP baru |
| JAR/Python dari ZIP pada folder spasi, PATH hanya C:\Windows\System32 | PASS test progres + 71 desktop checks tanpa ketergantungan Python sistem |

Referensi P3 menghasilkan 2 panen/5 aksi/9 request; young tetap YOUNG. Source panen semua petak mendapat INCOMPLETE, invalidHarvestCount=2, remainingTargets kosong dan tanpa progres; domain/runtime sepakat. Error sesudah referensi tetap ERROR. P3 tidak bisa dipilih sebelum P2 lulus; setelah kegagalan bisa Reset/Run ulang, completion dan draf puzzle lama tetap. UI menampilkan pesan young secara khusus, dan target 2 berbeda dari jumlah kolom 4. Screenshot development di lwjgl3/build/reports/puzzle/p3-success.png dan p3-young-violation.png diperiksa.

ZIP SHA-256: `c34345619a0f92ca4cec56b8b65d9838dd075fdaa3ae76ceaed129f17df14e61`. Laporan lokasi ekstraksi/environment: lwjgl3/build/reports/puzzle/p3-package-smoke.json. Tidak ada klaim uji komputer fisik kedua, playtest manusia, backend/save, persetujuan aslab atau P4–P9. Konsep source sensor/loop belum diverifikasi otomatis. Tidak ada commit/push/deployment.

## 14. Editor Python: indentasi dan pasangan otomatis — 19 November 2026

Windows x64, Java target 21 dan libGDX existing; tidak menambah dependency. Pemeriksaan aktual:

| Pemeriksaan | Hasil |
| --- | --- |
| :core:editorTest (ikut check) | PASS 25 transformasi teks/kursor/seleksi |
| :lwjgl3:puzzleDesktopTest | PASS 90 pemeriksaan desktop, termasuk 19 editor/program yang diketik dan seluruh P1–P3 existing |
| :lwjgl3:desktopSmokeTest / :core:controlTest | PASS 30 desktop existing / 46 kontrol |
| :lwjgl3:bundleWindows | PASS ZIP terbaru |
| JAR aplikasi dari ZIP + CPython paket, folder spasi, PATH hanya Windows System32 | PASS ulang 25 editor + 90 desktop checks |

Task dijalankan melalui Wrapper offline. Test awal gagal pada simbol transformasi yang belum dibuat, lalu lulus implementasi. API source libGDX 1.14.2 dibaca dari sources.jar cache proyek untuk subclass/listener/Undo. Perbedaan semantik selection kosong dan posisi kursor ditemukan saat uji widget dan diperbaiki. Update caret/scroll memakai jalur native TextArea; perubahan teks diteruskan melalui listener native agar Undo tidak dihapus oleh setText.

Bukti meliputi colon for/if dan trailing comment, colon dalam string/komentar, indent baris biasa/continuation, tab stop, ShiftTab, seleksi baris termasuk batas newline, quote/bracket wrap/skip/delete, tab lama dari paste yang dipertahankan, fokus, Undo, blok 100 baris, serta source loop/if yang diketik tanpa spasi manual dan dieksekusi sebagai referensi P2. Ctrl/Shift pada harness memakai proxy pembacaan modifier Input; bukan pengujian keyboard fisik OS/IME. Tidak ada klaim dukungan formatter/LSP/completion semantik penuh.

ZIP SHA-256: `e93df3e4734d1ba7715685dbb2ccbd90947c4a525f3ea8eace93df5d3978bcab`. Laporan ekstraksi/environment: lwjgl3/build/reports/puzzle/editor-package-smoke.json. Komputer kedua dan playtest manusia belum dilakukan; Python/runtime/gameplay/back-end tidak diubah oleh patch editor. Tidak ada commit/push/deployment.

## 15. Irigasi P4 dan dua skenario — 19 November 2026

Windows x64, Gradle Wrapper 9.8.0, target Java 21, CPython bundel 3.15.2. Versi/dependency/aset visual tidak diubah. Target lengkap tetap 9 puzzle/3 misi, tahap awal 1–4.

| Task / pemeriksaan aktual | Hasil |
| --- | --- |
| :core:check | PASS: 46 kontrol, 25 editor, 81 robot, 33 controller/protokol, 21 domain air; suite main JavaExec dijalankan |
| :core:pythonIntegrationTest | PASS 33 integrasi Python existing |
| :core:waterPuzzleTest | PASS 33 multi-skenario/runtime/progres P4, interpreter nyata |
| :lwjgl3:desktopSmokeTest | PASS 30 desktop checks |
| :lwjgl3:puzzleDesktopTest | PASS 109 desktop checks P1–P4 + editor; terakhir setelah koreksi frame akhir A dan FX pergantian |
| :lwjgl3:bundleWindows | PASS, ZIP dengan irigasi dua skenario |
| JAR/CPython dari ZIP, folder spasi, PATH hanya C:\Windows\System32 | PASS ulang 33 P4, 25 editor dan 109 desktop checks |
| Repack PETUNJUK.txt terakhir | PASS, 43 file non-petunjuk hash-identik dengan paket yang teruji |

Semua task melalui Wrapper offline dengan cache existing. Test baru mula-mula gagal kompilasi pada FOUR yang belum tersedia. Core:check sempat gagal di core:test karena suite repo bukan JUnit; failOnNoDiscoveredTests=false di core mengizinkan ketiadaan kasus JUnit, bukan menonaktifkan Test/check atau mengabaikan kegagalan suite main. Semua lima suite JavaExec wajib di check tetap berjalan. Property diperiksa pada [dokumentasi Gradle Test](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.testing.Test.html#org.gradle.api.tasks.testing.Test:failOnNoDiscoveredTests); konfigurasi terbukti pada Wrapper proyek, bukan klaim upgrade Gradle.

Bukti P4: masing-masing scenario 5 aksi/2 air/9 API call; total 10 aksi/4 air. P4-a dry x=0,2; p4-b x=1,3. Hardcode A gagal B, program boros gagal pertama (fail-fast), namespace baru setiap proses, error setelah target, Stop/timeout di proses kedua, frame A ditolak B, dan output per Run dibatasi tanpa salinan log subhasil. Air habis tetap memakai aksi, tidak negatif; panen tidak menyelesaikan target siram. Replay tidak memberi progres dari skenario pertama dan frame akhirnya terlihat sebelum memulai B; Stop di antara replay, Reset, lalu Run ulang bersih diuji.

Screenshot OpenGL p4-dry-start.png, p4-hardcode-failure.png dan p4-success.png di lwjgl3/build/reports/puzzle/ diperiksa: empat petak dengan label KERING/LEMBAP, sisa air, nomor skenario, hasil 2/2 serta total. Desain Antigravity/editor existing dipertahankan.

ZIP akhir: lwjgl3/build/distributions/MarsReboot-1.0.0-windows-x64.zip, SHA-256 `fd45d6f647ab48671d323d8b3efd9b494a08de880ee341b9acbd6429ce3b1463`. Report: lwjgl3/build/reports/puzzle/p4-package-smoke.json. Sesudah uji aplikasi, PETUNJUK.txt saja diperbarui; hash semua 43 file lainnya (termasuk JAR dan runtime) identik dengan ekstraksi teruji. Repack ini tidak menjalankan ulang seluruh aplikasi karena biner tidak berubah.

Batas: belum komputer fisik kedua/playtest manusia, backend/autentikasi/skor, save lintas restart, persetujuan aslab, atau P5–P9. Memenuhi tujuan dunia tidak membuktikan source memakai sensor/loop; tidak ada verifikator semantik baru. Full root build/CI/backend tidak diklaim. Tidak ada commit/push/deployment.

## 16. Gate demo tim, root build dan audit OOP — 19 November 2026

Pada source gameplay P1–P4 yang sama, root `build` awalnya gagal di `:lwjgl3:test`: sumber test berisi main JavaExec, bukan metode JUnit yang dapat ditemukan Gradle. Konfigurasi `failOnNoDiscoveredTests=false` diterapkan pada lwjgl3, konsisten dengan core. Task test tidak dinonaktifkan dan kegagalan assertion/exit JavaExec tidak diabaikan. Build biasa tetap mengompilasi test desktop; menjalankannya perlu gate desktop eksplisit.

Ditambahkan task root verifyDemo (suite Java domain/editor/lifecycle, integrasi Python nyata P1–P4, check/JAR desktop) dan verifyDesktopDemo (gate tersebut ditambah dua suite desktop). Tidak ada dependency/versi runtime atau source gameplay yang diubah dalam sesi gate ini.

Command aktual dari root pada Windows x64 dengan cache tersedia:

```powershell
.\gradlew.bat --offline build verifyDesktopDemo :lwjgl3:bundleWindows --console=plain
```

Hasil **exit 0**: root build dan kedua gate selesai; kontrol 46, editor 25, robot 81, controller/protokol 33, domain air 21; referensi P1/P2/P3 + 23 pemeriksaan progres/sensor/negatif; integrasi CPython 33; integrasi P4 33; desktop/Scene2D 30 dan desktop puzzle/editor 109. Semua suite di atas benar-benar mencetak PASS pada run ini. Suite desktop memakai OpenGL/CPython nyata, bukan browser. Penekanan tombol OS manual dan playtest manusia tetap belum dibuktikan.

bundleWindows selesai; SHA-256 ZIP tetap `fd45d6f647ab48671d323d8b3efd9b494a08de880ee341b9acbd6429ce3b1463`, identik dengan artefak §15 yang telah diuji dari folder spasi tanpa Python sistem pada PATH. Uji ekstraksi paket tidak diulang karena biner/artefaknya identik. ZIP/TAR generik task application build bukan paket Python; pemain memakai artefak `*-windows-x64.zip` dari bundleWindows.

[Audit OOP](docs/mars/OOP_AUDIT.md) memetakan source dan test yang ada terhadap empat pilar dan enam kandidat. Bukti pewarisan framework tidak disamakan dengan hierarki misi; switch/enum/pipeline private tidak dinyatakan sebagai enam pattern GoF. M-23 tetap terbuka untuk implementasi, kesesuaian materi dan demo. Belum ada bukti CI remote, backend, komputer kedua atau persetujuan aslab. Tidak ada commit/push/deployment.

Pemeriksaan dokumen: 44 target tautan relatif pada README, ARCHITECTURE, TASKS, VERIFICATION dan OOP_AUDIT ditemukan; git diff --check untuk file sesi ini lulus. Pemeriksaan tautan menguji keberadaan file, bukan anchor heading atau tautan internet.

## 17. Implementasi Command dan Strategy — 19 November 2026

Runtime Python kini membuat MoveCommand/HarvestCommand/WaterCommand sesudah validasi frame/argumen, lalu memanggil RobotCommand.execute(receiver). Sensor tetap query tanpa trace aksi. Domain tetap memiliki aturan biaya, mutasi dan error. PuzzleDefinition memilih EvaluationStrategy untuk panen (P1/P2), panen matang dengan pemeriksaan young (P3) dan irigasi (P4). RobotSimulation.finish(strategy) hanya menerapkan SUCCESS/INCOMPLETE dari snapshot immutable saat state ACTIVE; overload finish() dan finish(boolean) tetap kompatibel. Tidak ada perubahan protokol, fixture, versi, desain atau backend.

Test awal command gagal kompilasi karena kelas belum ada, kemudian lulus 4 pemeriksaan. Slice strategi juga gagal karena kelas belum ada, lalu lulus 6 pemeriksaan bersama robotTest 81 dan waterSimulationTest 21. Suite diperluas menjadi **64 pemeriksaan** untuk kesetaraan command/direct domain (termasuk young/kosong/terhalang/arah invalid/null/air habis/limit), strategi read-only, strict vs dasar pada snapshot sama, pesan irigasi, overload lama, idempotensi serta error/limit yang tidak dievaluasi ulang.

Command aktual, Windows x64, JDK target 21 dan CPython bundel 3.15.2:

```powershell
.\gradlew.bat --offline verifyDemo --console=plain
.\gradlew.bat --offline verifyDesktopDemo :lwjgl3:bundleWindows --console=plain
```

Keduanya **exit 0**. Run terakhir sesudah integrasi melalui interface: kontrol 46, editor 25, command/strategy 64, robot 81, controller/protokol 33, domain air 21; referensi P1/P2/P3 + 23 progres/sensor/negatif; integrasi CPython 33; P4 33; desktop Scene2D 30 dan desktop puzzle/editor 109. Ini menjalankan CPython/OpenGL nyata; bukan playtest manusia atau penekanan tombol fisik OS.

ZIP baru diuji ulang setelah JAR berubah. Ekstraksi ke `lwjgl3/build/Command Strategy Package With Spaces 92213fc6a6bd42c880a304286996bef3`; PATH hanya `C:\Windows\System32`, PYTHONHOME/PYTHONPATH dikosongkan. Kelas aplikasi hanya berasal dari fat JAR paket; test harness dari direktori test terkompilasi. Java 21 dipanggil lewat path absolut, mars.pythonDir menunjuk runtime/python paket. **PASS**: 64 command/strategy, 33 integrasi Python (termasuk resolusi runtime sebelah JAR tanpa property), referensi P1–P3 + 23 progres, 33 P4, 109 desktop puzzle/editor. Semua proses test exit 0. Report: `lwjgl3/build/reports/puzzle/pattern-package-smoke.json`; script reproduksi lokal di `build/verify-pattern-package.ps1` (keduanya artefak diabaikan Git).

Artefak: `lwjgl3/build/distributions/MarsReboot-1.0.0-windows-x64.zip`, SHA-256 `4dc4c09107d99c049c29750cdbbedcc38e6274fc1753cc1912579c6b6ade3531`. Hash lama §15–16 tetap bukti historis, bukan identitas paket terbaru.

Audit OOP diperbarui dengan relasi kelas dan test dua pattern. Empat kandidat lain serta penerimaan aslab masih terbuka; M-23 tidak dicentang selesai. Komputer kedua, playtest manusia, backend dan P5–P9 belum dibuktikan. Tidak ada commit/push/deployment.

## 18. Home, akun, scoreboard P1, setelan dan integrasi Java — 22 November 2026

Scope sesi: Home baru (daftar/masuk/tamu), scoreboard P1, tiga setelan tersimpan, pembukaan scoreboard dari Peta Misi saat bermain, penyegaran rekor sesudah login, endpoint leaderboard dan dokumentasi. Logika puzzle/robot tidak diubah.

| Pemeriksaan aktual | Hasil | Batas |
| --- | --- | --- |
| `javac --release 21` source `core` + libGDX 1.14.2 dari cache | PASS | Kompilasi Java saja; tidak menjalankan Gradle, test, window atau OpenGL. |
| `javac --release 21` source Spring backend dengan dependency JAR cache, annotation processing dimatikan | PASS | Kompilasi Java saja; tidak menguji bootstrap Spring, database, endpoint atau query JPA saat runtime. |
| `git diff --check` | PASS setelah seluruh perubahan sesi | Tidak menilai compile, API, atau perilaku UI. |
| `gradlew.bat :core:compileJava :lwjgl3:compileJava --offline --console=plain` | FAIL sebelum kompilasi | Gradle tidak dapat memuat `native-platform.dll` pada Windows 11 amd64. Ini kendala startup Gradle, bukan hasil compile Gradle. |
| UI Home/Peta Misi, auth baru dan ranking multiakun di game | BELUM DIJALANKAN | Belum dapat meluncurkan ulang desktop untuk sesi ini. Peta Misi memuat 5 baris teratas; Home memuat daftar sampai 10. |

Catatan: pada sesi sebelumnya pengguna menjalankan backend dan membuktikan register, login, submit skor, serta baca rekor pribadi lewat PowerShell. Itu bukan verifikasi untuk source scoreboard terbaru. Backend lokal memakai `127.0.0.1:8081`; anggota komputer lain belum berbagi database. Tidak mengklaim test aplikasi atau visual desktop terbaru lulus.

## 19. Polish visual, umpan balik, bantuan, judul dan SFX — 23 November 2026

Perubahan presentasi dan konten tampilan; tidak menyentuh HabitatWorld, RobotSimulation, fixture atau protokol. Fitur: petak kering/lembap terbaca (retak vs kilau, kilat cyan + percik saat petak menjadi basah); sorot koral dan siku pada petak belum selesai saat gagal; penanda `!N` di gutter pada baris error (`parseErrorLine`); bantuan 3 tingkat pada [?] (konsep → API → kerangka sebagian, tanpa solusi penuh); bintang efisiensi (`referenceActions` P1=3, P2=7, P3=5, P4=10 total; 3 bintang bila ≤ referensi, 2 bila ≤ separuh jalan ke batas, selain itu 1); ledakan partikel dan guncangan kamera saat sukses (mati bila Kurangi Animasi aktif); layar judul `TitleScreen` dengan tiga baris cerita; Field Log `[J]` per puzzle; lima SFX WAV sintetis (`assets/audio/`, dibuat `tools/GenerateSfx.java`, tercatat di `asset-manifest.json`) dengan tombol Efek Suara di Setelan.

Command aktual:

```powershell
.\gradlew.bat --offline build verifyDesktopDemo --console=plain
```

Run terakhir (sesudah SFX) **exit 0**: kontrol 49, editor 25, pattern 64, robot 81, controller/protokol 33, air 21, referensi P1–P3 + progres, Python 33, P4 33, desktop Scene2D 30, desktop puzzle 109, dan `titleDesktopTest` baru (waktu cerita, screenshot, Enter memulai tepat sekali; OpenGL nyata).

Belum dibuktikan: suara benar-benar terdengar (tidak ada test audio; volume/nada belum didengar), animasi partikel/denyut dan peralihan judul→habitat pada peluncur nyata, playtest manusia, dan `bundleWindows` ulang (hash ZIP §17 bukan identitas paket terbaru). Ambang bintang, petunjuk, dan teks cerita adalah usulan, belum disetujui tim. Keterbatasan: P3 posisi panen-muda tidak tersimpan sehingga tidak dapat disorot; timeout tanpa nomor baris; terminal masih menutup sebagian bed pada 640×480 (offset kamera 0,27 memotong bed ke-4 sehingga dikembalikan 0,20). Tidak ada commit/push/deployment.

Paket sesudah polish §19 (23 November 2026): `.\gradlew.bat --offline :lwjgl3:bundleWindows --console=plain` **exit 0**. Artefak `lwjgl3/build/distributions/MarsReboot-1.0.0-windows-x64.zip` (30.575.171 byte), SHA-256 `abc65060b4dc0af1c8e6deba7512c0a1bdb4c702fe717fdcfcbab68bea1b90b6`. Daftar isi JAR diperiksa: memuat `TitleScreen`, `SoundBank` dan lima `audio/sfx_*.wav`. **Belum** diekstrak/dijalankan dari folder spasi tanpa Python sistem, belum dicoba manual (suara/judul), belum playtest. Hash §17 kini hanya bukti historis; ZIP generik `MarsReboot-1.0.0.zip` bukan paket pemain.
