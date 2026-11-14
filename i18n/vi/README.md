<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <b>Tiếng Việt</b> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
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

Trình khởi chạy **Minecraft: Java Edition** trên Android, phát triển độc lập từ mã nguồn ZalithLauncher2. Đây không phải bản dựng chính thức.

Ý tưởng gọn lại một câu: **ít chạm hơn**. Thay vì bày ra hai chục cài đặt, trình khởi chạy tự quyết trước và chỉ hỏi khi thật sự không quyết được.

## ✨ Thay đổi gần đây

Toàn bộ thay đổi: [CHANGELOG.md](../../CHANGELOG.md).

## 🛠️ Tính năng

- **Trợ lý AI**: mặc định DeepSeek — cài game, modpack và mod, đổi cài đặt, đọc log, khởi chạy game
- **Bộ kết xuất**: Tự động / MobileGlues / GL4ES / LTW (thử nghiệm)
- **Lõi tài nguyên**: tìm trên Modrinth / CurseForge, phân giải phụ thuộc, tải, kiểm tra, cài đặt
- **Yêu thích và lưu trữ**: yêu thích trực tuyến + kho riêng, chống trùng bằng SHA-256
- **Bảo vệ API Fabric**: Fabric API / Quilted Fabric API / Legacy Fabric API do trình cài thêm — không nhân bản, không tìm thấy, không xoá được

## 🚀 3 bước để chơi

1. Tải APK ở [Releases](https://github.com/zzy89216-gif/ZyNova/releases) và cài (Android 8.0+)
2. Lần đầu mở: đọc hết ba tài liệu, tích từng cái, chọn nhóm tuổi
3. Đăng nhập Microsoft → chọn hoặc cài phiên bản → khởi chạy

## 📦 Tải về và tự build

- Tải về: <https://github.com/zzy89216-gif/ZyNova/releases> (luôn dùng bản mới nhất)
- Tự build: JDK 17 + Android SDK / NDK, sau đó `./gradlew ZalithLauncher:assembleRelease`
- Lưu ý: lần mở đầu tiên sẽ có quy trình tuân thủ; chi tiết ở [docs/en/licenses.md](../../docs/en/licenses.md)

## 📚 Tài liệu

> 📚 Tài liệu hiện có bản tiếng Trung và tiếng Anh (77 bài mỗi bộ); trang này là phần giới thiệu tiếng Việt.

| Nội dung | Tệp |
|---|---|
| Mục lục tài liệu | [docs/en/README.md](../../docs/en/README.md) |
| Cài đặt / sử dụng / xuất dữ liệu | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Kết xuất / đăng nhập / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Xử lý sự cố / FAQ / dữ liệu và quyền riêng tư | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Tự build / nhật ký thay đổi | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../CHANGELOG.md) |
| Bàn giao / giấy phép bên thứ ba / giấy phép | [HANDOFF.md](../../HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 Ủng hộ

- Báo lỗi：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- Email：<ZyNovaLauncher@outlook.com>
- X chính thức：<https://x.com/ZyNovaProject>
- Ủng hộ dự án：<https://afdian.com/a/hizzy>

## ⚖️ Giấy phép

GPL-3.0 — dự án giữ mã nguồn mở. Thành phần bên thứ ba và giấy phép tương ứng nằm trong
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) và [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (chơi nhiều người) dùng AGPL-3.0**, chặt hơn phần còn lại: phân phối nguyên bản.
> Nếu bạn sửa rồi phát hành lại, Điều 13 AGPL-3.0 buộc bạn phải công bố mã nguồn tương ứng.

---

**ZyNova — Context First. Less Steps.**
