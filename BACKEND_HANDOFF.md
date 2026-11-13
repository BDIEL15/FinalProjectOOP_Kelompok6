# BACKEND_HANDOFF — mars_reboot.py

Tanggal: 13 November 2026. Panduan serah-terima ke anggota manusia dan AI; **usulan pembagian kerja**, bukan penetapan nama anggota atau izin bootstrap/push/deployment. Backend di sini berarti Spring Boot untuk akun/autentikasi dan skor. Simulasi robot, evaluator puzzle, runtime Python, dan renderer tetap bagian klien Java/libGDX.

## 1. Status dan tujuan bersama

- Game lengkap menargetkan 9 puzzle dalam 3 misi; tahap awal puzzle 1–4.
- Source game existing berada di core/ dan lwjgl3/ pada repo yang sama dengan panduan ini. Habitat/kontrol dan visual tersedia; terminal/akun/hasil masih memuat preview. Periksa status source terbaru sebelum bekerja, jangan menganggap tampilan akun atau rekor sebagai integrasi backend.
- Backend/OpenAPI belum ditemukan pada repository dokumentasi dan daftar modul game yang diperiksa. Lokasi source backend, repository bersama, nama pemilik, dan commit dasar belum ditetapkan. Jangan membuat backend duplikat jika anggota lain sudah mulai; cari dan baca handoff mereka dahulu.
- Hasil awal yang dituju: registrasi/login nyata → simpan skor contoh yang mengikuti kontrak → ambil kembali setelah buka ulang. Tahap berikutnya memakai hasil puzzle nyata. Data contoh wajib diberi label, bukan bukti pemain sudah menyelesaikan puzzle.

## 2. Pembagian untuk tim empat orang — Usulan

**Default: satu developer backend utama, satu reviewer bergilir dari anggota lain.** Reviewer tetap memiliki bagian utama sendiri. Tiga bagian lainnya adalah simulasi/runtime, puzzle/evaluasi, dan dunia/UI. Pembagian rata dinilai dari beban kode, test, integrasi, serta pemahaman; bukan jumlah push atau file saja.

Jika backend memang dibagi ke dua developer, sepakati dahulu kontrak dan skema bersama:

| Ownership | Developer A | Developer B |
| --- | --- | --- |
| Bagian utama | Bootstrap, akun, autentikasi/sesi, konfigurasi keamanan | Penyimpanan/pengambilan skor, validasi metrik, rekor, idempotensi |
| Bukti | Test registrasi/login/logout, hash password, sesi invalid | Test isolasi akun, skor tersimpan, versi salah, retry tanpa duplikasi |
| File bersama | A menjadi editor build/config/security/schema dasar | B mengusulkan perubahan melalui review, tidak menimpa file bersama |

Tentukan satu pemilik integrasi klien HTTP dan layar login sebelum kode dibuat; jangan dua developer mengubah UI yang sama. A/B adalah slot tugas, belum nama orang. Jika ada empat orang total, pertahankan kapasitas untuk gameplay dan UI; jangan menempatkan seluruh tim di Spring Boot.

## 3. Urutan baca dan sumber kontrak

1. [AGENTS.md](AGENTS.md): batas kerja, status, dan otorisasi.
2. [PROJECT.md](PROJECT.md), terutama bagian 2, 6–7 dan 12: arah dan pembagian tanggung jawab.
3. [PRD.md](PRD.md): FR-001/002/017/018, NFR-005/006/007, AC-01/08/16 serta keputusan KT-02.
4. [ARCHITECTURE.md](ARCHITECTURE.md) bagian 7: **satu sumber draft kontrak HTTP**, sesi, validasi dan persistensi.
5. [RULES.md](RULES.md) bagian 5: metrik dan formula skor kandidat. Jangan menetapkan formula sendiri di backend.
6. [TEST_PLAN.md](TEST_PLAN.md): API-01–04 dan SEC-01; [TASKS.md](TASKS.md): M-09, M-12/13/17/22 serta handoff terakhir.

Kontrak endpoint di ARCHITECTURE masih Usulan. Lokasi backend, versi Spring/JDK/build, database, sesi/CSRF, normalisasi akun, validasi field, dan deployment belum otomatis dipilih. Catat keputusan tim/pengguna dan sinkronkan dokumen pemilik sebelum mengimplementasikan kontrak yang berbeda. Jika membuat OpenAPI, tautkan dari ARCHITECTURE dan gunakan untuk contoh request/response serta mock klien. File OpenAPI belum dibuat oleh panduan ini.

## 4. Kontrak yang harus dijaga

- Spring Boot menyimpan autentikasi dan skor; source Python serta output pemain tetap di klien.
- Identitas pemilik skor berasal dari sesi autentikasi, bukan userId yang dikirim body.
- Aturan validasi skor mengikuti puzzle/rulesVersion/fixtureVersion yang dipilih bersama pemilik evaluator. Jangan mengklaim server membuktikan kebenaran permainan hanya dengan memvalidasi angka klien.
- Retry hasil sukses memakai submissionId yang sama; perilaku duplikasi mengikuti kontrak terpilih. Tidak menghitung retry sebagai permainan baru.
- Simpan skor dan status pemulihan habitat adalah dua hal berbeda; kegagalan jaringan tidak menghapus source atau hasil lokal yang sah.
- Perubahan endpoint, field, tipe, sesi, status error, dan versi data harus diketahui pemilik klien sebelum digabungkan.
- Jangan menaruh password/secret pada source, log, contoh payload nyata, atau paket klien. Nilai dummy harus jelas.
- Preview UI, mock response, dan test terisolasi bukan bukti integrasi desktop–backend atau deployment.

