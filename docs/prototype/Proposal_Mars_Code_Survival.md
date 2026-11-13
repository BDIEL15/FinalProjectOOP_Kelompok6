# Proposal Game Mars Code Survival

Sumber isi proposal PDF. Nama dan NPM akan dilengkapi oleh kelompok.

## Identitas dan ringkasan proposal

PROPOSAL PROYEK GAME

Mars Code Survival

Game puzzle pemrograman Python untuk bertahan hidup di Mars

Proposal pengembangan selama 4 minggu | November 2026

### Ringkasan proyek

Mars Code Survival adalah game puzzle 2D pemain tunggal tentang astronaut berlatar belakang programming dan teknologi yang terdampar di Mars. Pemain menulis Python untuk mengendalikan robot pendamping, mengurus kebun habitat, dan memulihkan akses menuju fasilitas penting. Keberhasilan bergantung pada strategi penelusuran, pembacaan kondisi, dan pencarian rute yang dapat diterapkan pada beberapa skenario.

Tim mengusulkan pengembangan berbasis web dengan memanfaatkan prototype Farm Code Lab yang sudah tersedia. Target empat minggu adalah tutorial dan tiga misi inti, dilengkapi evaluasi program, umpan balik visual, serta demonstrasi penerapan Object Oriented Programming atau OOP pada implementasi game.

### Identitas kelompok

| No | Nama anggota | NPM |
| --- | --- | --- |
| 1 |  |  |
| 2 |  |  |
| 3 |  |  |
| 4 |  |  |

Nama dan NPM sengaja dikosongkan sesuai arahan penyusun. Jumlah baris dapat disesuaikan dengan jumlah anggota sebenarnya. Empat peran kerja dalam proposal merupakan pembagian tanggung jawab, bukan penetapan jumlah anggota.

### Luaran yang diajukan

Prototype web yang dapat dimainkan, source code dan test, dokumentasi aturan dan desain OOP, catatan playtest terbatas, serta materi demonstrasi. Judul merupakan nama kerja proyek. Jadwal memakai Minggu 1 sampai Minggu 4 karena tanggal mulai dan tenggat kalender belum ditetapkan.

## Latar belakang dan tujuan

### Latar belakang

Pembelajaran pemrograman memerlukan kesempatan untuk menghubungkan instruksi dengan akibatnya. Dalam proyek ini, hubungan tersebut diwujudkan melalui robot yang bergerak dan berinteraksi dengan lingkungan. Pemain dapat mengamati kesalahan strategi, memperbaiki program, lalu membandingkan hasilnya pada keadaan awal yang sama.

Inspirasi mekanik berasal dari The Farmer Was Replaced, yang menggunakan bahasa mirip Python untuk mengotomatisasi pertanian dan membuka teknologi melalui resource. [1] Proyek ini mengembangkan premis berbeda: otomatisasi menjadi alat seorang astronaut untuk menopang kehidupannya di Mars. Cerita, misi, visual, dan implementasi akan dibuat untuk proyek ini.

### Permasalahan yang ingin dijawab

Bagaimana merancang game coding yang mendorong pemain menyelesaikan masalah dengan algoritma, bukan sekadar menghafal urutan gerakan? Bagaimana meningkatkan kesulitan secara bertahap sambil menjaga error tetap dapat dipahami dan percobaan ulang tetap mudah dilakukan?

### Tujuan proyek

Pertama, menghasilkan pengalaman belajar melalui tutorial, penelusuran kebun, keputusan berbasis sensor, dan pencarian rute. Kedua, menyediakan tantangan yang menguji solusi pada beberapa skenario sehingga program harus menyesuaikan diri. Ketiga, menerapkan pemisahan tanggung jawab, enkapsulasi, pewarisan, dan polimorfisme secara terukur pada kode game. Keempat, menguji kelayakan konsep melalui demonstrasi dan playtest terbatas.

### Sasaran pemain dan pembeda

