# RULES — mars_reboot.py

Status: rancangan lengkap untuk review, 12 November 2026; belum diimplementasikan. Acuan kebutuhan: [PRD](PRD.md). Keputusan pengguna tetap di [PROJECT](PROJECT.md). Semua angka, fixture, nama API, dan kebijakan rinci baru di dokumen ini berstatus **Usulan**, bukan aturan yang sudah disetujui. Kontrak final baru berlaku setelah dipilih dan diuji. Aturan prototype di [arsip](docs/prototype/RULES.md) tidak berlaku otomatis.

## 1. Istilah dan sumber state

| Istilah | Arti |
| --- | --- |
| Misi besar | Bab pemulihan habitat; target 3 misi. |
| Puzzle | Level dengan tujuan dan skenario wajib; target 9, tahap pertama 1–4. |
| Skenario | Satu fixture tetap, tujuan, serta batasnya. |
| Percobaan | Satu Run, satu versi source, dan evaluasi skenario wajib puzzle. |
| Fixture | Data awal immutable; selalu disalin untuk setiap skenario/Run. |
| Habitat | State progres dan perubahan fasilitas yang sudah diperoleh. |
| Dunia percobaan | Grid robot sementara; kegagalan tidak mengubah habitat. |
| Replay | Penyajian trace simulasi; bukan perhitungan ulang aturan. |

**Rancangan kerja PROJECT:** Java memiliki state dan evaluator; runtime bahasa pemain meminta aksi/sensor, renderer hanya menyajikan snapshot. Waktu berjalan sebagai astronaut atau menulis kode tidak menghabiskan biaya puzzle. Tidak ada random atau FPS dalam aturan. Waktu watchdog adalah pengamanan eksekusi, bukan skor.

## 2. Dunia dan koordinat — Usulan

Grid persegi panjang, koordinat integer dengan (0,0) di kiri bawah; east menambah x, north menambah y. Grid tidak membungkus. Robot memiliki satu posisi di petak yang dapat dilalui; fasilitas astronaut dan robot dapat memakai representasi peta berbeda.

Setiap petak menyimpan passable, crop (empty/young/ripe), dan kebutuhan air. Tujuan adalah data terpisah, bukan ditebak dari gambar. Tanaman muda pada fixture puzzle 3 tetap muda selama percobaan: belum ada aturan kematangan berbasis aksi yang dipilih untuk Mars. Tidak ada tanam ulang, wait, atau panen manual astronaut pada tahap awal.

## 3. API robot tahap awal — Usulan

Python masih menunggu konfirmasi aslab. Signature berikut kandidat kontrak pemain, bukan API yang sudah tersedia.

| Perintah | Hasil yang diusulkan | Biaya dan kegagalan |
| --- | --- | --- |
| `move(direction)` | bool; bergerak satu petak bila valid | Arah north/east/south/west; langkah terhalang/keluar grid mengembalikan false dan tetap memakai satu aksi. |
| `harvest()` | bool; true bila crop ripe dipanen menjadi empty | Satu aksi setiap panggilan; young/empty mengembalikan false. Panen young dicatat sebagai pelanggaran pada puzzle 3. |
| `can_harvest()` | bool pada petak robot | Sensor; tidak mengubah dunia atau biaya aksi. |
| `water()` | bool; memakai satu unit air bila persediaan tersedia | Satu aksi. Menyiram petak lembap tetap membuang satu unit air. Jika air habis: false, tanpa pengurangan air lebih lanjut. |
| `needs_water()` | bool pada petak robot | Sensor; tidak mengubah dunia atau biaya aksi. |
| `get_pos()` | tuple (x,y) | Sensor; tidak mengubah dunia atau biaya aksi. |

Semua panggilan termasuk sensor dihitung sebagai apiCallCount. Argumen/tipe/arah yang tidak valid menghasilkan API_ERROR dan menghentikan skenario sebelum mutasi. Tidak ada mutasi parsial. Aksi legal tetapi tidak berhasil tetap tercatat agar umpan balik menjelaskan penyebabnya.

Untuk puzzle 7–9 diperlukan informasi peta, daftar checkpoint/tujuan, pemeriksaan checkpoint, serta aktivasi pompa. Nama/nilai balik API tambahannya belum dipilih; jangan menganggap `get_pos()` cukup untuk BFS atau menambahkan API diam-diam.

## 4. Run, Stop, Reset, dan hasil

**Rancangan kerja:** Run memulai fixture baru dan memakai source yang sama pada seluruh skenario wajib. Source dinilai sebagai snapshot; edit berikutnya berlaku pada Run baru. Skenario berikutnya memulai state dan namespace baru. Semua skenario/latihan wajib serta replay harus selesai sebelum progres dan rekor sukses diterapkan.

