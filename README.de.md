<p align="center"><img src="docs/logo.png" alt="Duo Launcher" width="520"></p>

# Duo Launcher

<p align="center"><a href="README.md">English</a> · <a href="README.fr.md">Français</a> · <a href="README.zh-CN.md">简体中文</a> · <a href="README.zh-TW.md">繁體中文</a> · <a href="README.ja.md">日本語</a> · <a href="README.ko.md">한국어</a> · <a href="README.es.md">Español</a> · <b>Deutsch</b> · <a href="README.it.md">Italiano</a> · <a href="README.pt.md">Português</a> · <a href="README.ru.md">Русский</a></p>

Ein Spiele-Launcher für zwei Bildschirme, gemacht für den **Retroid Pocket Duo** und andere Android-Handhelds mit zweitem Display – ganz im Geist des 3DS-Hauptmenüs.
Du gibst ihm deine ROM-Ordner, er findet die Spiele, sortiert sie nach Konsole, Genre und Reihe, zeigt sie unten an, die Grafiken dazu oben, und startet jedes Spiel in dem Emulator, den du festlegst.

**Download:** Die aktuelle APK gibt es auf der [Releases-Seite](https://github.com/GenKnot/DuoLauncher/releases/latest) (ab Android 11). Wenn du eine neue Version über die alte installierst, bleiben alle Einstellungen und deine Spielzeit erhalten.

## Warum noch ein Launcher?

Duo Launcher war anfangs der Launcher eines einzelnen Spielers für einen einzelnen Handheld, und er wächst bis heute aus der Art, wie dieser Handheld tatsächlich benutzt wird. Ein paar Grundgedanken ziehen sich durch alles:

- **Zwei Bildschirme, zwei Aufgaben.** Unten wird ausgewählt: große Ordner, ein Raster, eine Liste, Menüs. Oben wird geschaut: die Konsole vor ihrem eigenen gezeichneten Hintergrund, das Logo eines Spiels über seinem Cover, eine Videovorschau, deine bisherige Spielzeit, eine Notiz an dich selbst oder ein Gespräch mit der KI. Nichts muss sich den Platz mit etwas anderem teilen.
- **Das Gamepad hat Vorrang.** Jede Funktion geht ohne den Bildschirm anzufassen: eine Tastatur fürs Steuerkreuz, gehaltene Richtungen, die weiterscrollen, L / R zum Blättern, L halten für die Sprachsuche, R halten, um mit der KI zu reden. Touch geht auch, ist aber nie nötig.
- **Deine Bibliothek, so wie sie schon ist.** Er liest die Metadaten und Bilder von Pegasus und ES-DE, die du vielleicht schon hast, legt seine eigenen Ergänzungen in einem eigenen Ordner `DuoLauncher` ab und schreibt nie in die Dateien eines anderen Frontends.
- **KI, die für dich arbeitet – mit deinem Schlüssel.** Übersetzung, Sortierung, Bilder, Tipps und Gespräche laufen über den Dienst, den *du* auswählst und direkt bezahlst. Die Schlüssel bleiben auf dem Handheld, und jede Anfrage wird in den Einstellungen mitgezählt, samt geschätzter Kosten – nichts passiert hinter deinem Rücken.
- **Alles lässt sich ändern, ausblenden oder teilen.** Jede Kachel auf der Startseite kann umbenannt, umgefärbt, mit einem neuen Bild versehen, verschoben oder ausgeblendet werden. Deine ganze Einrichtung lässt sich als eine Datei exportieren und an Freunde weitergeben – ohne deine Schlüssel und ohne deinen Spielverlauf.

Vorschläge und Fehlerberichte sind im [Issue-Tracker](https://github.com/GenKnot/DuoLauncher/issues) willkommen.

## Das Wichtigste im Überblick

- **Zwei Bildschirme, ein Launcher** – unten auswählen, oben Logos, Cover, Videovorschauen, Spielzeit und Beschreibungen sehen.
- **Alle Emulatoren an einem Ort** – 3DS, NDS, GBA, GB/GBC, NES, SNES, N64, GameCube, Wii, Mega Drive, PS1, PS2, PSP, PS Vita und Switch, der Emulator wahlweise pro Konsole oder pro Spiel. Spiele mit mehreren Discs erscheinen als ein Eintrag.
- **Schon sortiert** – jedes Spiel landet in einem von 14 Genres und in seiner Reihe (Ys, Castlevania, Super Robot Wars …). Die Startseite zeigt deine Bibliothek nach Konsole, Genre oder Reihe, und intelligente Ordner halten „Neu hinzugefügt“, „Spiele ich“, „Möchte ich spielen“, „Durchgespielt“ und „Am besten bewertet“ von selbst aktuell.
- **Ein KI-Begleiter** – halte überall R gedrückt und sprich: Er empfiehlt Spiele aus deiner eigenen Bibliothek, erklärt, wie du an einer Stelle weiterkommst, erzählt dir etwas über eine Geschichte oder plaudert einfach. Die Antworten können vorgelesen werden.
- **Cheats im Griff** – Tausende fertige Cheats für 3DS, NDS, PSP, GBA und jedes RetroArch-System, mit einem Tippen in deine Sprache übersetzt, bearbeitbar und zwischen Emulatoren geteilt.
- **Ergänzt, was fehlt** – echte Cover aus den libretro-Thumbnail-Sammlungen, offizielle englische Titel und von der KI gezeichnete Cover, Logos und Hintergründe im Stil des Launchers.
- **Gemacht für einen Handheld** – OLED-schonender Bildschirmschoner, WLAN-Übertragung aus dem Browser, Sicherung der Spielstände, Spielstatistik, RetroAchievements-Fortschritt, eine Speicherübersicht und eine Startseite, die in einer Sekunde da ist.
- **Ganz nach deinem Geschmack** – Farbdesigns und Design-Pakete, heller und dunkler Modus, Schriftgröße, Rand um die Cover, eine Einstellungsseite wie bei einer Konsole und Konfigurationen, die du exportieren und teilen kannst. 11 Sprachen für die Oberfläche.

## Screenshots

<p align="center"><img src="docs/screenshots/light-dark.png" alt="Light and dark mode"></p>

<table>
  <tr>
    <th>Startseite nach Genre, mit Genre-Symbolen im Knetlook</th>
    <th>Startseite nach Reihe</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_genres.png" alt="Home by genre, with clay genre icons"></td>
    <td><img src="docs/screenshots/zh_series.png" alt="Home by series"></td>
  </tr>
  <tr>
    <th>Genre und Reihe auf der Spielseite</th>
    <th>Notiz, KI fragen, Status, So wird gespielt, Spielstände</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_game_series.png" alt="Genre and series on the game page"></td>
    <td><img src="docs/screenshots/zh_game_menu.png" alt="Note, Ask AI, status, how to play, saves"></td>
  </tr>
  <tr>
    <th>Gamepad-Tastatur; englische Titel werden auch gefunden</th>
    <th>Die KI fragen, was man spielen soll</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_keyboard.png" alt="Gamepad keyboard; English titles match too"></td>
    <td><img src="docs/screenshots/zh_ai_chat.png" alt="Ask the AI what to play"></td>
  </tr>
  <tr>
    <th>KI-Werkzeuge: Reihen, Empfehlungen, Speicher, Pausen-Erinnerung</th>
    <th>Speicher: Größe pro Konsole, große ungespielte Spiele</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_tools_ai.png" alt="AI tools: series, picks, storage, break reminder"></td>
    <td><img src="docs/screenshots/zh_storage.png" alt="Storage: size per console, unplayed big games"></td>
  </tr>
  <tr>
    <th>Spielseite mit Videovorschau</th>
    <th>Logo, Cover oder Video, pro Spiel</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_game.png" alt="Game page"></td>
    <td><img src="docs/screenshots/en_menu.png" alt="Game menu"></td>
  </tr>
  <tr>
    <th>Listenansicht</th>
    <th>Spielstatistik</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_list.png" alt="List view"></td>
    <td><img src="docs/screenshots/en_stats.png" alt="Play statistics"></td>
  </tr>
  <tr>
    <th>Farbdesigns</th>
    <th>Die Designfarben färben auch die Hintergründe</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_themes.png" alt="Theme menu"></td>
    <td><img src="docs/screenshots/en_pink.png" alt="Pink theme"></td>
  </tr>
  <tr>
    <th>Dunkler Modus</th>
    <th>Dunkler Modus</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_d3ds.png" alt="Dark mode, 3DS"></td>
    <td><img src="docs/screenshots/en_dstats.png" alt="Dark mode, statistics"></td>
  </tr>
  <tr>
    <th>WLAN-Übertragung</th>
    <th>Werkzeuge zum Aufräumen</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_wifi.png" alt="Wi-Fi transfer"></td>
    <td><img src="docs/screenshots/en_tools.png" alt="Tools"></td>
  </tr>
  <tr>
    <th>GameCube, mit Dolphin</th>
    <th>A halten, um einen Ordner zu verschieben</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_gamecube.png" alt="GameCube"></td>
    <td><img src="docs/screenshots/zh_move.png" alt="Moving a folder"></td>
  </tr>
  <tr>
    <th>Cheats vor dem Import auswählen, Namen schon übersetzt</th>
    <th>Spieldetails: Größe, Discs, Screenshots</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheat_pick.png" alt="Choosing cheats"></td>
    <td><img src="docs/screenshots/zh_details.png" alt="Game details"></td>
  </tr>
  <tr>
    <th>3DS-Cheats, mit einem Tippen übersetzt</th>
    <th>NDS-Cheats für DraStic, chinesische Datenbank</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheats.png" alt="3DS cheats"></td>
    <td><img src="docs/screenshots/zh_cheats_nds.png" alt="NDS cheats"></td>
  </tr>
  <tr>
    <th>RetroArch-Cheats (GBA)</th>
    <th>Bearbeiten, hinzufügen, herunterladen</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheats_gba.png" alt="GBA cheats"></td>
    <td><img src="docs/screenshots/zh_cheat_menu.png" alt="Cheat menu"></td>
  </tr>
  <tr>
    <th>Einen Cheat umbenennen oder löschen</th>
    <th>Alle Cheats auf einmal übersetzen</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheat_edit.png" alt="Edit a cheat"></td>
    <td><img src="docs/screenshots/zh_tools.png" alt="Tools"></td>
  </tr>
  <tr>
    <th>Bildschirmschoner (OLED-schonend)</th>
    <th>11 Sprachen</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_saver.png" alt="Screensaver"></td>
    <td><img src="docs/screenshots/en_lang.png" alt="Languages"></td>
  </tr>
</table>

## Erste Schritte

1. **Installiere die APK** von der [Releases-Seite](https://github.com/GenKnot/DuoLauncher/releases/latest) und öffne sie.
2. **Wähle eine Sprache** und erteile dann auf einer einzigen Seite die abgefragten Berechtigungen: Dateizugriff (damit deine Spiele gefunden werden), Nutzungsdatenzugriff (damit als Spielzeit nur die Zeit zählt, in der ein Spiel wirklich auf dem Bildschirm ist) und, wenn du magst, die Rolle als Startbildschirm. Zu dieser Seite kommst du jederzeit zurück über „Einstellungen“ → „System“ → „Berechtigungen“.
3. **Lass ihn deine Spiele finden.** Jeder Ordner `Roms` im internen Speicher oder auf einer SD-Karte wird von selbst erkannt. Weitere Ordner fügst du unter „Einstellungen“ → „Start und Bibliothek“ → „Spielordner“ hinzu. Die Konsolen werden an den Ordnernamen (`gba`, `Nintendo DS`, `psx` …) und an den Dateiendungen erkannt.
4. **Wähle Emulatoren**, wo du etwas anderes als den Standard willst: Drücke Y auf einem Konsolenordner für die ganze Konsole oder auf einem Spiel nur für dieses Spiel. Angeboten werden nur Emulatoren, die auch installiert sind.
5. **Optional: Trag einen KI-Schlüssel ein** unter „Einstellungen“ → „KI und Sprache“ – direkt auf dem Handheld oder bequemer vom Computer oder Handy aus übers WLAN („Einstellungen“ → „Spielstände, Übertragung, Teilen“ → „WLAN-Übertragung“). Alles außer den KI-Funktionen funktioniert auch ohne.
6. **Optional: Mach ihn zu deinem Startbildschirm** („Einstellungen“ → „System“ → „Als Startbildschirm festlegen“), dann startet er zusammen mit dem Handheld. Die Kachel „Android-Startbildschirm“ führt immer zurück zum Startbildschirm des Systems.

## Die Grundlagen

**Die Startseite** (unterer Bildschirm) enthält deine Konsolenordner und ein paar eingebaute Kacheln: „Weiterspielen“ (direkt zurück ins zuletzt gespielte Spiel), „Zuletzt gespielt“, „Favoriten“, „Neuer Ordner“, „Suchen“, „Spielstatistik“, „Speicher“, „Was spielen?“, „Einstellungen“ und „Android-Startbildschirm“. Mit SELECT schaltest du die Startseite zwischen Konsolen, Genres und Reihen um.

**Ein Ordner** zeigt seine Spiele als Raster aus Covern. X macht die Cover kleiner und verwandelt das Raster beim nächsten Mal in eine Liste mit Namen, Spielzeit und dem Zeitpunkt, an dem du zuletzt gespielt hast. SELECT sortiert („Standard“, „Name“, „Zuletzt gespielt“, „Meistgespielt“) und kann auf Veröffentlichungen in vereinfachtem Chinesisch, traditionellem Chinesisch oder Englisch filtern. L2 / R2 springen zum vorherigen oder nächsten Anfangsbuchstaben – chinesische Titel zählen dabei nach dem Anfangsbuchstaben ihres Pinyins.

**Der obere Bildschirm** folgt dem, was gerade ausgewählt ist: eine Konsole vor ihrem Hintergrund mit Spieleanzahl und Emulator; das Logo eines Spiels über seinem unscharfen Cover mit Konsole, Genre · Reihe, Emulator, Spielzeit und einer kurzen Beschreibung, nach einem Moment eine Videovorschau oder das Bild der Stelle, an der du aufgehört hast.

**Y** öffnet das Menü für das, was ausgewählt ist – bei einem Spiel: Emulator, Favorit, Ordner, Cheats, Details, Genre, Notiz, KI fragen, Status und Wertung, So wird gespielt, Spielstände, was der obere Bildschirm zeigt, Ausblenden. Bei einem Konsolenordner: Standard-Emulator, Umbenennen, Ordner zusammenführen, Ausblenden. Bei einer eingebauten Kachel: Name, Farbe, Symbol und Hintergrund.

### Steuerung

| Taste | Funktion |
|---|---|
| Steuerkreuz / linker Stick | Bewegen; halten, um weiterzulaufen |
| Links / rechts, L / R in einer Liste oder einem Menü | Seitenweise blättern |
| A | Öffnen / spielen; A auf einem Ordner der Startseite halten, um ihn aufzunehmen und zu verschieben |
| B | Zurück |
| L / R | Vorherige / nächste Konsole (in den Einstellungen: Kategorie) |
| L halten | Sprachsuche |
| R halten | Mit der KI sprechen |
| L2 / R2 | Vorheriger / nächster Anfangsbuchstabe |
| X | Covergröße, danach Listenansicht |
| Y | Menü für das ausgewählte Spiel, den Ordner oder die Kachel |
| START | Einstellungen (von überall auf der Startseite, auch wenn die Kachel „Einstellungen“ ausgeblendet ist); in einem Menü wie A |
| SELECT | Auf der Startseite: Konsolen / Genres / Reihen; in einem Ordner: Sortierung und Sprachfilter |
| R3 | Suchen |
| In der Suchtastatur | A tippt, B löscht, X Leerzeichen, Y System-Tastatur, SELECT leert, START oder R3 fertig, L halten zum Sprechen |
| Im KI-Chat | R halten zum Sprechen, Y zum Tippen, L2 / R2 zum Zurückblättern, B zum Schließen; mit Steuerkreuz und A wählst du aus den Spielen darunter |

Alles geht auch per Touch: antippen zum Auswählen, noch mal antippen zum Öffnen, wischen zum Scrollen, und statt L, R, X, SELECT oder Suche tippst du einfach die Schaltflächen am oberen Rand an. A/B und X/Y lassen sich tauschen, je nachdem, ob du die Bestätigungstaste von Nintendo oder von Xbox gewohnt bist („Einstellungen“ → „Spielen“ → „Tasten“).

## Deine Bibliothek

- **Findet deine Spiele von selbst.** Interner Speicher und SD-Karten werden nach Ordnern namens `Roms` durchsucht; weitere fügst du unter „Einstellungen“ → „Start und Bibliothek“ → „Spielordner“ hinzu. Der letzte Scan wird zwischengespeichert, deshalb ist die Startseite in einer Sekunde da, und der nächste Scan läuft still im Hintergrund.
- **Konsolen** – 3DS, NDS, GBA (inklusive der Ordner für GBA mit Rumble und GBA-Hacks), GBC, GB, NES, SNES, N64, GameCube, Wii, Mega Drive, PlayStation 1/2/3, PSP, PS Vita und Switch, dazu Grafiken für PS4, PS5, Xbox 360 und Xbox Series.
- **Mehrere Discs als ein Spiel.** `Disc A/B`, `Disc 1/2`, `CD1/CD2`, `(Disc 2 of 3)` im selben Ordner oder eine Pegasus-Liste `files:` werden zu einem einzigen Eintrag. RetroArch und DuckStation bekommen eine `.m3u`-Playlist der Discs, damit du im Spiel die Disc wechseln kannst.
- **Android-Spiele und Apps** – installierte Spiele bekommen automatisch einen eigenen Ordner; andere Apps (Video, Browser …) kannst du in einen Ordner „Apps“ aufnehmen.
- **Arbeitet mit dem, was du schon hast.** Er liest Pegasus-`metadata.pegasus.txt`, `gamelist.xml` und `downloaded_media` von ES-DE / EmulationStation, RetroArch-Thumbnails sowie Ordner `media/`, `covers/` und `images/` neben den Spielen. An deren Dateien wird nichts verändert; die eigenen Ergänzungen des Launchers kommen in seinen eigenen Ordner `DuoLauncher` (siehe [Deine eigenen Spielinfos](#deine-eigenen-spielinfos)).
- **Bemerkt neue Spiele.** Kopiere Spiele auf die Karte, und der nächste Scan findet sie; sie tragen eine Woche lang (oder bis du sie spielst) ein NEU-Abzeichen, und du wirst gefragt, ob sie in Genres und Reihen einsortiert werden sollen. Diese Frage lässt sich abschalten.
- **Ordner zusammenführen.** Zwei Ordner derselben Konsole von verschiedenen Orten (etwa zwei PS1-Ordner) lassen sich als einer anzeigen: Y → „Ordner zusammenführen“ auf einer Konsole. Jedes Spiel behält seinen eigenen Emulator, Ordner mit demselben Namen werden von selbst zusammengeführt, und „… abtrennen“ macht es wieder rückgängig.
- **Eigene Ordner.** Die Kachel „Neuer Ordner“ legt einen Ordner mit eigenem Namen, eigener Farbe, eigenem Symbol und Hintergrund an. Mit Y fügst du ihm beliebige Spiele oder Apps hinzu, mit Y → „Aus diesem Ordner entfernen“ nimmst du sie wieder heraus.
- **Intelligente Ordner** füllen sich selbst: „Neu hinzugefügt“ (die letzten 30 Tage), „Noch nicht gespielt“, „Meistgespielt“, „Spiele ich“, „Möchte ich spielen“, „Durchgespielt“ und „Am besten bewertet“. Y auf einem davon zeigt seine Regel.
- **Weiterspielen und Favoriten.** Die erste Kachel der Startseite bringt dich direkt zurück ins letzte Spiel. Markiere Spiele mit Y → „Zu Favoriten“, um sie im Ordner „Favoriten“ zu sammeln.
- **Blende aus, was du nicht spielst** – einen Konsolenordner oder ein einzelnes Spiel bzw. eine App (Y → „Dieses Spiel ausblenden“); zurückholen kannst du sie unter „Einstellungen“ → „Start und Bibliothek“.
- **Dein Katalog.** Nach jedem Scan wird die ganze Bibliothek in `DuoLauncher/catalog.tsv` geschrieben (Konsole, Datei, Titel, englischer Titel, Genre, Reihe, Spielzeit, Status, Wertung) – für Tabellenkalkulationen, andere Werkzeuge oder die KI.

### Genres und Reihen

Zweitausend Spiele nur nach Konsole durchzublättern wird schnell mühsam, deshalb bekommt jedes Spiel zusätzlich eines von 14 Genres (Rollenspiel, Action, Adventure, Jump 'n' Run, Taktik, Strategie, Shooter, Puzzle, Rennspiel, Sport, Kampfspiel, Simulation, Party & Musik, Sonstige) und seine Reihe.

- **Zuerst das eingebaute Wissen.** Eine Tabelle im Launcher (`assets/gamedb.tsv`, 3.860 Dateinamen, die alle Spiele der Pegasus-G-Paketlisten für diese Konsolen abdecken – mit dem `.iso`- und dem `.cso`-Namen, wo ein Paket beide hat) kennt Genre, Reihe und offiziellen englischen Titel gängiger ROM-Sets schon anhand von Konsole und Dateiname. Für Spiele, die sie kennt, braucht es weder KI noch Internet.
- **KI für den Rest.** „Einstellungen“ → „KI und Sprache“ → „KI: nach Genre ordnen“ / „KI: Spielreihen finden“ ordnet die übrigen Spiele ein. Die Ergebnisse werden in `DuoLauncher/<console>/genres.txt` und `series.txt` auf der Karte gespeichert – das passiert also nur einmal und übersteht auch eine Neuinstallation. Genres, die Pegasus oder ES-DE schon eingetragen haben („Action RPG“, „角色扮演“ …), werden auf die 14 Genres abgebildet.
- **Von Hand** – Y → „Genre“ bei jedem Spiel.
- **Auf dem Bildschirm.** Der obere Bildschirm zeigt Genre · Reihe eines Spiels, und SELECT auf der Startseite macht aus den Ordnern Genres oder Reihen. Die Namen der Reihen folgen der Sprache der Oberfläche.

### Suche

R3, die Kachel „Suchen“ oder die Suchschaltfläche öffnet oben eine Tastatur, die fürs Steuerkreuz gemacht ist, darüber deine letzten Suchen und eine Reihe Genre-Chips; die Treffer füllen schon beim Tippen den unteren Bildschirm. Chinesische Titel passen auf ihr Pinyin, dessen Anfangsbuchstaben und den Anfang jeder Silbe (`kdyg`, `mlao` → 马里奥), und jedes Spiel passt außerdem auf seinen offiziellen englischen Titel, sodass `zelda` auch 塞尔达传说 findet. Öffnest du die Suche aus einer Konsole heraus, sucht sie nur in dieser Konsole; die Schaltfläche „Alle Plattformen“ erweitert sie. Halte L, um einen Namen zu sagen, statt ihn zu tippen.

### Bilder, die sich selbst ergänzen

- **Echte Cover** – „Einstellungen“ → „Aufräumen“ → „Cover herunterladen“ lädt Cover aus den libretro-Thumbnail-Sammlungen. NDS- und GBA-Spiele werden über den Spielcode im ROM zugeordnet (auch gezippte ROMs, mit Titelbildschirmen), so finden auch übersetzte und umbenannte Dateien ihre Bilder; Spiele für PlayStation 1/2, PSP, N64, GameCube, Wii, 3DS, GB/GBC, NES, SNES und Mega Drive werden über ihren englischen Titel zugeordnet.
- **Symbole direkt aus den ROMs** – NDS-Banner-Symbole und 3DS-SMDH-Symbole; installierte 3DS-Titel zeigen ihren Namen in der Sprache der Oberfläche.
- **Cover, die zusammenpassen** – die Cover füllen ihre Kacheln oder sitzen alle mit demselben kleinen, mittleren oder großen Rand („Einstellungen“ → „Aussehen und Ton“ → „Rand um Cover“); weiße Rahmen, die fest im Coverbild stecken, werden abgeschnitten.
- **KI-Bilder** – mit einem Bild-Schlüssel lassen sich Cover, Titel-Logos und Hintergründe für Spiele zeichnen, die keine haben (Y → „Details“ → „Cover mit KI erstellen“, oder für eine ganze Konsole auf einmal unter „Einstellungen“ → „KI und Sprache“ → „KI-Bilder“). Siehe [KI](#ki).

## Spielen

- **Emulator pro Spiel oder pro Konsole** (Y). Angeboten werden nur installierte Emulatoren:

  | Konsole | Emulatoren (der erste ist der Standard) |
  |---|---|
  | 3DS | AzaharPlus, Azahar, Citra MMJ, Citra MMJ (Leistungsmodus) |
  | NDS | DraStic, DraStic (einfacher Pfad), RetroArch · melonDS DS |
  | GB / GBC | RetroArch · Gambatte, mGBA |
  | GBA | RetroArch · mGBA, gpSP, gpSP Rumble |
  | NES | RetroArch · FCEUmm, Mesen |
  | SNES | RetroArch · Snes9x |
  | Mega Drive | RetroArch · Genesis Plus GX, PicoDrive |
  | N64 | RetroArch · Mupen64Plus-Next, Mupen64Plus FZ |
  | GameCube / Wii | Dolphin MMJR, Dolphin |
  | PS1 | RetroArch · PCSX ReARMed, SwanStation, DuckStation |
  | PS2 | ARMSX2, AetherSX2 |
  | PSP | PPSSPP, RetroArch · PPSSPP |
  | PS Vita | Vita3K (über die Title-ID, z. B. eine Datei namens `PCSE00000.psvita` oder ein Name, der sie enthält) |
  | Switch | Eden |

- **Citra MMJ (Leistungsmodus)** – derselbe Citra-MMJ-Build, neu verpackt unter dem Paketnamen des Benchmarks AnTuTu, den viele Android-Systeme mit vollem Takt laufen lassen. Er wird nur angeboten, wenn hinter diesem Paket wirklich Citra steckt.
- **Spielzeit und Statistik** – wie lange und wann du jedes Spiel zuletzt gespielt hast, wie bei der Switch; mit Nutzungsdatenzugriff zählt nur die Zeit, in der das Spiel wirklich auf dem Bildschirm war. Die Kachel „Spielstatistik“ zeigt die Gesamtzeit, diese Woche Tag für Tag, die Zeit pro Konsole und die meistgespielten Spiele.
- **Wo du aufgehört hast** – der neueste Screenshot oder das Bild des neuesten Savestates eines gespielten Spiels wird zu seinem Hintergrund auf dem oberen Bildschirm.
- **Status, Wertung und Notizen** – markiere ein Spiel als „Spiele ich“, „Möchte ich spielen“, „Durchgespielt“ oder „Abgebrochen“ und gib ihm ein bis fünf Sterne (Y → „Status“); schreib oder sprich eine Zeile dazu, wo du gerade bist und was als Nächstes ansteht (Y → „Notiz“). All das steht beim nächsten Mal, wenn du das Spiel auswählst, auf dem oberen Bildschirm.
- **Spieldetails** (Y → „Details“) – Datei, Größe, Discs, Ordner, Emulator, Spielzeit, der offizielle englische Titel und die Screenshots des Spiels auf dem oberen Bildschirm.
- **Videovorschauen und Sounds** – nach einem Moment startet oben das Video eines Spiels (oder du wählst pro Spiel Logo oder Cover: Y → „Oberer Bildschirm“). Die dezenten Klänge der Oberfläche und die eigene Banner-Melodie jedes 3DS-Spiels lassen sich abschalten.
- **Wenn ein Spiel nicht startet** – eine fehlende Datei, ein nicht installierter Emulator, einer, der diese Art Datei nicht öffnen kann, oder ein Spiel, das sich sofort wieder schließt: Für jeden Fall gibt es eine Meldung, die sagt, was zu tun ist.
- **Pausen-Erinnerung** („Einstellungen“ → „Spielen“) – nach einer, zwei oder drei Stunden in einem Spiel schlägt der Launcher eine Pause vor, wenn du zurückkommst.
- **RetroAchievements** – mit deinem Benutzernamen und Web-API-Schlüssel siehst du zuletzt gespielte Spiele mit ihrem Erfolgsfortschritt, und Spiele mit Erfolgen bekommen ein 🏆.

## Dein KI-Begleiter

All das ist optional und nutzt den KI-Dienst, den du mit deinem eigenen Schlüssel auswählst (siehe [KI](#ki)).

- **Sprich überall mit ihm.** Halte R auf der Startseite, in einem Ordner oder in der Suchtastatur und sprich – oder drück Y zum Tippen. Das Gespräch füllt den oberen Bildschirm; die Spiele, die er empfiehlt oder findet, füllen den unteren und lassen sich direkt mit A starten. Frag, was du spielen sollst („ein entspanntes RPG, nicht zu lang“), grenz es weiter ein („etwas mit mehr Action“), frag, wie du an der Stelle im ausgewählten Spiel weiterkommst, nach einer Geschichte oder einer Figur, oder plaudere einfach. Er merkt sich das Gespräch eine halbe Stunde lang.
  *Warum:* Unten werden Spiele ausgewählt, also landen die Vorschläge der KI dort; oben ist Platz zum Lesen. Und auf einem Handheld geht eine Schultertaste halten und reden schneller als jedes Menü.
- **Antworten zum Vorlesen** – „Einstellungen“ → „KI und Sprache“ → „KI-Sprachantworten“: Soniox (mit eigenem Schlüssel) oder OpenAI (mit dem Bild-Schlüssel), mit Auswahl von Stimme und Sprechtempo und einer Schaltfläche zum Anhören. Aus, solange du es nicht einschaltest.
- **Was spiele ich heute** – Y auf der Kachel „Was spielen?“ lässt die KI sechs Spiele aus deiner Bibliothek aussuchen, die zu deinem Geschmack passen, jeweils mit Begründung. Ein einfacher Druck auf die Kachel wählt ein zufälliges Spiel, bevorzugt eines, das du noch nie gespielt hast.
- **Nach einem Spiel fragen** (Y → „KI fragen“) – eine Frage zum ausgewählten Spiel, getippt oder gesprochen, beantwortet auf dem oberen Bildschirm.
- **So wird gespielt** (Y → „So wird gespielt“) – die Steuerung (inklusive der vertauschten Bestätigen- und Abbrechen-Tasten japanischer Versionen) und die Grundlagen, die Neulinge brauchen, einmal pro Spiel geschrieben und aufbewahrt.
- **Noch kein Schlüssel?** Wenn du R hältst, wird erklärt, wie du einen einträgst – auf dem Handheld oder vom Computer übers WLAN.

## Cheats

Y → „Cheats“ bei einem Spiel listet seine Cheats auf; A schaltet einen an oder aus.

| | 3DS (Azahar, Citra) | NDS (DraStic) | PSP (PPSSPP) | RetroArch (GBA, GB, GBC, NES, SNES, N64, MD, PS1, …) |
|---|---|---|---|---|
| Wo sie liegen | `azahar/cheats/` und `citra-emu/cheats/`, `<title ID>.txt` – für beide geschrieben, ein Emulatorwechsel behält sie also | die ganze Liste in `DuoLauncher/cheats/NDS/`; die eingeschalteten landen in DraStics eigenen benutzerdefinierten Cheats (`DraStic/cheats/<game file name>.cht`), die auch bei übersetzten ROMs funktionieren | `PSP/Cheats/<game ID>.ini`, die eigenen Cheat-Dateien von PPSSPP; die Spiel-ID wird aus der ISO, CSO oder EBOOT.PBP gelesen | `RetroArch/cheats/<core>/<game file name>.cht`, von RetroArch beim Spielstart geladen |
| Schon auf dem Gerät | – | DraStics `usrcheat.dat` (auf Retroid-Handhelds auf Chinesisch), nur über den Spielcode zugeordnet – so finden gepatchte und übersetzte ROMs ihre Cheats auch dort, wo DraStic selbst keine zeigt | PPSSPPs `cheat.db` und auf Retroid-Handhelds chinesische Cheat-Dateien für rund 4.300 Spiele | Retroid liefert chinesische Cheats für Hunderte Spiele mit (`000 中文金手指` und die Core-Ordner); ein Spiel ohne eigene Datei bekommt eine Kopie, über den Namen zugeordnet |
| Download | [Sharkive](https://github.com/FlagBrew/Sharkive), rund 650 Spiele, über die aus dem ROM gelesene Title-ID | [libretro-database](https://github.com/libretro/libretro-database), rund 4.200 Spiele, über den Spielcode im ROM-Header | [CWCheat-Database-Plus+](https://github.com/Saramagrean/CWCheat-Database-Plus-), rund 4.300 Spiele, über die Spiel-ID | [libretro-database](https://github.com/libretro/libretro-database) für GBA, über den Spielcode im ROM-Header |

- **Vor dem Import auswählen** – ein Download listet zuerst auf, was er gefunden hat, schon übersetzt, mit „Alle wählen“ / „Keine wählen“; hinzugefügt werden nur die angehakten. *Warum:* Eine Datenbank kann Dutzende Codes pro Spiel enthalten, und die meisten Spieler wollen drei davon.
- **Übersetzen** – englische Cheat-Namen werden mit einem Tippen in die Sprache der Oberfläche übertragen, für ein Spiel oder für alle Spiele auf einmal („Einstellungen“ → „Aufräumen“). Gängige Cheats kommen aus einem eingebauten Glossar (无限血量, 金钱最大, 主代码 …); der Rest geht an den kostenlosen Dienst MyMemory, mit Googles kostenlosem Endpunkt als zweitem Versuch. Versionskennungen (`v1.2`) und Tastenhinweise (`(START+L)`) bleiben erhalten, und der ursprüngliche englische Name bleibt als Kommentar in der Datei.
- **Mit KI übersetzen** – mit einem KI-Schlüssel gehen die Cheats eines Spiels zusammen mit dem Spielnamen ans Modell, sodass es die offiziellen Namen von Figuren und Gegenständen verwendet (鬼神林克, 心之容器 …).
- **Bearbeiten** – jeden Cheat umbenennen oder löschen, alle Cheats eines Spiels auf einmal löschen oder eigene mit ihren Codes hinzufügen (3DS-, NDS- und PSP-Codes werden auf das Format `XXXXXXXX YYYYYYYY` geprüft).
- **Sicher für DraStic** – Flashcart-Patches (Umgehungen des Kopierschutzes, Overlay- und WLAN-Fixes) werden bei NDS-Downloads weggelassen, weil DraStic abstürzt, wenn sie eingeschaltet sind, und es können höchstens 256 Codezeilen gleichzeitig aktiv sein – mehr kann DraStic nicht laden.
- **Sicher für deine Dateien** – wenn eine von Hand geschriebene 3DS-Cheat-Datei zum ersten Mal geändert wird, bleibt das Original als `.txt.bak` erhalten.
- **RetroArch, einmalig** – schalte in RetroArch einmal „Schnellmenü“ → „Cheats“ → „Beim Laden automatisch anwenden“ („Auto-Apply Cheats During Game Load“) ein, dann sind die Cheats, die du hier einschaltest, ab dem Moment aktiv, in dem ein Spiel startet.

## Optik und Bedienung

- **Der obere Bildschirm (1920×1080)** – jede Konsole vor ihrem eigenen Scherenschnitt-Hintergrund, oder das Logo des ausgewählten Spiels über seinem unscharfen Cover, mit Tags und einer kurzen Beschreibung; Uhrzeit und Akku sitzen in der Ecke.
- **Der untere Bildschirm (1280×960)** – Konsolenordner, ein zoombares Cover-Raster und eine Listenansicht, mit Tastenhinweisen am unteren Rand, die kurz bleiben und weglassen, was bei großer Schrift nicht mehr passt.
- **Designs** – sieben Farbschemata, eigene Farben und Design-Pakete mit Konsolenbildern, Hintergründen, einer Schrift, Klängen und Musik. Ein Design färbt auch die Strichzeichnungen im Hintergrund des oberen Bildschirms. Siehe [Designs](#designs).
- **Heller und dunkler Modus** – folgt dem System oder wird von dir festgelegt, mit eigens gezeichneten Hintergründen für jeden Modus.
- **Alles auf der Startseite gehört dir.** Jede eingebaute Kachel und jeder Konsolenordner lässt sich umbenennen, umfärben, mit eigenem Symbol versehen (ein eigenes Bild oder eines, das der Bilddienst im Knetstil des Launchers zeichnet), verschieben (A halten oder lange antippen) oder ausblenden und unter „Einstellungen“ → „Start und Bibliothek“ → „Ausgeblendete Ordner“ wieder zurückholen. Eingebaute Kacheln wie „Spielstatistik“, „Speicher“ und „Was spielen?“ können auch einen eigenen Hintergrund für den oberen Bildschirm bekommen.
- **Aussehen der Konsolen** – Bild, heller und dunkler Hintergrund sowie Ordnerfarbe jeder Konsole lassen sich ersetzen („Einstellungen“ → „Aussehen und Ton“ → „Plattform-Aussehen“). Ein dunkler Hintergrund kann vom Bilddienst aus dem hellen neu gezeichnet werden, damit die beiden immer zusammenpassen.
- **Schriftgröße** – vier Größen für beide Bildschirme.
- **Bildschirmschoner** – nach ein paar Minuten ohne Eingabe zeigt der obere Bildschirm deine Cover mit einer Uhr, und der untere wird schwarz. Der Text wechselt mit jedem Bild die Ecke und wandert langsam, damit sich nichts in ein OLED-Panel einbrennt.

## Einstellungen und Teilen

- **Eine Einstellungsseite wie bei einer Konsole.** START öffnet eine einzige Seite: links die Kategorien („Aussehen und Ton“, „Start und Bibliothek“, „Spielen“, „KI und Sprache“, „Spielstände, Übertragung, Teilen“, „Aufräumen“, „System“), rechts die Zeilen der gewählten Kategorie mit ihren aktuellen Werten und Schaltern, und oben die Version, der freie Platz auf jedem Laufwerk und welche KI-Dienste aktiv sind. L / R wechseln die Kategorie.
- **Teile deine Einrichtung.** „Einstellungen“ → „Spielstände, Übertragung, Teilen“ → „Meine Konfiguration exportieren“ schreibt ein einziges Zip mit allen Einstellungen (Aussehen, Aufbau und Reihenfolge der Startseite, Ordner, Ausgeblendetes, Emulatorwahl, KI-Dienste) und deinen eigenen Bildern. „Konfiguration importieren“ holt eine aus `DuoLauncher/exports` oder dem Download-Ordner – egal ob per WLAN, Chat-App oder Speicherkarte geschickt.
  *Warum das Teilen sicher ist:* Schlüssel, Passwörter und Spielverlauf kommen nie in eine Konfiguration, beim Import bleiben deine eigenen erhalten, und ein Schlüssel von dir wird nie an eine Adresse geschickt, die aus der Konfiguration von jemand anderem stammt.
- **11 Sprachen** – 简体中文, 繁體中文, English, 日本語, 한국어, Français, Español, Deutsch, Italiano, Português, Русский.

## Nützliche Werkzeuge

- **WLAN-Übertragung** – „Einstellungen“ → „Spielstände, Übertragung, Teilen“ → „WLAN-Übertragung“ zeigt eine Adresse und eine PIN. Öffne die Adresse in einem Browser im selben Netzwerk, gib die PIN ein und zieh ROMs oder Design-Pakete direkt in einen beliebigen Konsolenordner. Auf derselben Seite liegen hinter einem Admin-Passwort (sechs zufällige Ziffern oder ein eigenes) die Einstellungen für KI, Sprache und RetroAchievements, sodass du lange Schlüssel am Computer einfügen kannst. Der Bildschirm des Handhelds bleibt an, solange die Übertragung läuft, denn bei ausgeschaltetem Bildschirm ist die Seite nicht erreichbar.
- **Speicher** – eine Kachel auf der Startseite zeigt, wie voll die Karte ist, und oben den freien Platz jedes Laufwerks. Darin: die Größe der Spiele jeder Konsole, die größten nie gespielten Spiele und das Löschen der Dateien eines Spiels (alle Discs und die Dateien, die eine `.cue` oder `.m3u` nennt) nach einer Rückfrage.
- **Sicherung der Spielstände** – sichere und stelle die Spielstände von DraStic, RetroArch, PPSSPP, AetherSX2, DuckStation, Mupen64Plus FZ, Azahar und Citra in einem einzigen Zip wieder her, abgelegt in `DuoLauncher/backups` auf der SD-Karte (im internen Speicher, wenn keine Karte drin ist). Nach drei Stunden Spielzeit ohne Sicherung erinnert dich der Launcher daran oder legt, wenn du willst, selbst eine an (die zehn neuesten automatischen Sicherungen bleiben erhalten; deine eigenen werden nie entfernt). Beim Wiederherstellen wird jeder Spielstand erst ersetzt, wenn seine Kopie vollständig ist. Y → „Spielstände“ sichert und stellt ein einzelnes Spiel für sich wieder her.
- **Aufräumen** – doppelte Spiele finden, Spiele ohne Bilder auflisten, fehlende Cover herunterladen, alle Cheats auf einmal übersetzen und eine Diagnoseseite; die KI-Werkzeuge (Namen und Beschreibungen für eine ganze Konsole, Genres, Reihen, Bilder, Genre-Symbole) findest du unter „KI und Sprache“.
- **Updates** – „Einstellungen“ → „System“ → „Info“ prüft einmal am Tag das neueste GitHub-Release und verlinkt darauf.
- **Startbildschirm** – leg ihn als Standard-Startbildschirm-App fest, dann startet er mit dem Handheld; die Kachel „Android-Startbildschirm“ (oder „Einstellungen“ → „System“ → „Zum Android-Startbildschirm“) führt zurück zum Startbildschirm des Systems, und „Einstellungen“ → „System“ → „Android-Einstellungen“ öffnet die Einstellungen des Systems selbst.

## KI

Alles hier ist optional und läuft über dein eigenes Konto. Die Schlüssel liegen in den Einstellungen des Launchers auf dem Handheld und werden nur an den Dienst geschickt, den du ausgewählt hast. Eintragen kannst du sie unter „Einstellungen“ → „KI und Sprache“ oder auf der WLAN-Seite vom Computer oder Handy aus.

- **KI-Übersetzung** – DeepSeek, Qwen, OpenAI, Claude, Gemini oder eine beliebige OpenAI-kompatible Adresse. Dieser Schlüssel übernimmt alles, was mit Text zu tun hat: den Chat, die Spielvorschläge, „KI fragen“, „So wird gespielt“, Genres und Reihen, Cheat-Namen (ein ganzes Spiel in einer Anfrage) sowie Spielnamen und Beschreibungen in deiner Sprache (in die eigenen Metadaten des Launchers geschrieben, zusammen mit dem offiziellen englischen Titel – so liest sich eine Bibliothek voller chinesischer Titel ganz natürlich auf Englisch und umgekehrt). Ohne Schlüssel werden Cheats über kostenlose Dienste übersetzt.
- **KI-Bilder** – ein OpenAI-Bild-Schlüssel (das neueste Bildmodell, das der Schlüssel nutzen darf, wird automatisch gewählt). Cover, Titel-Logos, Hintergründe, Konsolenbilder, Ordner- und Kachelsymbole sowie Hintergründe, alle im selben Knet- und Scherenschnitt-Stil wie die eingebauten Grafiken; Symbole werden mit transparentem Hintergrund gezeichnet, damit ihre weißen Stellen erhalten bleiben. Ein Bild dauert etwa eine halbe Minute. Derselbe Schlüssel verwandelt deine Stimme in Text (`/audio/transcriptions`), weil der Handheld keine eigene Spracherkennung hat.
- **KI-Sprachantworten** – Soniox mit eigenem Schlüssel oder OpenAI mit dem Bild-Schlüssel; Stimme und Sprechtempo („Normal“, „Zügig“, „Schnell“ – standardmäßig „Zügig“, da beide Chinesisch recht langsam vorlesen).
- **KI-Nutzung und Kosten** – jede Anfrage des Launchers wird auf dem Handheld gezählt: Anfragen, Tokens, Bilder und Sprechminuten, mit geschätzten Kosten für heute, diese Woche, diesen Monat und insgesamt. Die Schätzung nutzt die veröffentlichten Preise der Dienste für das, was du verwendest; jeder Preis lässt sich auf deinen eigenen setzen (ein anderes Modell, ein Wiederverkäufer, ein Rabatt). Maßgeblich ist die Rechnung deines Anbieters.

## Deine eigenen Spielinfos

Leg einen Ordner `DuoLauncher` im Hauptverzeichnis der SD-Karte (oder des internen Speichers) an, um Namen, Beschreibungen und Bilder zu ergänzen oder zu korrigieren, ohne die Dateien anderer Frontends anzufassen. Was hier steht, hat Vorrang vor Pegasus, ES-DE und den Daten des Spiels selbst.

```
DuoLauncher/
  3DS/metadata.txt
  3DS/media/<file name without extension>/logo.png   (also cover.png, background.png)
  NDS/metadata.txt
```

`metadata.txt` verwendet das Pegasus-Format. Einträge werden über `file:` zugeordnet, bei in einen Emulator installierten 3DS-Spielen über `titleid:`:

```
game: 勇者派遣会社
titleid: 0004000000065600
description: …
```

Namen und Beschreibungen kannst du pro Sprache mit `name.<language>:` und `description.<language>:` angeben (`en`, `zh-CN`, `zh-TW`, `ja`, `ko`, `fr`, `es`, `de`, `it`, `pt`, `ru`; `zh` gilt für beide Chinesisch-Varianten). Sie werden verwendet, solange der Launcher in dieser Sprache läuft, sodass ein Ordner Spielern mehrerer Sprachen dienen kann:

```
game: 塞尔达传说 姆祖拉的假面
file: 塞尔达传说 姆祖拉的假面 汉化版.cci
name.en: The Legend of Zelda: Majora's Mask 3D
description.en: Link has three days before the moon falls on Termina…
```

## Designs

„Einstellungen“ → „Aussehen und Ton“ → „Design“ bietet sieben eingebaute Farbdesigns („Himmelblau“, „Kirschrot“, „Mintgrün“, „Traubenlila“, „Mandarine“, „Sakura-Rosa“, „Graphit“), **eigene Farben** (wähl eine Hauptfarbe und eine Hintergrundfarbe; alle Abstufungen für hellen und dunklen Modus werden daraus berechnet) und alle installierten Design-Pakete. Ein Design färbt beide Bildschirme, auch die Strichzeichnungen im Hintergrund des oberen Bildschirms.

Ein Design-Paket ist ein Ordner (oder ein `.zip` davon, das beim ersten Gebrauch entpackt wird) in `DuoLauncher/themes/` auf der SD-Karte oder im internen Speicher. Alles darin ist optional; was ein Design weglässt, kommt aus dem eingebauten Aussehen – ein Design kann also auch nur aus ein paar Farben bestehen.

```
DuoLauncher/themes/my-theme/
  theme.txt                      name, author, colours (see below)
  consoles/<type>.png            console pictures      (3ds, nds, gba, ..., recent, search, settings, custom)
  backgrounds/<type>.jpg         upper-screen backdrops
  backgrounds/dark/<type>.jpg    backdrops for dark mode
  logo.png
  font.ttf, font-bold.ttf
  sounds/move|open|back|launch.ogg   (or .wav / .mp3)
  music.ogg (or music.mp3)       loops on the home screen (Settings → Background music appears when a theme has music)
```

Zur Baumstruktur oben: `theme.txt` enthält Name, Autor und Farben; `consoles/` die Konsolenbilder, `backgrounds/` die Hintergründe für den oberen Bildschirm und `backgrounds/dark/` die für den dunklen Modus. `music.ogg` läuft in Schleife auf der Startseite (die Einstellung „Hintergrundmusik“ erscheint, sobald ein Design Musik hat).

`theme.txt` besteht aus Zeilen der Form `key: value`: `color.<name>` für den hellen und `dark.color.<name>` für den dunklen Modus, mit Namen wie `accent`, `shelf-top`, `shelf-bottom`, `card`, `text`, `pill`, `footer`, `backdrop-paper` / `backdrop-ink` (die Strichzeichnungen oben) und `folder.<type>` für die Ordner der Startseite. `base-color: #RRGGBB` allein reicht schon für ein komplettes Farbschema. [themes/sakura](themes/sakura/theme.txt) ist ein vollständiges Beispiel, in dem jeder Schlüssel erklärt wird. „Einstellungen“ → „Spielstände, Übertragung, Teilen“ → „Aktuelles Design exportieren“ speichert alles, was du eingestellt hast, als Zip in `DuoLauncher/exports/`, das andere einfach in ihren eigenen Ordner `themes/` legen können; deine eigenen Bilder kommen unter `own/` mit.

## Selbst bauen

Kein Android Studio und kein Gradle nötig – nur die Kommandozeilen-Tools des Android SDK (build-tools 35, Plattform android-35) und JDK 17 oder neuer:

```powershell
powershell -File build.ps1            # builds DuoLauncher.apk
powershell -File build.ps1 -Install   # also installs over adb, grants file access and starts it
```

Die erste Zeile baut `DuoLauncher.apk`; mit `-Install` wird sie zusätzlich per adb installiert, bekommt Dateizugriff und wird gestartet. Ein Debug-Signaturschlüssel (`debug.keystore`) wird beim ersten Build erzeugt und nicht eingecheckt. `shot.ps1` schickt Tastendrücke über adb und speichert Screenshots beider Bildschirme – praktisch, wenn man an der Oberfläche arbeitet.

## Grafiken

`assets/consoles/<type>.png` (Konsolen- und Kachelsymbole im Knetlook) und `assets/backgrounds/<type>.jpg` (Scherenschnitt-Hintergründe in Strichzeichnung, mit eigenen Versionen für den dunklen Modus in `backgrounds/dark/`) wurden für dieses Projekt gemacht. Die Hintergründe sind in Graustufen gehalten, damit ein Farbdesign sie einfärben kann. Konsolen ohne eigenes Bild bekommen stattdessen eine gezeichnete Konsole.

## Hinweise zum Start der Emulatoren

- AzaharPlus akzeptiert nur eine Tree-URI unterhalb eines Ordners, auf den es schon Zugriff hat:
  `content://com.android.externalstorage.documents/tree/<volume>%3ARoms%2F3DS/document/<volume>%3ARoms%2F3DS%2F<file>`
- Citra MMJ: `org.citra.emu/org.citra.emu.ui.EmulationActivity` mit `-e GamePath <path>`.
- Citra MMJ (Leistungsmodus) ist derselbe Build, neu verpackt als `com.antutu.ABenchMark`; viele Android-Builds lassen diesen Paketnamen mit vollem CPU-/GPU-Takt laufen. Er wird nur angeboten, wenn dieses Paket wirklich Citra enthält.
- DraStic in der Fassung, die auf Retroid-Handhelds mitgeliefert wird, erwartet `-e GAMEPATH "/sf<path>"`; für unveränderte Builds gibt es die Variante „einfacher Pfad“.
- RetroArch: `com.retroarch.aarch64/...RetroActivityFuture` mit `-e ROM`, `-e LIBRETRO <core>` und `-e CONFIGFILE`. Die in der Startzeile genannte Konfigurationsdatei wird beibehalten: Auf dem Duo zeigt RetroArchs eigene `retroarch.cfg` Retroids Rahmen pro System in voller Größe, während Retroids bildschirmspezifische `retroarch1920x1080.cfg` die Spiele klein in einem TV-Rahmen darstellt.
- Dolphin (MMJR und offiziell): `.../ui.main.MainActivity` mit `--es AutoStartFile <path>`.
- ARMSX2, AetherSX2, Eden, PPSSPP und Mupen64Plus FZ bekommen das Spiel als schreibgeschützten Link `content://com.duo.launcher.rom/...` mit einer einmaligen Leseberechtigung und brauchen deshalb keinen eigenen Ordnerzugriff.
- Vita3K: `org.vita3k.emulator/.Emulator` mit dem String-Array-Extra `AppStartParameters = ["-r", "<title ID>"]`.
- Startzeilen aus Pegasus-Metadaten werden unverändert übernommen.

### Citra MMJ auf einem Handheld mit zwei Bildschirmen

Citra MMJ legt den Touchscreen standardmäßig auf das obere Display. Damit das Hauptbild oben liegt und die Bildschirmtasten verschwinden, beende Citra und trage in `/sdcard/citra-emu/config/config-mmj.ini` Folgendes ein:

```ini
[Controls]
input_overlay_hide = True
[Layout]
landscape_swap_screen = True
```

## Feedback

Duo Launcher entwickelt sich danach, wie er benutzt wird. Wenn etwas fehlt, unklar ist oder nicht funktioniert – oder wenn du ein Design gebaut hast, das sich zu teilen lohnt –, eröffne ein [Issue](https://github.com/GenKnot/DuoLauncher/issues).

## Lizenz

[MIT](LICENSE)
