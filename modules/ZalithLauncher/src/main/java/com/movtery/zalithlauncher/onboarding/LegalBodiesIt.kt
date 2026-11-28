/*
 * ZyNova Launcher — 三份声明的正文（意大利语）
 * GPL-3.0-or-later
 *
 * 意大利语：正式体（Lei），术语按意大利软件本地化惯例。
 */
package com.movtery.zalithlauncher.onboarding

internal val TERMS_IT = """
《Contratto di licenza con l'utente finale + Informativa sulla privacy》

1. Accettazione
Installando, copiando o utilizzando ZyNova Launcher («il Software») lei conferma di aver letto, compreso e accettato integralmente il presente contratto. In caso contrario, interrompa l'uso e disinstalli il Software.

2. Licenza e open source
Il Software è distribuito con licenza GNU GPL-3.0 e il codice è pubblico. Può usarlo, modificarlo e ridistribuirlo nel rispetto della GPL-3.0; in caso di ridistribuzione deve allegare licenza e avvisi di copyright, pubblicare il codice corrispondente e non imporre restrizioni aggiuntive. Il Software è un launcher di terze parti **non ufficiale**, senza alcun rapporto di affiliazione, rappresentanza, sponsorizzazione o approvazione con Mojang Studios, Microsoft, NetEase o le loro controllate. «Minecraft» è un marchio di Mojang Synergies AB.

3. Account e file di gioco
Il Software non fornisce, vende né condivide account Minecraft. Deve disporre di un proprio account legittimo oppure usare la modalità offline solo per file di gioco che possiede legalmente. I diritti su gioco, mod, modpack, pacchetti di risorse e shader appartengono ai rispettivi titolari.

4. Nessuna garanzia, limitazione di responsabilità
Il Software è fornito «così com'è», senza garanzie esplicite o implicite. Nei limiti massimi consentiti dalla legge, autori e collaboratori non rispondono di danni diretti, indiretti, incidentali, speciali o consequenziali derivanti dall'uso o dall'impossibilità d'uso, inclusa la perdita di salvataggi, mondi o impostazioni, surriscaldamento o rallentamenti del dispositivo, sospensione degli account e modifiche o chiusura di servizi di terzi. Esegua personalmente il backup dei salvataggi importanti.

5. Restrizioni d'uso
Non può usare il Software per attività illecite, per violare diritti di terzi, per attaccare reti o dispositivi altrui, né per eludere i termini di servizio di una piattaforma o le misure di protezione dei minori.

6. Servizi di terze parti e costi
Chat IA, ricerca web, tunnel e multigiocatore si basano su servizi di terzi e possono richiedere chiavi e quote proprie. Costi e condizioni sono risolti tra lei e il rispettivo fornitore.

7. Privacy
1. Per impostazione predefinita nessun dato personale viene raccolto o inviato; account, istanze, impostazioni e log restano sul dispositivo.
2. I dati lasciano il dispositivo solo se lei attiva queste funzioni: IA (messaggi e parametri degli strumenti al fornitore di modello configurato), ricerca web (query al motore di ricerca), tunnel (chiavi e informazioni del tunnel al nodo del servizio), invio dei log (solo su suo tocco).
3. Numeri di documento, immagini del volto e numeri di telefono non vengono raccolti, richiesti né inviati. Se in futuro servisse una verifica dell'età, il calcolo avverrebbe solo in memoria, senza salvataggio né trasmissione.
4. Autorizzazioni: archiviazione (lettura/scrittura della cartella di gioco, esportazione dei log), notifiche (avanzamento di download e tunnel), rete (download, IA, multigiocatore). Microfono e fotocamera non vengono richiesti.
5. Può consultare, esportare ed eliminare i dati locali in qualsiasi momento, oppure disinstallare l'app per rimuoverli tutti.

8. Modifiche e cessazione
Il contratto può cambiare con le versioni; **un nuovo consenso è richiesto solo in caso di modifiche sostanziali**. La violazione estingue la licenza automaticamente: interrompa l'uso ed elimini tutte le copie.

9. Varie
La nullità di una clausola non pregiudica le altre. In caso di conflitto con norme imperative locali, prevale la legge.
""".trimIndent()

internal val LICENSES_IT = """
《Note sui componenti open source + Ulteriori avvisi di rischio》

1. Il Software stesso
GPL-3.0; il testo completo è nel file LICENSE del repository. Gli avvisi di copyright dei progetti di origine ZalithLauncher2 / Fold Craft Launcher / PojavLauncher sono integralmente conservati.

2. Componente con gli obblighi più stringenti
Terracotta (multigiocatore) = AGPL-3.0, ridistribuito senza modifiche. L'AGPL è più restrittiva della GPL: se lo modifica e lo ridistribuisce, o ne offre le funzionalità come servizio di rete, deve pubblicare il codice sorgente corrispondente.

3. Componenti con copyleft debole
MobileGlues (LGPL-2.1), sora-editor / language-textmate (LGPL-2.1), LTW-Turbo (LGPL-3.0), TouchController (LGPL-3.0) — distribuiti come librerie dinamiche o moduli separati, sostituibili dall'utente.

4. Componenti con licenze permissive
MIT: GL4ES, ByteHook, OpenNBT, skinview3d; BSD-3-Clause: ANGLE, LWJGL, MMKV; Zlib: SDL3, sdl2-compat; EPL-2.0: tm4e; 0BSD: XZ for Java; Apache-2.0: frp / frpc, AndroidX, Ktor, OkHttp e altri. L'elenco dettagliato e i testi completi sono nelle note seguenti e in Impostazioni → Informazioni → Licenze open source.

5. Ulteriori avvisi di rischio
1. Salvataggi, mondi, screenshot, impostazioni e mod possono andare persi o danneggiarsi per crash, mancanza di corrente, guasti di archiviazione o conflitti con altri software;
2. Il dispositivo può surriscaldarsi, consumare più batteria, rallentare o subire la chiusura dei processi in background;
3. L'uso di account, server o servizi di terzi può comportare sospensioni, limitazioni, addebiti o trattamento dei dati;
4. Mod o modpack di origine sconosciuta possono contenere codice malevolo;
5. Il tunnel espone porte locali su Internet e può essere scansionato o abusato.
Lei valuta e assume questi rischi; il Software non promette backup, recupero o risarcimento.

6. Cosa può fare
Uso e modifica personali: consentiti. Ridistribuzione: consentita con licenza e avvisi di copyright, pubblicazione del codice e attribuzione ai progetti di origine. Uso commerciale a codice chiuso: non consentito. Ridistribuzione di componenti AGPL modificati: pubblicazione delle modifiche obbligatoria.
""".trimIndent()

