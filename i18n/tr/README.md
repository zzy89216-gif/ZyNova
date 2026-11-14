<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <b>Türkçe</b> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> 📌 **Current release: 28.2.1** · [Changelog](../../CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.2.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.2.1)

---

Android için **Minecraft: Java Edition** başlatıcısı. ZalithLauncher2 kaynağından bağımsız olarak geliştirilip yayımlanıyor; resmî bir yapı değil.

Fikir tek: **daha az dokunuş**. Yirmi ayarı önüne dökmek yerine önce kendisi karar veriyor, gerçekten karar veremediğinde soruyor.

## ✨ Son değişiklikler

Tüm değişiklikler: [CHANGELOG.md](../../CHANGELOG.md).

## 🛠️ Neler yapabilir

- **Yapay zekâ ajanı**: varsayılan DeepSeek — oyunu, mod paketlerini ve modları kurar, ayarları değiştirir, günlükleri okur, oyunu başlatır
- **Görüntüleyiciler**: Otomatik / MobileGlues / GL4ES / LTW (deneysel)
- **Kaynak çekirdeği**: Modrinth / CurseForge araması, bağımlılık çözümleme, indirme, doğrulama, kurulum
- **Favoriler ve arşiv**: çevrim içi favoriler + özel arşiv, SHA-256 ile tekilleştirme
- **Fabric API koruması**: Fabric API / Quilted Fabric API / Legacy Fabric API yalnızca kurulum akışıyla eklenir — çoğaltılamaz, aranamaz, silinemez

## 🚀 Hızlı başlangıç

1. [Releases](https://github.com/zzy89216-gif/ZyNova/releases) sayfasından APK'yı indirip kurun (Android 8.0+)
2. İlk açılışta üç belgeyi sonuna kadar okuyup her birini işaretleyin, yaş grubunu seçin
3. Microsoft hesabıyla giriş yapın, sürüm seçin veya kurun, başlatın

## 📦 İndirme ve derleme

- İndirme: <https://github.com/zzy89216-gif/ZyNova/releases> (her zaman en son sürüm)
- Kendiniz derlemek için: JDK 17 + Android SDK / NDK, ardından `./gradlew ZalithLauncher:assembleRelease`
- Not: ilk açılışta uyum akışı çıkar; ayrıntılar [docs/en/licenses.md](../../docs/en/licenses.md) içinde

## 📚 Belgeler

> 📚 Belgeler şu an Çince ve İngilizce (her biri 77 makale); bu sayfa Türkçe tanıtım.

| İçerik | Dosya |
|---|---|
| Belge dizini | [docs/en/README.md](../../docs/en/README.md) |
| Kurulum / kullanım / dışa aktarma | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Görüntüleyici / giriş / yapay zekâ | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Sorun giderme / SSS / veri ve gizlilik | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Derleme / sürüm notları | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../CHANGELOG.md) |
| Devir / üçüncü taraf lisansları / lisans | [HANDOFF.md](../../HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 Destek

- Sorun bildir：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- E-posta：<ZyNovaLauncher@outlook.com>
- Resmî X：<https://x.com/ZyNovaProject>
- Destek ol：<https://afdian.com/a/hizzy>

## ⚖️ Lisans

GPL-3.0 — proje açık kaynak kalır. Üçüncü taraf bileşenler ve lisansları
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) ile [docs/en/licenses.md](../../docs/en/licenses.md) içinde.

> ⚠️ **Terracotta (çok oyunculu) AGPL-3.0 altında** ve diğerlerinden daha katı: değiştirilmeden dağıtılır.
> Değiştirip yeniden dağıtırsanız AGPL-3.0 §13 uyarınca ilgili kaynak kodu yayımlamanız gerekir.

---

**ZyNova — Context First. Less Steps.**
