# TEST_PLAN — mars_reboot.py

Status: rencana uji, diperbarui 22 November 2026; habitat/kontrol dan kandidat domain robot puzzle 1 sudah diuji pada sesi terdahulu. UI Home/login/leaderboard dan Spring backend kini ditambahkan, tetapi belum lolos build/test Gradle pada sesi ini. Acuan [PRD](PRD.md), [RULES](RULES.md), [ARCHITECTURE](ARCHITECTURE.md), [DESIGN](DESIGN.md). Bukti aktual dicatat terpisah di [VERIFICATION](VERIFICATION.md). Angka/fixture Usulan baru menjadi acceptance gate setelah dipilih.

## 1. Lapisan bukti dan lingkungan

- Unit domain Java: fixture/evaluator/aksi tanpa window, backend, atau Python nyata.
- Controller/protokol: runtime/clock/HTTP palsu untuk pembatalan dan pesan terlambat.
- Integrasi runtime: runner Python nyata dan simulasi Java; bukan mock.
- Integrasi backend: Spring Boot dengan storage/sesi nyata yang dipilih, transaksi dan validasi.
- Desktop manual: build libGDX aktual pada ukuran target; Playwright bukan bukti desktop.
- Distribusi/playtest: paket final di komputer lain dan pemain manusia.

Catat OS, JDK, libGDX, Python, backend/database, build/commit atau hash artefak, fixture/rulesVersion, command aktual, hasil, dan keterbatasan. Command Wrapper existing dan batas eksekusinya kini dicatat pada handoff TASKS/VERIFICATION; backend Spring ada di `backend/`. Script npm hanya untuk [prototype](PROTOTYPE.md). Jangan memasang tool global hanya untuk menandai checklist lulus.

Untuk subset habitat, jalankan `:core:controlTest` (gerak 30/60/144 FPS, diagonal, alias/oposisi arah, sweep collider/batas, range, modal, E/hold/Esc, clear input) dan `:lwjgl3:desktopSmokeTest` (window nyata, wiring Screen/Scene2D, input processor, fokus/draf, resize dan disposal). Test kedua opt-in, memerlukan desktop. Ini bukti parsial UI-01–04 saja: belum robot, hasil puzzle, EXP-02 lengkap atau AC-02/15 keseluruhan. Ulangi pengujian keyboard fisik tahan, drag, clipboard/seleksi/scroll dan keterbacaan pada ukuran minimum sebelum penerimaan UI.

Subset robot tersedia melalui `.\gradlew.bat :core:robotTest`: 81 assertion mencakup DOM-01/02, panen dan sensor DOM-03 (metrik young saja), referensi/negatif **p1-a saja** pada DOM-05/06, DOM-07/08, serta isolasi fixture/snapshot. DOM-04 siram, keputusan pelanggaran young puzzle 3, puzzle lain dan seluruh runtime/lifecycle belum tercakup. Test domain tidak membuktikan terminal/replay/progres atau sandbox Python.

## 2. Kasus uji aturan dan fixture

| ID | Kasus | Hasil yang harus diamati | Pemilik |
| --- | --- | --- | --- |
| DOM-01 | Inisialisasi dan Run ulang setiap fixture | State awal identik; perubahan Run sebelumnya tidak bocor. | 1–2 |
| DOM-02 | Empat arah, penghalang/tepi, argumen salah | Tidak wrap; biaya/false/API_ERROR sesuai RULES; tidak mutasi parsial. | 1 |
| DOM-03 | Panen ripe/young/empty dan sensor | Panen benar mengosongkan; young mencatat pelanggaran puzzle 3; sensor tidak memutasi. | 1–2 |
| DOM-04 | Siram dry/lembap/air habis | Air dan actionCount tepat; tidak ada air negatif. | 1–2 |
| DOM-05 | Solusi referensi p1–p4 | Target tercapai, aksi 3/7/5/5 per skenario, p4 air 2; berlaku jika kandidat fixture dipilih. | 2 |
| DOM-06 | Source kosong/tujuan sebagian/harvest young/hardcode p4 | incomplete atau pelanggaran; p4 hardcode gagal pada variasi pembeda. | 2 |
| DOM-07 | Batas aksi/request: bawah, tepat, melewati | Pada tepat batas masih boleh lulus; aksi over-budget tidak dimutasi; hasil melampaui batas tidak sukses. | 1–2 |
| DOM-08 | Program gagal setelah target sempat tercapai | error/incomplete, tidak memberi progres sebelum akhir normal sesuai kandidat RULES. | 1–2 |
| EDU-01 | Loop/fungsi sah, dead code, teks komentar, solusi tanpa konstruksi | Penilaian latihan terpisah dari dunia; jangan klaim lulus sebelum metode EXP-05 dipilih. | 2 |
| CONTENT-01 | Puzzle 5–9, beberapa skenario, checkpoint/pompa | Referensi lulus; strategi hardcode atau target parsial gagal; solusi non-BFS yang sah diterima. | 2 |

