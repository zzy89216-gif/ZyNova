/*
 * ZyNova Launcher — 三份声明的正文（德语 / 法语）
 * GPL-3.0-or-later
 *
 * 德语：正式体（Sie），术语按德国软件行业惯例（Spielstand = 存档、Modpack = Modpack）。
 * 法语：正式体（vous），术语按法国/加拿大通用译法（sauvegarde = 存档、module = 模组）。
 */
package com.movtery.zalithlauncher.onboarding

// ─────────────────────────────────────────────────────────────────
//  Deutsch
// ─────────────────────────────────────────────────────────────────

internal val TERMS_DE = """
《Endnutzer-Lizenzvereinbarung + Datenschutzerklärung》

1. Annahme der Vereinbarung
Mit der Installation, Vervielfältigung oder Nutzung von ZyNova Launcher („die Software") bestätigen Sie, diese Vereinbarung vollständig gelesen, verstanden und akzeptiert zu haben. Wenn Sie nicht einverstanden sind, beenden Sie die Nutzung und deinstallieren Sie die Software.

2. Lizenz und Open Source
Die Software wird unter GNU GPL-3.0 vertrieben, der Quellcode ist öffentlich. Sie dürfen sie im Rahmen der GPL-3.0 nutzen, ändern und weitergeben. Bei einer Weitergabe müssen Lizenz- und Urheberrechtshinweise beigefügt, der entsprechende Quellcode veröffentlicht und dürfen keine zusätzlichen Beschränkungen auferlegt werden. Die Software ist ein **inoffizieller** Drittanbieter-Launcher und steht in keiner Verbindung zu Mojang Studios, Microsoft, NetEase oder verbundenen Unternehmen; „Minecraft" ist eine Marke von Mojang Synergies AB.

3. Konten und Spieldateien
Die Software stellt keine Minecraft-Konten bereit, verkauft oder teilt sie nicht. Sie benötigen ein eigenes rechtmäßiges Konto oder den Offline-Modus ausschließlich für Spieldateien, die Sie rechtmäßig besitzen. Die Rechte an Spiel, Mods, Modpacks, Ressourcenpaketen und Shadern liegen bei den jeweiligen Rechteinhabern.

4. Keine Gewährleistung, Haftungsbeschränkung
Die Software wird „wie besehen" bereitgestellt, ohne ausdrückliche oder stillschweigende Gewährleistung. Soweit gesetzlich zulässig, haften Autoren und Mitwirkende nicht für direkte, indirekte, zufällige, besondere oder Folgeschäden aus der Nutzung oder Nichtnutzbarkeit der Software, einschließlich verlorener Spielstände, Welten oder Einstellungen, Überhitzung oder Ruckeln des Geräts, Kontosperren sowie Änderungen oder Abschaltungen von Drittanbieterdiensten. Sichern Sie wichtige Spielstände bitte selbst.

5. Nutzungsbeschränkungen
Sie dürfen die Software nicht für rechtswidrige Handlungen, zur Verletzung von Rechten Dritter, für Angriffe auf fremde Netze und Geräte oder zur Umgehung von Nutzungsbedingungen und Jugendschutzmaßnahmen einsetzen.

6. Dienste Dritter und Kosten
KI-Chat, Websuche, Tunnel und Mehrspieler beruhen auf Diensten Dritter und können eigene Schlüssel und Kontingente erfordern. Kosten und Bedingungen regeln Sie mit dem jeweiligen Anbieter.

7. Datenschutz
1. Standardmäßig werden keine personenbezogenen Daten erhoben oder übermittelt; Konten, Instanzen, Einstellungen und Protokolle bleiben auf Ihrem Gerät.
2. Daten verlassen das Gerät nur, wenn Sie folgende Funktionen selbst aktivieren: KI (Nachrichten und Werkzeugparameter an den von Ihnen konfigurierten Modellanbieter), Websuche (Suchbegriffe an die Suchmaschine), Tunnel (Schlüssel und Tunnelinformationen an den Dienstknoten), Protokoll-Upload (nur auf Ihren Klick).
3. Ausweisnummern, Gesichtsbilder und Telefonnummern werden nicht erhoben, nicht abgefragt und nicht übermittelt. Sollte künftig eine Altersprüfung nötig sein, erfolgt die Berechnung ausschließlich im Arbeitsspeicher – ohne Speichern und ohne Übertragung.
4. Berechtigungen: Speicher (Lesen/Schreiben des Spielverzeichnisses, Protokollexport), Benachrichtigungen (Download- und Tunnel-Fortschritt), Netzwerk (Downloads, KI, Mehrspieler). Mikrofon und Kamera werden nicht angefordert.
5. Sie können lokale Daten jederzeit einsehen, exportieren und löschen oder die App deinstallieren, wodurch alle lokalen Daten entfernt werden.

8. Änderungen und Beendigung
Diese Vereinbarung kann mit Aktualisierungen angepasst werden; eine **erneute Zustimmung ist nur bei wesentlichen Änderungen** erforderlich. Bei einem Verstoß endet die Lizenz automatisch: Stellen Sie die Nutzung ein und löschen Sie alle Kopien.

9. Sonstiges
Die Unwirksamkeit einzelner Klauseln lässt die übrigen unberührt. Bei Konflikten mit zwingendem lokalem Recht gilt das Recht.
""".trimIndent()

