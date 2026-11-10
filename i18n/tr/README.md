<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 yayımlandı (küresel uyum sürümü).**


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.1.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.1.0)

<p align="center"><sub>
<a href="../en/README.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <b>Türkçe</b> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

---

Android için **Minecraft: Java Edition** başlatıcısı. ZalithLauncher2 kaynak kodundan bağımsız olarak geliştirilip yayımlanıyor; resmî bir yapı değildir.

Amaç: **daha az dokunuş**. Oyunu kurmak, modları ve mod paketlerini toplamak, ayarları değiştirmek, kayıtları okumak — bunları yapay zekâ ajanı üstleniyor.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Hata ve öneriler：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- X (resmî ana hesap)：<https://x.com/ZyNovaProject>
- Çin ekosistemi posta kutusu：<ZyNovaLauncher@163.com>
- Küresel posta kutusu：<ZyNovaLauncher@outlook.com>

## ✨ 28.1.0 yenilikleri

- **İlk açılışta uyum akışı**：üç birleşik belge — ① EULA + gizlilik politikası, ② üçüncü taraf açık kaynak bildirimi + sorumluluk reddi ve riskler, ③ kullanım kılavuzu + geçerli bölgeler ve yargı yetkisi. Her belge **sonuna kadar kaydırılıp ayrı ayrı işaretlenmeli**; ardından yaş grubu seçilir (mevcut kullanıcılar da güncellemeden sonra bir kez görür).
- **Reşit olmayanlar için saat aralığı**：18 yaş altı yalnızca cuma, cumartesi, pazar ve resmî tatillerde **20:00–21:00** arasında oyunu başlatabilir. 2026 tatilleri Çin Devlet Konseyi takvimine (国办发明电〔2025〕7号) göre gün gün listelenir; telafi çalışma günleri iş günü sayılır.
- **Sorumluluk reddi açıkça yazıldı**：kayıt kaybı, cihazın ısınması, üçüncü taraf yasaklamaları, hizmetin kapanması gibi riskler açıkça bildirilir.
- **Ayrıca**：ana sayfa kartları artık yüklemede takılmıyor, yapay zekâ web araması çalışıyor, içe aktarma örnek (instance) klasörüne gidiyor, otomatik çökme onarımı kaldırıldı.

> 🌍 **28.1.0'da kullanılabilen bölgeler**：Çin, ABD, Güney Kore, Japonya ve Rusya. Diğer bölgelerde ilk açılışta bilgi sayfası gösterilip uygulama güvenli biçimde kapanır.

## 🛠️ Neler yapabilir

- **Yapay zekâ ajanı**：varsayılan DeepSeek — oyunu, mod paketlerini ve modları kurar, ayarları değiştirir, günlükleri okur, oyunu başlatır
- **Görüntüleyiciler (renderer)**：Otomatik / MobileGlues / GL4ES / LTW (deneysel)
- **Kaynak çekirdeği**：Modrinth / CurseForge araması, bağımlılık çözümleme, indirme, doğrulama, kurulum
- **Favoriler ve arşiv**：çevrim içi favoriler + özel arşiv, SHA-256 ile tekilleştirme
- **Fabric API koruması**：Fabric API / Quilted Fabric API / Legacy Fabric API kurulum akışıyla eklenir; elle çoğaltılamaz, aranamaz, silinemez

## 📦 İndirme ve derleme

- İndirme：<https://github.com/zzy89216-gif/ZyNova/releases> (her zaman en son sürümü kullanın)
- Kendiniz derlemek için：JDK 17 + Android SDK / NDK, ardından `./gradlew ZalithLauncher:assembleRelease`
- Not：ilk açılışta uyum akışı çıkar; bölge ve yaş sınırlarının ayrıntıları [docs/en/licenses.md](../../docs/en/licenses.md) içinde.

## 📚 Belgeler

> 📚 Belgeler şu an Çince ve İngilizce (70+ makale); bu sayfa Türkçe tanıtımdır.

| İçerik | Dosya |
|---|---|
| Belge dizini | [docs/en/README.md](../../docs/en/README.md) |
| Kurulum / kullanım / dışa aktarma | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Görüntüleyici / giriş / favoriler / yapay zekâ | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/favorites.md](../../docs/en/favorites.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Sorun giderme / SSS / veri ve gizlilik | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Derleme | [docs/en/build.md](../../docs/en/build.md) |
| Sürüm notları | [CHANGELOG.md](../../CHANGELOG.md) |
| Devir / mimari / iş akışı | [HANDOFF.md](../../HANDOFF.md) |
| Katkı girişi (durum / sürüm / tuzaklar / belge kuralları) | [AGENTS.md](../../AGENTS.md) |
| Üçüncü taraf lisansları | [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) |
| Lisans | [LICENSE](../../LICENSE) |

## 💖 Destek

- Sorun bildir：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- E-posta：küresel <ZyNovaLauncher@outlook.com> ｜ Çin <ZyNovaLauncher@163.com>
- Resmî X：<https://x.com/ZyNovaProject>
- Destek ol：<https://afdian.com/a/hizzy>

## ⚖️ Lisans

GPL-3.0 — proje açık kaynak kalır. Üçüncü taraf bileşenler ve lisansları
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) ile [docs/en/licenses.md](../../docs/en/licenses.md) içinde listelenmiştir.

> ⚠️ **Terracotta (çok oyunculu) AGPL-3.0 altında** ve diğerlerinden daha katıdır: değiştirilmeden dağıtılır.
> Değiştirip yeniden dağıtırsanız AGPL-3.0 §13 uyarınca ilgili kaynak kodu yayımlamanız gerekir.

---

**ZyNova — Context First. Less Steps.**
