/*
 * ZyNova Launcher — 三份声明的正文（波兰语）
 * GPL-3.0-or-later
 */
package com.movtery.zalithlauncher.onboarding

internal val TERMS_PL = """
《Umowa licencyjna użytkownika końcowego + Polityka prywatności》

1. Akceptacja
Instalując, kopiując lub używając ZyNova Launcher („Oprogramowanie"), potwierdzasz, że przeczytałeś, zrozumiałeś i akceptujesz całą niniejszą umowę. Jeśli nie wyrażasz zgody, zaprzestań korzystania i odinstaluj Oprogramowanie.

2. Licencja i open source
Oprogramowanie jest rozpowszechniane na licencji GNU GPL-3.0, a kod źródłowy jest jawny. Możesz go używać, modyfikować i redystrybuować zgodnie z GPL-3.0; przy redystrybucji musisz dołączyć licencję i informacje o prawach autorskich, opublikować odpowiedni kod źródłowy i nie nakładać dodatkowych ograniczeń. Oprogramowanie jest **nieoficjalnym** launcherem firmy trzeciej, niezwiązanym stosunkiem afiliacji, przedstawicielstwa, sponsorowania ani poparcia z Mojang Studios, Microsoft, NetEase ani ich podmiotami powiązanymi. „Minecraft" jest znakiem towarowym Mojang Synergies AB.

3. Konta i pliki gry
Oprogramowanie nie udostępnia, nie sprzedaje i nie współdzieli kont Minecraft. Potrzebujesz własnego legalnego konta lub trybu offline wyłącznie dla plików gry, które posiadasz legalnie. Prawa do gry, modów, modpacków, paczek zasobów i shaderów należą do ich właścicieli.

4. Brak gwarancji, ograniczenie odpowiedzialności
Oprogramowanie jest dostarczane „tak jak jest", bez gwarancji wyraźnych ani dorozumianych. W maksymalnym zakresie dozwolonym przez prawo autorzy i współtwórcy nie odpowiadają za jakiekolwiek szkody bezpośrednie, pośrednie, przypadkowe, szczególne lub wynikowe powstałe wskutek korzystania lub niemożności korzystania, w tym utratę zapisów, światów lub ustawień, przegrzewanie lub przycinanie urządzenia, blokadę konta oraz zmiany lub wyłączenie usług podmiotów trzecich. Rób własne kopie zapasowe ważnych zapisów.

5. Ograniczenia użytkowania
Nie wolno używać Oprogramowania do działań niezgodnych z prawem, naruszania praw innych osób, atakowania cudzych sieci i urządzeń ani omijania regulaminów platform i środków ochrony nieletnich.

6. Usługi podmiotów trzecich i koszty
Czat AI, wyszukiwanie w sieci, tunel i tryb wieloosobowy zależą od usług podmiotów trzecich i mogą wymagać własnych kluczy i limitów. Koszty i warunki rozstrzygasz z danym dostawcą.

7. Prywatność
1. Domyślnie nie zbieramy ani nie wysyłamy danych osobowych; konta, instancje, ustawienia i logi pozostają na urządzeniu.
2. Dane opuszczają urządzenie tylko wtedy, gdy samodzielnie włączysz te funkcje: AI (wiadomości i parametry narzędzi trafiają do skonfigurowanego dostawcy modelu), wyszukiwanie w sieci (zapytania do wyszukiwarki), tunel (klucze i informacje o tunelu do węzła usługi), wysyłka logów (tylko po naciśnięciu).
3. Numery dokumentów, obrazy twarzy i numery telefonów nie są zbierane, wymagane ani wysyłane. Gdyby w przyszłości potrzebna była weryfikacja wieku, obliczenia odbywałyby się wyłącznie w pamięci, bez zapisu i przesyłania.
4. Uprawnienia: pamięć (odczyt i zapis katalogu gry, eksport logów), powiadomienia (postęp pobierania i tunelu), sieć (pobieranie, AI, tryb wieloosobowy). Mikrofon i kamera nie są wymagane.
5. Możesz w każdej chwili przeglądać, eksportować i usuwać dane lokalne albo odinstalować aplikację, co usuwa je wszystkie.

8. Zmiany i rozwiązanie
Umowa może zmieniać się wraz z wersjami; **ponowna zgoda jest wymagana tylko przy istotnej zmianie treści**. Naruszenie kończy licencję automatycznie: zaprzestań korzystania i usuń wszystkie kopie.

9. Postanowienia końcowe
Nieważność jednego postanowienia nie wpływa na pozostałe. W razie sprzeczności z bezwzględnie obowiązującym prawem miejscowym stosuje się prawo.
""".trimIndent()

