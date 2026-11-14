<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <b>한국어</b> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
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

Android용 **Minecraft: Java Edition** 런처입니다. ZalithLauncher2 소스에서 독립적으로 개발·배포하며 공식 빌드가 아닙니다.

설계는 **탭 줄이기** 하나입니다. 설정을 늘어놓고 고르게 하는 대신, 먼저 판단하고 판단이 안 될 때만 한 번 묻습니다.

## 🚀 3단계 시작

1. [Releases](https://github.com/zzy89216-gif/ZyNova/releases)에서 APK를 받아 설치 (Android 8.0 이상)
2. 첫 실행에서 약관 3종을 끝까지 읽고 각각 체크한 뒤 연령대 선택
3. Microsoft 계정 로그인 → 버전 선택(또는 AI에게 설치 요청) → 실행

## ✨ 최근 변경 사항

전체 변경 사항: [CHANGELOG.md](../../CHANGELOG.md).

## 🛠️ 주요 기능

- **AI Agent**: 기본 DeepSeek — 게임 본체·모드팩·모드 설치, 설정 변경, 로그 확인, 실행까지 수행
- **렌더러**: 자동 / MobileGlues / GL4ES / LTW(실험적)
- **리소스 코어**: Modrinth / CurseForge 검색, 의존성 해석, 다운로드, 검증, 설치
- **즐겨찾기와 보관**: 온라인 즐겨찾기 + 앱 전용 저장소(SHA-256 중복 제거)
- **Fabric API 보호**: 설치 흐름에서만 추가되며 수동 중복 설치·검색·삭제가 불가능합니다

## 📦 다운로드 및 빌드

- 다운로드: <https://github.com/zzy89216-gif/ZyNova/releases> (항상 최신 버전)
- 직접 빌드: JDK 17 + Android SDK / NDK, 이어서 `./gradlew ZalithLauncher:assembleRelease`
- 참고: 첫 실행 시 규정 준수 절차가 나타납니다. 자세한 내용은 [docs/en/licenses.md](../../docs/en/licenses.md)

## 📚 문서

> 📚 문서는 현재 중국어와 영어 두 벌(각 77편)입니다. 이 페이지는 한국어 소개입니다.

| 내용 | 파일 |
|---|---|
| 문서 목록 | [docs/en/README.md](../../docs/en/README.md) |
| 설치 / 사용 / 내보내기 | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| 렌더러 / 로그인 / 즐겨찾기 / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| 문제 해결 / FAQ / 데이터와 개인정보 | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| 빌드 / 변경 기록 | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../CHANGELOG.md) |
| 인수인계 / 제3자 라이선스 / 라이선스 | [HANDOFF.md](../../HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 지원

- 문제 신고：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- 이메일：<ZyNovaLauncher@outlook.com>
- 공식 X：<https://x.com/ZyNovaProject>
- 후원：<https://afdian.com/a/hizzy>

## ⚖️ 라이선스

GPL-3.0 — 프로젝트는 오픈소스로 유지됩니다. 제3자 구성 요소와 라이선스는
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md)와 [docs/en/licenses.md](../../docs/en/licenses.md)에 있습니다.

> ⚠️ **Terracotta(멀티플레이)는 AGPL-3.0**으로 다른 구성 요소보다 엄격합니다. 본 프로젝트는
> 바이너리를 수정 없이 배포합니다. 수정 후 재배포하려면 AGPL-3.0 §13에 따라 해당 소스 코드를
> 공개해야 합니다.

---

**ZyNova — Context First. Less Steps.**