internal val LICENSES_DE = """
《Hinweise zu Open-Source-Komponenten + Ergänzende Risikohinweise》

1. Die Software selbst
GPL-3.0; der vollständige Text steht in der LICENSE des Repositories. Die Urheberrechtshinweise der Upstream-Projekte ZalithLauncher2 / Fold Craft Launcher / PojavLauncher bleiben vollständig erhalten.

2. Komponente mit den strengsten Pflichten
Terracotta (Mehrspieler) = AGPL-3.0, unverändert weitergegeben. AGPL ist strenger als GPL: Wer sie verändert und weitergibt oder ihre Funktionen als Netzwerkdienst anbietet, muss den entsprechenden Quellcode veröffentlichen.

3. Komponenten mit schwachem Copyleft
MobileGlues (LGPL-2.1), sora-editor / language-textmate (LGPL-2.1), LTW-Turbo (LGPL-3.0), TouchController (LGPL-3.0) – als separate dynamische Bibliotheken bzw. Module ausgeliefert und von Ihnen austauschbar.

4. Komponenten mit permissiven Lizenzen
MIT: GL4ES, ByteHook, OpenNBT, skinview3d; BSD-3-Clause: ANGLE, LWJGL, MMKV; Zlib: SDL3, sdl2-compat; EPL-2.0: tm4e; 0BSD: XZ for Java; Apache-2.0: frp / frpc, AndroidX, Ktor, OkHttp u. a. Die Einzelliste und die Volltexte stehen in den Hinweisen unten sowie unter Einstellungen → Über → Open-Source-Lizenzen.

5. Ergänzende Risikohinweise
1. Spielstände, Welten, Screenshots, Einstellungen und Mods können durch Abstürze, Stromausfall, Speicherfehler oder Konflikte mit Fremdsoftware verloren gehen oder beschädigt werden;
2. Das Gerät kann sich erwärmen, schneller entladen, ruckeln oder Hintergrundprozesse können vom System beendet werden;
3. Drittanbieter-Konten, -Server oder -Dienste können zu Sperren, Drosselung, Kosten oder Datenverarbeitung führen;
4. Mods oder Modpacks aus unbekannten Quellen können Schadcode enthalten;
5. Tunneling legt lokale Ports im Internet offen und kann gescannt oder missbraucht werden.
Sie bewerten und tragen diese Risiken selbst; die Software verspricht weder Backup noch Wiederherstellung oder Ersatz.

6. Was Sie dürfen
Private Nutzung und private Änderung: erlaubt. Weitergabe: erlaubt mit Lizenz- und Urheberrechtshinweisen, veröffentlichtem Quellcode und Nennung der Upstream-Urheber. Kommerzielle Nutzung ohne Quellcode: nicht erlaubt. Weitergabe geänderter AGPL-Komponenten: Änderungen müssen veröffentlicht werden.
""".trimIndent()

