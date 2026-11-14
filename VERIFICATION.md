# VERIFICATION — mars_reboot.py

Status diperbarui 14 November 2026. Bukti terbaru penyempurnaan UI, keterbacaan `18` unit UI, dan alur Puzzle 1 ada di bagian 9; bagian 8 dan sebelumnya adalah bukti historis tahap terdahulu. Rencana TEST_PLAN bukan hasil uji; prototype tetap terpisah di docs/prototype/VERIFICATION.md.

## 1. Status produk

| Area | Bukti aktual | Status |
| --- | --- | --- |
| Java/libGDX habitat | Root MarsReboot; `.\gradlew.bat --offline` lulus 14 Nov 2026: 40 assertion kontrol (`:core:controlTest`) dan 29 assertion desktop/Scene2D (`:lwjgl3:desktopSmokeTest`) | Habitat teruji; input tahan tombol OS manusia penuh belum disimulasikan. |
| Runtime script dan Stop desktop | CPython bundel (`build/python/windows-x64`): 81 assertion robot (`:core:robotTest`), 33 controller/protokol (`:core:runtimeTest`), 33 integrasi Python (`:core:pythonIntegrationTest`) lulus 14 Nov 2026 | Teruji untuk p1 Windows x64; bukan sandbox atau seluruh platform. |
| Backend/auth/skor | Belum ada implementasi atau integrasi nyata | Belum diuji. |
| UI/aset/Figma | Adegan Outpost Mars `2×` (`HabitatRenderer`) dgn petak panen 2×1 tidak tertutup dan penanda lokal `PANEN P1 SELESAI (SESI)`; font BMFont `ui_sans`/`ui_sans_bold` (`36px`, skala `0.50f`) & `ui_mono` (`34px`, skala `0.53f`) ~`18` unit UI (`~12px` di `640×480`); terminal `556×518` di kiri + editor/output scroll berbatas + footer `Coba lagi`/`Kembali ke habitat` + `[O] Setelan` Preview Visual + Peta Misi P1 berbasis `RunCoordinator.View`; screenshot `1280×720`, `1440×900`, `900×600`, `640×480` diperiksa visual 14 Nov 2026 | Teruji desktop otomatis & inspeksi PNG pada keempat resolusi; drag mouse OS manusia dan review playtest tetap terbuka. |
| Puzzle 1–9 | Kandidat p1-a melalui Python–Java–desktop (41 assertion desktop puzzle pada `:lwjgl3:puzzleDesktopTest`, 14 Nov 2026); P2–9 tampil jujur `BELUM TERSEDIA` | P1 teruji lintas alur inspeksi → terminal → Run → replay → hasil → ulang/kembali; puzzle 2–9 belum ada. |
| Distribusi/playtest/publikasi | ZIP Windows + Python diuji 13 Nov dari folder spasi tanpa Python PATH; `:lwjgl3:bundleWindows` belum dibangun ulang pasca-penyempurnaan UI 14 Nov | Paket 13 Nov teruji lokal; re-bundle 14 Nov, komputer kedua, manusia, dan publikasi belum dilakukan. |

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
