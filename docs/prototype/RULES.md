# RULES — Aturan permainan v0.1

Status: kontrak rules-v1 diimplementasikan; mekanik dan baseline diverifikasi pada CPython serta Pyodide. Sumber kebenaran untuk fixture, API, tick, dan skor. Semua perubahan mekanik harus memperbarui versi aturan dan kasus ujinya.

## 1. Dunia dan fixture

- Grid berukuran tetap `6 × 6`, dengan koordinat integer `x=0..5`, `y=0..5`.
- Titik `(0, 0)` berada di kiri bawah. Timur menambah x; utara menambah y.
- Satu drone mulai di `(0, 0)`, tick `0`, hasil panen `0`.
- Setiap petak berisi satu tanaman; total `36` tanaman. Tidak ada tanam ulang.
- Dunia tidak membungkus di tepi dan tidak memiliki rintangan lain.
- Tidak ada random, tanggal sistem, input jaringan, atau FPS dalam aturan simulasi.

Fixture kematangan awal menggunakan waktu absolut simulasi:

```python
ripe_at = 0 if (x + y) % 2 == 0 else 8
```

Tanaman yang belum dipanen matang jika `tick >= ripe_at`. Panen mengubahnya menjadi petak kosong permanen untuk percobaan itu. Waktu hanya bergerak karena aksi game yang memiliki biaya tick.

## 2. Tujuan dan skor

- Sukses segera setelah tanaman ke-36 dipanen, termasuk jika aksi terakhir berakhir tepat pada tick `200`.
- Skor percobaan sukses adalah tick akhir; lebih kecil lebih baik.
- Percobaan yang berakhir tanpa memenuhi tujuan bukan rekor sukses.
- Batas percobaan adalah `200` tick. Jika tick mencapai 200 tanpa sukses, hasil `budget_exceeded`.
- Jika biaya suatu aksi akan melewati tick 200, aksi tidak diterapkan, tick tidak berubah, dan percobaan berakhir `budget_exceeded`.
- Waktu nyata, kecepatan animasi, jumlah baris kode, dan kecepatan CPU bukan skor.
- Rekor lokal hanya dibandingkan dalam versi aturan yang sama. Skor sama mempertahankan rekor sebelumnya.

## 3. API pemain

Nama fungsi tersedia langsung di namespace script. Python standar seperti `range`, variabel, loop, fungsi, tuple, dan `print` dapat digunakan sesuai dukungan runtime browser.

| Fungsi | Nilai balik | Biaya | Perilaku |
| --- | --- | --- | --- |
| `move(direction)` | `bool` | 1 tick | Pindah satu petak jika arah valid dan tidak melewati tepi. |
| `can_harvest()` | `bool` | 0 tick | Periksa tanaman di bawah drone pada tick saat ini. |
| `harvest()` | `bool` | 2 tick | Panen jika tanaman sudah matang dan belum dipanen. |
| `get_pos()` | `tuple[int, int]` | 0 tick | Kembalikan `(x, y)` drone. |
| `wait()` | `None` | 1 tick | Majukan waktu tanpa bergerak. |

`direction` hanya menerima string persis `"north"`, `"east"`, `"south"`, atau `"west"`. Huruf besar dan alias tidak diterima. Jumlah/jenis argumen salah menghasilkan error Python yang jelas dan mengakhiri script; tidak mengubah state atau tick.

### Urutan sebuah aksi

1. Pastikan percobaan masih aktif dan argumen valid.
2. Pastikan biaya aksi tidak melewati batas tick.
3. Evaluasi dan terapkan efek berdasarkan state **sebelum** biaya aksi ditambahkan.
4. Tambahkan biaya tick, termasuk untuk aksi valid yang gagal melakukan pekerjaan.
5. Hitung keadaan matang dari tick terbaru; kirim event dan snapshot.
6. Periksa sukses terlebih dahulu, lalu batas tick.

Konsekuensi penting:

