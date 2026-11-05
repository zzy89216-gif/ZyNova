<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-27.10.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.10.1)

[简体中文](README.md) | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | **한국어** | [Русский](README_RU.md)

---

Android용 Minecraft: Java Edition 런처입니다. ZalithLauncher2 소스를 기반으로 독립적으로 개발·배포합니다.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 27.10.1 업데이트

- **SakuraFrp 터널**: 멀티플레이 화면에서 액세스 토큰만 붙여넣으면 사용할 수 있습니다. 터널 생성/편집/삭제/시작·중지/주소 복사를 지원합니다. 토큰은 Android Keystore로 암호화되고 포그라운드 서비스가 유지합니다.
- **새 상단 바**: 중앙에 액체 유리 캡슐(홈 / AI / 멀티 / 다운로드 / 파일 / 개인 설정 / 설정). 제목은 왼쪽에 유지됩니다.
- **AI 채팅 Markdown 렌더링**: 제목, 목록, 표, 코드 블록, 링크. 어시스턴트는 전체 폭, 사용자는 최대 82%.
- **Terracotta는 변경되지 않았습니다.**
- **프리릴리스 버전**: 위 기능을 검증하기 위한 빌드입니다.

## 🛠️ 주요 기능

- **AI Agent**: 기본 DeepSeek. 게임 본체·모드팩·모드 설치, 설정 변경, 로그 확인, 게임 실행까지 직접 수행
- **렌더러**: 자동 / MobileGlues / GL4ES / LTW(실험적)
- **리소스 코어**: Modrinth / CurseForge 검색, 의존성 해석, 다운로드, 검증, 설치
- **즐겨찾기와 보관**: 온라인 즐겨찾기 + 앱 전용 저장소(SHA-256 중복 제거)
- **Fabric API 보호**: Fabric 설치 흐름에서만 자동 추가, 수동 중복 설치 불가

## 📦 다운로드 및 빌드

- 다운로드: <https://github.com/zzy89216-gif/ZyNova/releases>
- 빌드: JDK 17과 Android SDK / NDK 준비 후 `./gradlew ZalithLauncher:assembleRelease`

## 📚 문서

> 📚 **전체 문서(72편, 영어)**：[docs/en/README.md](docs/en/README.md)



| 내용 | 파일 |
|---|---|
| 사용자 문서 | [docs/en/README.md](docs/en/README.md) |
| 설치 / 사용 / 내보내기 | [docs/en/install.md](docs/en/install.md) · [docs/en/usage.md](docs/en/usage.md) |
| 렌더러 / 로그인 / 즐겨찾기 / AI | [docs/en/renderers.md](docs/en/renderers.md) · [docs/en/login.md](docs/en/login.md) · [docs/en/favorites.md](docs/en/favorites.md) · [docs/en/ai-agent.md](docs/en/ai-agent.md) |
| 문제 해결 / FAQ / 데이터 | [docs/en/troubleshooting.md](docs/en/troubleshooting.md) · [docs/en/faq.md](docs/en/faq.md) · [docs/en/data-and-privacy.md](docs/en/data-and-privacy.md) |
| 빌드 | [docs/en/build.md](docs/en/build.md) |
| 변경 기록 | [CHANGELOG.md](CHANGELOG.md) |
| 인수인계 / 구조 / 개발 흐름 | [HANDOFF.md](HANDOFF.md) |
| 서드파티 라이선스 | [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) |
| 라이선스 | [LICENSE](LICENSE) |



## 💖 지원

- 문제 신고: <https://github.com/zzy89216-gif/ZyNova/issues>
- Discord: <https://discord.gg/QwPpZQHrTa>
- 후원: <https://afdian.com/a/hizzy>

## ⚖️ 라이선스

GPL-3.0(오픈소스 유지). 서드파티 라이선스는 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

---

**ZyNova — Context First. Less Steps.**
