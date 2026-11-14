# Backend Mars Reboot (draf untuk ditinjau kelompok)

Modul Spring Boot mandiri. Kebijakan akun, API, alur sesi/CSRF, arti skor, dan penyimpanan data masih berupa usulan. Sepakati dulu dengan kelompok sebelum menghubungkannya ke game.

## Menjalankan secara lokal

Dari folder utama repository di PowerShell, jalankan perintah ini:

```powershell
.\gradlew.bat -p backend bootRun
```

Modul ini membutuhkan JDK 21, berjalan di `127.0.0.1:8081`, dan menyimpan data H2 lokal di `backend/data`.

## Endpoint yang tersedia

- `GET /api/csrf` memberikan nama header dan token CSRF.
- `POST /api/auth/register` mendaftarkan akun dengan kata sandi yang disimpan sebagai hash BCrypt.
- `POST /api/auth/login` membuat sesi pengguna.
- `GET /api/auth/me` menampilkan akun yang sedang masuk.
- `POST /api/auth/logout` mengakhiri sesi.
- `POST /api/scores` menyimpan skor pada akun yang sedang masuk.
- `GET /api/scores/me` menampilkan skor milik akun tersebut.
- `GET /api/scores/me/best` menampilkan skor terbaik untuk puzzle dan versi aturan yang dipilih.

Kontrak API lengkap ada di `openapi.yaml` dan masih berupa draf.

## Hubungan dengan UI saat ini

UI Peta Misi menampilkan nama akun serta rekor dalam bentuk jumlah aksi dan air. Setelah UI dihubungkan ke API, `GET /api/auth/me` menyediakan nama akun dan `GET /api/scores/me` mengembalikan daftar kiriman per akun. Nilai `metrics.actionCount` dan `metrics.waterUsed` dapat dipakai untuk mengisi teks rekor. Kirim satu kiriman hanya setelah puzzle dinyatakan berhasil di game.

Nama `abdiel_01` dan rekor P1 yang tertulis di UI saat ini adalah teks preview. Backend tidak membuat akun atau rekor contoh otomatis; daftar akan kosong sampai akun membuat kiriman skor.

Contoh isi `POST /api/scores`:

```json
{
  "submissionId": "c07bfe4f-8a45-4c91-945b-786ecfe3ad19",
  "puzzleId": "P1",
  "rulesVersion": "v1",
  "fixtureVersion": "p1-a",
  "score": 6,
  "metrics": {
    "actionCount": 6,
    "waterUsed": 1
  }
}
```

Untuk bentuk UI saat ini, `metrics` wajib memuat `actionCount` dan `waterUsed`, dan `score` harus sama dengan `actionCount`. Pemilihan jumlah aksi sebagai skor masih perlu dikonfirmasi pemilik gameplay sebelum dijadikan aturan final.

Sebelum permintaan yang mengubah data, ambil token dari `/api/csrf` dan kirimkan melalui header yang disebutkan di respons. Ambil token baru setelah login atau logout.

## Aturan akun dan skor sementara

Nama pengguna dipangkas, dijadikan huruf kecil, dan dibatasi 3–32 karakter berupa huruf, angka, titik, garis bawah, atau tanda hubung. Kata sandi harus berukuran 8–72 byte UTF-8. Pendaftaran tidak otomatis membuat sesi.

Pengiriman skor menggunakan `submissionId` UUID. Mengirim ID dan isi yang sama lagi tidak membuat catatan duplikat; memakai ID yang sama untuk skor berbeda ditolak. Skor terbaik sementara memakai angka terkecil untuk puzzle dan versi aturan yang sama. Kelompok perlu mengonfirmasi arti dan aturan skor ini. Backend menyimpan skor yang dikirim klien, tetapi belum membuktikan bahwa puzzle benar-benar diselesaikan.

Ini masih dasar untuk pengembangan lokal. Konfigurasi produksi seperti migrasi database, TLS, pembatasan percobaan, dan pemulihan kata sandi belum tersedia.