Sasaran awal adalah mahasiswa atau pemain pemula yang bersedia belajar Python. Pemain tidak diharapkan sudah memahami BFS. Bantuan diberikan bertahap, dari penjelasan masalah, contoh kecil, sampai petunjuk algoritma. Pembeda utama adalah hubungan antara keputusan program dan kebutuhan habitat, serta evaluasi lintas skenario yang memberi alasan untuk merancang solusi umum.

Kondisi Mars dan sistem survival dipakai sebagai penyederhanaan fiksi untuk kebutuhan puzzle. Proposal tidak mengklaim menjadi simulasi ilmiah atau sudah membuktikan peningkatan kemampuan belajar.

## Konsep cerita dan siklus permainan

### Premis dan peran pemain

Setelah terpisah dari dukungan misinya, seorang astronaut harus mempertahankan habitat sementara menunggu kesempatan memperoleh bantuan. Sebuah robot pendamping masih dapat dioperasikan. Astronaut memanfaatkan keahlian programming untuk mengubah robot menjadi asisten yang dapat merawat tanaman dan mencapai perangkat yang sulit diakses.

Narasi disampaikan melalui pengantar misi dan catatan singkat astronaut. Untuk cakupan empat minggu, cerita berfungsi memberi tujuan pada puzzle; cutscene, dialog bercabang, dan sistem survival waktu nyata tidak menjadi kebutuhan utama.

### Siklus permainan

Baca masalah -> Amati peta -> Tulis Python -> Jalankan robot -> Evaluasi hasil -> Perbaiki strategi

Pemain membaca tujuan dan aturan, memeriksa data lingkungan, lalu menulis program. Setelah Run, robot menjalankan instruksi dan antarmuka menampilkan posisi, status objek, penggunaan aksi, serta pesan error. Pemain dapat menghentikan percobaan, kembali ke keadaan awal, dan mencoba strategi lain.

### Contoh misi pemulihan akses pompa

Pompa air habitat berada di ujung area yang terhalang bebatuan. Pemain mengetahui posisi awal, posisi tujuan, dan petak yang dapat dilalui. Robot perlu mencapai pompa melalui gerakan empat arah dengan biaya gerakan seragam. Satu program diuji pada tiga susunan penghalang yang sudah disiapkan.

Urutan gerak yang dihafal mungkin berhasil pada satu peta, tetapi gagal pada peta lain. Pemain diperkenalkan pada BFS sebagai strategi pencarian jalur terpendek pada peta berbiaya seragam. Solusi alternatif tetap diterima jika memenuhi tujuan; jumlah gerakan menjadi bahan optimasi, bukan kewajiban meniru bentuk kode tertentu.

### Percobaan yang aman untuk belajar

Dalam rancangan awal ini, waktu misi maju melalui aksi simulasi. Kegagalan kode mengulang percobaan misi tanpa menghapus progres misi sebelumnya. Pendekatan checkpoint ini diusulkan agar pemain berani bereksperimen. Angka resource dan batas aksi akan ditetapkan setelah solusi referensi diuji.

## Progres puzzle dan batas cakupan

Kesulitan meningkat melalui kebutuhan berpikir baru, bukan hanya ukuran peta atau panjang program. Tutorial mengenalkan kontrol, sedangkan misi inti menuntut pola penelusuran, keputusan berdasarkan keadaan, dan pencarian rute.

| Tahap | Tujuan permainan | Materi dan tantangan |
| --- | --- | --- |
| Tutorial | Gerakkan robot menuju satu persediaan. | Pemanggilan fungsi, argumen, urutan instruksi; latihan singkat tanpa tuntutan optimasi. |
| Misi 1 Kebun habitat | Kunjungi petak dan panen tanaman yang siap. | Loop, koordinat, fungsi bantu, dan penelusuran grid yang tidak melewatkan petak. |
| Misi 2 Perawatan adaptif | Rawat tanaman dengan keadaan berbeda dalam batas persediaan air. | Sensor, if, variabel, dan prioritas tindakan. Hindari menyiram petak yang tidak membutuhkan air. |
| Misi 3 Akses pompa | Capai tujuan pada tiga variasi peta berpenghalang. | List atau queue, lokasi yang sudah dikunjungi, rekonstruksi rute, dan pengenalan BFS. |

