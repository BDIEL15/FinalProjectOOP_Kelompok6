# MarsReboot

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

Launcher dan modul hasil generator dipertahankan. `MarsRebootGame` sekarang membuka habitat placeholder melalui `HabitatScreen`; tidak menjalankan generator ulang atau mengganti versi dependency.

## Habitat dasar (12 November 2026)

WASD/panah untuk bergerak, E untuk interaksi dekat panel air/terminal, Esc atau tombol kembali untuk keluar. Dinding, kebun dan fasilitas menghalangi astronaut. Saat panel terbuka gerak nonaktif. Terminal sementara dapat digeser lewat judul, draf tetap ada selama layar yang sama, dan **kode belum dieksekusi**. Belum ada robot/Python, evaluator puzzle, backend atau autentikasi.

Versi existing: Java source/target 21, libGDX 1.14.2, LWJGL 3.4.3, Gradle Wrapper 9.8.0. Dari folder ini:

```powershell
.\gradlew.bat :lwjgl3:run
.\gradlew.bat :lwjgl3:jar
.\gradlew.bat :core:controlTest
.\gradlew.bat :lwjgl3:desktopSmokeTest
```

`controlTest` tanpa dependency test tambahan dan ikut `check`; smoke desktop opt-in memerlukan OpenGL/window. Wrapper belum berhasil pada sesi ini karena unduhan distribusi timeout. Pemeriksaan alternatif langsung memakai JDK 25.0.4.1 (`javac --release 21`) dan dependency persis di atas: 40 assertion kontrol + 29 assertion desktop lulus, screenshot di `lwjgl3/build/reports/habitat/`. Ini bukan bukti task Gradle atau runtime JDK 21 lulus. Cache sementara `.gradle-local/` diabaikan Git, bukan tool global. Pengujian keyboard fisik tahan, drag manual dan editor lengkap masih diperlukan.

Source perilaku: `core/src/main/java/com/marsreboot/habitat/HabitatWorld.java` dan `HabitatInput.java`. Visual/widget: `HabitatRenderer.java` (adegan representatif 2D top-down 3/4 dengan `TextureFilter.Nearest`, pipa penghubung, dan pencahayaan 2D ringan) dan `HabitatUi.java` (HUD atas/bawah di luar area lantai, panel inspeksi terstruktur, dan floating draggable terminal di sisi kiri); lifecycle/viewport: `HabitatScreen.java`. Menggunakan Skin existing `assets/ui/` serta aset pixel-art placeholder konsisten di `assets/habitat/` (tercatat di `assets/habitat/asset-manifest.json`, belum aset produksi final). Dokumentasi utama dan handoff berada di workspace `C:\Users\abdie\OneDrive\Documents\ChatGPT\FinalProjectOOP` (`TASKS.md`, `VERIFICATION.md`, `DESIGN.md`, `ARCHITECTURE.md`).

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.
