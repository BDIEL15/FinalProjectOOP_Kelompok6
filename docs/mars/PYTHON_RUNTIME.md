# Runtime Python — Integrasi eksperimen puzzle 1

Implementasi 13 November 2026, Windows x64. Ini kontrak integrasi yang diimplementasikan untuk kandidat `p1-a`, bukan persetujuan aslab, penyelesaian backend, atau penerimaan seluruh game. Target tetap 9 puzzle/3 misi; tahap awal 1–4. Keputusan pembundelan Python berasal dari pengguna pada sesi ini.

## Runtime dan paket

- CPython **3.15.2 embeddable Windows x64**, standard library saja, tanpa pip/venv/global install. URL/SHA-256 dikunci dalam [manifest](../../runtime/python-runtime.json). Paket resmi berasal dari [Python.org](https://www.python.org/ftp/python/3.15.2/); jenis distribusi dijelaskan dalam [dokumentasi Python](https://docs.python.org/3.15/using/windows.html#the-embeddable-package).
- `:lwjgl3:preparePython` mengunduh sekali, memverifikasi SHA-256, lalu mengekstrak ke `build/python/windows-x64/`. Cache ZIP ada di `build/python-download/`; kedua lokasi diabaikan Git. Clone baru memerlukan jaringan pada persiapan pertama. Offline tanpa cache gagal dengan pesan, tidak memakai Python sistem diam-diam.
- `:lwjgl3:bundleWindows` menghasilkan `lwjgl3/build/distributions/MarsReboot-1.0.0-windows-x64.zip`: JAR, `runtime/python/`, lisensi, manifest, `run.bat` dan petunjuk. Ekstrak semua file dan buka `run.bat`. Java 21+ tetap diperlukan; Java belum dibundel.
- Task Run/test mengirim `-Dmars.pythonDir=<runtime proyek>`. Run kelas launcher langsung dari IntelliJ mencari root repo dari lokasi kelas terkompilasi, lalu memakai runtime build tersebut. Pada JAR, default adalah `runtime/python/python.exe` di sebelah JAR. Property hanya untuk lokasi build khusus; tidak ada fallback ke PATH.

## API dan wire

`assets/python/runner.py` diekstrak ke direktori sementara per Run. Java menjalankan executable absolut dengan `-I -X utf8 -u -B`, tanpa shell. Source dikirim sebagai data, bukan command line. Environment proses dibatasi ke lokasi Windows dan direktori temporer; source/output tidak dikirim ke backend.

API Python: `move(direction) -> bool`, `harvest() -> bool`, `can_harvest() -> bool`, `get_pos() -> tuple[int,int]`. Arah: north/east/south/west. Python meneruskan request; Java `RobotSimulation` tetap menghitung tanaman, posisi, target, biaya, dan hasil. API water/puzzle lain belum tersedia.

Transport UTF-8 NDJSON. Envelope wajib: `protocolVersion=1`, `generation`, `runId="run-<generation>"`, `scenarioId="p1-a"`, `sequence` integer berurutan per arah mulai 1, `type`, dan object `payload`. Identitas berasal dari host; frame berbeda versi/identitas/urutan ditolak sebelum mutasi.

| Arah / type | Payload |
| --- | --- |
| Java → Python `start` | `source`: string snapshot editor, maks 16 KiB UTF-8 |
| Python → Java `ready` | Object kosong; mulai watchdog komputasi |
| Python → Java `request` | `requestId` integer berurutan mulai 1, `name`, `args` array; satu request menunggu response |
| Java → Python `response` | `requestId`, `ok=true`, `value` bool atau array koordinat; API invalid menghentikan proses sebagai error |
| Python → Java `output` | `text`: fragmen output yang dibatasi |
| Python → Java `output_truncated` | Object kosong; satu pemberitahuan pemotongan |
| Python → Java `error` | `message`, `line`: nomor baris atau null |
| Python → Java `finished` | Object kosong; baru diterima sebagai akhir normal setelah EOF dan exit code 0 |

Frame `start` maksimal **128 KiB** termasuk newline agar source 16 KiB yang di-escape JSON tetap muat; frame lain maksimal **16 KiB**. Reader membatasi byte sebelum parsing dan menolak UTF-8 invalid/frame terpotong. Hasil Python tidak pernah dipakai sebagai snapshot atau keputusan sukses.

## Lifecycle dan batas

- `RunCoordinator.run(source)`, `stop()`, `reset()`, `update(delta,reducedMotion)`, `view()`, `close()` adalah batas UI. View/snapshot/hasil immutable. Pekerja berurutan memiliki simulasi dan proses sendiri; render thread tidak menunggu pipe atau terminasi.
- Run baru membuat fixture, namespace dan proses baru. Satu pekerjaan aktif serta satu hasil berbatas; Run ditolak saat proses lama belum selesai dibersihkan atau replay masih berjalan.
- Status: READY → PREPARING → RUNNING → REPLAYING → SUCCESS/INCOMPLETE/ERROR/LIMIT_EXCEEDED/TIMEOUT; Stop menjadi STOPPED. Startup/error tanpa trace dapat langsung tampil tanpa replay. Status aplikasi berbeda dari outcome snapshot domain.
- Startup 15 detik; komputasi 5 detik sejak ready, tidak diperpanjang output. Batas p1: 6 aksi, 1.024 request termasuk sensor, 256 snapshot aksi. Output 64 baris atau 8 KiB UTF-8 (mana lebih dahulu), ditambah satu pemberitahuan; sisa output dibuang/dikuras. stderr dikuras dengan penyimpanan diagnostik maksimal 8 KiB.
- Host memanggil `finish()` hanya setelah `finished` + EOF + exit 0. Syntax/runtime/API/protokol error, exit tidak normal, timeout dan Stop tidak menjadi sukses meski tanaman sudah habis.
- Snapshot aksi diputar setiap 0,35 detik tanpa interpolasi, termasuk reduced motion. Sukses/progres sesi diterapkan setelah langkah akhir terlihat; kecepatan replay tidak mengubah metrik.
- Stop/Reset membatalkan identitas lebih dahulu; hasil lama diabaikan. Watchdog meminta destroy, menunggu hingga 500 ms, lalu force dan wait hingga 1 detik. Jika proses masih hidup, Run diblokir sampai game ditutup. Detail proses mengikuti [API Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Process.html).
- Stop mempertahankan snapshot terakhir yang terlihat. Reset memulihkan fixture/output, mempertahankan draf serta pencapaian sesi. Editing saat Run memengaruhi Run berikutnya saja. Esc menutup terminal tanpa membatalkan proses; hide/dispose layar membatalkan dan melepas runtime.
- Puzzle 1 selesai hanya selama sesi layar; tidak mengaktifkan seluruh habitat atau membuka puzzle lain. Preview Before/After tetap visual dan tidak memberi progres. Rekor/server/save belum dibuat.

Proses terpisah dan mode isolated bukan sandbox keamanan. Script pembelajaran tepercaya masih dapat memakai kemampuan Python; tidak ada jaminan pembatasan file, jaringan, memori, atau seluruh proses turunan.

## Pemeriksaan

```powershell
.\gradlew.bat :lwjgl3:preparePython
.\gradlew.bat :core:robotTest :core:controlTest :core:runtimeTest :core:pythonIntegrationTest
.\gradlew.bat :lwjgl3:desktopSmokeTest :lwjgl3:puzzleDesktopTest
.\gradlew.bat :lwjgl3:bundleWindows
```

`runtimeTest` ikut `check`; test interpreter/desktop opt-in dan memakai runtime bundel. Bukti aktual, jumlah assertion, hash paket serta batas komputer kedua ada di [VERIFICATION](../../VERIFICATION.md). Untuk uji paket: ekstrak ke folder berisi spasi, gunakan JAR paket sebagai classpath aplikasi, jalankan kedua test integrasi dengan executable Java absolut dan PATH hanya `%SystemRoot%\System32`. Pemeriksaan ini membuktikan tidak bergantung Python sistem, bukan bukti komputer fisik kedua.