**Usulan rincian evaluasi:** skenario dinilai ketika program berakhir normal; tidak otomatis sukses saat target sempat tercapai. Error setelah mencapai target tetap gagal. Hentikan evaluasi pada skenario gagal pertama dan tampilkan identitasnya. Ini tidak menyatakan semua skenario sudah lulus. Solusi harus di-Run ulang seluruhnya setelah diperbaiki.

| Hasil | Penyebab | Progres/rekor sukses |
| --- | --- | --- |
| success | Seluruh skenario dan latihan wajib lulus; replay selesai | Terapkan sekali. |
| incomplete | Program berakhir normal tetapi target belum selesai/pelanggaran puzzle terjadi | Tidak. |
| limit_exceeded | Batas aksi/request/trace dilampaui | Tidak. |
| error | Syntax, runtime, API, protokol, atau proses gagal | Tidak. |
| timeout | Batas startup/komputasi terlampaui | Tidak. |
| stopped | Pemain membatalkan komputasi atau replay | Tidak. |

Stop tidak bergantung pada script merespons pesan. Batalkan identitas percobaan lebih dahulu, lalu hentikan proses; pertahankan source dan snapshot terakhir yang terlihat. Reset membatalkan percobaan aktif, mengembalikan fixture/log, serta mempertahankan source dan rekor. Hasil lama diabaikan setelah Stop/Reset/Run baru. Setelah error/timeout, pemulihan runtime harus memungkinkan Run baru tanpa restart game.

## 5. Batas dan metrik — Usulan untuk eksperimen

Angka ini dipilih sebagai kandidat awal Mars, bukan hasil pengukuran atau salinan kontrak prototype. Kalibrasi melalui eksperimen runtime dan editor sebelum dijadikan konfigurasi rilis.

| Batas | Kandidat awal | Perilaku |
| --- | --- | --- |
| Source | 16 KiB UTF-8 per Run | Tolak sebelum menjalankan, source tetap tersedia. |
| Output | 64 baris atau 8 KiB per percobaan, mana lebih dahulu | Potong pada batas karakter UTF-8, tampilkan satu pemberitahuan; lanjut drain tanpa menumpuk. |
| API request | 1.024 per skenario termasuk sensor | Request berikutnya ditolak; limit_exceeded. |
| Trace/event belum diputar | 256 per percobaan | Tolak penambahan di luar batas dan batalkan; jangan membiarkan memori tumbuh. |
| Startup runtime | 15 detik per proses | Timeout, pulihkan sebelum Run berikutnya. |
| Komputasi | 5 detik per skenario setelah ready | Timeout; stdout tidak memperpanjang waktu. |
| Aksi puzzle 1/2/3/4 | 6 / 12 / 12 / 12 per skenario | Aksi yang melampaui sisa batas tidak dimutasi. Tepat batas masih boleh lulus jika program berakhir normal. |

actionCount menghitung move/harvest/water yang tervalidasi termasuk gagal; moveCount hanya langkah yang berhasil; waterUsed adalah unit yang benar-benar dipakai. Panen berhasil dan invalidHarvestCount dicatat terpisah. Sensor tidak menambah actionCount tetapi dibatasi request/watchdog. Pengaruh sumber nondeterministik yang dapat dipakai Python harus ditangani saat runtime dipilih; belum ada jaminan sandbox atau batas memori keras.

**Usulan skor sederhana:** jumlah actionCount semua skenario wajib, lebih kecil lebih baik. Hanya percobaan success yang boleh menjadi rekor; simpan juga metrik air. Bandingkan rekor hanya dalam puzzleId, rulesVersion, dan fixtureVersion yang sama. Formula ini belum dipilih dan harus divalidasi melalui solusi referensi/playtest. Loop dengan aksi sama tidak otomatis menghasilkan skor lebih kecil. Tidak ada leaderboard kompetitif yang ditambahkan.

## 6. Fixture puzzle 1–4 — Usulan

Semua contoh satu baris 4×1 kecuali puzzle 1 berukuran 2×1; semua petak dapat dilalui, robot mulai (0,0), y=0. Fixture ini untuk membuktikan kontrak awal, bukan peta habitat final. Air hanya relevan untuk puzzle 4. Satu baris dipilih agar tutorial fokus pada satu konsep, bukan navigasi kompleks.

| Puzzle / fixture kandidat | Kondisi awal dan target | Solusi referensi dan metrik |
| --- | --- | --- |
| 1 / p1-a | Crop ripe di x=0,1; panen keduanya | harvest, move east, harvest: 3 aksi. |
| 2 / p2-a | Crop ripe di x=0,1,2,3; panen semuanya; latihan for terpisah | 4 panen + 3 gerak = 7 aksi; batas 12. |
| 3 / p3-a | Crop ripe di x=0,2; young di x=1,3; panen semua ripe tanpa mencoba panen young | 2 panen + 3 gerak = 5 aksi; empat pembacaan sensor. |
| 4 / p4-a | Dry di x=0,2; lainnya lembap; air 2 unit | 2 siram + 3 gerak = 5 aksi, air 2. |
| 4 / p4-b | Dry di x=1,3; lainnya lembap; air 2 unit | Source yang sama: 5 aksi, air 2. |

