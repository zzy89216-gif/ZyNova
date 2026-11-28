<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <b>Français</b>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a> · <a href="../lzh/README.md">文言</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> 📌 **Current release: 28.4.0** · [Changelog](../../CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.4.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.4.0)

---

Un lanceur **Minecraft: Java Edition** pour Android, développé à partir des sources de ZalithLauncher2 et publié de façon indépendante.

La promesse tient en trois mots : **moins de clics**. Plutôt que d'étaler des réglages, le lanceur tranche d'abord et ne pose la question que lorsqu'il ne peut pas faire autrement.

## 🎯 Pour qui

- Ceux qui veulent un lanceur qu'on ouvre et qui joue, sans configurer pendant vingt minutes
- Les joueurs de modpacks : des dizaines de mods, des dépendances en cascade
- Ceux qui savent seulement dire « ça plante » : donner le log à l'IA va plus vite que fouiller un forum

## ✨ Changements récents

**28.4.0** (dernière) : principaux changements :

- Page d’accueil repensée : mondes récents + liste complète des mondes + fil personnalisé.
- Page « Personnalisation » supprimée, titre des détails du moteur corrigé, lien Discord mis à jour.
- La suppression d’une version affiche désormais une progression déterminée en plein écran.
- Suppression des anciennes cartes de mondes groupées par version au centre de l’accueil (#100).
- Correction de la détection erronée de l’état vide : lorsque toutes les versions sont masquées, aucun téléchargement n’est plus suggéré.
- Annulation des changements de 28.3.2 qui faisaient planter certains écrans de gestion des ressources (cache des listes / cache global des icônes / chargement asynchrone des extensions) ; seule la correction plein écran de la suppression de version est conservée.

> L’apparence reste celle de 28.3.0 : pas de verre dynamique, de réfraction ni d’effets magnétiques.

Les points clés de la version **28.3.0** (version stable) :

Cette version stable regroupe tout le contenu des pré-versions précédentes (ces versions ont été retirées).

- **Plusieurs téléchargements et installations en parallèle** : versions du jeu, modpacks et ressources peuvent être lancés en même temps ; progression, vitesse et annulation sont indépendantes pour chaque tâche. Un second clic sur la même cible affiche désormais un message au lieu de rester sans effet.
- **Réorganisation des cartes d'accueil plus stable** : le glisser-déposer par appui long ne clignote plus et ne plante plus ; un glissement rapide échange les cartes une par une, et le glissement horizontal est pris en charge.
- **Lecteur de musique enrichi** : chargement automatique des paroles `.lrc` de même nom et import depuis la page ; la notification affiche la ligne précédente et la ligne actuelle. Une mini-barre dans la barre supérieure (titre + lecture/pause + changement de piste) fait son apparition. La position et la durée restent correctes en pause, et l'interface ne déborde plus en paysage ou sur écran bas.
- **« Retour au jeu » corrigé** : l'entrée disparaît dès que le jeu se ferme, et un clic juste avant ne plante plus.
- **IA renforcée** : installation simultanée de plusieurs versions du jeu ; nouveaux outils de contrôle musical (53 outils au total).
- **Nouveau « Multijoueur » dans le menu en jeu** : le menu se referme, le lanceur ouvre directement la page Multijoueur — le jeu continue de tourner en arrière-plan.

Changements complets : [CHANGELOG.md](../../CHANGELOG.md).

## 🛠️ Fonctionnalités

- **Agent IA** : DeepSeek par défaut — installe le jeu, les modpacks et les mods, modifie les réglages, lit les logs, lance le jeu
- **Moteurs de rendu** : Auto / MobileGlues / Ironized Zink / GL4ES / LTW (expérimental)
- **Cœur de ressources** : recherche Modrinth / CurseForge, résolution des dépendances, téléchargement, vérification, installation
- **Favoris et archives** : favoris en ligne + archive privée, déduplication SHA-256
- **Garde-fou de l'API Fabric** : Fabric API / Quilted Fabric API / Legacy Fabric API sont ajoutés par l'installation — impossible de les dupliquer, de les chercher ou de les supprimer

## 📦 Téléchargement et compilation

- Téléchargement : <https://github.com/zzy89216-gif/ZyNova/releases> (toujours la dernière version)
- Compiler soi-même : JDK 17 + Android SDK / NDK, puis `./gradlew ZalithLauncher:assembleRelease`
- À noter : le parcours de conformité s'affiche au premier lancement ; les détails sont dans [docs/en/licenses.md](../../docs/en/licenses.md)

## 📚 Documentation

> 📚 La documentation existe en chinois et en anglais (78 articles chacune) ; cette page est l'introduction française.

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
- Discord：<https://discord.gg/dbHTApYNYJ>
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
