# DESIGN — Spesifikasi antarmuka prototype

Status: spesifikasi diterapkan pada prototype v0.1 dan diperiksa lewat screenshot browser aktual. Belum ada persetujuan visual final atau playtest manusia. Catatan implementasi berada di bagian 9.

## 1. Prinsip dan arah

Mode permukaan: **Operate**. Pemain datang untuk menulis, menjalankan, mengamati, dan memperbaiki program. Mekanik yang sedang bekerja harus terlihat pada layar pertama.

Arah awal: meja kerja coding yang tenang dengan kebun bergaya diagram 2D. Identitas muncul melalui grid, tanaman, dan gerakan drone; kontrol memakai pola aplikasi yang familier. Tidak perlu landing page, hero pemasaran, modal onboarding wajib, dekorasi 3D, atau animasi pembuka.

Tujuan visual: pemain dapat menemukan Run, memahami tujuan, dan membedakan drone, tanaman matang, tanaman muda, serta petak kosong tanpa penjelasan panjang.

## 2. Layout desktop

Target pemeriksaan awal: 1440×900 dan 1280×720. Gunakan layout fluid; angka berikut adalah titik awal implementasi, bukan ukuran kaku.

```text
┌──────────────────────────────────────────────────────────────┐
│ Farm Code Lab             Prototype · Python          Bantuan │
├──────────────────────────────────────────────────────────────┤
│ Panen 36 tanaman dalam ≤200 tick. Lebih sedikit lebih baik.   │
├─────────────────────────────┬────────────────────────────────┤
│ Kode Python                 │ Kebun 6×6                      │
│ [Run] [Stop] [Reset]        │ Panen 0/36       Tick 0/200     │
│                             │                                │
│ Editor multiline            │ Grid, drone, label koordinat   │
│                             │                                │
│                             │ Kecepatan  [1×] [2×] [4×]      │
├─────────────────────────────┼────────────────────────────────┤
│ Panduan perintah            │ Hasil dan output               │
│ API + biaya + arah          │ Rekor lokal / pesan error      │
└─────────────────────────────┴────────────────────────────────┘
```

- Bagian kerja kiri sekitar 48%, kanan 52%, dengan batas lebar yang masuk akal untuk keduanya.
- Tujuan, Run/Stop/Reset, editor, serta grid harus terjangkau tanpa melewati bagian pemasaran.
- Editor minimal sekitar 18 baris pada layar cukup tinggi; panel output memiliki scroll sendiri dengan batas tinggi.
- Panduan API dapat diringkas, tetapi biaya aksi dan fakta sensor tidak memajukan waktu harus mudah ditemukan.
- Grid tetap persegi, mengisi ruang tersedia, tanpa blur akibat scaling Canvas pada layar HiDPI.

Di bawah lebar 960 px, susun menjadi tujuan → kontrol → editor → kebun → panduan/output. Hindari horizontal scroll seluruh halaman. Pada layar sempit, tampilkan catatan ringan bahwa pengalaman coding direkomendasikan pada laptop/PC, tanpa memblokir akses.

## 3. Token awal

| Token semantik | Nilai awal | Pemakaian |
| --- | --- | --- |
| `--color-page` | `#F5F3EC` | Latar halaman. |
| `--color-surface` | `#FFFFFF` | Editor dan panel. |
| `--color-text` | `#202B24` | Teks utama. |
| `--color-muted` | `#536258` | Teks sekunder. |
| `--color-border` | `#CBD2C5` | Pemisah non-interaktif. |
| `--color-primary` | `#245C3C` | Run dan aksi utama. |
| `--color-primary-text` | `#FFFFFF` | Teks tombol utama. |
| `--color-focus` | `#1D4ED8` | Outline fokus 2–3 px dengan offset. |
| `--color-success` | `#245C3C` | Hasil sukses. |
| `--color-warning` | `#805400` | Batas/percobaan belum selesai. |
| `--color-error` | `#A52A25` | Error dan kegagalan runtime. |
| `--color-soil` | `#D9C5A4` | Petak kosong. |
| `--color-young` | `#496C46` | Tanaman muda. |
| `--color-ripe` | `#AE7010` | Tanaman siap panen. |

