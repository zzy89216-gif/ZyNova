<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <b>Português (Brasil)</b> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 lançado (versão em conformidade global).**

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.1.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.1.0)

---

Um launcher de **Minecraft: Java Edition** para Android, mantido e publicado de forma independente a partir do código-fonte do ZalithLauncher2. Não é um build oficial.

A ideia: **menos toques**. Tudo o que dá para automatizar — instalar o jogo, juntar mods e modpacks, mexer nas configurações, ler os logs — fica com o agente de IA.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Bugs e sugestões：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- X (conta principal oficial)：<https://x.com/ZyNovaProject>
- E-mail do ecossistema chinês：<ZyNovaLauncher@163.com>
- E-mail global：<ZyNovaLauncher@outlook.com>

## ✨ Novidades da 28.1.0

- **Fluxo de conformidade no primeiro uso**：três documentos unificados — ① EULA + política de privacidade, ② avisos de código aberto de terceiros + isenções e riscos, ③ tutorial + regiões atendidas e foro competente. Cada documento precisa ser **rolado até o fim e marcado separadamente**, e só então você escolhe a faixa etária (quem já usa o app passa por isso uma vez depois de atualizar).
- **Janela de horário para menores**：menores de 18 anos só podem iniciar o jogo às sextas, sábados, domingos e feriados nacionais, das **20h às 21h**. Os feriados de 2026 seguem o calendário do Conselho de Estado chinês (国办发明电〔2025〕7号); dias de compensação contam como dias úteis.
- **Isenções escritas sem rodeios**：perda de save, aquecimento do aparelho, banimento por terceiros, encerramento de serviço e riscos parecidos são avisados com clareza.
- **Também**：os cards da tela inicial não travam mais no carregamento, a busca na web da IA funciona, a importação vai para a pasta da instância e a correção automática de crash foi removida.

## 🛠️ O que faz

- **Agente de IA**：DeepSeek por padrão — instala o jogo, modpacks e mods, altera configurações, lê logs, inicia o jogo
- **Renderizadores**：Automático / MobileGlues / GL4ES / LTW (experimental)
- **Núcleo de recursos**：busca no Modrinth / CurseForge, resolução de dependências, download, verificação, instalação
- **Favoritos e arquivo**：favoritos online + arquivo privado, com deduplicação por SHA-256
- **Proteção das APIs do Fabric**：Fabric API / Quilted Fabric API / Legacy Fabric API entram pelo instalador — não dá para duplicar, buscar nem apagar

## 📦 Download e compilação

- Download：<https://github.com/zzy89216-gif/ZyNova/releases> (use sempre a versão mais recente)
- Compilar por conta própria：JDK 17 + Android SDK / NDK, depois `./gradlew ZalithLauncher:assembleRelease`
- Atenção：o fluxo de conformidade aparece no primeiro uso; detalhes de regiões e limites de idade estão em [docs/en/licenses.md](../../docs/en/licenses.md).

## 📚 Documentação

> 📚 A documentação existe por enquanto em chinês e inglês (mais de 70 artigos); esta página é a introdução em português.

| Conteúdo | Arquivo |
|---|---|
| Índice da documentação | [docs/en/README.md](../../docs/en/README.md) |
| Instalação / uso / exportação | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Renderização / login / favoritos / IA | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/favorites.md](../../docs/en/favorites.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Solução de problemas / FAQ / dados e privacidade | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Compilação | [docs/en/build.md](../../docs/en/build.md) |
| Changelog | [CHANGELOG.md](../../CHANGELOG.md) |
| Passagem de bastão / arquitetura / fluxo | [HANDOFF.md](../../HANDOFF.md) |
| Entrada para quem assume (estado / release / armadilhas / regras de doc) | [AGENTS.md](../../AGENTS.md) |
| Licenças de terceiros | [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) |
| Licença | [LICENSE](../../LICENSE) |

## 💖 Apoio

- Reportar problema：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- E-mail：global <ZyNovaLauncher@outlook.com> ｜ China <ZyNovaLauncher@163.com>
- X oficial：<https://x.com/ZyNovaProject>
- Apoiar o projeto：<https://afdian.com/a/hizzy>

## ⚖️ Licença

GPL-3.0 — o projeto continua aberto. Componentes de terceiros e suas licenças estão em
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) e [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **O Terracotta (multijogador) usa AGPL-3.0**, mais rígida que o restante: é distribuído sem alterações.
> Se você modificá-lo e redistribuí-lo, o §13 da AGPL-3.0 exige a publicação do código-fonte correspondente.

---

**ZyNova — Context First. Less Steps.**
