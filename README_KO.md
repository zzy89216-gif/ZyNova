<p align="center">
  <img src="assets/icon.png" width="180" alt="ZyNova">
</p>

# ZyNova

> **Minecraft: Java Edition · Android Launcher**
>
> ZalithLauncher2 오픈소스 코드를 기반으로 개발된 독립 비공식 프로젝트

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Primary-Kotlin-blue)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-GPL--3.0-orange)](https://www.gnu.org/licenses/gpl-3.0.html)
[![Release](https://img.shields.io/badge/Release-27.7.2-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.7.2)

[简体中文](README.md) | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | **[한국어](README_KO.md)** | [Русский](README_RU.md)

---

## ✦ 소개

**ZyNova**는 [ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2)의 오픈소스 코드를
기반으로 개발된 **비공식** Minecraft: Java Edition Android 런처입니다.

> **ZyNova는 ZalithLauncher2의 공식 버전이 아닙니다.**
>
> 독립적으로 유지보수되고 배포되는 비공식 수정 프로젝트입니다.

- GitHub: <https://github.com/zzy89216-gif/ZyNova>
- Issues: <https://github.com/zzy89216-gif/ZyNova/issues>
- Discord: <https://discord.gg/QwPpZQHrTa> (영구 초대)

---

## ✨ 27.7.2 하이라이트

- **즐겨찾기 통합**: 온라인 즐겨찾기(Modrinth / CurseForge, 한 번에 설치 및 필수 선행 모드 자동 다운로드)와 로컬 보관을 한 페이지 두 구역으로 통합. 다운로드 화면 왼쪽에서도 진입 가능
- **렌더러 페이지 분리**: MobileGlues / Ironized Zink / GL4ES 각각 별도 페이지로
- **설정을 7개 분류로 재구성**: 기본 / 게임 / 렌더러 / 네트워크 및 다운로드 / AI / 고급 / 정보 및 지원
- **네트워크 및 다운로드**: 연결 수, 연결/읽기 시간 초과, 재시도 횟수 조정 가능. 서비스별로 다운로드 소스를 변경할 수 있으며(잘못된 값은 공식 주소로 자동 복귀), 캐시 확인 및 정리 지원
- **렌더러 설정 통합**: 선택한 렌더러의 설정만 표시하고, 모든 조정 가능한 매개변수를 '고급 설정'에 모음
- **AI 설정을 설정 화면으로 통합**: 채팅 화면의 별도 진입점 제거(최초 설정 안내는 유지), 기존 API Key/모델/권한 모드는 그대로 유지
- **즐겨찾기 및 로컬 보관**: 리소스를 앱 전용 저장소로 복사하고 SHA-256으로 중복 제거. 인스턴스에 한 번에 설치하고 필수 선행 모드를 자동 다운로드하며, 무결성 검사·내보내기·개별 삭제 지원
- **동영상 배경 시작 최적화**: 초기화 단계에서 플레이어를 만들지 않고 첫 프레임 이후 페이드 인하여 흰 화면과 깜빡임을 줄임
- **다운로드와 설치를 백그라운드로**: 다운로드 화면을 떠나도 설치가 중단되지 않음
- **월드 생성**: 하드코어에서는 치트와 아이템 유지가 강제로 꺼짐
- **NeoForge 다운로드**: 실제 바이트 기준 진행률, 정지 시 사유와 함께 실패, 재시도 시 완료된 파일 재사용

> 이전 버전의 전체 변경 내역은 [CHANGELOG.md](CHANGELOG.md)를 참고하세요.

## 🛠️ 주요 기능

### AI Agent

ZyNova에는 로그 확인, 모드 관리, 설정 편집, 리소스 설치, 게임 실행을 실제로 수행할 수 있는
내장 AI Agent가 있습니다. 기본 제공자는 DeepSeek이며, OpenAI 호환 API도 사용할 수 있습니다.
모델 목록은 공급자에게서 동적으로 가져오고, API Key와 Base URL은 별도로 저장됩니다.

### 렌더러

- **설정 7개 분류**: 기본 / 게임 / 렌더러 / 네트워크 및 다운로드 / AI / 고급 / 정보 및 지원.
  렌더러 매개변수와 서비스별 다운로드 소스를 여기에서 조정할 수 있습니다
- **즐겨찾기 및 로컬 보관**: 리소스를 앱 전용 저장소로 복사하고 SHA-256으로 중복 제거. 인스턴스에 한 번에
  설치하고 필수 선행 모드를 자동 다운로드하며 무결성 검사·내보내기·개별 삭제 지원
- **자동**: Minecraft 버전과 호환되는 렌더러를 자동 선택
- **MobileGlues**
- **Ironized Zink**
- **GL4ES**

### 카드형 홈

설치된 인스턴스를 카드로 보여 주고, 각 카드에서 월드/서버를 바로 실행하거나 새 월드/서버를
만들 수 있습니다. 카드 크기, 순서, 홈 버튼 재질을 설정할 수 있습니다.

### 통합 리소스 관리

Modrinth / CurseForge 검색, 버전 호환성 확인, 필요 의존성 재귀 설치, 다운로드, SHA1 검증,
설치를 하나의 흐름으로 처리합니다. 이미 설치된 파일과 중복 프로젝트는 자동으로 건너뜁니다.

### Vulkan 감지

기기와 드라이버의 Vulkan 지원 여부를 확인해 렌더러 선택에 도움을 줍니다.

## 📦 빌드 방법 (개발자)

### 요구 사항

- JDK 17
- Android SDK / NDK
- Gradle (저장소의 wrapper 사용)

### 빌드

```bash
./gradlew ZalithLauncher:assembleRelease
```

릴리스 빌드는 코드 난독화와 리소스 축소가 적용되며, `mapping.txt`가 함께 생성됩니다.
공식 배포는 GitHub Actions에서 범용 APK 하나로 빌드됩니다.

## 📜 라이선스

ZyNova는 **GPL-3.0**으로 배포됩니다. 원본 ZalithLauncher2와 마찬가지로 소스 공개가 요구됩니다.
추가 조항과 전체 라이선스는 [LICENSE](LICENSE)를 확인하세요.

## 오픈소스 라이브러리

LWJGL, GL4ES, MobileGlues, Ironized Zink, OpenNBT, Haze, AndroidX / Compose 등 다양한
오픈소스 프로젝트를 사용합니다. 라이선스와 저작권 고지는
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)에 정리되어 있습니다.

## 💖 후원

ZyNova는 개인이 유지보수하는 프로젝트입니다. 후원은 필수가 아니며, 프로젝트의 지속적인
개발에 큰 도움이 됩니다. 자세한 내용은 README의 후원 섹션을 참고하세요.

---

**ZyNova — Context First. Less Steps.**