Gunakan system sans-serif untuk UI dan system monospace untuk source/output. Ukuran awal: body 15–16 px, editor 14–16 px, heading utama 20–24 px; line-height editor sekitar 1.5. Spacing dasar 4, 8, 12, 16, 24, 32 px. Radius panel 8 px, kontrol 6 px. Prioritaskan border dan jarak daripada shadow berlapis.

Token belum diverifikasi secara rendered. Implementasi wajib memeriksa kontras teks dan indikator kontrol; warna border panel tidak otomatis cocok untuk outline input interaktif.

## 4. Kebun dan koordinat

- Canvas menggambar grid 6×6 dengan label x di bawah dan y di kiri.
- `(0, 0)` kiri bawah; UI membalik y untuk koordinat layar tanpa mengubah simulasi.
- Tanaman muda: tunas kecil. Tanaman matang: bentuk lebih penuh dengan tanda siap panen. Petak kosong: tanah tanpa tanaman.
- Drone: bentuk sederhana dengan outline dan penunjuk pusat yang tetap terbaca di atas tanaman.
- Beri legenda tekstual. Jangan memakai warna sebagai satu-satunya pembeda keadaan.
- Sediakan ringkasan DOM yang menyebut posisi drone, panen, tick, serta data petak dalam daftar/tabel yang dapat dibuka. Canvas saja tidak cukup sebagai representasi aksesibel.
- Status tanaman mengikuti tick simulasi, bukan waktu animasi browser.

## 5. Editor dan kontrol

Versi awal boleh memakai textarea native dengan label eksplisit, spellcheck off, serta font monospace. Syntax highlighting dan autocomplete bukan syarat. Jangan membuat editor sendiri dengan contenteditable.

- Run: tindakan utama; disabled selama loading, running, replaying, atau source tidak valid ukurannya.
- Stop: aktif saat running/replaying; mengakhiri percobaan, tidak menjanjikan resume.
- Reset: mengembalikan dunia dan log; tooltip/helper menyatakan kode dan rekor dipertahankan.
- Kecepatan: label “Kecepatan animasi”; pilihan 1×/2×/4×, boleh diubah saat replay.
- Bila menyediakan Ctrl/Cmd+Enter untuk Run, tetap sediakan tombol dan petunjuk shortcut.
- Tab pada editor tetap dapat memindahkan fokus. Jika indentasi Tab ditambahkan, sediakan cara keluar yang jelas; jangan membuat keyboard trap.
- Saat aktif, editor read-only agar source yang terlihat sama dengan source percobaan. Setelah selesai, fokus tidak dipindahkan paksa.

## 6. State dan copy

| State | Pesan utama | Interaksi |
| --- | --- | --- |
| Loading | “Menyiapkan Python… pemuatan pertama bisa lebih lama.” | Run disabled; jangan tampilkan persentase unduhan palsu. |
| Runtime gagal | “Python belum berhasil dimuat. Periksa koneksi lalu coba lagi.” | Tombol Coba lagi; kode tetap ada. |
| Ready | “Siap. Jalankan contoh atau ubah kodenya.” | Run aktif. |
| Running | “Menjalankan Python…” | Stop/Reset aktif. |
| Replaying | “Menampilkan hasil gerakan…” | Stop/Reset dan kecepatan aktif. |
| Success | “Selesai! 36/36 tanaman dipanen dalam {tick} tick.” | Run ulang; bandingkan rekor aturan yang sama. |
| Incomplete | “Program selesai. Masih ada {remaining} tanaman.” | Ubah kode lalu Run. |
| Budget exceeded | “Batas 200 tick tercapai atau aksi berikutnya akan melewatinya.” | Tampilkan panen dan tick aktual; tidak mengklaim semuanya dipakai. |
| Error | “Ada kesalahan di kode: {pesan}.” | Tampilkan baris player.py jika diketahui, jangan menebak lokasi. |
| Timeout | “Program melewati batas waktu. Periksa loop yang tidak selesai.” | Siapkan runtime ulang; pertahankan kode. |
| Stopped | “Percobaan dihentikan. Kode kamu tetap tersimpan.” | Run berikutnya mulai dari kebun awal. |
| Storage gagal | “Penyimpanan lokal tidak tersedia; kamu tetap bisa bermain.” | Tidak memblokir Run. |

