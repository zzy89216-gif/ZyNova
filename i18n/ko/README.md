<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <b>한국어</b> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a> · <a href="../lzh/README.md">文言</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> 📌 **Current release: 28.4.0** · [Changelog](../../CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.4.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.4.0)

---

Android용 **Minecraft: Java Edition** 런처입니다. ZalithLauncher2 소스에서 독립적으로 개발·배포하며 공식 빌드가 아닙니다.

설계는 **탭 줄이기** 하나입니다. 설정을 늘어놓고 고르게 하는 대신, 먼저 판단하고 판단이 안 될 때만 한 번 묻습니다.

## 🚀 3단계 시작

1. [Releases](https://github.com/zzy89216-gif/ZyNova/releases)에서 APK를 받아 설치 (Android 8.0 이상)
2. 첫 실행에서 약관 3종을 끝까지 읽고 각각 체크한 뒤 연령대 선택
3. Microsoft 계정 로그인 → 버전 선택(또는 AI에게 설치 요청) → 실행

## ✨ 최근 변경 사항

**28.4.0**(최신) 주요 변경 사항:

- 홈 화면 개편: 최근 월드 + 전체 월드 목록 + 사용자 지정 홈 피드.
- «개인 설정» 페이지 제거, 렌더러 세부 정보 제목 수정, Discord 링크 갱신.
- 버전 삭제가 이제 전체 화면 확정 진행률로 표시됩니다.
- 홈 화면 가운데의 예전 인스턴스별 월드 카드를 제거(#100).
- 홈의 빈 상태 오판 수정: 모든 인스턴스가 숨겨져 있을 때 더 이상 다운로드를 권하지 않습니다.
- 28.3.2에서 일부 리소스 관리 화면이 종료되던 변경을 되돌림(목록 캐시 / 아이콘 전역 캐시 / 플러그인 비동기 로딩). 버전 삭제 전체 화면 수정만 유지.

> 화면 디자인은 28.3.0과 동일하며, 동적 유리·굴절·자석 효과는 포함되지 않습니다.

**28.3.0** (정식 버전)의 주요 변경 사항입니다:

이 버전은 이전 프리릴리스의 모든 내용을 통합한 정식 버전입니다(프리릴리스 릴리스는 삭제됨).

- **여러 다운로드·설치를 동시에 진행**: 게임 본체, 모드팩, 리소스를 한꺼번에 시작할 수 있고 각 작업의 진행률·속도·취소가 독립적으로 동작합니다. 같은 대상을 다시 누르면 안내가 표시되어 조용히 무시되지 않습니다.
- **홈 화면 카드 정렬 안정화**: 길게 눌러 끌어 옮길 때 화면 깜빡임과 앱 종료가 사라졌습니다. 빠르게 끌어도 한 장씩 자리를 바꾸며 가로 방향 끌기도 지원합니다.
- **음악 플레이어 개선**: 같은 이름의 `.lrc` 가사를 자동으로 불러오고 페이지에서 가사를 가져올 수 있습니다. 알림에는 이전 줄과 현재 줄이 표시됩니다. 상단 바에 미니 플레이어(곡명+재생/일시정지+다음 곡)를 추가했습니다. 일시정지 중에도 재생 위치와 길이가 정확히 표시되며, 가로 화면이나 낮은 화면에서도 레이아웃이 넘치지 않습니다.
- **'게임으로 돌아가기' 수정**: 게임을 종료하면 항목이 즉시 사라지고, 사라지기 직전에 눌러도 더 이상 앱이 종료되지 않습니다.
- **AI 강화**: 게임 본체를 동시에 여러 개 설치할 수 있고, 음악 제어 도구를 추가했습니다(도구 총 53개).
- **게임 내 메뉴에 '멀티플레이' 추가**: 메뉴를 닫고 런처로 돌아가 멀티플레이 화면을 바로 엽니다. 게임은 종료되지 않고 백그라운드에서 계속 실행됩니다.

전체 변경 사항: [CHANGELOG.md](../../CHANGELOG.md).

## 🛠️ 주요 기능

- **AI Agent**: 기본 DeepSeek — 게임 본체·모드팩·모드 설치, 설정 변경, 로그 확인, 실행까지 수행
- **렌더러**: 자동 / MobileGlues / Ironized Zink / GL4ES / LTW(실험적)
- **리소스 코어**: Modrinth / CurseForge 검색, 의존성 해석, 다운로드, 검증, 설치
- **즐겨찾기와 보관**: 온라인 즐겨찾기 + 앱 전용 저장소(SHA-256 중복 제거)
- **Fabric API 보호**: 설치 흐름에서만 추가되며 수동 중복 설치·검색·삭제가 불가능합니다

## 📦 다운로드 및 빌드

- 다운로드: <https://github.com/zzy89216-gif/ZyNova/releases> (항상 최신 버전)
- 직접 빌드: JDK 17 + Android SDK / NDK, 이어서 `./gradlew ZalithLauncher:assembleRelease`
- 참고: 첫 실행 시 규정 준수 절차가 나타납니다. 자세한 내용은 [docs/en/licenses.md](../../docs/en/licenses.md)

## 📚 문서

> 📚 문서는 현재 중국어와 영어 두 벌(각 78편)입니다. 이 페이지는 한국어 소개입니다.

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
- Discord：<https://discord.gg/dbHTApYNYJ>
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