internal val LICENSES_PL = """
《Informacje o komponentach open source + Dodatkowe ostrzeżenia o ryzyku》

1. Samo Oprogramowanie
GPL-3.0; pełny tekst w pliku LICENSE repozytorium. Informacje o prawach autorskich projektów źródłowych ZalithLauncher2 / Fold Craft Launcher / PojavLauncher są w całości zachowane.

2. Komponent o najsurowszych obowiązkach
Terracotta (tryb wieloosobowy) = AGPL-3.0, rozpowszechniana bez modyfikacji binariów. AGPL jest surowsza niż GPL: po modyfikacji i redystrybucji albo udostępnieniu jej funkcji jako usługi sieciowej trzeba opublikować odpowiedni kod źródłowy.

3. Komponenty o słabym copylefcie
MobileGlues (LGPL-2.1), sora-editor / language-textmate (LGPL-2.1), LTW-Turbo (LGPL-3.0), TouchController (LGPL-3.0) — dostarczane jako osobne biblioteki dynamiczne lub moduły, które możesz podmienić.

4. Komponenty na licencjach permisywnych
MIT: GL4ES, ByteHook, OpenNBT, skinview3d; BSD-3-Clause: ANGLE, LWJGL, MMKV; Zlib: SDL3, sdl2-compat; EPL-2.0: tm4e; 0BSD: XZ for Java; Apache-2.0: frp / frpc, AndroidX, Ktor, OkHttp i inne. Szczegółowa lista i pełne teksty znajdują się w informacjach poniżej oraz w Ustawienia → O programie → Licencje open source.

5. Dodatkowe ostrzeżenia o ryzyku
1. Zapisy, światy, zrzuty ekranu, ustawienia i mody mogą zostać utracone lub uszkodzone wskutek awarii, braku zasilania, usterki pamięci lub konfliktu z innym oprogramowaniem;
2. Urządzenie może się nagrzewać, szybciej rozładowywać, przycinać, a procesy w tle mogą zostać zatrzymane przez system;
3. Korzystanie z kont, serwerów lub usług podmiotów trzecich może skutkować blokadą, limitami, opłatami lub przetwarzaniem danych;
4. Mody lub modpacki z nieznanych źródeł mogą zawierać złośliwy kod;
5. Tunel wystawia lokalne porty do internetu i może być skanowany lub nadużywany.
Ryzyka te oceniasz i ponosisz samodzielnie; Oprogramowanie nie obiecuje kopii zapasowej, odzyskania danych ani odszkodowania.

6. Co możesz robić
Użytek prywatny i modyfikacje na własne potrzeby: dozwolone. Redystrybucja: dozwolona z licencją i informacjami o prawach autorskich, publikacją kodu i wskazaniem projektów źródłowych. Użytek komercyjny z zamkniętym kodem: niedozwolony. Redystrybucja zmodyfikowanych komponentów AGPL: publikacja zmian obowiązkowa.
""".trimIndent()