internal val GUIDE_DE = """
《Erste Schritte + Regionen und anwendbares Recht》

1. Erster Start
Stimmen Sie den drei Dokumenten zu und wählen Sie Ihre Altersgruppe. Tippen Sie auf dem Startbildschirm auf „Instanz herunterladen", wählen Sie eine Version (optional mit Fabric / NeoForge / Quilt) und warten Sie den Download ab.

2. Mods, Ressourcenpakete, Shader, Modpacks
Die Download-Seite unterstützt Modrinth / CurseForge und löst Abhängigkeiten automatisch auf. Sie können Dateien auch in den richtigen Ordner legen oder „An ZyNova teilen" verwenden.

3. Mehrspieler
Terracotta: Beide Spieler installieren diesen Launcher und betreten denselben Raum (P2P, kein Port-Forwarding). Sakura-Tunnel: Sakura-Konto und Zugangsschlüssel nötig; der Tunnel ist TCP-only, lokaler Port standardmäßig 25565 (änderbar).

4. KI-Assistent
Er kann Spiel oder Mods installieren, Protokolle lesen, Einstellungen ändern und Word-/PPT-Dateien erzeugen. Die Websuche nutzt DeepSeek oder DuckDuckGo; gelöschte Dateien landen im Papierkorb.

5. Bei Problemen
Einstellungen → Über → Protokolle zum Ansehen und Exportieren; nach einem Absturz den Absturzbericht öffnen oder die KI analysieren lassen. Bitte Modell, Android-Version, Launcher-Version und Protokolldatei mitschicken.

6. Leistungstipps
Arbeitsspeicher unter der Hälfte des verfügbaren RAM halten; zuerst MobileGlues / LTW-Turbo probieren, bei Kompatibilitätsproblemen GL4ES. Große Modpacks in den internen Speicher mit genug freiem Platz legen.

7. Deinstallation und Backup
Beim Deinstallieren bleiben Spieldateien im externen Speicher erhalten; Instanzen im Launcher löschen. Vor Aktualisierungen ein Modpack-Backup exportieren.
""".trimIndent()

// ─────────────────────────────────────────────────────────────────
//  Français
// ─────────────────────────────────────────────────────────────────

internal val TERMS_FR = """
《Contrat de licence utilisateur final + Politique de confidentialité》

1. Acceptation
En installant, copiant ou utilisant ZyNova Launcher (« le Logiciel »), vous confirmez avoir lu, compris et accepté l'intégralité du présent contrat. Si vous n'êtes pas d'accord, cessez l'utilisation et désinstallez le Logiciel.

2. Licence et open source
Le Logiciel est distribué sous GNU GPL-3.0 et son code source est public. Vous pouvez l'utiliser, le modifier et le redistribuer dans le respect de la GPL-3.0 ; toute redistribution doit inclure la licence et les mentions de copyright, publier le code source correspondant et n'ajouter aucune restriction supplémentaire. Le Logiciel est un lanceur tiers **non officiel**, sans lien d'affiliation, de mandat, de parrainage ou d'approbation avec Mojang Studios, Microsoft, NetEase ou leurs sociétés affiliées. « Minecraft » est une marque de Mojang Synergies AB.

3. Comptes et fichiers de jeu
Le Logiciel ne fournit, ne vend et ne partage aucun compte Minecraft. Vous devez disposer d'un compte légitime ou n'utiliser le mode hors ligne que pour des fichiers de jeu que vous possédez légalement. Les droits sur le jeu, les mods, les modpacks, les packs de ressources et les shaders appartiennent à leurs titulaires respectifs.

4. Absence de garantie, limitation de responsabilité
Le Logiciel est fourni « en l'état », sans garantie expresse ou implicite. Dans la limite permise par la loi, les auteurs et contributeurs ne sauraient être tenus responsables des dommages directs, indirects, accessoires, spéciaux ou consécutifs résultant de l'utilisation ou de l'impossibilité d'utiliser le Logiciel, notamment la perte de sauvegardes, de mondes ou de réglages, la surchauffe ou les ralentissements de l'appareil, le bannissement de comptes, ainsi que les modifications ou arrêts de services tiers. Sauvegardez vous-même vos parties importantes.

5. Restrictions d'usage
Vous ne pouvez pas utiliser le Logiciel à des fins illicites, pour porter atteinte aux droits d'autrui, pour attaquer des réseaux ou appareils tiers, ni pour contourner les conditions d'utilisation d'une plateforme ou les mesures de protection des mineurs.

6. Services tiers et frais
Le chat IA, la recherche web, le tunnel et le multijoueur reposent sur des services tiers et peuvent nécessiter vos propres clés et quotas. Les frais et conditions se règlent entre vous et le fournisseur concerné.

7. Confidentialité
1. Aucune donnée personnelle n'est collectée ni transmise par défaut ; comptes, instances, réglages et journaux restent sur votre appareil.
2. Les données ne quittent l'appareil que si vous activez vous-même ces fonctions : IA (messages et paramètres d'outils envoyés au fournisseur de modèle configuré), recherche web (requêtes envoyées au moteur), tunnel (clés et informations de tunnel envoyées au nœud de service), envoi de journaux (uniquement sur votre clic).
3. Les numéros d'identité, images du visage et numéros de téléphone ne sont ni collectés, ni demandés, ni transmis. Si une vérification d'âge devenait nécessaire, le calcul se ferait uniquement en mémoire, sans enregistrement ni transmission.
4. Autorisations : stockage (lecture/écriture du dossier de jeu, export des journaux), notifications (progression des téléchargements et du tunnel), réseau (téléchargements, IA, multijoueur). Le microphone et la caméra ne sont pas demandés.
5. Vous pouvez consulter, exporter et supprimer vos données locales à tout moment, ou désinstaller l'application pour tout effacer.

8. Modifications et résiliation
Le présent contrat peut évoluer avec les versions ; **un nouvel accord n'est requis qu'en cas de modification substantielle**. Toute violation met fin à la licence automatiquement : cessez l'utilisation et supprimez toutes les copies.

9. Divers
La nullité d'une clause n'affecte pas les autres. En cas de conflit avec une disposition impérative locale, celle-ci prévaut.
""".trimIndent()