Referensi puzzle 1:

```python
harvest()
move("east")
harvest()
```

Referensi puzzle 2 (latihan loop belum memiliki verifikator final):

```python
for x in range(4):
    harvest()
    if x < 3:
        move("east")
```

Referensi puzzle 3:

```python
for x in range(4):
    if can_harvest():
        harvest()
    if x < 3:
        move("east")
```

Referensi puzzle 4, sama untuk p4-a dan p4-b:

```python
for x in range(4):
    if needs_water():
        water()
    if x < 3:
        move("east")
```

Kasus pembeda: source kosong → incomplete; puzzle 1 hanya harvest sekali → incomplete; puzzle 3 harvest tanpa sensor di semua petak → pelanggaran young; puzzle 4 menyiram dua petak pertama atau hardcode x=0,2 → gagal salah satu skenario. Sensor tanpa akhir → limit/timeout; loop tanpa API → timeout atau stopped. Solusi referensi ini baru rancangan; belum diuji pada Java/runtime produk.

## 7. Puzzle 5–9 dan pemulihan habitat

| Puzzle | Arah konten PROJECT | Bukti yang perlu disiapkan sebelum implementasi |
| --- | --- | --- |
| 5 Kebun Berubah | Generalisasi irigasi pada variasi lebih banyak | Fixture dry berbeda; satu source lulus semua, hardcode gagal. |
| 6 Rutinitas Perawatan | def/parameter untuk rutinitas kebun | Fixture beberapa bagian dan evaluasi latihan fungsi yang dijelaskan sejak awal. |
| 7 Periksa Jalur Pompa | List/tuple koordinat dan checkpoint | Daftar target, aksi inspeksi, sensor, serta bukti semua checkpoint dikunjungi. |
| 8 Lorong Buntu | Grid/set dan debugging navigasi | Peta bercabang kecil, kerangka program, target keluar, batas aksi. |
| 9 Pulihkan Pompa Utama | Antrean/pencarian rute dan integrasi | Beberapa peta tetap, data penghalang yang tersedia ke program, syarat aktivasi pompa. Strategi selain BFS diterima jika memenuhi tujuan. |

Fixture numerik 5–9 belum ditetapkan: fase konten lanjutan harus melengkapinya sebelum pekerjaan puzzle terkait dinyatakan siap. Tidak mengurangi target 9 puzzle. Panen → irigasi → pompa adalah rancangan hubungan cerita; graf ruang rinci masih perlu dipilih.

**Rancangan kerja:** kegagalan hanya mengubah percobaan sementara. Pemulihan diterapkan satu kali setelah hasil sah; puzzle berikutnya dimulai eksplisit. **Usulan replay:** mengulang puzzle selesai tidak menghapus akses/pemulihan, hanya dapat memperbaiki rekor. Simpan progres/kode per puzzle secara lokal jika usulannya dipilih. Kegagalan simpan skor tidak mengubah hasil lokal menjadi gagal puzzle; tampilkan status simpan terpisah.

## 8. Latihan konsep dan keputusan terbuka

Latihan loop pada puzzle 2 dan fungsi pada puzzle 6 berbeda dari tujuan dunia. Pemeriksaan teks/regex saja tidak membuktikan penggunaan konsep; kode dengan dead code tidak boleh otomatis dianggap memenuhi latihan. **Usulan eksperimen:** parsing source dan bukti jalur eksekusi yang benar-benar memakai konstruksi terkait; metode serta dukungan runtime belum dipilih. Jangan mengunci progres berdasarkan verifikator yang belum dibuktikan.

| ID | Pertanyaan yang belum dipilih | Langkah penutupan |
| --- | --- | --- |
| R-01 | Apakah API, biaya, fixture dan batas kandidat bagian 2–6 diterima? | Uji referensi/negatif di evaluator Java, tinjau bersama tim. |
| R-02 | Apakah formula actionCount sesuai tujuan optimasi? | Bandingkan strategi pada fixture sama dan playtest; jangan menyimpulkan Big O. |
| R-03 | Bagaimana latihan loop/fungsi dinilai? | Eksperimen parsing/trace, kasus dead code dan solusi sah, konfirmasi tim/aslab. |
| R-04 | Apa kontrak peta/checkpoint/pompa dan fixture 5–9? | Lengkapi sebelum tiap puzzle dikerjakan. |

Kebutuhan FR/NFR/AC tetap di [PRD](PRD.md); struktur dan pembatalan di [ARCHITECTURE](ARCHITECTURE.md); daftar test di [TEST_PLAN](TEST_PLAN.md).
