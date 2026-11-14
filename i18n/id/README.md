<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <b>Bahasa Indonesia</b> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> 📌 **Current release: 28.2.1** · [Changelog](../../CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.2.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.2.1)

---

Peluncur **Minecraft: Java Edition** untuk Android, dikelola sendiri dari kode ZalithLauncher2. Bukan build resmi.

Idenya satu: **lebih sedikit ketukan**. Daripada menumpahkan dua puluh pengaturan, peluncur memutuskan dulu dan hanya bertanya kalau memang tidak bisa.

## ✨ Perubahan terbaru

Perubahan lengkap: [CHANGELOG.md](../../CHANGELOG.md).

## 🛠️ Kemampuan

- **Agen AI**: bawaan DeepSeek — memasang game, modpack, dan mod, mengubah pengaturan, membaca log, menjalankan game
- **Renderer**: Otomatis / MobileGlues / GL4ES / LTW (eksperimental)
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

> 📚 Dokumentasi tersedia dalam bahasa Tionghoa dan Inggris (77 artikel masing-masing); halaman ini pengantar bahasa Indonesia.

| Isi | Berkas |
|---|---|
| Indeks dokumentasi | [docs/en/README.md](../../docs/en/README.md) |
| Pasang / pemakaian / ekspor | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Renderer / masuk akun / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Penanganan masalah / FAQ / privasi | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Build / catatan perubahan | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../CHANGELOG.md) |
| Serah terima / lisensi pihak ketiga / lisensi | [HANDOFF.md](../../HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 Dukungan

- Laporkan masalah：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- Email：<ZyNovaLauncher@outlook.com>
- X resmi：<https://x.com/ZyNovaProject>
- Dukung proyek：<https://afdian.com/a/hizzy>

## ⚖️ Lisensi

GPL-3.0 — proyek tetap sumber terbuka. Komponen pihak ketiga dan lisensinya ada di
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) dan [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (multipemain) memakai AGPL-3.0**, lebih ketat dari yang lain: didistribusikan tanpa
> perubahan. Jika Anda mengubahnya lalu menyebarkan ulang, Pasal 13 AGPL-3.0 mewajibkan Anda
> mempublikasikan kode sumber terkait.

---

**ZyNova — Context First. Less Steps.**
