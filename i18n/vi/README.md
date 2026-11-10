<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 đã phát hành (bản tuân thủ toàn cầu).**


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.1.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.1.0)

<p align="center"><sub>
<a href="../en/README.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <b>Tiếng Việt</b> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

---

Trình khởi chạy **Minecraft: Java Edition** trên Android, được duy trì và phát hành độc lập dựa trên mã nguồn ZalithLauncher2. Đây không phải bản dựng chính thức.

Mục tiêu: **ít thao tác hơn**. Những việc có thể tự động hoá — cài game, gom mod và modpack, chỉnh cài đặt, đọc log — đều giao cho trợ lý AI.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Báo lỗi và góp ý：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- X (tài khoản chính thức)：<https://x.com/ZyNovaProject>
- Hộp thư cho thị trường Trung Quốc：<ZyNovaLauncher@163.com>
- Hộp thư toàn cầu：<ZyNovaLauncher@outlook.com>

## ✨ Có gì mới trong 28.1.0

- **Quy trình tuân thủ khi mở lần đầu**：ba tài liệu gộp lại — ① EULA + chính sách quyền riêng tư, ② thông báo mã nguồn mở của bên thứ ba + miễn trừ trách nhiệm và rủi ro, ③ hướng dẫn sử dụng + khu vực áp dụng và thẩm quyền pháp lý. Mỗi tài liệu phải **cuộn hết xuống và tích riêng**, sau đó chọn nhóm tuổi (người dùng cũ cũng gặp lại một lần sau khi cập nhật).
- **Khung giờ cho người chưa thành niên**：dưới 18 tuổi chỉ được khởi chạy vào thứ Sáu, thứ Bảy, Chủ nhật và ngày lễ theo quy định, từ **20:00 đến 21:00**. Ngày lễ 2026 được liệt kê theo lịch của Quốc vụ viện Trung Quốc (国办发明电〔2025〕7号); ngày đi làm bù tính như ngày làm việc.
- **Miễn trừ trách nhiệm nói rõ**：mất dữ liệu lưu, máy nóng, bị bên thứ ba khoá tài khoản, dịch vụ ngừng hoạt động… đều được nêu minh bạch.
- **Ngoài ra**：trang chủ không còn treo ở trạng thái tải, tìm kiếm web của AI đã chạy, nhập dữ liệu vào đúng thư mục instance, đã bỏ tính năng tự sửa lỗi khi crash.

> 🌍 **Khu vực dùng được trong 28.1.0**：Trung Quốc, Hoa Kỳ, Hàn Quốc, Nhật Bản và Nga. Ở khu vực khác, lần mở đầu tiên sẽ hiện trang thông báo rồi thoát an toàn.

## 🛠️ Tính năng

- **Trợ lý AI**：mặc định DeepSeek — cài game, modpack và mod, đổi cài đặt, đọc log, khởi chạy game
- **Bộ kết xuất**：Tự động / MobileGlues / GL4ES / LTW (thử nghiệm)
- **Lõi tài nguyên**：tìm trên Modrinth / CurseForge, phân giải phụ thuộc, tải, kiểm tra, cài đặt
- **Yêu thích và lưu trữ**：yêu thích trực tuyến + kho lưu riêng, chống trùng bằng SHA-256
- **Bảo vệ API Fabric**：Fabric API / Quilted Fabric API / Legacy Fabric API do trình cài tự thêm — không nhân bản, không tìm thấy, không xoá được

## 📦 Tải về và tự build

- Tải về：<https://github.com/zzy89216-gif/ZyNova/releases> (luôn dùng bản mới nhất)
- Tự build：JDK 17 + Android SDK / NDK, sau đó `./gradlew ZalithLauncher:assembleRelease`
- Lưu ý：lần mở đầu tiên sẽ có quy trình tuân thủ; chi tiết khu vực và giới hạn tuổi ở [docs/en/licenses.md](../../docs/en/licenses.md).

## 📚 Tài liệu

> 📚 Tài liệu hiện có bản tiếng Trung và tiếng Anh (hơn 70 bài); trang này là phần giới thiệu tiếng Việt.

| Nội dung | Tệp |
|---|---|
| Mục lục tài liệu | [docs/en/README.md](../../docs/en/README.md) |
| Cài đặt / sử dụng / xuất dữ liệu | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Kết xuất / đăng nhập / yêu thích / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/favorites.md](../../docs/en/favorites.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Xử lý sự cố / FAQ / dữ liệu và quyền riêng tư | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Tự build | [docs/en/build.md](../../docs/en/build.md) |
| Nhật ký thay đổi | [CHANGELOG.md](../../CHANGELOG.md) |
| Bàn giao / kiến trúc / quy trình | [HANDOFF.md](../../HANDOFF.md) |
| Cửa vào cho người tiếp nhận (trạng thái / phát hành / cạm bẫy / quy tắc tài liệu) | [AGENTS.md](../../AGENTS.md) |
| Giấy phép bên thứ ba | [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) |
| Giấy phép | [LICENSE](../../LICENSE) |

## 💖 Ủng hộ

- Báo lỗi：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- Email：toàn cầu <ZyNovaLauncher@outlook.com> ｜ Trung Quốc <ZyNovaLauncher@163.com>
- X chính thức：<https://x.com/ZyNovaProject>
- Ủng hộ dự án：<https://afdian.com/a/hizzy>

## ⚖️ Giấy phép

GPL-3.0 — dự án giữ mã nguồn mở. Thành phần bên thứ ba và giấy phép tương ứng nằm trong
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) và [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (chơi nhiều người) dùng AGPL-3.0**, chặt hơn phần còn lại: được phân phối nguyên bản.
> Nếu bạn sửa rồi phát hành lại, Điều 13 AGPL-3.0 buộc bạn phải công bố mã nguồn tương ứng.

---

**ZyNova — Context First. Less Steps.**