internal val GUIDE_IT = """
《Primi passi + Regioni e legge applicabile》

1. Primo avvio
Accetti i tre documenti e scelga la fascia d'età. Nella schermata iniziale tocchi «Scarica un'istanza», scelga una versione (facoltativamente con Fabric / NeoForge / Quilt) e attenda il download.

2. Mod, pacchetti di risorse, shader e modpack
La pagina Download supporta Modrinth / CurseForge e risolve le dipendenze automaticamente. Può anche inserire i file nella cartella corretta o usare «Condividi con ZyNova».

3. Multigiocatore
Terracotta: entrambi i giocatori installano questo launcher ed entrano nella stessa stanza (P2P, nessuna apertura di porte). Tunnel Sakura: servono account Sakura e chiave di accesso; il tunnel è solo TCP e la porta locale predefinita è 25565 (modificabile).

4. Assistente IA
Può installare gioco o mod, leggere i log, modificare impostazioni e creare file Word / PPT. La ricerca web usa DeepSeek o DuckDuckGo; i file eliminati finiscono nel cestino.

5. Se qualcosa non funziona
Impostazioni → Informazioni → Log per consultare ed esportare; dopo un crash apra il report o chieda all'IA di analizzarlo. Nella segnalazione includa modello del dispositivo, versione di Android, versione del launcher e file di log.

6. Consigli sulle prestazioni
Tenga la memoria sotto la metà della RAM disponibile; provi prima MobileGlues / LTW-Turbo e passi a GL4ES in caso di incompatibilità; collochi i modpack grandi nella memoria interna con spazio sufficiente.

7. Disinstallazione e backup
La disinstallazione non cancella i file di gioco nella memoria esterna; elimini le istanze dall'app. Esporti un backup del modpack prima di aggiornare.
""".trimIndent()

internal val JURIS_TERMS_IT = """
Supplemento regionale — {region}

1. Dati personali
In questa regione si applica {law}; l'autorità di controllo è {regulator}. Lei ha diritto di accesso, rettifica, cancellazione, revoca del consenso, limitazione del trattamento e reclamo secondo la legge locale. Per impostazione predefinita nulla viene raccolto fuori dal dispositivo; se i dati lo lasciano dipende solo dalle funzioni di terzi che attiva.

2. Trasferimento transfrontaliero
I dati vengono trasferiti solo se attiva una funzione di terzi (modello IA, motore di ricerca, relay multigiocatore, tunnel). In tal caso sono trattati dal fornitore secondo la sua informativa privacy, fuori dal controllo dell'autore.

3. Minori
In questa regione la maggiore età è fissata a {age} anni.{timeWindow}
""".trimIndent()

internal val JURIS_LICENSES_IT = """
Supplemento regionale — {region}

1. Efficacia degli obblighi di licenza
Gli obblighi GPL-3.0 / AGPL-3.0 / LGPL sono interpretati ed eseguiti secondo la legge locale; l'art. 13 della AGPL-3.0 (pubblicazione del codice quando il software è offerto come servizio di rete) è azionabile nella maggior parte delle giurisdizioni che riconoscono il diritto d'autore.

2. Controllo delle esportazioni e sanzioni
Non è consentito usare questo prodotto in paesi o regioni sanzionati, da persone o entità sanzionate, né per scopi contrari alle norme sul controllo delle esportazioni o sulle sanzioni.

3. Disponibilità dei servizi di terzi
Disponibilità, prezzi e requisiti di conformità di modelli IA, motori di ricerca, tunnel e relay multigiocatore sono decisi da ciascun fornitore e possono essere indisponibili o limitati nella sua regione.
""".trimIndent()

internal val JURIS_GUIDE_IT = """
Supplemento regionale — {region}

1. Legge applicabile e foro competente
Le controversie tra lei e questo prodotto sono regolate dalla legge di {region} e decise dai tribunali competenti del suo domicilio. I diritti inderogabili del consumatore previsti dalla legge locale restano fermi.

2. Minori
In questa regione la maggiore età è fissata a {age} anni.{timeWindow}

3. Lingua e rilevamento della regione
La lingua del testo segue la lingua di sistema; le clausole regionali seguono paese e fuso orario di sistema. Entrambi devono essere supportati per accedere alla schermata principale.

4. Se viene bloccato per errore
La schermata di blocco mostra il criterio applicato (paese / fuso orario / lingua). Se si trova in una regione supportata ma viene bloccato, alleghi quella riga a una Issue su GitHub.
""".trimIndent()
