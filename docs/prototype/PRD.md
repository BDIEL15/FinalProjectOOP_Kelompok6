# PRD — Farm Code Lab

Status: prototype v0.1 diimplementasikan dan diverifikasi lokal; deployment publik serta playtest belum dilakukan.  
Tanggal: 2 November 2026.  
Nama kerja: **Farm Code Lab**, dapat diganti tanpa mengubah mekanik.

## 1. Tujuan

Membuat prototype web yang memungkinkan pemain menulis Python, menjalankan drone di kebun, melihat hasilnya, lalu memperbaiki algoritmanya. Prototype dipakai untuk mencoba ide sendiri dan meminta masukan teman sebelum memutuskan pengembangan game yang lebih besar.

Hipotesis utama: melihat dampak kode di dunia kecil, disertai hasil yang dapat dibandingkan, mendorong pemain mencoba solusi kedua atas kemauannya sendiri.

Inspirasi mekanik: [The Farmer Was Replaced](https://store.steampowered.com/app/2060160/The_Farmer_Was_Replaced/). Gunakan nama, tampilan, ilustrasi, dan kode milik proyek sendiri.

## 2. Pengguna dan platform

- Pengguna awal: pembuat proyek dan 3–5 teman dengan pengalaman coding beragam.
- Sesi uji: sekitar 5–10 menit, melalui link di browser laptop/PC dengan keyboard.
- Target utama: Chrome dan Edge desktop; Firefox diperiksa sebagai target tambahan.
- Ponsel boleh membuka informasi dan tampilan bertumpuk, tetapi editing Python di ponsel bukan sasaran validasi awal.
- Bahasa antarmuka: Indonesia. Nama API dan sintaks Python menggunakan bahasa Inggris.

## 3. Pengalaman inti

1. Pemain membuka halaman dan menunggu runtime Python siap.
2. Pemain melihat tujuan, kebun 6×6, daftar perintah singkat, dan starter code.
3. Pemain menjalankan contoh, melihat drone bergerak, lalu membaca hasil.
4. Pemain mengubah program dan menjalankan ulang dari kondisi awal yang sama.
5. Pemain membandingkan hasil dengan percobaan sukses sebelumnya.

Tantangan tunggal: **panen seluruh 36 tanaman dalam paling banyak 200 tick.** Skor adalah jumlah tick yang dipakai pada percobaan sukses; lebih kecil lebih baik. Aturan lengkap berada di [RULES.md](RULES.md).

## 4. Kebutuhan wajib

| ID | Kebutuhan | Kriteria penerimaan |
| --- | --- | --- |
| P-01 | Menulis Python asli | Variabel, `if`, `for`, `while`, dan fungsi Python dapat dipakai dengan runtime Pyodide. |
| P-02 | Dunia kecil yang terlihat | Grid 6×6 menampilkan satu drone dan keadaan tanaman tanpa bergantung hanya pada warna. |
| P-03 | API permainan | `move`, `can_harvest`, `harvest`, `get_pos`, `wait` mengikuti RULES.md. |
| P-04 | Eksekusi yang dapat dihentikan | Run, Stop, Reset mengikuti state machine; UI tetap menerima Stop saat script berulang tanpa akhir. |
| P-05 | Perbandingan adil | Semua Run mulai dari fixture sama; kecepatan animasi tidak memengaruhi tick. |
| P-06 | Umpan balik | Ada progres panen, tick, hasil, output `print`, dan error dengan lokasi script jika tersedia. |
| P-07 | Pemulihan | Stop, timeout, error, dan kegagalan runtime tidak menghapus kode pemain. |
| P-08 | Persistensi ringan | Kode dan rekor sukses disimpan lokal bila storage tersedia; kegagalan storage tidak menghalangi bermain. |
| P-09 | Dibagikan lewat link | Build statis dapat disajikan melalui HTTP/HTTPS; tester tidak perlu memasang Python. |
| P-10 | Bantuan pertama | Starter code valid, petunjuk koordinat, biaya aksi, dan pesan hasil cukup untuk mencoba tanpa pendamping. |

## 5. Batas cakupan

Termasuk: satu peta, satu drone, satu jenis tanaman, kematangan sederhana, API terbatas, Python asli, animasi 2D, dan rekor lokal.

Ditunda: menanam, penyiraman, ekonomi, toko, pohon unlock, energi, inventory, beberapa drone, multiplayer, login, leaderboard daring, backend, interpreter khusus, aset 3D, dan integrasi Steam.

Step per baris, breakpoint, autocomplete IDE, serta pause/resume bukan kebutuhan v0.1. Stop menghentikan percobaan; bukan pause. Mempercepat animasi bukan mempercepat algoritma.

## 6. Validasi produk

Gunakan sesi observasi, tanpa analytics atau pengiriman kode otomatis. Catat secara manual:

| Pertanyaan | Bukti yang dicatat |
| --- | --- |
| Apakah tujuan dipahami? | Tester menjelaskan kembali tujuan dan arti skor. |
| Apakah onboarding cukup? | Tester menjalankan lalu mengubah starter code; catat bantuan yang diperlukan. |
| Apakah kegagalan dipahami? | Tester menjelaskan error atau aksi gagal dan mencoba memperbaikinya. |
| Apakah ada keinginan mengoptimalkan? | Tester mengubah strategi setelah sukses, bukan hanya menjalankan ulang. |
| Apakah mekanik ingin dilanjutkan? | Tester menjelaskan bagian yang ingin dicoba lagi dan bagian yang membingungkan. |

Ambang awal untuk diskusi tim, bukan bukti minat pasar: setidaknya 3 dari 5 tester dapat mengubah kode dan menjalankannya tanpa arahan langkah demi langkah; setidaknya 2 dari 5 mencoba memperbaiki solusi setelah sukses. Jika tester kurang dari lima, laporkan jumlah aktual dan jangan mengklaim ambang terpenuhi.

Keputusan setelah playtest: lanjutkan, perbaiki lalu uji ulang, atau ubah mekanik. Persetujuan teman untuk game lebih besar belum ada dan tidak boleh ditulis seolah sudah diberikan.

## 7. Risiko dan asumsi

- Python asli lebih cepat membuktikan pengalaman coding dibanding membuat interpreter; pengaturan biaya tiap ekspresi Python tidak termasuk prototype.
- Waktu unduh/inisialisasi Pyodide bergantung jaringan dan perangkat; loading harus terlihat dan dapat dicoba ulang.
- Batas tick mengukur aksi game, bukan seluruh biaya CPU program Python.
- Satu tantangan dapat terasa dangkal; playtest menguji dorongan mengoptimalkan, bukan retensi game panjang.
- Pindah ke Godot/C# bisa memerlukan penulisan ulang UI dan simulasi. Aturan, fixture, API, dan kasus uji merupakan aset yang lebih mudah dibawa.

## 8. Sumber kebenaran dokumen

| Dokumen | Tanggung jawab |
| --- | --- |
| PRD.md | Tujuan, pengguna, cakupan, penerimaan, dan playtest. |
| RULES.md | Aturan permainan, fixture, API, biaya, dan penilaian. |
| ARCHITECTURE.md | Runtime, aliran data, lifecycle, batas keamanan, dan struktur implementasi. |
| DESIGN.md | Layout, token visual, interaksi, aksesibilitas, dan copy. |
| AGENTS.md | Instruksi kontributor manusia dan agen coding. |
| TASKS.md | Urutan pekerjaan, dependensi, dan bukti penyelesaian. |

Dokumen ini merekam kebutuhan v0.1. Bukti build dan integrasi lokal tercatat di [VERIFICATION.md](VERIFICATION.md); deployment publik dan playtest masih terbuka.
