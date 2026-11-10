<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 telah dirilis (versi kepatuhan global).**


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.1.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.1.0)

<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <b>Bahasa Indonesia</b> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

---

Peluncur **Minecraft: Java Edition** untuk Android, dikelola dan dirilis secara independen dari kode sumber ZalithLauncher2. Ini bukan build resmi.

Tujuannya: **lebih sedikit ketukan**. Apa pun yang bisa diotomatiskan — memasang game, mengumpulkan mod dan modpack, mengubah pengaturan, membaca log — diserahkan ke agen AI.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Laporan bug dan saran：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- X (akun utama resmi)：<https://x.com/ZyNovaProject>
- Kotak surat ekosistem Tiongkok：<ZyNovaLauncher@163.com>
- Kotak surat global：<ZyNovaLauncher@outlook.com>

## ✨ Yang baru di 28.1.0

- **Alur kepatuhan saat pertama dibuka**：tiga dokumen gabungan — ① EULA + kebijakan privasi, ② pemberitahuan sumber terbuka pihak ketiga + penafian dan risiko, ③ panduan pemakaian + wilayah yang berlaku dan yurisdiksi. Setiap dokumen harus **digulir sampai habis dan dicentang satu per satu**, baru setelah itu memilih kelompok usia (pengguna lama juga melewatinya sekali setelah pembaruan).
- **Jendela waktu untuk anak di bawah umur**：di bawah 18 tahun hanya bisa menjalankan game pada Jumat, Sabtu, Minggu, dan hari libur resmi, pukul **20.00–21.00**. Libur 2026 mengikuti kalender Dewan Negara Tiongkok (国办发明电〔2025〕7号); hari kerja pengganti dihitung sebagai hari kerja.
- **Penafian ditulis tuntas**：kehilangan save, perangkat panas, akun diblokir pihak ketiga, layanan berhenti, dan risiko sejenis dinyatakan jelas.
- **Selain itu**：kartu beranda tidak lagi macet memuat, pencarian web AI berfungsi, impor masuk ke folder instance, perbaikan otomatis setelah crash dihapus.

> 🌍 **Wilayah yang tersedia di 28.1.0**：Tiongkok, Amerika Serikat, Korea Selatan, Jepang, dan Rusia. Di wilayah lain, saat pertama dibuka akan muncul halaman pemberitahuan lalu aplikasi keluar dengan aman.

## 🛠️ Kemampuan

- **Agen AI**：bawaan DeepSeek — memasang game, modpack, dan mod, mengubah pengaturan, membaca log, menjalankan game
- **Renderer**：Otomatis / MobileGlues / GL4ES / LTW (eksperimental)
- **Inti sumber daya**：pencarian Modrinth / CurseForge, penyelesaian dependensi, unduh, verifikasi, pasang
- **Favorit dan arsip**：favorit daring + arsip pribadi, deduplikasi SHA-256
- **Perlindungan API Fabric**：Fabric API / Quilted Fabric API / Legacy Fabric API ditambahkan oleh alur pemasangan — tidak bisa diduplikasi, dicari, atau dihapus

## 📦 Unduh dan build sendiri

- Unduh：<https://github.com/zzy89216-gif/ZyNova/releases> (selalu pakai versi terbaru)
- Build sendiri：JDK 17 + Android SDK / NDK, lalu `./gradlew ZalithLauncher:assembleRelease`
- Catatan：saat pertama dibuka muncul alur kepatuhan; rincian wilayah dan batas usia ada di [docs/en/licenses.md](../../docs/en/licenses.md).

## 📚 Dokumentasi

> 📚 Dokumentasi saat ini tersedia dalam bahasa Tionghoa dan Inggris (70+ artikel); halaman ini adalah pengantar bahasa Indonesia.

| Isi | Berkas |
|---|---|
| Indeks dokumentasi | [docs/en/README.md](../../docs/en/README.md) |
| Pasang / pemakaian / ekspor | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Renderer / masuk akun / favorit / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/favorites.md](../../docs/en/favorites.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Penanganan masalah / FAQ / data dan privasi | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Build sendiri | [docs/en/build.md](../../docs/en/build.md) |
| Catatan perubahan | [CHANGELOG.md](../../CHANGELOG.md) |
| Serah terima / arsitektur / alur kerja | [HANDOFF.md](../../HANDOFF.md) |
| Pintu masuk kontributor (status / rilis / jebakan / aturan dokumen) | [AGENTS.md](../../AGENTS.md) |
| Lisensi pihak ketiga | [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) |
| Lisensi | [LICENSE](../../LICENSE) |

## 💖 Dukungan

- Laporkan masalah：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- Email：global <ZyNovaLauncher@outlook.com> ｜ Tiongkok <ZyNovaLauncher@163.com>
- X resmi：<https://x.com/ZyNovaProject>
- Dukung proyek：<https://afdian.com/a/hizzy>

## ⚖️ Lisensi

GPL-3.0 — proyek tetap sumber terbuka. Komponen pihak ketiga dan lisensinya tercantum di
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) dan [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (multipemain) memakai AGPL-3.0**, lebih ketat daripada komponen lain: didistribusikan
> tanpa perubahan. Jika Anda mengubahnya lalu menyebarkan ulang, Pasal 13 AGPL-3.0 mewajibkan Anda
> mempublikasikan kode sumber terkait.

---

**ZyNova — Context First. Less Steps.**
