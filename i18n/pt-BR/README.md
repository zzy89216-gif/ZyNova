<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <b>Português (Brasil)</b> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a> · <a href="../lzh/README.md">文言</a>
</sub></p>

<p align="center">
  <img src="../../docs/assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> 📌 **Current release: 28.4.0** · [Changelog](../../meta/CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.4.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.4.0)

---

Um launcher de **Minecraft: Java Edition** para Android, mantido de forma independente a partir do código do ZalithLauncher2. Não é um build oficial.

A ideia é simples: **menos toques**. Em vez de despejar vinte configurações na sua frente, o launcher decide primeiro e só pergunta quando não dá.

## 🚀 Passo a passo

1. Baixe o APK em [Releases](https://github.com/zzy89216-gif/ZyNova/releases) e instale (Android 8.0+)
2. No primeiro uso, leia os três documentos até o fim, marque cada um e escolha a faixa etária
3. Entre com a conta Microsoft, escolha ou instale uma versão, aperte jogar

## ✨ Mudanças recentes

**28.4.0** (mais recente): principais mudanças:

- Tela inicial reformulada: mundos recentes + lista completa de mundos + feed personalizado.
- Página «Personalização» removida, título dos detalhes do renderizador corrigido, link do Discord atualizado.
- Excluir uma versão agora mostra progresso determinado em tela cheia.
- Removidos os antigos cartões de mundos agrupados por versão do centro da tela inicial (#100).
- Corrigida a detecção errada do estado vazio: com todas as versões ocultas, não sugere mais download.
- Revertidas as mudanças da 28.3.2 que causavam fechamentos em algumas telas de gerenciamento de recursos (cache de listas / cache global de ícones / carregamento assíncrono de plugins); mantida apenas a correção em tela cheia da exclusão de versão.

> A aparência continua como na 28.3.0 — sem vidro dinâmico, refração ou efeitos magnéticos.

**28.3.0** (versão estável): os principais destaques.

Esta versão estável reúne todo o conteúdo das versões de pré-lançamento anteriores (elas foram removidas).

- **Vários downloads e instalações ao mesmo tempo**: versões do jogo, modpacks e recursos podem começar juntos; cada tarefa tem seu próprio progresso, velocidade e botão de cancelar. Tocar de novo no mesmo item agora mostra um aviso, em vez de não fazer nada.
- **Reordenar os cartões da tela inicial ficou mais estável**: arrastar com toque longo não pisca mais nem fecha o app; ao arrastar rápido os cartões trocam de lugar um a um, e o arrasto na horizontal também funciona.
- **Player de música melhorado**: carrega automaticamente letras `.lrc` com o mesmo nome e permite importar pela página; a notificação mostra a linha anterior e a atual. Uma mini barra no topo (título + play/pausa + trocar faixa) foi adicionada. A posição e a duração ficam corretas mesmo pausado, e a interface não estoura mais no modo paisagem ou em telas baixas.
- **«Voltar ao jogo» corrigido**: o atalho some assim que o jogo fecha e tocar nele um instante antes não causa mais fechamento.
- **IA reforçada**: dá para instalar várias versões do jogo ao mesmo tempo; novas ferramentas de controle de música (53 ferramentas no total).
- **Novo atalho «Multijogador» no menu do jogo**: o menu fecha, o launcher abre direto a página Multijogador e o jogo continua rodando em segundo plano.

Mudanças completas: [CHANGELOG.md](../../meta/CHANGELOG.md).

## 🛠️ O que faz

- **Agente de IA**: DeepSeek por padrão — instala o jogo, modpacks e mods, altera configurações, lê logs, inicia o jogo
- **Renderizadores**: Automático / MobileGlues / Ironized Zink / GL4ES / LTW (experimental)
- **Núcleo de recursos**: busca no Modrinth / CurseForge, resolução de dependências, download, verificação, instalação
- **Favoritos e arquivo**: favoritos online + arquivo privado, com deduplicação por SHA-256
- **Proteção das APIs do Fabric**: Fabric API / Quilted Fabric API / Legacy Fabric API entram pelo instalador — nada de duplicar, buscar ou apagar

## 📖 Glossário

- **Modpack**: um pacote de mods que se instala de uma vez
- **Shader**: iluminação e água mais bonitas; fica nos ajustes gráficos
- **Instância**: pasta própria com versão, mods e configurações
- **Renderizador**: a camada que desenha o jogo (MobileGlues / Ironized Zink / GL4ES / LTW)

## 📦 Download e compilação

- Download: <https://github.com/zzy89216-gif/ZyNova/releases> (sempre a versão mais recente)
- Compilar por conta própria: JDK 17 + Android SDK / NDK e depois `./gradlew ZalithLauncher:assembleRelease`
- Atenção: o fluxo de conformidade aparece no primeiro uso; detalhes em [docs/en/licenses.md](../../docs/en/licenses.md)

## 📚 Documentação

> 📚 A documentação existe em chinês e inglês (78 artigos cada); esta página é a introdução em português.

| Conteúdo | Arquivo |
|---|---|
| Índice | [docs/en/README.md](../../docs/en/README.md) |
| Instalação / uso / exportação | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Renderização / login / IA | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Problemas / FAQ / dados e privacidade | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Compilação / changelog | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../meta/CHANGELOG.md) |
| Passagem de bastão / licenças de terceiros / licença | [HANDOFF.md](../../meta/HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../meta/THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 Apoio

- Reportar problema：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/dbHTApYNYJ>
- E-mail：<ZyNovaLauncher@outlook.com>
- X oficial：<https://x.com/ZyNovaProject>
- Apoiar o projeto：<https://afdian.com/a/hizzy>

## ⚖️ Licença

GPL-3.0 — o projeto continua aberto. Componentes de terceiros e suas licenças estão em
[THIRD_PARTY_NOTICES.md](../../meta/THIRD_PARTY_NOTICES.md) e [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **O Terracotta (multijogador) usa AGPL-3.0**, mais rígida que o restante: é distribuído sem
> alterações. Se você modificá-lo e redistribuí-lo, o §13 da AGPL-3.0 exige publicar o código-fonte
> correspondente.

---

**ZyNova — Context First. Less Steps.**