## 5. Skill bersama

Gunakan versi paket proyek yang dicatat [SKILLS.md](SKILLS.md) dan [skills-lock.json](skills-lock.json), bukan daftar skill global masing-masing laptop. Paket tersedia di `.agents/skills/` dalam repo ini dan ikut clone setelah di-commit/push oleh pemilik. Semua resource pendukung perlu dipertahankan.

| Skill | Pemakaian |
| --- | --- |
| domain-modeling | Istilah akun, hasil, percobaan, versi puzzle, dan rekor konsisten. |
| codebase-design | Interface dan tanggung jawab modul sederhana. |
| karpathy-guidelines, lean-build | Perubahan terarah dan satu hasil yang dapat diuji. |
| tdd | Implementasi perilaku/test pada antarmuka yang disepakati di tugas aktif. |
| diagnosing-bugs | Diagnosis kegagalan reproduktif sebelum memperbaiki. |
| verify-and-stop | Menjalankan bukti relevan, melaporkan batas, berhenti ketika selesai. |
| doc-coauthoring | Menulis atau menyelaraskan kontrak dan handoff. |

Bagikan folder skill lengkap, bukan hanya SKILL.md. Pertahankan hash/versi yang sama; jangan menyalin credential, cache pribadi, atau seluruh konfigurasi akun. Jangan menjalankan semua workflow atau menginstal dependency global hanya karena suatu skill terpasang. Periksa penemuan skill pada setiap aplikasi dan laporkan terpisah dari ketersediaan filenya. `mars-backend` khusus belum dibuat; tidak diperlukan untuk mulai membaca kontrak. Samanya skill tidak menggantikan kontrak dan review yang sama. Tidak ada subagent otomatis.

## 6. Siklus satu tugas

1. Tetapkan ID tugas, satu pemilik, reviewer, file/modul, commit dasar, dependensi, dan kriteria selesai. Mulai dari kontrak M-09, kemudian autentikasi M-12, skor M-13, integrasi M-17; backend tidak harus menunggu seluruh puzzle.
2. Gunakan clone/worktree terpisah jika paralel. Branch per tugas dengan nama jelas; jangan berbagi folder sinkronisasi untuk edit bersamaan.
3. Implementasikan satu perilaku dan bukti relevan. Hindari upgrade dependency, perubahan aturan/gameplay, atau refactor tetangga di luar tugas.
4. Pemilik melakukan commit/push dengan identitasnya setelah otorisasi Git sesi tersedia, lalu membuat PR yang menyebut cara mencoba dan hasil test. Tidak ada commit/push dilakukan oleh sesi penulisan panduan ini.
5. Reviewer mencoba hasil dan dapat menjelaskan input/output serta kasus gagalnya. Pemilik memperbaiki temuan; integrasi dilakukan berurutan setelah kontrak dan test cocok.
6. Demo bersama: register/login, simpan/ambil skor, lalu backend mati/sesi habis/retry. Perbarui handoff dan VERIFICATION berdasarkan hasil aktual.

Selesai backend lokal tidak sama dengan integrasi game teruji atau deployment. Test minimal: akun A/B terisolasi, kredensial salah/sesi invalid, payload/versi tidak valid, persistensi buka ulang, retry idempotent, gangguan jaringan, dan tidak ada kebocoran source/output/secret. Command build/test dicatat dari proyek nyata; jangan mengarang command atau menganggap script npm prototype menjalankan backend.

## 7. Prompt awal untuk teman atau AI

```text
Kamu membantu anggota backend mars_reboot.py. Baca BACKEND_HANDOFF.md,
AGENTS.md, dokumen yang dirujuk, serta handoff terbaru TASKS.md.
Periksa git status dan temukan source backend existing sebelum membuat file.

Tugas aktif: [isi M-ID dan satu hasil konkret].
Pemilik / reviewer: [nama].
Root backend / branch / commit dasar: [isi yang benar-benar ada].
Ownership file/modul: [batas yang disepakati].
Kriteria selesai dan bukti: [perilaku dan pemeriksaan].

Jika kolom tugas/ownership belum diisi, lakukan audit read-only dan usulkan
satu tugas; jangan mengimplementasikan seluruh backlog. Bedakan kontrak
yang sudah dipilih dari Usulan. Jangan mengubah UI/simulasi/runtime atau
kontrak bersama tanpa koordinasi pemiliknya. Gunakan skill proyek yang
relevan; jangan membuat subagent, instalasi global, atau deployment.
Commit/push mengikuti otorisasi eksplisit sesi, bukan otomatis.
Tulis handoff: hasil, file, kontrak berubah/tetap, verifikasi aktual,
blocker, dan langkah berikutnya. Jangan menyebut mock sebagai integrasi.
```

Isi slot tugas/nama/branch sebelum menyerahkan pekerjaan. Panduan ini tidak membuktikan semua anggota sudah menerima konteks atau skill, tidak membuat backend, dan tidak menandai milestone implementasi selesai.