DOM-05 bukan pengujian engine jika hanya dihitung dengan model ilustrasi; harus diulang melalui implementasi Java/runtime produk. Menandai rencana atau menghitung contoh tidak membuktikan game berjalan.

## 3. Lifecycle, protokol, dan runtime

| ID | Kasus | Bukti yang dicari |
| --- | --- | --- |
| LIFE-01 | Stop saat preparing/running/replaying | Outcome stopped, source/snapshot terakhir ada, tidak ada sukses/skor baru; Run berikutnya bersih. |
| LIFE-02 | Reset dari aktif/selesai/error | Fixture dan output awal; source/rekor tetap. |
| LIFE-03 | Kirim frame/result lama setelah Stop/Reset/Run baru | Tidak ada mutasi dunia, UI, progres, rekor, atau pengiriman skor. |
| LIFE-04 | Fixture/source sama di kecepatan replay berbeda dan reduced motion | Hasil serta metrik sama; sukses menunggu finalSequence terlihat. |
| PROTO-01 | Versi/ID/sequence/domain nilai salah, JSON/frame terpotong atau terlalu besar | Ditolak sebelum mutasi; error terjelaskan; memori terbatas dan recovery tersedia. |
| RUN-01 | Python nyata memanggil harvest, move, sensor | Nilai balik dan state Java sesuai kontrak; source tidak dikirim server. |
| RUN-02 | while True tanpa API, sensor loop, print flood | Stop tetap bereaksi; watchdog/request/output limit sesuai kebijakan; output tidak memperpanjang waktu. |
| RUN-03 | Runtime hilang, startup hang, syntax/runtime error, process crash | Error/timeout dengan pesan; source tetap, Run ulang bisa pulih. |
| RUN-04 | Pipe macet, antrean penuh, proses tidak segera mati | Cleanup berbatas waktu; runId invalid; Run diblokir bila proses belum aman diganti. |
| RUN-05 | Source/output pada batas byte dan karakter UTF-8 multi-byte | Source ditolak sesuai batas; pemotongan output tidak merusak UTF-8; pemberitahuan sekali. |
| SAVE-01 | Buka ulang, ganti puzzle/profil, save tidak tersedia/rusak/versi lain | Data sesuai kebijakan; kode tidak ditimpa; profil terisolasi; error tidak menghancurkan cadangan. |
| SAVE-02 | Sukses/Stop berlomba dengan replay selesai dan retry score | Progres diterapkan maksimal sekali, hasil batal tidak memberi rekor. |

Pemilik LIFE/PROTO/RUN: peran 1; SAVE: integrasi peran 1/4. Uji fake memudahkan reproduksi race, kemudian buktikan jalur nyata pada desktop.

## 4. Backend, UI, distribusi, dan manusia

| ID | Skenario | Kriteria |
| --- | --- | --- |
| API-01 | Registrasi/login benar/salah, username duplikat, logout/sesi habis | Response sesuai kontrak terpilih, password hash, sesi invalid ditolak. |
| API-02 | Dua akun membaca/menulis skor; userId palsu di body | Akses hanya milik sesi; field asing/versi tidak dikenal/metrik salah ditolak. |
| API-03 | Simpan, ambil setelah restart, kirim ulang submissionId | Skor bertahan; retry sama tidak duplikasi; payload berbeda dengan ID sama ditolak. |
| API-04 | Backend mati/timeout/5xx/401/403 | Status simpan terpisah, retry eksplisit, login ulang tanpa menghapus kode; tidak mengklaim tersimpan sebelum respons. |
| API-05 | Papan skor untuk P1 dengan beberapa akun, skor sama, skor lebih baik dan lebih buruk | Satu baris per akun, skor lebih kecil di atas, peringkat konsisten, maksimal 10 akun; daftar dapat dibaca tanpa login. |
| SEC-01 | Inspeksi paket, payload, log, dan konfigurasi | Tidak ada source/output di HTTP, password/secret tidak bocor; tidak mengklaim sandbox atau anticheat. |
| UI-01 | Habitat → inspeksi → terminal → hasil → habitat | Petunjuk relevan, tidak perlu inspeksi ulang setiap gagal, progres terlihat. |
| UI-02 | Multiline, indentasi, seleksi, clipboard, scrolling, error panjang | Source tetap utuh; fokus keluar/masuk; input gerak tidak aktif saat mengetik. |
| UI-03 | Keyboard-only dan semua fase kontrol | Fokus terlihat; Stop bisa dicapai; tidak ada keyboard trap atau aksi ganda. |
| UI-04 | 1280×720, 1440×900 dan resize | Kontrol/pesan tidak terpotong; teks terbaca; koordinat simulasi tidak berubah. Ukuran kandidat perlu dipilih. |
| UI-05 | Kontras, indikator nonwarna, reduced motion, audio | Ukur warna aktual; state terbaca tanpa warna; motion tidak mengubah skor. |
| UI-06 | Home, login/daftar, mulai sebagai tamu, setelan dan papan skor Home/Peta Misi | Alur jelas, pilihan setelan tersimpan, papan skor bisa dimuat ulang setelah skor baru dikirim, layout tidak terpotong. |
| BUILD-01 | Paket desktop pada komputer kedua | Login, satu puzzle, Stop, dan skor berjalan dengan panduan prasyarat; catat OS/runtime/backend. |
| OOP-01 | Demo empat pilar dan minimal enam pattern | Relasi kelas dan perilaku nyata; cocok dengan materi kelas/aslab, bukan nama saja. |
| ASSET-01 | Manifest dan kredit | Asal/lisensi/ukuran/versi jelas; aset terbaca pada skala game. |
| PLAY-01 | Pemain baru mencoba tahap 1 | Catat dapat/tidak: aksi pertama, pemakaian temuan, pemahaman gagal, hubungan kode–habitat. |

