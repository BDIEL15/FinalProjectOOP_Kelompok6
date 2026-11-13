# VERIFICATION — mars_reboot.py

Status 12 November 2026. File ini mencatat bukti aktual untuk Mars; [bukti prototype historis](docs/prototype/VERIFICATION.md) disimpan terpisah. Rencana di [TEST_PLAN](TEST_PLAN.md) bukan hasil lulus.

## 1. Status produk

| Area | Bukti aktual | Status |
| --- | --- | --- |
| Java/libGDX habitat | Proyek existing terpisah `MarsReboot`; kompilasi Java 21 target, 40 assertion kontrol dan 29 assertion desktop lulus | Habitat dasar tersedia; Wrapper belum lulus. Evaluator robot belum ada. |
| Runtime script dan Stop desktop | Belum ada eksperimen produk Mars | Belum diuji. |
| Backend/auth/skor | Belum ada implementasi atau integrasi nyata | Belum diuji. |
| UI/aset/Figma | Adegan Outpost Permukaan Mars pixel art `2×` (`HabitatRenderer`), animasi astronaut 4 arah (`astronaut_eva_sheet.png`), pencahayaan radial 2D aditif (`light_glow.png`), font Hi-DPI `ui_sans`/`ui_sans_bold`/`ui_mono` + NinePatch 3× supersampled (`HabitatUi`), screenshot desktop `1280×720`, `1440×900`, `900×600`, `640×480` | Teruji pada desktop; aset robot/puzzle gameplay, wrapper Gradle, dan verifikasi manual OS penuh belum dijalankan. |
| Puzzle 1–9 | Kandidat konten/fixture; belum test Java/runtime | Belum terverifikasi. |
| Distribusi/playtest/publikasi | Tidak ada bukti rilis atau sesi manusia | Belum dilakukan. |

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