Pisahkan ringkasan hasil dari stdout. Aksi gagal seperti bergerak ke tepi adalah umpan balik permainan, bukan error aplikasi. Hindari traceback internal panjang pada tampilan utama; informasi debug pengembang dapat berada di detail terpisah.

## 7. Motion dan aksesibilitas

- Gerakan menunjukkan perpindahan satu petak, bukan efek dekoratif. Biaya aksi memengaruhi durasi sesuai ARCHITECTURE.md.
- Hormati prefers-reduced-motion; hapus interpolasi dan kedipan tanpa mengubah skor atau state.
- Semua kontrol memakai elemen native dan fokus terlihat; target klik sekitar 40 px atau lebih pada area yang memungkinkan.
- Kontras teks normal minimal 4.5:1; teks besar dan indikator grafis penting minimal 3:1 sesuai kebutuhannya.
- Gunakan aria-live untuk perubahan fase dan hasil, bukan setiap tick atau setiap fragmen stdout.
- Output dirender sebagai teks, bukan HTML. Panjang dibatasi oleh kontrak runtime.
- Jangan mengubah layout besar saat error atau hasil muncul; sediakan area status yang stabil.

## 8. Checklist pemeriksaan implementasi

- Starter, tujuan, biaya, koordinat, dan hasil tidak bertentangan dengan RULES.md.
- Pada 1280×720 dan 1440×900 tidak ada kontrol terpotong atau panel saling menutup.
- Pada lebar 390 px halaman tetap dapat dibaca tanpa scroll horizontal global.
- Keyboard dapat mencapai Run, Stop, Reset, bantuan, dan editor tanpa terjebak.
- Loading, error, timeout, stopped, incomplete, dan success sudah dilihat langsung.
- Reduced motion dan kecepatan berbeda memberikan hasil simulasi yang sama.
- Screenshot hanya ditambahkan sebagai bukti sesudah aplikasi benar-benar dijalankan.

## 9. Sistem yang terimplementasi

- Token warna bagian 3 berlaku pada `src/styles.css`. Amber siap panen disesuaikan dari nilai awal `#B77712` menjadi `#AE7010` agar kontras bagian tanaman terhadap petak melewati 3:1. Tidak ada perubahan mekanik.
- System UI sans-serif; editor/output menggunakan Cascadia Code, SFMono-Regular, atau Consolas. Font disediakan perangkat, tanpa unduhan font pihak ketiga.
- Header 76 px; workspace dua kolom 0.96:1.04, gap 24 px; radius panel 8 px dan kontrol 6 px. Di bawah 960 px panel bertumpuk. Bagian bantuan memakai progressive disclosure native.
- Canvas menggambar geometri tanaman dan drone buatan proyek, memakai devicePixelRatio dan ResizeObserver. Interpolasi gerakan maksimal 100 ms dihapus saat reduced motion; urutan snapshot dan tick tetap berasal dari Python.
- Ready, success, incomplete, budget, syntax/runtime error, timeout, stopped, serta kegagalan unduhan/recovery diuji di browser. Screenshot lokal 1440×900, 1280×720, 390×844 dan rincian kontras tercatat di [VERIFICATION.md](VERIFICATION.md).
- Stop/timeout menyediakan tombol pemulihan eksplisit. Editor memakai textarea native; Tab keluar dari editor. Tabel petak menandai posisi drone dengan teks, bukan hanya outline.
- Review visual dilakukan dalam chat tanpa delegasi; ini bukan persetujuan desain oleh pengguna atau audit pembaca layar manusia.
