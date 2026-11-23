# Panduan audio — titik awal tugas audio

Status per 23 November 2026: lima efek suara (SFX) sudah terpasang dan game berjalan normal tanpa perangkat audio. Belum ada musik latar, dan suaranya belum pernah didengar/diuji oleh manusia. Dokumen ini adalah titik awal bagi pemilik tugas audio; keputusan gaya suara dan musik tetap milik tim.

## Peta file

| File | Fungsi |
| --- | --- |
| [`core/.../habitat/SoundBank.java`](../../core/src/main/java/com/marsreboot/habitat/SoundBank.java) | Satu-satunya pemutar SFX. Enum `Sfx` berisi nama file + volume; `play(Sfx)` memuat suara saat pertama dipakai; `setMuted` untuk tombol on/off. Jika tidak ada perangkat audio (test/headless), diam tanpa error. |
| [`assets/audio/*.wav`](../../assets/audio/) | File suara: `sfx_click`, `sfx_water`, `sfx_harvest`, `sfx_success`, `sfx_fail` (WAV mono 16-bit 22,05 kHz). |
| [`tools/GenerateSfx.java`](../../tools/GenerateSfx.java) | Pembuat kelima WAV di atas dari gelombang sinus/kotak (tanpa aset pihak ketiga). Jalankan ulang: `java tools/GenerateSfx.java assets/audio`. |
| [`assets/habitat/asset-manifest.json`](../../assets/habitat/asset-manifest.json) | Catatan asal dan peran tiap file audio (bagian `"audio"`). Perbarui saat file diganti/ditambah. |

## Kapan suara diputar

| Suara | Dipicu di | Peristiwa |
| --- | --- | --- |
| `CLICK` | `HabitatUi.onAction(...)` | Setiap tombol Scene2D di habitat diklik. |
| `HARVEST` | `HabitatRenderer` | Tanaman matang dipanen saat replay. |
| `WATER` | `HabitatRenderer` | Petak kering menjadi lembap (P4). |
| `SUCCESS` | `HabitatRenderer` | Puzzle selesai sukses. |
| `FAIL` | `HabitatRenderer` | Percobaan berakhir INCOMPLETE, ERROR, TIMEOUT, atau LIMIT_EXCEEDED. |

Suara hanya presentasi: jangan memanggil `SoundBank` dari `RobotSimulation`, `RunCoordinator`, atau `PythonRuntime`, dan jangan membuat hasil puzzle bergantung pada audio.

## Menambah atau mengganti suara

1. Taruh file di `assets/audio/` (WAV atau OGG; OGG lebih kecil untuk musik).
2. Tambah entri di enum `SoundBank.Sfx` (nama file tanpa ekstensi + volume 0–1). Jika memakai OGG, sesuaikan ekstensi di `load()`.
3. Panggil `SoundBank.play(SoundBank.Sfx.NAMA)` di titik presentasi yang sesuai.
4. Catat asal dan lisensi file di `asset-manifest.json`. Pakai aset buatan sendiri atau berlisensi jelas (misalnya CC0); jangan mengambil suara dari game lain.
5. Jalankan `.\gradlew.bat verifyDemo` lalu `.\gradlew.bat :lwjgl3:run` dan dengarkan langsung. Catat hasilnya di VERIFICATION.

Untuk musik latar, gunakan `Gdx.audio.newMusic(...)` (streaming, `setLooping(true)`) dalam kelas terpisah dari `SoundBank`, dan hentikan/dispose saat pindah layar.

## Pekerjaan audio yang masih terbuka

- [ ] Dengarkan kelima SFX dan sesuaikan volume/nada; belum ada pemeriksaan manusia.
- [ ] Pengaturan **Efek Suara** di habitat belum tersimpan setelah game ditutup (mute tidak masuk `HabitatUi.saveSettings/loadSettings`), dan belum ada di setelan Home.
- [ ] `SoundBank.dispose()` belum dipanggil saat game ditutup (misalnya dari `MarsRebootGame.dispose()`).
- [ ] TitleScreen dan HomeScreen belum memutar suara klik/transisi.
- [ ] Musik latar belum ada (opsional; perlu keputusan tim).
- [ ] Setelah mengganti aset, bangun ulang paket `:lwjgl3:bundleWindows` dan pastikan `assets/audio` ikut terbawa.