internal val GUIDE_PL = """
《Pierwsze kroki + Regiony i prawo właściwe》

1. Pierwsze uruchomienie
Zaakceptuj trzy dokumenty i wybierz grupę wiekową. Na ekranie głównym dotknij „Pobierz instancję", wybierz wersję (opcjonalnie z Fabric / NeoForge / Quilt) i poczekaj na pobranie.

2. Mody, paczki zasobów, shadery i modpacki
Strona Pobierania obsługuje Modrinth / CurseForge i automatycznie rozwiązuje zależności. Możesz też wrzucić pliki do właściwego folderu lub użyć „Udostępnij do ZyNova".

3. Tryb wieloosobowy
Terracotta: obaj gracze instalują ten launcher i wchodzą do tego samego pokoju (P2P, bez przekierowania portów). Tunel Sakura: wymaga konta Sakura i klucza dostępu; tunel działa tylko po TCP, lokalny port domyślnie 25565 (można zmienić).

4. Asystent AI
Może zainstalować grę lub mody, przeczytać logi, zmienić ustawienia i utworzyć pliki Word / PPT. Wyszukiwanie w sieci korzysta z DeepSeek lub DuckDuckGo; usunięte pliki trafiają do kosza.

5. Gdy coś nie działa
Ustawienia → O programie → Logi, aby przejrzeć i wyeksportować; po awarii otwórz raport lub poproś AI o analizę. W zgłoszeniu podaj model urządzenia, wersję Androida, wersję launchera i plik logu.

6. Wskazówki wydajności
Trzymaj pamięć poniżej połowy dostępnego RAM; najpierw wypróbuj MobileGlues / LTW-Turbo, a przy problemach z kompatybilnością GL4ES; duże modpacki trzymaj w pamięci wewnętrznej z zapasem miejsca.

7. Odinstalowanie i kopie zapasowe
Odinstalowanie nie usuwa plików gry w pamięci zewnętrznej; instancje usuwaj w aplikacji. Przed aktualizacją wyeksportuj kopię modpacka.
""".trimIndent()

internal val JURIS_TERMS_PL = """
Suplement regionalny — {region}

1. Dane osobowe
W tym regionie obowiązuje {law}; organem nadzorczym jest {regulator}. Masz prawa dostępu, sprostowania, usunięcia, wycofania zgody, ograniczenia przetwarzania i skargi zgodnie z prawem lokalnym. Domyślnie nic nie jest zbierane poza urządzeniem; o tym, czy dane opuszczą urządzenie, decydują wyłącznie włączone przez Ciebie funkcje podmiotów trzecich.

2. Przekazywanie transgraniczne
Dane są przekazywane tylko wtedy, gdy włączysz funkcję podmiotu trzeciego (model AI, wyszukiwarka, przekaźnik wieloosobowy, tunel). Przetwarza je wtedy dostawca zgodnie z własną polityką prywatności, poza kontrolą autora.

3. Nieletni
W tym regionie pełnoletność przypada na {age} lat.{timeWindow}
""".trimIndent()

internal val JURIS_LICENSES_PL = """
Suplement regionalny — {region}

1. Skuteczność obowiązków licencyjnych
Obowiązki GPL-3.0 / AGPL-3.0 / LGPL są wykładane i egzekwowane według prawa lokalnego; art. 13 AGPL-3.0 (ujawnienie kodu przy udostępnianiu jako usługa sieciowa) jest egzekwowalny w większości jurysdykcji uznających prawo autorskie.

2. Kontrola eksportu i sankcje
Nie wolno używać tego produktu w krajach lub regionach objętych sankcjami, przez osoby lub podmioty objęte sankcjami ani w celach naruszających przepisy o kontroli eksportu i sankcjach.

3. Dostępność usług podmiotów trzecich
Dostępność, ceny i wymogi zgodności modeli AI, wyszukiwarek, tuneli i przekaźników wieloosobowych ustala każdy dostawca i mogą być niedostępne lub ograniczone w Twoim regionie.
""".trimIndent()

internal val JURIS_GUIDE_PL = """
Suplement regionalny — {region}

1. Prawo właściwe i jurysdykcja
Spory między Tobą a tym produktem podlegają prawu {region} i są rozstrzygane przez właściwe sądy Twojego miejsca zamieszkania. Bezwzględnie obowiązujące prawa konsumenta pozostają nienaruszone.

2. Nieletni
W tym regionie pełnoletność przypada na {age} lat.{timeWindow}

3. Język i wykrywanie regionu
Język tekstu zależy od języka systemu, a klauzule regionalne od kraju i strefy czasowej systemu. Oba muszą być obsługiwane, aby wejść na ekran główny.

4. Jeśli zostaniesz zablokowany omyłkowo
Ekran blokady pokazuje zastosowane kryterium (kraj / strefa czasowa / język). Jeśli jesteś w obsługiwanym regionie, a mimo to zablokowany, dołącz ten wiersz do zgłoszenia GitHub.
""".trimIndent()