Peran 4 memegang API/SEC/BUILD, peran 3 memegang UI/ASSET, semua peran OOP dan playtest. Rating, waktu penyelesaian, dan jumlah partisipan hanya ditulis dari observasi nyata; target jumlah partisipan belum ditetapkan. Tidak ada hasil teman rekaan.

## 5. Pemetaan seluruh kriteria PRD

| AC PRD | Kasus pengujian |
| --- | --- |
| AC-01 | API-01, SEC-01 |
| AC-02 | UI-01, RUN-01, DOM-03 |
| AC-03 | DOM-05, DOM-06, EDU-01 |
| AC-04 | RUN-02, LIFE-01, RUN-03 |
| AC-05 | DOM-01, LIFE-04 |
| AC-06 | UI-01, SAVE-02 |
| AC-07 | SAVE-01, UI-02 |
| AC-08 | API-03, API-04 |
| AC-09 | LIFE-03, SAVE-02 |
| AC-10 | BUILD-01 |
| AC-11 | OOP-01 |
| AC-12 | LIFE-02, DOM-01 |
| AC-13 | RUN-03, UI-02, DOM-06 |
| AC-14 | DOM-07, RUN-02, RUN-04, RUN-05, PROTO-01 |
| AC-15 | UI-02, UI-03, UI-04, UI-05 |
| AC-16 | SEC-01 |
| AC-17 | UI-01, SAVE-01, PLAY-01 |
| AC-18 | CONTENT-01, EDU-01, UI-01 |
| AC-19 | DOM-01, DOM-05, DOM-06 |
| AC-20 | ASSET-01 |
| AC-21 | UI-06, API-05 |

Setiap FR/NFR terhubung ke AC di PRD; tabel ini meneruskan AC ke kasus uji. Pemetaan bukan hasil lulus. Case Usulan yang belum dipilih dicatat menunggu keputusan, bukan dilewati lalu dianggap memenuhi penerimaan.

## 6. Gerbang dan format bukti

1. Fondasi: build dan unit domain berjalan; EXP-01/02 membuktikan runtime/editor.
2. Integrasi pertama: login → panel → terminal → aksi robot → hasil → simpan/ambil skor nyata.
3. Demo 1–4: referensi/negatif, lifecycle, UI, save/backend, dan OOP teruji sesuai cakupan terpilih.
4. Lengkap 1–9: konten lanjutan dan progres diuji, playtest nyata dicatat.
5. Distribusi: paket final komputer kedua dan halaman/luaran ditinjau; publikasi hanya jika diotorisasi.

Format catatan: tanggal, target/build, ID test, command/langkah, hasil PASS/FAIL/BELUM DIJALANKAN, bukti, keterbatasan, pemilik tindak lanjut. Jangan mengganti laporan bug dengan checklist sukses. Jalankan test fokus dan gate terdekat; ulangi luas hanya setelah perubahan/temuan yang relevan.

## 7. Pemeriksaan integrasi p1 yang tersedia

`:core:runtimeTest` (ikut check) menguji controller/replay, byte/protokol, pembatalan, hasil lama dan blok cleanup. `:core:pythonIntegrationTest` memakai CPython bundel nyata, opt-in tanpa OpenGL. `:lwjgl3:puzzleDesktopTest` memakai pointer events Scene2D, source snapshot, Stop/Reset/error, Esc/reopen dan resize bersama interpreter nyata. Task desktop existing tetap tersedia. Ini subset LIFE/PROTO/RUN/UI p1, bukan bukti backend, seluruh save, komputer kedua atau playtest.

Uji ZIP memakai JAR dari paket dan Python di folder ekstraksi berisi spasi, PATH hanya Windows System32 dan tanpa PYTHONHOME/PYTHONPATH. Jalur pengujian serta batas di [kontrak runtime](docs/mars/PYTHON_RUNTIME.md), bukti aktual di VERIFICATION. Jangan menganggap menjalankan test dari classpath development sebagai bukti paket ZIP.
