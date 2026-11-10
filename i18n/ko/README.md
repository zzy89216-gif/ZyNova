<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <b>한국어</b> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 출시(글로벌 규정 준수 버전).**


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.1.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.1.0)

---

Android용 **Minecraft: Java Edition** 런처입니다. ZalithLauncher2 소스에서 독립적으로 개발·배포하며 공식 빌드가 아닙니다.

설계는 **탭 줄이기** 하나입니다. 설정을 늘어놓고 고르게 하는 대신, 먼저 판단하고 판단이 안 될 때만 한 번 묻습니다.

## 🚀 3단계 시작

1. [Releases](https://github.com/zzy89216-gif/ZyNova/releases)에서 APK를 받아 설치 (Android 8.0 이상)
2. 첫 실행에서 약관 3종을 끝까지 읽고 각각 체크한 뒤 연령대 선택
3. Microsoft 계정 로그인 → 버전 선택(또는 AI에게 설치 요청) → 실행

## ✨ 28.1.0 업데이트

- **첫 실행 규정 준수 절차**: 3개의 통합 문서(①EULA + 개인정보 처리방침, ②제3자 오픈소스 고지 + 면책·위험 고지, ③사용 안내 + 적용 지역 및 법적 관할). 각 문서를 **끝까지 스크롤하고 개별 체크**해야 다음으로 넘어가며, 완료 후 연령대를 고릅니다(기존 사용자도 업데이트 후 한 번).
- **미성년자 시간 제한**: 만 18세 미만은 금·토·일 및 법정 공휴일 **20:00–21:00**에만 실행할 수 있습니다. 2026년 공휴일은 중국 국무원 통지에 따라 날짜별로 열거되며, 대체 근무일은 평일로 처리됩니다.
- **면책 조항 명시**: 세이브 손실, 기기 발열, 제3자 계정 제재, 서비스 종료 같은 위험을 분명히 알립니다.
- **함께**: 홈 카드가 로딩에서 멈추지 않고, AI 웹 검색이 동작하며, 가져오기는 인스턴스 폴더로, 크래시 자동 복구는 제거되었습니다.

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