### Aturan evaluasi program

Setiap percobaan memakai fixture yang tetap dan dapat diulang. Misi 2 menyediakan tiga variasi kondisi tanaman, sedangkan Misi 3 menyediakan tiga variasi peta. Pemain dapat melihat dan mencoba seluruh skenario. Misi dinyatakan berhasil ketika tujuan pada semua skenario terpenuhi. Pencapaian tambahan membandingkan biaya aksi dengan solusi referensi yang valid.

Program dinilai dari perilakunya. Tidak ada kewajiban menggunakan nama variabel, struktur loop, atau algoritma tertentu jika hasilnya benar. Petunjuk algoritma membantu pemain memahami strategi, sementara umpan balik menunjukkan lokasi gagal dan aksi yang terbuang.

### Prioritas empat minggu

Wajib: satu robot, tampilan grid 2D, tutorial, tiga misi inti, Python asli, Run Stop Reset, bantuan API, hasil per skenario, penyimpanan progres lokal, dan pengujian. Misi 2 menambahkan satu jenis resource yaitu air; Misi 3 memakai peta statis berbiaya gerak seragam.

Pengembangan setelah target utama: eksplorasi dengan DFS, dependensi perbaikan perangkat, energi robot yang lebih kompleks, beberapa robot, badai dinamis, dan narasi lebih panjang. Fitur tersebut tidak dijanjikan dalam jadwal empat minggu. Multiplayer, akun, backend, dan engine 3D berada di luar cakupan.

## Arsitektur dan penerapan OOP

Implementasi melanjutkan stack prototype: TypeScript dan Vite untuk aplikasi, Canvas 2D untuk visual, serta Python melalui Pyodide dalam module Web Worker. Pemisahan Worker membuat komputasi Python tidak dijalankan pada thread antarmuka. [2] Browser desktop dengan keyboard menjadi target utama.

Antarmuka web <-> Module Web Worker dan Pyodide <-> Simulasi misi Python

Python menjadi sumber aturan misi dan state dunia. Worker mengirim snapshot dan hasil kepada UI; renderer tidak menghitung ulang aturan pertumbuhan atau penilaian. API baru untuk data peta dan sensor akan dirancang bersama misi. Watchdog, batas output, dan penghentian Worker mempertahankan kemampuan Stop ketika script tidak selesai.

### Rancangan objek yang diusulkan

| Objek | Tanggung jawab |
| --- | --- |
| Robot | Menyimpan posisi dan memvalidasi perpindahan melalui operasi yang terkontrol. |
| GridMap | Menyimpan petak, penghalang, dan akses tetangga yang dapat dilalui. |
| Crop | Menyimpan keadaan tanaman dan aturan perubahan setelah panen atau perawatan. |
| Mission | Mendefinisikan kontrak penyiapan skenario dan evaluasi tujuan. |
| FarmMission dan RouteMission | Mengkhususkan evaluasi tujuan kebun dan tujuan mencapai lokasi. |
| MissionSimulation | Menggabungkan objek dunia, biaya aksi, dan event yang dikirim ke tampilan. |

### Bukti konsep OOP

Enkapsulasi menjaga perubahan state melalui method yang memvalidasi aturan. Komposisi menghubungkan simulasi dengan robot, peta, dan tanaman. Pewarisan serta polimorfisme diterapkan pada Mission: controller memanggil kontrak evaluasi yang sama untuk jenis misi berbeda. Class menggabungkan data dan perilaku; Python mendukung pewarisan serta overriding method. [3]

Penerapan OOP berada pada kode pengembang; pemain pemula tetap boleh menyelesaikan misi dengan Python prosedural. Struktur di atas merupakan rencana refaktor bertahap, bukan klaim bahwa seluruh class Mars sudah tersedia.