internal val LICENSES_FR = """
《Mentions open source tierces + Avertissements de risque complémentaires》

1. Le Logiciel lui-même
GPL-3.0 ; le texte complet figure dans le fichier LICENSE du dépôt. Les mentions de copyright des projets amont ZalithLauncher2 / Fold Craft Launcher / PojavLauncher sont intégralement conservées.

2. Composant aux obligations les plus lourdes
Terracotta (multijoueur) = AGPL-3.0, redistribué sans modification. L'AGPL est plus stricte que la GPL : en cas de modification et de redistribution, ou d'exposition de ses fonctionnalités comme service réseau, le code source correspondant doit être publié.

3. Composants à copyleft faible
MobileGlues (LGPL-2.1), sora-editor / language-textmate (LGPL-2.1), LTW-Turbo (LGPL-3.0), TouchController (LGPL-3.0) — livrés en bibliothèques dynamiques ou modules séparés, remplaçables par vos soins.

4. Composants sous licences permissives
MIT : GL4ES, ByteHook, OpenNBT, skinview3d ; BSD-3-Clause : ANGLE, LWJGL, MMKV ; Zlib : SDL3, sdl2-compat ; EPL-2.0 : tm4e ; 0BSD : XZ for Java ; Apache-2.0 : frp / frpc, AndroidX, Ktor, OkHttp, etc. La liste détaillée et les textes intégraux figurent dans les mentions ci-dessous et dans Paramètres → À propos → Licences open source.

5. Avertissements de risque complémentaires
1. Les sauvegardes, mondes, captures d'écran, réglages et mods peuvent être perdus ou corrompus par un plantage, une coupure de courant, une défaillance du stockage ou un conflit logiciel ;
2. L'appareil peut chauffer, se décharger plus vite, ralentir, ou voir ses processus d'arrière-plan tués par le système ;
3. L'usage de comptes, serveurs ou services tiers peut entraîner bannissement, limitation, frais ou traitement de données ;
4. Des mods ou modpacks de source inconnue peuvent contenir du code malveillant ;
5. Le tunnel expose des ports locaux sur Internet et peut être scanné ou détourné.
Vous évaluez et assumez ces risques ; le Logiciel ne promet ni sauvegarde, ni récupération, ni indemnisation.

6. Ce que vous pouvez faire
Usage et modification personnels : autorisés. Redistribution : autorisée avec licence et mentions de copyright, publication du code source et attribution des projets amont. Usage commercial en code fermé : interdit. Redistribution de composants AGPL modifiés : publication des modifications obligatoire.
""".trimIndent()

