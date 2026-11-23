# mars_reboot.py — MarsReboot

![Banner animasi mars_reboot.py](docs/mars/design-refs/mars_reboot_banner.gif)

MarsReboot adalah game puzzle pemrograman berlatar koloni Mars. Pemain menulis program Python untuk menggerakkan robot dan menyelesaikan misi, sambil membantu memulihkan habitat.

## Fitur

- Home dengan pilihan masuk, daftar akun, atau bermain sebagai tamu.
- Setelan sederhana yang tersimpan di komputer.
- Habitat Mars yang dapat dijelajahi dan terminal untuk menulis program Python.
- Puzzle 1: gerakkan robot ke petak kebun dan panen tanaman.
- Jalankan, hentikan, dan reset program; lihat gerakan robot dan hasil percobaan.
- Skor P1 tersimpan untuk akun yang masuk. Papan skor menampilkan hingga 10 pemain, diurutkan dari jumlah aksi paling sedikit.

Target pengembangan mencakup 9 puzzle dalam 3 misi. Saat ini puzzle 1 merupakan alur utama yang tersedia; puzzle 2–9, penyimpanan progres lintas restart, dan papan skor bersama lintas komputer belum terverifikasi.

## Menjalankan game

### Persyaratan

- Windows
- JDK 21
- Backend lokal untuk daftar/masuk dan penyimpanan skor

### 1. Jalankan backend

Buka terminal dari folder proyek dan jalankan:

```powershell
.\backend\gradlew.bat bootRun
```

Biarkan backend berjalan pada port `8081`.

### 2. Jalankan game

Buka terminal kedua dari folder proyek dan jalankan:

```powershell
.\gradlew.bat :lwjgl3:run
```

Pemain dapat masuk atau bermain sebagai tamu. Skor tersimpan jika pemain masuk dan backend lokal sedang berjalan. Database backend berjalan di komputer ini, sehingga pemain di komputer lain belum memakai papan skor yang sama.

## Cara bermain

1. Dari Home, daftar akun lalu masuk, atau pilih bermain sebagai tamu.
2. Pilih **Mulai misi** dan dekati terminal di habitat, lalu tekan **E**.
3. Lengkapi program puzzle 1. Contoh perintah robot:

   ```python
   move("east")
   harvest()
   ```

4. Tekan **Run** untuk menjalankan program. **Stop** menghentikan program, sedangkan **Reset** memulihkan petak tanpa menghapus draf.
5. Setelah puzzle berhasil, buka **Misi [M] → Muat skor** untuk melihat skor.

Skor dihitung dari jumlah aksi; skor dengan aksi lebih sedikit berada lebih tinggi di papan skor. Progres kelulusan saat ini berlaku selama sesi permainan.

## Kontrol

| Tombol | Aksi |
| --- | --- |
| WASD / panah | Bergerak |
| E | Berinteraksi dengan terminal atau objek |
| Esc | Menutup panel atau editor |
| M | Membuka misi |

## Status verifikasi

Source core dan backend berhasil dikompilasi langsung dengan Java 21; kompilasi Gradle untuk core dan lwjgl3 juga berhasil pada 23 November 2026. Game dan dialog konfirmasi keluar belum diperiksa melalui peluncuran desktop pada sesi ini. Rincian fitur, keputusan gameplay, dan bukti pengujian tersedia di [DESIGN.md](DESIGN.md), [RULES.md](RULES.md), [TEST_PLAN.md](TEST_PLAN.md), dan [VERIFICATION.md](VERIFICATION.md).

## Dokumentasi proyek

- [PRD.md](PRD.md) — tujuan dan cakupan game.
- [ARCHITECTURE.md](ARCHITECTURE.md) — rancangan sistem game dan backend.
- [DESIGN.md](DESIGN.md) — antarmuka, kontrol, dan pengalaman bermain.
- [RULES.md](RULES.md) — aturan puzzle dan perhitungan skor.
- [backend/README.md](backend/README.md) — konfigurasi dan endpoint backend.
- [TASKS.md](TASKS.md) — status pekerjaan dan catatan pengembangan.
- [VERIFICATION.md](VERIFICATION.md) — hasil pemeriksaan yang sudah dilakukan.