## Manajemen proyek dan pembagian tugas

Proyek dikelola melalui backlog sederhana dengan status Direncanakan, Dikerjakan, Ditinjau, dan Selesai. Setiap tugas memiliki penanggung jawab, kriteria penerimaan, serta bukti pengujian. Pemetaan peran ke nama dan NPM dilakukan setelah identitas kelompok diisi; satu anggota dapat memegang lebih dari satu peran bila diperlukan.

| Peran | Tanggung jawab utama | Hasil dan peninjau |
| --- | --- | --- |
| R1 Koordinator dan desainer puzzle | Menjaga cakupan, merancang tutorial dan misi, menyiapkan fixture, solusi referensi, serta integrasi. | Spesifikasi dan misi layak dimainkan; ditinjau R2 dan R4. |
| R2 Pengembang simulasi Python | Menerapkan objek OOP, sensor, resource air, evaluasi tujuan, serta dukungan data peta. | Aturan dan unit test; ditinjau R1 dan R4. |
| R3 Pengembang antarmuka | Menyesuaikan visual Mars, editor, pemilih misi/skenario, animasi robot, dan feedback. | UI dan integrasi Worker; ditinjau R2 dan R4. |
| R4 QA dan dokumentasi | Menyusun kasus uji, menguji integrasi, mengelola catatan playtest, laporan, dan skenario demo. | Bukti verifikasi dan laporan; ditinjau R1. |

### Ritme kerja dan kendali perubahan

Pada awal minggu, tim memilih target dan membagi tugas berdasarkan dependensi. Pemeriksaan singkat di pertengahan minggu membahas hambatan, sedangkan akhir minggu digunakan untuk demo build yang dapat dijalankan. Perubahan mekanik dicatat pada dokumen aturan dan disertai pembaruan fixture atau test. Fitur tambahan masuk backlog setelah target wajib stabil.

### Definisi pekerjaan selesai

Sebuah tugas selesai ketika perilakunya memenuhi kriteria, test relevan lulus, integrasi dapat dijalankan, dan perubahan ditinjau anggota lain. Untuk misi, solusi referensi wajib menyelesaikan semua skenario. Untuk UI, Run Stop Reset dan pesan gagal harus dapat dipahami. Untuk dokumentasi, isi harus sesuai build, bukan rencana yang belum terwujud.

### Ketergantungan utama

Kontrak data peta dan sensor disepakati sebelum integrasi. Pengembangan UI dapat berjalan paralel memakai fixture contoh yang sama. Playtest dilakukan setelah semua misi inti dapat dijalankan dari awal sampai akhir. R1 menyelesaikan konflik prioritas agar pekerjaan minggu terakhir berfokus pada stabilisasi dan demonstrasi.

## Gantt chart pengembangan empat minggu

Jadwal berikut adalah usulan relatif sejak pengembangan konsep Mars dimulai. Fondasi prototype yang telah ada digunakan kembali. Durasi dan pembagian peran perlu dicocokkan dengan ketersediaan anggota serta tenggat resmi mata kuliah.

| Pekerjaan | Peran | M1 | M2 | M3 | M4 |
| --- | --- | --- | --- | --- | --- |
| Spesifikasi dan solusi referensi | R1 R4 | Aktif |  |  |  |
| Pemetaan OOP dan kontrak data | R2 R1 | Aktif |  |  |  |
| Adaptasi UI dan visual Mars | R3 | Aktif | Aktif |  |  |
| Tutorial dan misi kebun | R1 R2 | Aktif | Aktif |  |  |
| Sensor dan perawatan adaptif | R2 R1 |  | Aktif |  |  |
| Peta dan misi pencarian rute | R2 R1 |  | Aktif | Aktif |  |
| Evaluasi beberapa skenario | R1 R2 R3 |  |  | Aktif |  |
| Integrasi dan regression test | R2 R3 R4 | Aktif | Aktif | Aktif | Aktif |
| Playtest dan perbaikan utama | R4 R1 R3 |  |  |  | Aktif |
| Laporan dan persiapan demo | R4 semua |  |  | Aktif | Aktif |

