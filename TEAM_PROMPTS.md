# TEAM_PROMPTS — Prompt awal tim dan AI

Gunakan pada Codex atau Antigravity yang dibuka di root clone MarsReboot. Prompt tidak menyinkronkan chat; konteks bersama berasal dari repository. Pilih satu peran/tugas per sesi. Isi nama/ID/ownership sebelum implementasi, jangan mengarang penugasan.

## 1. Prompt pertama setelah clone — semua anggota

```text
Kamu membantu tim empat orang mengembangkan mars_reboot.py pada repo ini.
Proyek libGDX SUDAH ADA di core/ dan lwjgl3/. Jangan menjalankan generator
ulang atau membuat game/backend pengganti sebelum memeriksa source existing.

Baca AGENTS.md, TEAM_GUIDE.md, PROJECT.md bagian 2, 5–7, 11–12,
serta handoff terbaru TASKS.md. Baca PRD/RULES/ARCHITECTURE/DESIGN dan
TEST_PLAN hanya pada bagian yang relevan. Baca SKILLS.md, lalu gunakan
skill proyek yang diperlukan saja.

Lakukan audit read-only dahulu: git status/branch/remote, modul/build,
versi aktual, source yang sudah ada, dan bukti terakhir. Ringkas:
1. Game yang dibuat, target 9 puzzle, dan tahap awal 1–4.
2. Apa yang nyata bekerja versus preview/mock/rencana.
3. Bagian yang perlu dikerjakan berikutnya dan dependensinya.
4. Saran satu tugas kecil untuk peran saya, ownership dan bukti selesai.

Peran saya: [1/2/3/4]. Nama saya: [nama].
Jika peran/tugas belum jelas, tanyakan hanya keputusan yang tidak dapat
ditemukan dari repo; jangan mengeksekusi seluruh backlog. Gunakan bahasa
Indonesia yang ringkas dan jelaskan aliran input-state-output agar saya
memahami kode, bukan sekadar menerima hasil AI.

Jangan mengedit pada tahap audit, memasang software global, membuat
subagent, commit, push, atau deployment. Tugas implementasi diberikan
terpisah setelah konteks dan ownership dipahami.
```

## 2. Prompt implementasi satu tugas — semua anggota

```text
Lanjutkan pemahaman repo sebelumnya dan kerjakan satu tugas berikut.
M-ID / hasil konkret: [isi]. Pemilik / reviewer: [nama / nama].
Branch / commit dasar: [hasil pemeriksaan aktual].
File/modul yang boleh diubah: [batas].
Kontrak yang dipakai: [dokumen/DTO/OpenAPI terpilih dan versi].
Kriteria selesai / test: [perilaku dan cara membuktikannya].

Periksa perubahan lokal dan pertahankan edit orang lain. Reuse source
existing; bedakan Usulan dari keputusan yang dipilih. Jika kontrak tidak
cukup atau berbeda, jelaskan dampaknya dan koordinasikan pemiliknya;
jangan mengubah kedua sisi secara diam-diam. Jangan menambah fitur,
dependency, engine, atau refactor di luar tugas.

Implementasi, jalankan bukti fokus yang relevan, lalu laporkan PASS/FAIL/
BELUM DIJALANKAN dengan command dan batasnya. Mock bukan bukti integrasi;
browser bukan bukti desktop libGDX. Jelaskan kelas utama dan satu kasus
gagal agar saya bisa mendemokan kontribusi sendiri.

Perbarui task/handoff serta dokumen pemilik bila keputusan berubah.
Jangan membuat subagent, instalasi global, commit/push/deployment kecuali
secara eksplisit termasuk otorisasi sesi ini. Berhenti setelah tugas
memenuhi kriteria; jangan otomatis mengerjakan M-ID berikutnya.
```

Jika kolom kontrak/ownership belum diisi, agen membantu merumuskannya dari repository sebelum menyentuh implementasi, bukan menebak nama anggota atau mengklaim persetujuan tim.

## 3. Tambahan prompt sesuai peran

### Peran 1 — Simulasi dan runtime, biasanya Codex

