<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-27.10.2-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.10.2)

[简体中文](README.md) | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | **한국어** | [Русский](README_RU.md)

---

Android용 Minecraft: Java Edition 런처입니다. ZalithLauncher2 소스를 기반으로 독립적으로 개발·배포합니다.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 27.10.2 업데이트

- **개인화 화면**: 배경, 배경 불투명도, 흐림, 모서리 반경, 유리 강도를 한 페이지에서 조정 (상단 캡슐의 팔레트 버튼)
- **유리 기반 재작업**: 앱 전체에서 배경 흐림은 한 층만, 상시 애니메이션 전부 제거. 카드는 단색 그라데이션 + 1px 테두리
- **AI 채팅**: 도구 호출은 `명령 실행 · 요약` 한 줄로 표시하고 원본 출력은 기본 접힘. 도구 실행 전 모델이 한 줄 요약을 먼저 작성
- **Quilt 보호**: Quilted / Legacy Fabric API도 Fabric API와 동일하게 "검색 불가·설치 불가·삭제 불가·비활성화 불가"
- **수정**: 뒤로 가기 버튼이 동작하지 않거나 화면이 깜빡이던 문제, SakuraFrp 접속 키 입력 직후 오류, 다이얼로그 이중 흐림
- 이 빌드는 **테스트판(pre-release)** 입니다

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
