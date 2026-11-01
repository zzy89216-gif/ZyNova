<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-27.7.2-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.7.2)

[简体中文](README.md) | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | **한국어** | [Русский](README_RU.md)

---

Android용 Minecraft: Java Edition 런처입니다. ZalithLauncher2 소스를 기반으로 독립적으로 개발·배포합니다.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 27.7.2 업데이트

- **설정 7개 분류**: 기본 / 게임 / 렌더러 / 네트워크 및 다운로드 / AI / 고급 / 정보
- **렌더러별 페이지**: MobileGlues / Ironized Zink / GL4ES 각각 독립 페이지
- **즐겨찾기 통합**: 온라인 즐겨찾기(필수 선행 모드 자동 설치) + 로컬 보관
- **다운로드**: 상단 진행률 표시(상세 보기), 설치는 백그라운드에서 조용히, 배경 동영상 반복 재생 수정

## 🛠️ 주요 기능

- **AI Agent**: 기본 DeepSeek. 로그 확인, 모드 관리, 설정 변경, 리소스 설치, 게임 실행
- **렌더러**: 자동 / MobileGlues / Ironized Zink / GL4ES
- **리소스 코어**: Modrinth / CurseForge 검색, 의존성 해석, 다운로드, 검증, 설치
- **즐겨찾기와 보관**: 온라인 즐겨찾기 + 앱 전용 저장소(SHA-256 중복 제거)
- **Fabric API 보호**: Fabric 설치 흐름에서만 자동 추가, 수동 중복 설치 불가

## 📦 다운로드 및 빌드

- 다운로드: <https://github.com/zzy89216-gif/ZyNova/releases>
- 빌드: JDK 17과 Android SDK / NDK 준비 후 `./gradlew ZalithLauncher:assembleRelease`

## 📚 Docs

[CHANGELOG.md](CHANGELOG.md) · [HANDOFF.md](HANDOFF.md) · [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) · [LICENSE](LICENSE)

## ⚖️ 라이선스

GPL-3.0(오픈소스 유지). 서드파티 라이선스는 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

---

**ZyNova — Context First. Less Steps.**
