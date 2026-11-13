<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <b>Français</b>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 est disponible (version conforme à l'international).**
>
> 📌 **Current release: 28.2.0** · [Changelog](../../CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.2.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.2.0)

---

Un lanceur **Minecraft: Java Edition** pour Android, développé à partir des sources de ZalithLauncher2 et publié de façon indépendante.

La promesse tient en trois mots : **moins de clics**. Plutôt que d'étaler des réglages, le lanceur tranche d'abord et ne pose la question que lorsqu'il ne peut pas faire autrement.

## 🎯 Pour qui

- Ceux qui veulent un lanceur qu'on ouvre et qui joue, sans configurer pendant vingt minutes
- Les joueurs de modpacks : des dizaines de mods, des dépendances en cascade
- Ceux qui savent seulement dire « ça plante » : donner le log à l'IA va plus vite que fouiller un forum

## ✨ Nouveautés de la 28.1.0

- **Parcours de conformité au premier lancement** : trois documents regroupés (① CLUF + politique de confidentialité, ② mentions open source de tiers + avertissements et risques, ③ tutoriel + zones couvertes et juridiction). Chacun doit être **fait défiler jusqu'en bas et validé séparément**, puis on choisit sa tranche d'âge (les utilisateurs existants repassent par là une fois).
- **Créneau pour les mineurs** : moins de 18 ans, lancement uniquement vendredi, samedi, dimanche et jours fériés légaux, de **20 h à 21 h**. Les jours fériés 2026 suivent le calendrier du Conseil des affaires d'État chinois ; les journées de rattrapage comptent comme travaillées.
- **Avertissements explicites** : perte de sauvegarde, chauffe de l'appareil, bannissement par un tiers, arrêt d'un service — tout est annoncé.
- **Également** : la page d'accueil ne reste plus bloquée en chargement, la recherche web de l'IA fonctionne, les imports vont dans le dossier de l'instance, la réparation automatique après crash a été retirée.

## 🛠️ Fonctionnalités

- **Agent IA** : DeepSeek par défaut — installe le jeu, les modpacks et les mods, modifie les réglages, lit les logs, lance le jeu
- **Moteurs de rendu** : Auto / MobileGlues / GL4ES / LTW (expérimental)
- **Cœur de ressources** : recherche Modrinth / CurseForge, résolution des dépendances, téléchargement, vérification, installation
- **Favoris et archives** : favoris en ligne + archive privée, déduplication SHA-256
- **Garde-fou de l'API Fabric** : Fabric API / Quilted Fabric API / Legacy Fabric API sont ajoutés par l'installation — impossible de les dupliquer, de les chercher ou de les supprimer

## 📦 Téléchargement et compilation

- Téléchargement : <https://github.com/zzy89216-gif/ZyNova/releases> (toujours la dernière version)
- Compiler soi-même : JDK 17 + Android SDK / NDK, puis `./gradlew ZalithLauncher:assembleRelease`
- À noter : le parcours de conformité s'affiche au premier lancement ; les détails sont dans [docs/en/licenses.md](../../docs/en/licenses.md)

## 📚 Documentation

> 📚 La documentation existe en chinois et en anglais (77 articles chacune) ; cette page est l'introduction française.

| Contenu | Fichier |
|---|---|
| Sommaire | [docs/en/README.md](../../docs/en/README.md) |
| Installation / utilisation / export | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Rendu / connexion / favoris / IA | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Dépannage / FAQ / données et vie privée | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Compilation / journal des modifications | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../CHANGELOG.md) |
| Reprise / licences tierces / licence | [HANDOFF.md](../../HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 Soutien

- Signaler un problème：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- E-mail：<ZyNovaLauncher@outlook.com>
- X officiel：<https://x.com/ZyNovaProject>
- Soutenir le projet：<https://afdian.com/a/hizzy>

## ⚖️ Licence

GPL-3.0 : le projet reste open source. Les composants tiers et leurs licences sont listés dans
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) et [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (multijoueur) est sous AGPL-3.0**, plus stricte que le reste : distribué tel quel.
> Si vous le modifiez et le redistribuez, l'article 13 de l'AGPL-3.0 vous oblige à publier le code source
> correspondant.

---

**ZyNova — Context First. Less Steps.**
