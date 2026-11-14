<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <b>Português (Brasil)</b> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> 📌 **Current release: 28.2.1** · [Changelog](../../CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.2.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.2.1)

---

Um launcher de **Minecraft: Java Edition** para Android, mantido de forma independente a partir do código do ZalithLauncher2. Não é um build oficial.

A ideia é simples: **menos toques**. Em vez de despejar vinte configurações na sua frente, o launcher decide primeiro e só pergunta quando não dá.

## 🚀 Passo a passo

1. Baixe o APK em [Releases](https://github.com/zzy89216-gif/ZyNova/releases) e instale (Android 8.0+)
2. No primeiro uso, leia os três documentos até o fim, marque cada um e escolha a faixa etária
3. Entre com a conta Microsoft, escolha ou instale uma versão, aperte jogar

## ✨ Mudanças recentes

Mudanças completas: [CHANGELOG.md](../../CHANGELOG.md).

## 🛠️ O que faz

- **Agente de IA**: DeepSeek por padrão — instala o jogo, modpacks e mods, altera configurações, lê logs, inicia o jogo
- **Renderizadores**: Automático / MobileGlues / GL4ES / LTW (experimental)
- **Núcleo de recursos**: busca no Modrinth / CurseForge, resolução de dependências, download, verificação, instalação
- **Favoritos e arquivo**: favoritos online + arquivo privado, com deduplicação por SHA-256
- **Proteção das APIs do Fabric**: Fabric API / Quilted Fabric API / Legacy Fabric API entram pelo instalador — nada de duplicar, buscar ou apagar

## 📖 Glossário

- **Modpack**: um pacote de mods que se instala de uma vez
- **Shader**: iluminação e água mais bonitas; fica nos ajustes gráficos
- **Instância**: pasta própria com versão, mods e configurações
- **Renderizador**: a camada que desenha o jogo (MobileGlues / GL4ES / LTW)

## 📦 Download e compilação

- Download: <https://github.com/zzy89216-gif/ZyNova/releases> (sempre a versão mais recente)
- Compilar por conta própria: JDK 17 + Android SDK / NDK e depois `./gradlew ZalithLauncher:assembleRelease`
- Atenção: o fluxo de conformidade aparece no primeiro uso; detalhes em [docs/en/licenses.md](../../docs/en/licenses.md)

## 📚 Documentação

> 📚 A documentação existe em chinês e inglês (77 artigos cada); esta página é a introdução em português.

| Conteúdo | Arquivo |
|---|---|
| Índice | [docs/en/README.md](../../docs/en/README.md) |
| Instalação / uso / exportação | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Renderização / login / IA | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Problemas / FAQ / dados e privacidade | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Compilação / changelog | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../CHANGELOG.md) |
| Passagem de bastão / licenças de terceiros / licença | [HANDOFF.md](../../HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 Apoio

- Reportar problema：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- E-mail：<ZyNovaLauncher@outlook.com>
- X oficial：<https://x.com/ZyNovaProject>
- Apoiar o projeto：<https://afdian.com/a/hizzy>

## ⚖️ Licença

GPL-3.0 — o projeto continua aberto. Componentes de terceiros e suas licenças estão em
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) e [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **O Terracotta (multijogador) usa AGPL-3.0**, mais rígida que o restante: é distribuído sem
> alterações. Se você modificá-lo e redistribuí-lo, o §13 da AGPL-3.0 exige publicar o código-fonte
> correspondente.

---

**ZyNova — Context First. Less Steps.**