```text
Saya memegang domain robot/runtime. Pisahkan dari habitat astronaut.
Aturan Java menjadi sumber state; Python meminta aksi/sensor dan UI
menerima snapshot. Mulai dari fixture/puzzle 1 serta kontrak yang sudah
dipilih, lalu buktikan gerak/panen/sensor, hasil gagal, Stop loop, Reset,
timeout, dan penolakan hasil lama pada tugas yang relevan. Jangan mengubah
visual atau backend sebagai bagian tambahan. Pertahankan gameplay dasar
existing. Runtime/bahasa yang belum disetujui tetap eksperimen berlabel.
```

### Peran 2 — Puzzle, evaluator, progres, biasanya Codex

```text
Saya memegang konten/evaluasi. Gunakan RULES dan PRD; angka/fixture Usulan
tidak otomatis final. Buat reference solution dan kasus negatif, evaluator
Java, serta progres sesuai tugas. Pisahkan tujuan dunia dari latihan
loop/fungsi. Source yang sama harus memenuhi semua skenario wajib; gerakan
dan air mengikuti metrik yang sama dengan backend. Jangan membuat aturan
kedua di renderer atau memproduksi sembilan puzzle sekaligus.
```

### Peran 3 — Dunia, UI, visual, biasanya Antigravity

```text
Saya memegang UI/visual. Baca DESIGN dan docs/mars/design-refs serta handoff
kontrol. Gunakan HabitatRenderer/Ui dan aset existing. Jangan mengubah
HabitatWorld/Input/Screen atau collider hanya untuk mencocokkan gambar
tanpa koordinasi pemilik. UI mengikuti state Java; preview diberi label,
tidak dianggap runtime/akun/progres nyata. Periksa fokus editor, Esc,
gerak modal, interaksi dan resize pada desktop; jangan mengklaim Figma
atau screenshot browser sebagai hasil build game.
```

### Peran 4 — Backend, klien HTTP, distribusi, biasanya Codex

```text
Saya memegang backend/distribusi. Baca BACKEND_HANDOFF.md dan kontrak
ARCHITECTURE bagian 7. Cari backend existing sebelum bootstrap. Sepakati
endpoint/data/sesi/validasi dengan pemilik klien, baru implementasikan
autentikasi atau skor sesuai satu tugas. Jangan menerima source/output
pemain, mempercayai userId dari body, atau menyebut validasi skor sebagai
anticheat. Uji akun berbeda, sesi invalid, persistensi, idempotensi retry,
dan kegagalan jaringan. Jangan mengubah runtime Python/gameplay/UI di
luar ownership tugas atau melakukan deployment otomatis.
```

## 4. Prompt reviewer sebelum penggabungan

```text
Review perubahan tugas [M-ID] pada branch/diff [basis aktual] terhadap
kebutuhan dan kontrak yang disepakati. Jangan mengedit dahulu. Periksa
bug, regresi, state/versi/privasi, scope dan bukti test yang relevan.
Laporkan temuan dengan file, dampak, cara reproduksi, serta prioritas.
Bedakan blocker dari saran opsional. Minta penulis mendemokan alur dan
satu kasus gagal. Tidak ada approval/hasil test rekaan atau commit/push.
```

Panduan manusia, urutan milestone dan Git ada di [TEAM_GUIDE](TEAM_GUIDE.md). Skill bersama membantu metode kerja; konsistensi berasal dari kontrak, ownership, review dan bukti yang sama, bukan sekadar nama model yang sama.

## Handoff integrasi terbaru — 13 November 2026

Per 22 November 2026, kandidat puzzle 1 panen tetap terhubung ke terminal Python nyata, robot/replay/hasil, Run/Stop/Reset dan progres sesi; Home/login/tamu, setelan, akun/skor Spring lokal dan scoreboard P1 juga ada. Kompilasi `javac` langsung lulus, tetapi Gradle Wrapper gagal start karena `native-platform.dll`, sehingga UI terbaru belum diverifikasi. Puzzle 2–9, save lintas restart, server bersama dan komputer kedua belum terverifikasi. Sebelum menjalankan prompt peran di atas, baca [kontrak runtime](docs/mars/PYTHON_RUNTIME.md) dan handoff terbaru TASKS. Jangan membangun runtime kedua atau mengganti terminal nyata dengan mock. Pemilik UI membaca View immutable; pemilik runtime menjaga pembatalan, batas dan source; backend tidak menerima source/output pemain. Command dan bukti paket terbaru di README/VERIFICATION.
