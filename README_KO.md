<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-27.8.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.8.1)

[简体中文](README.md) | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | **한국어** | [Русский](README_RU.md)

---

Android용 Minecraft: Java Edition 런처입니다. ZalithLauncher2 소스를 기반으로 독립적으로 개발·배포합니다.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 27.8.0 업데이트

- **AI는 에이전트**: 게임 본체·모드팩·모드 설치와 인스턴스 설정 변경을 말 한마디로 실행 (기본값 '완전 제어')
- **다운로드는 완전히 조용히**: 대화상자 없이 상단 얇은 진행 표시줄에만 표시, 탭하면 제목 / 단계 / 진행률 / 속도 / 취소
- **NeoForge 수정**: 미러가 maven.neoforged 저장소 전체를 커버해 실패와 멈춤 해결
- **세부**: 입력창 안내 문구는 입력하면 사라짐, 즐겨찾기·리소스·모드팩이 같은 작업 큐 공유

## 🛠️ 주요 기능

- **AI Agent**: 기본 DeepSeek. 게임 본체·모드팩·모드 설치, 설정 변경, 로그 확인, 게임 실행까지 직접 수행
- **렌더러**: 자동 / MobileGlues / Ironized Zink / GL4ES
- **리소스 코어**: Modrinth / CurseForge 검색, 의존성 해석, 다운로드, 검증, 설치
- **즐겨찾기와 보관**: 온라인 즐겨찾기 + 앱 전용 저장소(SHA-256 중복 제거)
- **Fabric API 보호**: Fabric 설치 흐름에서만 자동 추가, 수동 중복 설치 불가

## 📦 다운로드 및 빌드

- 다운로드: <https://github.com/zzy89216-gif/ZyNova/releases>
- 빌드: JDK 17과 Android SDK / NDK 준비 후 `./gradlew ZalithLauncher:assembleRelease`

## 📚 문서

| 내용 | 파일 |
|---|---|
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
