<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.0.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.0.1)

[简体中文](README.md) | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | **한국어** | [Русский](README_RU.md)

---

Android용 Minecraft: Java Edition 런처입니다. ZalithLauncher2 소스를 기반으로 독립적으로 개발·배포합니다.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 28.0.1 업데이트

- **SakuraFrp가 실제로 연결됩니다**: `frpc`를 공식 형식 `frpc -f <액세스 키>:<터널 ID>`로 실행합니다(예전 환경 변수 방식은 Sakura 버전 frpc가 무시해서 바로 종료되었습니다)
- **새로고침해도 오류가 나지 않습니다**: BOM / 배열 / `{data}` 래핑 응답을 지원하고, 연결 주소는 frpc 로그에서 추출해 **맨 위에 표시 + 원탭 복사**
- **뒤로 가기로 화면 전체가 종료되지 않습니다**: 상단 뒤로 가기가 시스템 뒤로 가기를 거치지 않고(콜백이 없어 화면이 종료되었습니다), 한 단계만 되돌아가며 스택을 비우지 않습니다
- **게임을 끄지 않고 런처로 돌아갈 수 있습니다**: 게임 내 메뉴 첫 항목 "런처로 돌아가기"는 **게임을 중지하지 않습니다**. 런처 하단에 "게임으로 돌아가기" 알약 버튼이 나타납니다
- **포트와 유형을 몰라도 됩니다**: 유형은 TCP 고정, 로컬 포트는 `/proc/net/tcp`에서 자동 감지, 노드는 계정 등급으로 필터링
- **AI가 멀티플레이를 전자동으로 시작**: `frp_status` / `frp_create_tunnel` / `frp_start_tunnel` / `frp_stop_tunnel` / `multiplayer_status`

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