- Bergerak ke luar peta mengembalikan `False`, tidak memindahkan drone, tetapi tetap menghabiskan 1 tick.
- Panen tanaman belum matang atau petak kosong mengembalikan `False`, tetap menghabiskan 2 tick.
- Panen pada tick 7 untuk tanaman `ripe_at=8` gagal, meskipun tick menjadi 9 setelah percobaan panen.
- Sensor tidak memajukan waktu. `while not can_harvest(): pass` tidak mematangkan tanaman; gunakan `wait()`.
- Pembaruan visual dan `print` tidak mengubah tick.
- Selesai tujuan dan batas tick menghentikan eksekusi melalui kontrol runner, tanpa mengharuskan pemain menulis `break`.

## 4. Starter code

Gunakan contoh Python valid berikut sebagai baseline yang bisa diubah pemain. Dokumentasikan bahwa ukuran peta tetap 6 pada prototype.

```python
for row in range(6):
    for column in range(6):
        while not can_harvest():
            wait()
        harvest()

        if column < 5:
            if row % 2 == 0:
                move("east")
            else:
                move("west")

    if row < 5:
        move("north")
```

Baseline yang diharapkan: **36 panen, 35 gerakan, 36 aksi panen, 5 wait, 112 tick, posisi akhir `(0, 5)`**. Ini pembanding awal, bukan klaim solusi optimal. Fixture dan runner harus membuktikan hasil tersebut sebelum contoh dinyatakan lulus.

## 5. Hasil percobaan

| Hasil | Pemicu | Rekor |
| --- | --- | --- |
| `success` | Seluruh 36 tanaman dipanen | Boleh disimpan. |
| `incomplete` | Script berakhir normal sebelum tujuan | Tidak disimpan sebagai rekor. |
| `budget_exceeded` | Batas tick tercapai/dilampaui oleh permintaan aksi | Tidak disimpan. |
| `error` | Syntax error, runtime error, atau argumen API salah | Tidak disimpan. |
| `timeout` | Melewati batas waktu nyata komputasi | Tidak disimpan. |
| `stopped` | Pemain menekan Stop saat eksekusi atau replay | Tidak disimpan. |

Timeout dan batas output adalah perlindungan runtime di ARCHITECTURE.md, bukan mekanik pemberian skor. Script yang sudah menghasilkan sukses tetap boleh dibatalkan pemain saat replay; percobaan itu ditandai stopped dan tidak dicatat sebagai rekor.

## 6. Reset dan percobaan ulang

- Setiap Run memulai fixture baru dari tick 0; tidak melanjutkan dunia percobaan sebelumnya.
- Reset mengembalikan dunia, progres, dan log percobaan ke awal; kode dan rekor tetap disimpan.
- Stop membekukan state terakhir yang sudah ditampilkan, membuang event yang belum ditampilkan, dan mempertahankan kode.
- Reset boleh ditekan saat aktif; implementasinya membatalkan percobaan lama dahulu.
- Pesan dari percobaan lama tidak boleh mengubah percobaan baru.

## 7. Kasus uji mekanik minimum

1. Fixture memiliki 36 tanaman dengan pola kematangan yang ditetapkan.
2. Semua arah, tepi peta, dan argumen tidak valid mengikuti kontrak API.
3. Panen gagal, matang, serta petak kosong membebankan tick dengan benar.
4. Sensor berulang tidak memajukan tick; wait mengubah kematangan.
5. Starter code menyelesaikan fixture tepat seperti baseline di atas.
6. Panen terakhir tepat pada tick 200 sukses; non-sukses pada 200 berhenti.
7. Aksi yang akan melewati batas tidak mengubah state.
8. Script berakhir dini menghasilkan incomplete.
9. Hasil sama pada kecepatan animasi berbeda dan Run berulang.

Gunakan fixture kecil/rekayasa state dalam unit test untuk batas tick; ukuran peta pada produk tetap 6×6.