internal val GUIDE_FR = """
《Premiers pas + Régions et droit applicable》

1. Premier lancement
Acceptez les trois documents et choisissez votre tranche d'âge. Sur l'écran d'accueil, touchez « Télécharger une instance », choisissez une version (avec Fabric / NeoForge / Quilt si besoin) et attendez la fin du téléchargement.

2. Mods, packs de ressources, shaders, modpacks
La page Téléchargements prend en charge Modrinth / CurseForge et résout les dépendances automatiquement. Vous pouvez aussi déposer les fichiers dans le bon dossier ou utiliser « Partager vers ZyNova ».

3. Multijoueur
Terracotta : les deux joueurs installent ce lanceur et rejoignent la même salle (P2P, aucune ouverture de port). Tunnel Sakura : compte Sakura et clé d'accès requis ; le tunnel est en TCP uniquement, port local 25565 par défaut (modifiable).

4. Assistant IA
Il peut installer le jeu ou des mods, lire les journaux, modifier les réglages et produire des fichiers Word / PPT. La recherche web utilise DeepSeek ou DuckDuckGo ; les suppressions passent par la corbeille.

5. En cas de problème
Paramètres → À propos → Journaux pour consulter et exporter ; après un plantage, ouvrez le rapport ou faites-le analyser par l'IA. Joignez le modèle d'appareil, la version d'Android, la version du lanceur et le journal.

6. Conseils de performance
Gardez la mémoire sous la moitié de la RAM disponible ; essayez d'abord MobileGlues / LTW-Turbo, puis GL4ES en cas d'incompatibilité ; placez les gros modpacks dans le stockage interne avec assez d'espace.

7. Désinstallation et sauvegarde
La désinstallation ne supprime pas les fichiers de jeu du stockage externe ; supprimez les instances depuis l'application. Exportez une sauvegarde de modpack avant une mise à jour.
""".trimIndent()

// ─────────────────────────────────────────────────────────────────
//  本地区补充条款（德语 / 法语）
// ─────────────────────────────────────────────────────────────────

internal val JURIS_TERMS_DE = """
Regionale Ergänzung — {region}

1. Personenbezogene Daten
In dieser Region gilt {law}; Aufsichtsbehörde ist {regulator}. Ihnen stehen die Rechte auf Auskunft, Berichtigung, Löschung, Widerruf der Einwilligung, Einschränkung der Verarbeitung und Beschwerde nach lokalem Recht zu. Standardmäßig werden keine Daten außerhalb Ihres Geräts erhoben; ob Daten das Gerät verlassen, hängt allein von den von Ihnen aktivierten Funktionen Dritter ab.

2. Grenzüberschreitende Übermittlung
Daten werden nur übermittelt, wenn Sie eine Funktion Dritter aktivieren (KI-Modell, Suchmaschine, Mehrspieler-Relay, Tunnel). Die Verarbeitung erfolgt dann durch den jeweiligen Anbieter nach dessen Datenschutzerklärung und liegt außerhalb des Einflusses des Autors.

3. Minderjährige
In dieser Region gilt ein Alter von {age} Jahren als Volljährigkeitsgrenze.{timeWindow}
""".trimIndent()

internal val JURIS_LICENSES_DE = """
Regionale Ergänzung — {region}

1. Wirkung der Lizenzpflichten
Pflichten aus GPL-3.0 / AGPL-3.0 / LGPL werden nach lokalem Recht ausgelegt und durchgesetzt; AGPL-3.0 §13 (Quellcodeoffenlegung bei Angebot als Netzwerkdienst) ist in den meisten urheberrechtlich geprägten Rechtsordnungen durchsetzbar.

2. Exportkontrolle und Sanktionen
Die Software darf nicht in sanktionierten Ländern/Regionen, von sanktionierten Personen oder Einrichtungen oder für Zwecke verwendet werden, die gegen Exportkontroll- oder Sanktionsvorschriften verstoßen.

3. Verfügbarkeit von Diensten Dritter
Verfügbarkeit, Preise und Compliance-Anforderungen von KI-Modellen, Suchmaschinen, Tunneln und Mehrspieler-Relays legt jeder Anbieter selbst fest; in Ihrer Region können sie eingeschränkt oder nicht verfügbar sein.
""".trimIndent()

