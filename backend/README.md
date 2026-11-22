# Backend Mars Reboot (versi pengembangan lokal)

Modul backend Spring Boot untuk akun dan skor, dihubungkan ke game desktop. Papan skor P1 ditambahkan atas permintaan pengguna; keputusan kompetisi dan penerimaan kelompok belum dicatat. Jalankan hanya sebagai backend lokal untuk demo.

## Menjalankan secara lokal

Dari folder utama repository di PowerShell, jalankan perintah ini:

```powershell
.\backend\gradlew.bat bootRun
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
- `GET /api/scores/leaderboard` menampilkan sampai 10 pemain dengan skor P1 terbaik. Skor terendah muncul di atas dan satu akun hanya tampil sekali.

## Cara membaca kodenya

Mulai dari controller, karena controller menerima permintaan dari game. Untuk alur skor, baca `ScoreController` lalu `ScoreService`, kemudian `ScoreRepository` dan `ScoreEntity`.

| Berkas | Tugasnya |
| --- | --- |
| `auth/AuthController.java` | Menerima permintaan daftar, masuk, melihat akun aktif, dan keluar. |
| `score/ScoreController.java` | Menerima permintaan untuk menyimpan atau melihat skor. |
| `score/ScoreService.java` | Menjalankan aturan sederhana sebelum skor disimpan atau ditampilkan. |
| `user/UserEntity.java` dan `score/ScoreEntity.java` | Bentuk data akun dan skor yang disimpan di database. |
| `user/UserRepository.java` dan `score/ScoreRepository.java` | Mencari dan menyimpan data menggunakan Spring Data JPA. |
| `config/SecurityConfig.java` | Mengatur login berbasis sesi, kata sandi, CSRF, dan endpoint yang perlu login. |
| `api/ApiExceptionHandler.java` | Mengubah kesalahan menjadi respons API yang lebih rapi. |

### Contoh alur simpan skor

1. Game mengirim data ke `POST /api/scores`.
2. `ScoreController` menerima data dan meneruskan nama akun aktif serta data skor ke `ScoreService`.
3. `ScoreService` memeriksa metrik, mencari skor lama dengan ID kiriman yang sama, lalu memutuskan apakah perlu menyimpan data baru.
4. `ScoreRepository` menyimpan atau mencari data di database.
5. Backend mengirim `ScoreView` sebagai jawaban. Skor yang dikirim ulang dengan ID dan isi yang sama tidak dibuat dua kali.

### Contoh alur login

1. Game mengirim nama pengguna dan kata sandi ke `POST /api/auth/login`.
2. Spring Security mencocokkan kata sandi dengan hash BCrypt di database.
3. Jika benar, backend menyimpan sesi login. Permintaan API berikutnya memakai sesi tersebut untuk mengenali akun.

Bagian penyimpanan sesi dan CSRF menggunakan Spring Security, jadi detailnya lebih banyak daripada aturan skor. Untuk memahami tugas backend, fokus dahulu pada alur controller → service → repository.

Kontrak API lengkap ada di `openapi.yaml` dan masih berupa draf.

## Hubungan dengan UI saat ini

Halaman awal game memiliki form masuk/daftar, tombol mulai sebagai tamu, setelan dasar, dan papan skor. Peta Misi `[M]` menunjukkan rekor pribadi dan papan skor juga, jadi hasil bisa diperiksa setelah bermain. Game mengirim skor hanya setelah puzzle dinyatakan berhasil.

Papan skor hanya berisi skor yang benar-benar dikirim pemain. Endpoint leaderboard dapat dibaca tanpa login; perubahan data tetap memerlukan sesi. Karena server lokal mendengarkan di `127.0.0.1`, akun dari komputer lain belum berbagi papan skor sampai backend dipasang pada server bersama.

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