### Target keluaran setiap minggu

| Minggu | Milestone dan bukti selesai |
| --- | --- |
| 1 | Cakupan dan API disepakati; build tema Mars dasar dan alur tutorial dapat dijalankan. |
| 2 | Misi kebun dan perawatan adaptif berfungsi; aturan dan test Python tersedia; peta rute mulai terintegrasi. |
| 3 | Misi rute dan evaluasi tiga skenario berfungsi; seluruh alur inti dapat dimainkan; cakupan fitur dibekukan. |
| 4 | Regression test, playtest terbatas, perbaikan masalah utama, laporan, dan demo akhir selesai. |

Dependensi kritis: aturan dan fixture -> simulasi misi -> evaluasi skenario -> integrasi -> playtest -> demo. Jika target minggu ketiga terlambat, tim mempertahankan misi wajib dan menyederhanakan aset/narasi; algoritma lanjutan tetap ditunda.

## Evaluasi risiko dan luaran akhir

### Rencana evaluasi

Pengujian mekanik memeriksa biaya aksi, sensor, resource air, batas peta, goal, dan fixture yang dapat diulang. Peta rute memakai gerakan empat arah berbiaya sama; hasil pemain dibandingkan dengan rute referensi yang telah diuji. Pengujian integrasi mencakup error Python, Stop pada loop tidak selesai, Reset, pesan Worker lama, dan konsistensi skor pada kecepatan animasi berbeda.

Playtest melibatkan 3 sampai 5 teman dengan pengalaman coding beragam. Catatan mencakup keberhasilan mengubah contoh, bantuan yang diperlukan, pemahaman error, serta keinginan mencoba strategi kedua. Hasil dilaporkan sebagai observasi terbatas; belum ada data yang membuktikan efektivitas belajar atau ketertarikan pasar.

| Risiko | Penanganan |
| --- | --- |
| Cakupan melebar | Batasi tutorial dan tiga misi; bekukan fitur pada akhir minggu ketiga. |
| Pemain kesulitan memahami algoritma | Gunakan contoh kecil, visual state, dan petunjuk bertahap tanpa memberi solusi lengkap sejak awal. |
| Script tidak selesai atau runtime gagal dimuat | Pertahankan Worker, watchdog, batas output, pemulihan runtime, serta kode pemain. |
| Solusi menghafal satu susunan | Evaluasi beberapa fixture terlihat dan berulang; nilai perilaku, bukan bentuk source. |

### Status awal dan luaran

Repository saat ini memiliki prototype pertanian web sebagai baseline, dengan catatan verifikasi lokal pada README.md dan VERIFICATION.md. [4] Cerita Mars, misi adaptif, dan puzzle rute masih merupakan usulan pengembangan. Luaran akhir ditargetkan berupa build web, kode dengan penerapan OOP, test, dokumentasi, catatan playtest, dan demonstrasi alur misi.

### Daftar referensi

[1] Timon Herzog. The Farmer Was Replaced. Halaman resmi Steam. store.steampowered.com/app/2060160 (https://store.steampowered.com/app/2060160/The_Farmer_Was_Replaced/)

[2] Pyodide Contributors. Using Pyodide in a web worker. pyodide.org/en/stable/usage/webworker.html (https://pyodide.org/en/stable/usage/webworker.html)

[3] Python Software Foundation. The Python Tutorial - Classes. docs.python.org/3/tutorial/classes.html (https://docs.python.org/3/tutorial/classes.html)

[4] Dokumen internal proyek. README.md dan VERIFICATION.md, catatan baseline Farm Code Lab tanggal 2 November 2026. Referensi web diakses untuk penyusunan proposal pada 4 November 2026.