internal val JURIS_GUIDE_DE = """
Regionale Ergänzung — {region}

1. Anwendbares Recht und Gerichtsstand
Streitigkeiten zwischen Ihnen und diesem Produkt unterliegen dem Recht von {region} und werden von den zuständigen Gerichten an Ihrem Wohnsitz entschieden. Zwingende Verbraucherschutzrechte bleiben unberührt.

2. Minderjährige
In dieser Region gilt ein Alter von {age} Jahren als Volljährigkeitsgrenze.{timeWindow}

3. Spracherkennung und Region
Die Sprache des Textes folgt Ihrer Systemsprache, die Regionalklauseln folgen Systemland und Zeitzone. Beide müssen im unterstützten Bereich liegen, um den Startbildschirm zu erreichen.

4. Wenn Sie fälschlich blockiert werden
Der Sperrbildschirm zeigt die Grundlage (Land / Zeitzone / Sprache). Wenn Sie sich in einer unterstützten Region befinden und dennoch blockiert werden, hängen Sie diese Zeile an ein GitHub-Issue an.
""".trimIndent()

internal val JURIS_TERMS_FR = """
Supplément régional — {region}

1. Données personnelles
Dans cette région, {law} s'applique ; l'autorité de contrôle est {regulator}. Vous disposez des droits d'accès, de rectification, d'effacement, de retrait du consentement, de limitation du traitement et de réclamation prévus par la loi locale. Par défaut, aucune donnée n'est collectée hors de votre appareil ; la sortie de données dépend uniquement des fonctions tierces que vous activez.

2. Transferts transfrontaliers
Les données ne sont transférées que si vous activez une fonction tierce (modèle d'IA, moteur de recherche, relais multijoueur, tunnel). Elles sont alors traitées par le fournisseur concerné selon sa propre politique de confidentialité, hors du contrôle de l'auteur.

3. Mineurs
Dans cette région, l'âge de la majorité retenu est de {age} ans.{timeWindow}
""".trimIndent()

internal val JURIS_LICENSES_FR = """
Supplément régional — {region}

1. Effet des obligations de licence
Les obligations GPL-3.0 / AGPL-3.0 / LGPL sont interprétées et appliquées selon le droit local ; l'article 13 de l'AGPL-3.0 (publication du code source en cas d'offre en tant que service réseau) est opposable dans la plupart des juridictions reconnaissant le droit d'auteur.

2. Contrôle des exportations et sanctions
Le Logiciel ne doit pas être utilisé dans des pays ou régions sanctionnés, par des personnes ou entités sanctionnées, ni à des fins contraires aux règles de contrôle des exportations ou de sanctions.

3. Disponibilité des services tiers
La disponibilité, les tarifs et les exigences de conformité des modèles d'IA, moteurs de recherche, tunnels et relais multijoueurs sont fixés par chaque fournisseur et peuvent être limités ou indisponibles dans votre région.
""".trimIndent()

internal val JURIS_GUIDE_FR = """
Supplément régional — {region}

1. Droit applicable et juridiction
Les litiges entre vous et ce produit sont régis par le droit de {region} et relèvent des tribunaux compétents de votre lieu de résidence. Les droits impératifs des consommateurs restent applicables.

2. Mineurs
Dans cette région, l'âge de la majorité retenu est de {age} ans.{timeWindow}

3. Langue et détection de région
La langue du texte suit votre langue système ; les clauses régionales suivent le pays et le fuseau horaire du système. Les deux doivent être pris en charge pour accéder à l'écran d'accueil.

4. Si vous êtes bloqué à tort
L'écran de blocage affiche le critère retenu (pays / fuseau horaire / langue). Si vous êtes dans une région prise en charge mais bloqué, joignez cette ligne à un ticket GitHub.
""".trimIndent()
