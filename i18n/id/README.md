<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <b>Bahasa Indonesia</b> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a> · <a href="../lzh/README.md">文言</a>
</sub></p>

<p align="center">
  <img src="../../docs/assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> 📌 **Current release: 28.4.0** · [Changelog](../../meta/CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.4.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.4.0)

---

Peluncur **Minecraft: Java Edition** untuk Android, dikelola sendiri dari kode ZalithLauncher2. Bukan build resmi.

Idenya satu: **lebih sedikit ketukan**. Daripada menumpahkan dua puluh pengaturan, peluncur memutuskan dulu dan hanya bertanya kalau memang tidak bisa.

## ✨ Perubahan terbaru

**28.4.0** (terbaru): perubahan utama:

- Halaman utama dirombak: dunia terakhir + daftar lengkap dunia + feed kustom.
- Halaman «Personalisasi» dihapus, judul detail perender diperbaiki, tautan Discord diperbarui.
- Menghapus versi kini menampilkan progres pasti layar penuh.
- Kartu dunia lama yang dikelompokkan per versi di tengah halaman utama dihapus (#100).
- Deteksi status kosong yang salah diperbaiki: saat semua versi disembunyikan, tidak lagi menyarankan unduhan.
- Perubahan 28.3.2 yang menyebabkan beberapa layar pengelolaan sumber daya mogok dibatalkan (cache daftar / cache ikon global / pemuatan plugin asinkron); hanya perbaikan layar penuh penghapusan versi yang dipertahankan.

> Tampilan tetap seperti 28.3.0 — tanpa kaca dinamis, refraksi, atau efek magnetik.

**28.3.0** (versi stabil): perubahan utama.

Versi stabil ini menggabungkan seluruh isi dari versi prarilis sebelumnya (versi tersebut telah dihapus).

- **Beberapa unduhan dan pemasangan sekaligus**: versi game, modpack, dan sumber daya bisa dijalankan bersamaan; tiap tugas punya progres, kecepatan, dan pembatalan sendiri. Menekan item yang sama lagi kini menampilkan pemberitahuan, bukan diam saja.
- **Menyusun ulang kartu di beranda lebih stabil**: tekan lama lalu seret tidak lagi berkedip atau membuat aplikasi tertutup; saat diseret cepat kartu bertukar satu per satu, dan seret horizontal juga didukung.
- **Pemutar musik diperlengkapi**: lirik `.lrc` bernama sama dimuat otomatis dan bisa diimpor dari halaman; notifikasi menampilkan baris sebelumnya dan baris saat ini. Bilah mini di bilah atas (judul + putar/jeda + ganti trek) ditambahkan. Posisi dan durasi tetap benar saat dijeda, dan tata letak tidak lagi meluber di mode lanskap atau layar pendek.
- **«Kembali ke game» diperbaiki**: entri langsung hilang begitu game ditutup, dan menekannya sesaat sebelum itu tidak lagi membuat aplikasi crash.
- **AI diperkuat**: beberapa versi game bisa dipasang sekaligus; alat kendali musik ditambahkan (total 53 alat).
- **Entri baru «Multipemain» di menu dalam game**: menu menutup, launcher langsung membuka halaman Multipemain, dan game tetap berjalan di latar belakang.

Perubahan lengkap: [CHANGELOG.md](../../meta/CHANGELOG.md).

## 🛠️ Kemampuan

- **Agen AI**: bawaan DeepSeek — memasang game, modpack, dan mod, mengubah pengaturan, membaca log, menjalankan game
- **Renderer**: Otomatis / MobileGlues / Ironized Zink / GL4ES / LTW (eksperimental)
- **Inti sumber daya**: pencarian Modrinth / CurseForge, penyelesaian dependensi, unduh, verifikasi, pasang
- **Favorit dan arsip**: favorit daring + arsip pribadi, deduplikasi SHA-256
- **Perlindungan API Fabric**: hanya ditambahkan lewat alur pemasangan — tidak bisa diduplikasi, dicari, atau dihapus

## 🚀 3 langkah main

1. Unduh APK dari [Releases](https://github.com/zzy89216-gif/ZyNova/releases) lalu pasang (Android 8.0+)
2. Saat pertama dibuka, baca tiga dokumen sampai habis, centang satu per satu, pilih kelompok usia
3. Masuk dengan akun Microsoft, pilih atau pasang versi, jalankan

## 📖 Glosarium

- **Modpack**: kumpulan mod yang dipasang sekaligus
- **Shader**: pencahayaan dan air lebih bagus, diaktifkan di pengaturan grafis
- **Instance**: folder terpisah berisi versi, mod, dan pengaturannya sendiri

## 📦 Unduh dan build sendiri

- Unduh: <https://github.com/zzy89216-gif/ZyNova/releases> (selalu versi terbaru)
- Build sendiri: JDK 17 + Android SDK / NDK, lalu `./gradlew ZalithLauncher:assembleRelease`
- Catatan: alur kepatuhan muncul saat pertama dibuka; rincian di [docs/en/licenses.md](../../docs/en/licenses.md)

## 📚 Dokumentasi

> 📚 Dokumentasi tersedia dalam bahasa Tionghoa dan Inggris (78 artikel masing-masing); halaman ini pengantar bahasa Indonesia.

| Isi | Berkas |
|---|---|
| Indeks dokumentasi | [docs/en/README.md](../../docs/en/README.md) |
| Pasang / pemakaian / ekspor | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Renderer / masuk akun / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Penanganan masalah / FAQ / privasi | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Build / catatan perubahan | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../meta/CHANGELOG.md) |
| Serah terima / lisensi pihak ketiga / lisensi | [HANDOFF.md](../../meta/HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../meta/THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 Dukungan

- Laporkan masalah：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/dbHTApYNYJ>
- Email：<ZyNovaLauncher@outlook.com>
- X resmi：<https://x.com/ZyNovaProject>
- Dukung proyek：<https://afdian.com/a/hizzy>

## ⚖️ Lisensi

GPL-3.0 — proyek tetap sumber terbuka. Komponen pihak ketiga dan lisensinya ada di
[THIRD_PARTY_NOTICES.md](../../meta/THIRD_PARTY_NOTICES.md) dan [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (multipemain) memakai AGPL-3.0**, lebih ketat dari yang lain: didistribusikan tanpa
> perubahan. Jika Anda mengubahnya lalu menyebarkan ulang, Pasal 13 AGPL-3.0 mewajibkan Anda
> mempublikasikan kode sumber terkait.

---

**ZyNova — Context First. Less Steps.**
