<p align="center"><img src="docs/logo.png" alt="Duo Launcher" width="520"></p>

# Duo Launcher

<p align="center"><a href="README.md">English</a> · <a href="README.fr.md">Français</a> · <a href="README.zh-CN.md">简体中文</a> · <a href="README.zh-TW.md">繁體中文</a> · <a href="README.ja.md">日本語</a> · <a href="README.ko.md">한국어</a> · <a href="README.es.md">Español</a> · <a href="README.de.md">Deutsch</a> · <b>Italiano</b> · <a href="README.pt.md">Português</a> · <a href="README.ru.md">Русский</a></p>

Un launcher di giochi a doppio schermo per il **Retroid Pocket Duo** e le altre console portatili Android con un secondo display, sullo stile del menu Home del 3DS.
Gli indichi le cartelle delle ROM e lui trova i giochi, li ordina per console, genere e serie, li mostra sullo schermo inferiore con le immagini su quello superiore e avvia ogni gioco con l'emulatore che hai scelto.

**Download:** l'APK più recente è nella [pagina delle release](https://github.com/GenKnot/DuoLauncher/releases/latest) (Android 11 o successivo). Installando una nuova versione sopra la vecchia mantieni tutte le impostazioni e il tempo di gioco.

## Perché un altro launcher?

Duo Launcher è nato come il launcher di un giocatore per una sola console portatile, e continua a crescere da come quella console si usa davvero. Alcune idee lo attraversano tutto:

- **Due schermi, due compiti.** Lo schermo inferiore serve a scegliere: cartelle grandi, una griglia, un elenco, i menu. Quello superiore serve a guardare: la console sul suo sfondo illustrato, il logo di un gioco sopra la copertina, un'anteprima video, quanto hai giocato, una nota per te stesso o una conversazione con l'IA. Nessuno deve dividere lo spazio con qualcos'altro.
- **Prima di tutto il gamepad.** Ogni funzione si usa senza toccare lo schermo: una tastiera pensata per la croce direzionale, direzioni che continuano a scorrere se le tieni premute, L / R per cambiare pagina, L tenuto premuto per cercare a voce, R tenuto premuto per parlare con l'IA. Il touch funziona lo stesso, solo che non è obbligatorio.
- **La tua libreria, così com'è già.** Legge i metadati e le immagini di Pegasus ed ES-DE che magari hai già, tiene le sue aggiunte nella sua cartella `DuoLauncher` e non riscrive mai i file di un altro frontend.
- **Un'IA che lavora per te, con la tua chiave.** Traduzioni, ordinamento, immagini, consigli e conversazioni usano il servizio che scegli *tu* e che paghi direttamente. Le chiavi restano sulla console e ogni richiesta viene contata nelle impostazioni con un costo stimato, così niente succede alle tue spalle.
- **Tutto si può cambiare, nascondere o condividere.** Ogni riquadro della Home si può rinominare, ricolorare, spostare, nascondere o dotare di una nuova immagine. L'intera configurazione si può esportare in un unico file da passare a un amico, senza le tue chiavi e senza la tua cronologia di gioco.

Suggerimenti e segnalazioni di bug sono benvenuti nell'[issue tracker](https://github.com/GenKnot/DuoLauncher/issues).

## In breve

- **Due schermi, un launcher**: scegli sullo schermo inferiore; su quello superiore vedi loghi, copertine, anteprime video, tempo di gioco e descrizioni.
- **Tutti gli emulatori in un posto solo**: 3DS, NDS, GBA, GB/GBC, NES, SNES, N64, GameCube, Wii, Mega Drive, PS1, PS2, PSP, PS Vita e Switch, con l'emulatore scelto per console o per singolo gioco. I giochi su più dischi compaiono come uno solo.
- **Già in ordine**: ogni gioco finisce in uno di 14 generi e nella sua serie (Ys, Castlevania, Super Robot Wars …). La Home mostra la libreria per console, per genere o per serie, e le cartelle smart tengono aggiornati «Aggiunti di recente», «In corso», «Da giocare», «Finito» e «I più votati».
- **Un compagno IA**: tieni premuto R ovunque e parla. Ti consiglia giochi della tua libreria, ti spiega come superare un punto difficile, ti racconta una trama o fa due chiacchiere. Le risposte possono essere lette ad alta voce.
- **Trucchi sotto controllo**: migliaia di trucchi pronti per 3DS, NDS, PSP, GBA e tutti i sistemi RetroArch, tradotti nella tua lingua con un tocco, modificabili e condivisi tra emulatori.
- **Completa ciò che manca**: vere copertine dai set di miniature di libretro, titoli ufficiali in inglese e copertine, loghi e sfondi disegnati dall'IA nello stile del launcher.
- **Fatto per una portatile**: salvaschermo sicuro per gli OLED, trasferimento Wi-Fi dal browser, backup dei salvataggi, statistiche di gioco, progressi RetroAchievements, una panoramica dello spazio e una Home pronta in un secondo.
- **Tuo da modificare**: temi di colore e pacchetti di temi, modalità chiara e scura, dimensione del testo, margini delle copertine, una pagina delle impostazioni come quella di una console e configurazioni da esportare e condividere. 11 lingue dell'interfaccia.

## Screenshot

<p align="center"><img src="docs/screenshots/light-dark.png" alt="Light and dark mode"></p>

<table>
  <tr>
    <th>Impostazioni in stile 3DS</th>
    <th>Impostazioni in modalità scura</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_settings.png" alt="Impostazioni in stile 3DS"></td>
    <td><img src="docs/screenshots/en_settings_dark.png" alt="Impostazioni in modalità scura"></td>
  </tr>
  <tr>
    <th>Uso e costo dell'IA</th>
    <th>Esporta e importa la configurazione</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_ai_usage.png" alt="Uso e costo dell'IA"></td>
    <td><img src="docs/screenshots/en_config.png" alt="Esporta e importa la configurazione"></td>
  </tr>
  <tr>
    <th colspan="2">Sfondi disegnati a mano per la modalità scura</th>
  </tr>
  <tr>
    <td colspan="2" align="center"><img src="docs/screenshots/en_dark_home.png" width="50%" alt="Sfondi disegnati a mano per la modalità scura"></td>
  </tr>
  <tr>
    <th>Home per genere, con le icone dei generi in plastilina</th>
    <th>Home per serie</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_genres.png" alt="Home by genre, with clay genre icons"></td>
    <td><img src="docs/screenshots/zh_series.png" alt="Home by series"></td>
  </tr>
  <tr>
    <th>Genere e serie nella pagina del gioco</th>
    <th>Nota, Chiedi all'IA, stato, come si gioca, salvataggi</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_game_series.png" alt="Genre and series on the game page"></td>
    <td><img src="docs/screenshots/zh_game_menu.png" alt="Note, Ask AI, status, how to play, saves"></td>
  </tr>
  <tr>
    <th>Tastiera per gamepad; trova anche i titoli inglesi</th>
    <th>Chiedi all'IA a cosa giocare</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_keyboard.png" alt="Gamepad keyboard; English titles match too"></td>
    <td><img src="docs/screenshots/zh_ai_chat.png" alt="Ask the AI what to play"></td>
  </tr>
  <tr>
    <th>Strumenti IA: serie, consigli, spazio, promemoria pausa</th>
    <th>Spazio: dimensione per console, giochi grandi mai giocati</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_tools_ai.png" alt="AI tools: series, picks, storage, break reminder"></td>
    <td><img src="docs/screenshots/zh_storage.png" alt="Storage: size per console, unplayed big games"></td>
  </tr>
  <tr>
    <th>Pagina del gioco con anteprima video</th>
    <th>Logo, copertina o video, per ogni gioco</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_game.png" alt="Game page"></td>
    <td><img src="docs/screenshots/en_menu.png" alt="Game menu"></td>
  </tr>
  <tr>
    <th>Vista a elenco</th>
    <th>Statistiche di gioco</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_list.png" alt="List view"></td>
    <td><img src="docs/screenshots/en_stats.png" alt="Play statistics"></td>
  </tr>
  <tr>
    <th>Temi di colore</th>
    <th>I colori del tema arrivano fino agli sfondi</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_themes.png" alt="Theme menu"></td>
    <td><img src="docs/screenshots/en_pink.png" alt="Pink theme"></td>
  </tr>
  <tr>
    <th>Modalità scura</th>
    <th>Modalità scura</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_d3ds.png" alt="Dark mode, 3DS"></td>
    <td><img src="docs/screenshots/en_dstats.png" alt="Dark mode, statistics"></td>
  </tr>
  <tr>
    <th>Trasferimento Wi-Fi</th>
    <th>Strumenti per fare ordine</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_wifi.png" alt="Wi-Fi transfer"></td>
    <td><img src="docs/screenshots/en_tools.png" alt="Tools"></td>
  </tr>
  <tr>
    <th>GameCube, con Dolphin</th>
    <th>Tieni premuto A per spostare una cartella</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_gamecube.png" alt="GameCube"></td>
    <td><img src="docs/screenshots/zh_move.png" alt="Moving a folder"></td>
  </tr>
  <tr>
    <th>Scegli i trucchi prima di importarli, nomi già tradotti</th>
    <th>Dettagli del gioco: dimensione, dischi, screenshot</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheat_pick.png" alt="Choosing cheats"></td>
    <td><img src="docs/screenshots/zh_details.png" alt="Game details"></td>
  </tr>
  <tr>
    <th>Trucchi 3DS, tradotti con un tocco</th>
    <th>Trucchi NDS per DraStic, database cinese</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheats.png" alt="3DS cheats"></td>
    <td><img src="docs/screenshots/zh_cheats_nds.png" alt="NDS cheats"></td>
  </tr>
  <tr>
    <th>Trucchi RetroArch (GBA)</th>
    <th>Modifica, aggiungi, scarica</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheats_gba.png" alt="GBA cheats"></td>
    <td><img src="docs/screenshots/zh_cheat_menu.png" alt="Cheat menu"></td>
  </tr>
  <tr>
    <th>Rinomina o elimina un trucco</th>
    <th>Traduci tutti i trucchi in una volta</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheat_edit.png" alt="Edit a cheat"></td>
    <td><img src="docs/screenshots/zh_tools.png" alt="Tools"></td>
  </tr>
  <tr>
    <th>Salvaschermo (sicuro per OLED)</th>
    <th>11 lingue</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_saver.png" alt="Screensaver"></td>
    <td><img src="docs/screenshots/en_lang.png" alt="Languages"></td>
  </tr>
</table>

## Per iniziare

1. **Installa l'APK** dalla [pagina delle release](https://github.com/GenKnot/DuoLauncher/releases/latest) e aprilo.
2. **Scegli una lingua**, poi concedi in un'unica pagina le autorizzazioni richieste: l'accesso ai file (per trovare i giochi), l'accesso ai dati di utilizzo (così il tempo di gioco conta solo quando il gioco è davvero sullo schermo) e, se vuoi, il ruolo di schermata Home. Puoi tornare su questa pagina quando vuoi da «Impostazioni → Sistema → Autorizzazioni».
3. **Lascia che trovi i tuoi giochi.** Qualsiasi cartella `Roms` nella memoria interna o su una scheda SD viene rilevata da sola. Altre cartelle si aggiungono in «Impostazioni → Home e libreria → Cartelle dei giochi». Le console vengono riconosciute dai nomi delle cartelle (`gba`, `Nintendo DS`, `psx` …) e dalle estensioni dei file.
4. **Scegli gli emulatori** dove vuoi qualcosa di diverso da quello predefinito: premi Y su una cartella console per tutta la console, o su un gioco solo per quel gioco. Vengono proposti solo gli emulatori installati.
5. **Facoltativo: aggiungi una chiave IA** in «Impostazioni → IA e voce», direttamente sulla console o, più comodamente, da un computer o da un telefono via Wi-Fi («Impostazioni → Salvataggi, trasferimento, condivisione → Trasferimento Wi-Fi»). Tutto tranne le funzioni IA funziona anche senza.
6. **Facoltativo: rendilo la tua schermata Home** («Impostazioni → Sistema → Imposta come schermata Home») così parte insieme alla console. Il riquadro «Home di Android» riporta sempre alla schermata Home di sistema.

## Le basi

**La Home** (schermo inferiore) contiene le cartelle delle console e alcuni riquadri integrati: «Continua» (per tornare subito all'ultimo gioco), «Giocati di recente», «Preferiti», «Nuova cartella», «Cerca», «Statistiche», «Spazio», «A cosa giocare?», «Impostazioni» e «Home di Android». SELECT passa dalla vista per console a quella per genere o per serie.

**Una cartella** mostra i suoi giochi come una griglia di copertine. X rimpicciolisce le copertine e, premuto ancora, trasforma la griglia in un elenco con nomi, tempo di gioco e data dell'ultima partita. SELECT ordina (predefinito, nome, giocati di recente, più giocati) e può mostrare solo le edizioni in cinese semplificato, cinese tradizionale o inglese. L2 / R2 saltano alla lettera iniziale precedente o successiva; i titoli cinesi contano con l'iniziale del pinyin.

**Lo schermo superiore** segue quello che hai selezionato: una console sul suo sfondo con il numero di giochi e l'emulatore; il logo di un gioco sopra la copertina sfocata, con console, genere · serie, emulatore, tempo di gioco e una breve descrizione, dopo un attimo un'anteprima video oppure l'immagine del punto in cui ti eri fermato.

**Y** apre il menu dell'elemento selezionato. Su un gioco: emulatore, preferito, cartelle, trucchi, dettagli, genere, nota, chiedi all'IA, stato e voto, come si gioca, salvataggi, cosa mostra lo schermo superiore, nascondi. Su una cartella console: emulatore predefinito, rinomina, unisci cartelle, nascondi. Su un riquadro integrato: nome, colore, icona e sfondo.

### Comandi

| Tasto | Azione |
|---|---|
| Croce direzionale / stick sinistro | Muoversi; tieni premuto per continuare |
| Sinistra / destra, L / R in un elenco o menu | Una pagina alla volta |
| A | Apri / gioca; tieni premuto A su una cartella della Home per prenderla e spostarla |
| B | Indietro |
| L / R | Console precedente / successiva (nelle impostazioni: categoria) |
| Tieni premuto L | Ricerca vocale |
| Tieni premuto R | Parla con l'IA |
| L2 / R2 | Lettera iniziale precedente / successiva |
| X | Dimensione copertine, poi vista a elenco |
| Y | Menu del gioco, della cartella o del riquadro selezionato |
| START | Impostazioni (da qualunque punto della Home, anche con il riquadro «Impostazioni» nascosto); in un menu fa come A |
| SELECT | Nella Home: console / generi / serie; in una cartella: ordine e filtro per lingua |
| R3 | Cerca |
| Nella tastiera di ricerca | A scrive, B cancella, X spazio, Y tastiera di sistema, SELECT svuota, START o R3 per finire, tieni premuto L per parlare |
| Nella chat con l'IA | Tieni premuto R per parlare, Y per scrivere, X per una nuova chat, SELECT per la cronologia, L2 / R2 per scorrere indietro, B per chiudere; con la croce e A scegli tra i giochi sotto |

Tutto funziona anche al tocco: tocca per selezionare, tocca di nuovo per aprire, scorri con il dito e tocca le etichette in alto al posto di L, R, X, SELECT o Cerca. A/B e X/Y si possono scambiare per avere la conferma in stile Nintendo o Xbox («Impostazioni → Gioco → Tasti»).

## La tua libreria

- **Trova i giochi da solo.** Cerca cartelle `Roms` nella memoria interna e sulle schede SD; altre si aggiungono in «Impostazioni → Home e libreria → Cartelle dei giochi». L'ultima scansione resta in memoria, così la Home è pronta in un secondo e la scansione successiva gira in silenzio in background.
- **Console**: 3DS, NDS, GBA (con le cartelle GBA vibrazione e GBA modificati), GBC, GB, NES, SNES, N64, GameCube, Wii, Mega Drive, PlayStation 1/2/3, PSP, PS Vita e Switch, con le immagini anche per PS4, PS5, Xbox 360 e Xbox Series.
- **Giochi su più dischi come uno solo.** `Disc A/B`, `Disc 1/2`, `CD1/CD2`, `(Disc 2 of 3)` nella stessa cartella, oppure una lista `files:` di Pegasus, diventano una sola voce. RetroArch e DuckStation ricevono una playlist `.m3u` dei dischi, così puoi cambiare disco durante il gioco.
- **Giochi e app Android**: i giochi installati hanno automaticamente la loro cartella; qualsiasi altra app (video, browser …) si può aggiungere a una cartella «App».
- **Funziona con quello che hai già.** Legge `metadata.pegasus.txt` di Pegasus, `gamelist.xml` e `downloaded_media` di ES-DE / EmulationStation, le miniature di RetroArch e le cartelle `media/`, `covers/` e `images/` accanto ai giochi. Nessuno di questi file viene modificato; le aggiunte del launcher vanno nella sua cartella `DuoLauncher` (vedi [Le tue informazioni sui giochi](#le-tue-informazioni-sui-giochi)).
- **Si accorge dei giochi nuovi.** Copia dei giochi sulla scheda e la scansione successiva li trova; per una settimana (o finché non li giochi) portano l'etichetta NEW e ti viene proposto di ordinarli per genere e serie. La domanda si può disattivare. Y → «Segna come visto» toglie l'etichetta da un gioco; «Segna tutti come visti» (Y su una console o una cartella, oppure «Impostazioni → Home e libreria») la toglie da molti in una volta, e l'interruttore «Etichette NEW» lì le disattiva del tutto.
- **Unire cartelle.** Due cartelle della stessa console in posti diversi (due cartelle PS1, per esempio) si possono mostrare come una sola: Y → «Unisci cartelle» su una console. Ogni gioco mantiene il suo emulatore, le cartelle con lo stesso nome si uniscono da sole e «Separa» annulla l'unione.
- **Cartelle tue.** Il riquadro «Nuova cartella» crea una cartella con nome, colore, icona e sfondo propri. Ci aggiungi qualsiasi gioco o app con Y e lo togli con Y → «Rimuovi da questa cartella».
- **Le cartelle smart** si riempiono da sole: aggiunti di recente (ultimi 30 giorni), mai giocati, più giocati, in corso, da giocare, finiti e più votati. Y su una di esse mostra la sua regola.
- **Continua e preferiti.** Il primo riquadro della Home ti riporta subito nell'ultimo gioco. Segna i giochi con la stella tramite Y per raccoglierli nella cartella «Preferiti».
- **Nascondi quello che non giochi**: una cartella console, oppure un singolo gioco o app (Y → «Nascondi questo gioco»); li ritrovi in «Impostazioni → Home e libreria».
- **Il tuo catalogo.** Dopo ogni scansione l'intera libreria viene scritta in `DuoLauncher/catalog.tsv` (console, file, titolo, titolo inglese, genere, serie, tempo di gioco, stato, voto), da usare con un foglio di calcolo, con altri strumenti o con l'IA.

### Generi e serie

Sfogliare duemila giochi solo per console stanca presto, quindi ogni gioco riceve anche uno di 14 generi (gioco di ruolo, azione, avventura, piattaforme, tattico, strategia, sparatutto, rompicapo, corse, sport, picchiaduro, simulazione, party e musica, altro) e la sua serie.

- **Prima le conoscenze integrate.** Una tabella interna al launcher (`assets/gamedb.tsv`, 3.860 nomi di file che coprono tutti i giochi delle liste dei pacchetti Pegasus G per queste console, con sia il nome `.iso` sia quello `.cso` quando un pacchetto li ha entrambi) conosce già genere, serie e titolo ufficiale inglese dei set di ROM più comuni, per console e nome del file. I giochi che conosce non hanno bisogno né dell'IA né di internet.
- **L'IA per il resto.** «Impostazioni → IA e voce → IA: ordina per genere» / «IA: serie di giochi» sistema i giochi rimanenti, e i risultati restano in `DuoLauncher/<console>/genres.txt` e `series.txt` sulla scheda: si fa una volta sola e sopravvive alle reinstallazioni. I generi già scritti da Pegasus o ES-DE ("Action RPG", "角色扮演" …) vengono ricondotti ai 14 generi.
- **A mano**: Y → «Genere» su qualsiasi gioco.
- **Sullo schermo.** Lo schermo superiore mostra genere · serie di un gioco, e SELECT nella Home trasforma le cartelle in generi o serie. I nomi delle serie seguono la lingua dell'interfaccia.

### Ricerca

R3, il riquadro «Cerca» o il pulsante di ricerca aprono sullo schermo superiore una tastiera pensata per la croce direzionale, con le ricerche recenti e una fila di generi sopra; i risultati riempiono lo schermo inferiore mentre scrivi. I titoli cinesi si trovano con il pinyin, con le sue iniziali e con l'inizio di ogni sillaba (`kdyg`, `mlao` → 马里奥), e ogni gioco si trova anche con il suo titolo ufficiale inglese, quindi `zelda` trova 塞尔达传说. Se la apri dentro una console cerca solo in quella console, con un pulsante «Tutte le piattaforme» per allargare la ricerca. Tieni premuto L per dire un nome invece di scriverlo.

### Immagini che si completano da sole

- **Copertine vere**: «Impostazioni → Riordina → Scarica copertine» scarica le copertine dai set di miniature di libretro. I giochi NDS e GBA vengono riconosciuti dal codice del gioco dentro la ROM (anche le ROM zippate, con le schermate del titolo), così anche i file tradotti o rinominati trovano le loro immagini; i giochi PlayStation 1/2, PSP, N64, GameCube, Wii, 3DS, GB/GBC, NES, SNES e Mega Drive vengono riconosciuti dal titolo inglese.
- **Icone prese direttamente dalle ROM**: le icone dei banner NDS e le icone SMDH del 3DS; i titoli 3DS installati mostrano il nome nella lingua dell'interfaccia.
- **Copertine uniformi**: le copertine riempiono il riquadro oppure stanno tutte con lo stesso margine piccolo, medio o grande («Impostazioni → Aspetto e suono → Margine copertine»); le cornici bianche presenti nelle immagini vengono tagliate.
- **Immagini IA**: con una chiave per le immagini, si possono disegnare copertine, loghi del titolo e sfondi per i giochi che non ne hanno (Y → «Dettagli» → «Crea copertina con l'IA», oppure un'intera console in una volta da «Impostazioni → IA e voce → Immagini IA»). Vedi [IA](#ia).

## Giocare

- **Emulatore per gioco o per console** (Y). Vengono proposti solo gli emulatori installati:

  | Console | Emulatori (il primo è quello predefinito) |
  |---|---|
  | 3DS | AzaharPlus, Azahar, Citra MMJ, Citra MMJ (modalità prestazioni) |
  | NDS | DraStic, DraStic (percorso semplice), RetroArch · melonDS DS |
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
  | PS Vita | Vita3K (tramite title ID, per esempio un file chiamato `PCSE00000.psvita` o un nome che lo contiene) |
  | Switch | Eden |

- **Citra MMJ (modalità prestazioni)**: la stessa build di Citra MMJ ripacchettizzata con il nome di pacchetto del benchmark AnTuTu, che molti sistemi Android fanno girare alla massima frequenza. Viene proposta solo se quel pacchetto è davvero Citra.
- **Tempo di gioco e statistiche**: quanto e quando hai giocato l'ultima volta a ogni gioco, come su Switch; con l'accesso ai dati di utilizzo conta solo il tempo in cui il gioco era davvero sullo schermo. Il riquadro «Statistiche» mostra il totale, la settimana in corso giorno per giorno, il tempo per console e i giochi più giocati.
- **Dove ti eri fermato**: lo screenshot o l'immagine dello stato di salvataggio più recente di un gioco diventa il suo sfondo sullo schermo superiore.
- **Stato, voto e note**: segna un gioco come in corso, da giocare, finito o abbandonato e dagli da una a cinque stelle (Y → «Stato»); scrivi o detta una riga su dove sei arrivato e cosa fare dopo (Y → «Nota»). Tutto compare sullo schermo superiore la prossima volta che riprendi il gioco.
- **Dettagli del gioco** (Y → «Dettagli»): file, dimensione, dischi, cartella, emulatore, tempo di gioco, titolo ufficiale inglese e gli screenshot del gioco sullo schermo superiore.
- **Anteprime video e suoni**: dopo un attimo parte sullo schermo superiore il video del gioco (oppure scegli logo o copertina per ogni gioco: Y → «Schermo superiore»). I suoni discreti dell'interfaccia e la musichetta del banner di ogni gioco 3DS si possono disattivare.
- **Quando un gioco non parte**: un file mancante, un emulatore non installato, uno che non sa aprire quel tipo di file o un gioco che si chiude subito: per ogni caso compare un messaggio che dice cosa fare.
- **Promemoria pausa** («Impostazioni → Gioco»): dopo una, due o tre ore in un gioco, al ritorno il launcher ti suggerisce una pausa.
- **RetroAchievements**: con nome utente e chiave Web API, vedi i giochi giocati di recente con i progressi degli obiettivi, e un'etichetta 🏆 sui giochi che hanno obiettivi.

## Il tuo compagno IA

Tutto questo è facoltativo e usa il servizio IA che scegli tu, con la tua chiave (vedi [IA](#ia)).

- **Parlagli ovunque.** Tieni premuto R nella Home, in una cartella o nella tastiera di ricerca e parla, oppure premi Y per scrivere. La conversazione riempie lo schermo superiore; i giochi che consiglia o trova riempiono quello inferiore, pronti da avviare con A. Chiedigli a cosa giocare ("un GDR rilassante, non troppo lungo"), restringi la scelta ("qualcosa con più azione"), chiedi come superare il punto del gioco che hai selezionato, informazioni su una trama o un personaggio, o fai semplicemente due chiacchiere. Ricorda la conversazione per mezz'ora.
  *Perché:* lo schermo inferiore è quello dove si scelgono i giochi, quindi i consigli dell'IA finiscono lì; quello superiore resta libero per leggere. E su una console portatile tenere premuto un dorsale e parlare è più veloce di qualsiasi menu.
- **Nuova chat, cronologia salvata.** X nella chat ne inizia una nuova; SELECT elenca le precedenti, le più recenti in alto, per riaprirle, eliminarle una per una o tutte insieme (anche in «Impostazioni → IA e voce → Cronologia chat IA»). Finché la chat è aperta, anteprime video, musiche dei banner 3DS e suoni dei tasti restano in silenzio, e le risposte lette usano il volume multimediale.
  *Perché:* una domanda nuova non deve trascinarsi dietro l'argomento di prima, una risposta utile non deve sparire al riavvio, e una risposta letta non serve se sopra c'è il trailer di un gioco. La cronologia resta sulla console, non finisce mai in una configurazione esportata, e «Salva le chat IA» la disattiva.
- **Risposte lette ad alta voce**: «Impostazioni → IA e voce → Risposte vocali dell'IA»: Soniox (con la sua chiave) oppure OpenAI (la chiave delle immagini), con scelta della voce e della velocità e un pulsante di prova. Disattivate finché non le accendi.
- **A cosa giocare oggi**: Y sul riquadro «A cosa giocare?» fa scegliere all'IA sei giochi della tua libreria in base ai tuoi gusti, con un motivo per ciascuno. Premendo semplicemente il riquadro ne esce uno a caso, con la preferenza per i giochi che non hai mai provato.
- **Chiedi su un gioco** (Y → «Chiedi all'IA»): una domanda sul gioco selezionato, scritta o a voce, con la risposta sullo schermo superiore.
- **Come si gioca** (Y → «Come si gioca»): i comandi (compresi i tasti di conferma / annulla invertiti delle edizioni giapponesi) e le basi che servono a chi inizia, scritti una volta per gioco e conservati.
- **Non hai ancora una chiave?** Tenendo premuto R ti viene spiegato come aggiungerne una, sulla console o da un computer via Wi-Fi.

## Trucchi

Y → «Trucchi» su un gioco elenca i suoi trucchi; A ne attiva o disattiva uno.

| | 3DS (Azahar, Citra) | NDS (DraStic) | PSP (PPSSPP) | RetroArch (GBA, GB, GBC, NES, SNES, N64, MD, PS1, …) |
|---|---|---|---|---|
| Dove stanno | `azahar/cheats/` e `citra-emu/cheats/`, `<title ID>.txt`: scritti per entrambi, così cambiando emulatore restano | l'elenco completo in `DuoLauncher/cheats/NDS/`; quelli attivi finiscono nei trucchi personalizzati di DraStic (`DraStic/cheats/<game file name>.cht`), che funzionano anche con le ROM tradotte | `PSP/Cheats/<game ID>.ini`, i file dei trucchi di PPSSPP; l'ID del gioco viene letto dall'ISO, dal CSO o dall'EBOOT.PBP | `RetroArch/cheats/<core>/<game file name>.cht`, caricato da RetroArch all'avvio del gioco |
| Già sul dispositivo | – | `usrcheat.dat` di DraStic (in cinese sulle console Retroid), abbinato solo tramite il codice del gioco, così le ROM patchate e tradotte trovano i loro trucchi anche dove DraStic stesso non ne mostra | `cheat.db` di PPSSPP e, sulle console Retroid, file di trucchi in cinese per circa 4.300 giochi | Retroid include trucchi in cinese per centinaia di giochi (`000 中文金手指` e le cartelle dei core); un gioco senza file proprio ne riceve una copia, abbinata per nome |
| Download | [Sharkive](https://github.com/FlagBrew/Sharkive), circa 650 giochi, tramite il title ID letto dalla ROM | [libretro-database](https://github.com/libretro/libretro-database), circa 4.200 giochi, tramite il codice del gioco nell'intestazione della ROM | [CWCheat-Database-Plus+](https://github.com/Saramagrean/CWCheat-Database-Plus-), circa 4.300 giochi, tramite l'ID del gioco | [libretro-database](https://github.com/libretro/libretro-database) per GBA, tramite il codice del gioco nell'intestazione della ROM |

- **Scegli prima di importare**: un download mostra prima cosa ha trovato, già tradotto, con «Seleziona tutto» / «Deseleziona tutto»; vengono aggiunti solo quelli spuntati. *Perché:* un database può avere decine di codici per gioco, e alla maggior parte dei giocatori ne servono tre.
- **Traduci**: i nomi inglesi dei trucchi passano nella lingua dell'interfaccia con un tocco, per un gioco o per tutti i giochi insieme («Impostazioni → Riordina»). I trucchi comuni vengono da un glossario integrato (无限血量, 金钱最大, 主代码 …); gli altri passano dal servizio gratuito MyMemory, con l'endpoint gratuito di Google come secondo tentativo. Le indicazioni di versione (`v1.2`) e dei tasti (`(START+L)`) restano, e il nome inglese originale rimane nel file come nota.
- **Traduci con l'IA**: con una chiave IA, i trucchi di un gioco vengono inviati al modello insieme al nome del gioco, così usa i nomi ufficiali di personaggi e oggetti (鬼神林克, 心之容器 …).
- **Modifica**: rinomina o elimina qualsiasi trucco, elimina in un colpo tutti i trucchi di un gioco, o aggiungine di tuoi con i relativi codici (i codici 3DS, NDS e PSP vengono controllati nel formato `XXXXXXXX YYYYYYYY`).
- **Sicuro per DraStic**: le patch per flashcart (aggiramenti anti-pirateria, correzioni per overlay e Wi-Fi) vengono escluse dai download NDS perché DraStic va in crash se sono attive, e si possono attivare al massimo 256 righe di codice alla volta, il massimo che DraStic riesce a caricare.
- **Sicuro per i tuoi file**: la prima volta che viene modificato un file di trucchi 3DS scritto a mano, l'originale viene conservato come `.txt.bak`.
- **RetroArch, una volta sola**: in RetroArch attiva una volta «Menu rapido → Trucchi → "Auto-Apply Cheats During Game Load"» (il nome esatto dipende dalla lingua di RetroArch) e i trucchi che accendi qui saranno attivi fin dall'avvio del gioco.

## Aspetto

- **Lo schermo superiore (1920×1080)**: ogni console sul suo sfondo in carta ritagliata, oppure il logo del gioco selezionato sopra la copertina sfocata, con etichette e una breve descrizione; orologio e batteria stanno nell'angolo.
- **Lo schermo inferiore (1280×960)**: cartelle delle console, una griglia di copertine ingrandibile e una vista a elenco, con i suggerimenti dei tasti in basso che restano brevi e lasciano fuori ciò che non ci sta quando il testo è grande.
- **Temi**: sette combinazioni di colori, colori personalizzati e pacchetti di temi con immagini delle console, sfondi, un font, suoni e musica. Un tema colora anche gli sfondi a linee dello schermo superiore. Vedi [Temi](#temi).
- **Modalità chiara e scura**: segue il sistema o la imposti tu, con una serie di sfondi disegnata apposta per ciascuna.
- **Tutto nella Home è tuo.** Ogni riquadro integrato e ogni cartella console si può rinominare, ricolorare, dotare di un'icona propria (un'immagine tua, o una disegnata dal servizio immagini nello stile in plastilina del launcher), spostare (tieni premuto A, o pressione prolungata) o nascondere, e poi ritrovare in «Impostazioni → Home e libreria → Cartelle nascoste». Anche i riquadri integrati come «Statistiche», «Spazio» e «A cosa giocare?» possono avere un proprio sfondo sullo schermo superiore.
- **Aspetto delle console**: immagine, sfondo chiaro e scuro e colore della cartella di ogni console si possono sostituire («Impostazioni → Aspetto e suono → Aspetto piattaforme»). Uno sfondo scuro si può far ridisegnare dal servizio immagini partendo da quello chiaro, così i due restano sempre coordinati.
- **Dimensione del testo**: quattro dimensioni per entrambi gli schermi.
- **Salvaschermo**: dopo qualche minuto di inattività lo schermo superiore mostra le tue copertine con un orologio e quello inferiore diventa nero. Il testo cambia angolo a ogni immagine e si sposta lentamente, così niente resta impresso su un pannello OLED.

## Impostazioni e condivisione

- **Una pagina delle impostazioni come quella di una console.** START apre un'unica pagina: a sinistra le categorie («Aspetto e suono», «Home e libreria», «Gioco», «IA e voce», «Salvataggi, trasferimento, condivisione», «Riordina», «Sistema»), a destra le voci della categoria con i valori attuali e gli interruttori, e sullo schermo superiore la versione, lo spazio libero su ogni unità e quali servizi IA sono attivi. L / R cambiano categoria.
- **Condividi la tua configurazione.** «Impostazioni → Salvataggi, trasferimento, condivisione → Esporta la mia configurazione» crea un unico zip con tutte le impostazioni (aspetto, disposizione e ordine della Home, cartelle, elementi nascosti, scelte degli emulatori, servizi IA) e le tue immagini. «Importa una configurazione» ne prende una da `DuoLauncher/exports` o dalla cartella Download, che sia arrivata via Wi-Fi, da un'app di chat o su una scheda.
  *Perché è sicuro condividerla:* chiavi, password e cronologia di gioco non finiscono mai in una configurazione, importarne una mantiene le tue, e una tua chiave non viene mai indirizzata verso un indirizzo arrivato dalla configurazione di qualcun altro.
- **11 lingue**: 简体中文, 繁體中文, English, 日本語, 한국어, Français, Español, Deutsch, Italiano, Português, Русский.

## Strumenti utili

- **Trasferimento Wi-Fi**: «Impostazioni → Salvataggi, trasferimento, condivisione → Trasferimento Wi-Fi» mostra un indirizzo e un PIN. Apri l'indirizzo in un browser sulla stessa rete, inserisci il PIN e trascina ROM o pacchetti di temi direttamente in qualsiasi cartella console. La stessa pagina contiene le impostazioni di IA, voce e RetroAchievements, protette da una password amministratore (sei cifre casuali, o una tua), così le chiavi lunghe si incollano dal computer. Lo schermo della console resta acceso durante il trasferimento, perché a schermo spento la pagina non è raggiungibile.
- **Spazio**: un riquadro della Home mostra quanto è piena la scheda, e lo schermo superiore lo spazio libero di ogni unità. All'interno: quanto occupano i giochi di ogni console, i giochi più grandi mai giocati e l'eliminazione dei file di un gioco (tutti i dischi e i file indicati da un `.cue` o `.m3u`) dopo una conferma.
- **Backup dei salvataggi**: esegue il backup e il ripristino dei salvataggi di DraStic, RetroArch, PPSSPP, AetherSX2, DuckStation, Mupen64Plus FZ, Azahar e Citra in un unico zip, conservato in `DuoLauncher/backups` sulla scheda SD (nella memoria interna se non c'è una scheda). Dopo tre ore di gioco senza backup il launcher te lo ricorda, oppure, se preferisci, lo fa da solo (vengono tenuti i dieci backup automatici più recenti; i tuoi non vengono mai cancellati). Un ripristino sostituisce ogni salvataggio solo quando la sua copia è completa. Y → «Salvataggi» fa backup e ripristino di un singolo gioco.
- **Riordina**: trova i giochi doppi, elenca i giochi senza immagini, scarica le copertine mancanti, traduce tutti i trucchi in una volta e offre una pagina di diagnostica; gli strumenti IA (nomi e descrizioni per un'intera console, generi, serie, immagini, icone dei generi) si trovano in «IA e voce».
- **Aggiornamenti**: «Impostazioni → Sistema → Informazioni» controlla una volta al giorno l'ultima release su GitHub e ti dà il link.
- **Schermata Home**: impostalo come app Home predefinita per farlo partire con la console; il riquadro «Home di Android» (o «Impostazioni → Sistema → Esci alla Home di Android») riporta alla schermata Home di sistema, e «Impostazioni → Sistema → Impostazioni Android» apre le impostazioni del sistema.

## IA

Tutto qui è facoltativo e usa il tuo account. Le chiavi restano nelle impostazioni del launcher sulla console e vengono inviate solo al servizio che hai scelto. Inseriscile in «Impostazioni → IA e voce», oppure nella pagina Wi-Fi da un computer o da un telefono.

- **Traduzione IA**: DeepSeek, Qwen, OpenAI, Claude, Gemini o qualsiasi indirizzo compatibile con OpenAI. Questa chiave si occupa della parte testuale: la chat, i consigli sui giochi, «Chiedi all'IA», «Come si gioca», generi e serie, i nomi dei trucchi (un intero gioco in una sola richiesta) e i nomi e le descrizioni dei giochi nella tua lingua (scritti nei metadati del launcher insieme al titolo ufficiale inglese, così una libreria di titoli cinesi si legge in modo naturale in inglese e viceversa). Senza chiave, i trucchi vengono tradotti dai servizi gratuiti.
- **Immagini IA**: una chiave OpenAI per le immagini (viene scelto automaticamente il modello di immagini più recente che la chiave può usare). Copertine, loghi del titolo, sfondi, immagini delle console, icone e sfondi di cartelle e riquadri, tutto nello stesso stile di icone in plastilina e carta ritagliata delle immagini integrate; le icone vengono disegnate con lo sfondo trasparente, così le loro parti bianche restano. Un'immagine richiede circa mezzo minuto. La stessa chiave trasforma la tua voce in testo (`/audio/transcriptions`), visto che la console non ha un riconoscimento vocale proprio.
- **Risposte vocali dell'IA**: Soniox con la sua chiave, oppure OpenAI con la chiave delle immagini; voce e velocità (normale, svelto, veloce; «Svelto» è quella predefinita, perché entrambi leggono il cinese piuttosto lentamente).
- **Uso e costo dell'IA**: ogni richiesta del launcher viene contata sulla console: richieste, token, immagini e minuti di voce, con un costo stimato per oggi, questa settimana, questo mese e in totale. La stima usa i prezzi pubblicati dai servizi per quello che usi; ogni prezzo si può impostare a mano (un altro modello, un rivenditore, uno sconto). Quello che conta è la fattura del tuo fornitore.

## Le tue informazioni sui giochi

Metti una cartella `DuoLauncher` nella radice della scheda SD (o della memoria interna) per aggiungere o correggere nomi, descrizioni e immagini senza toccare i file degli altri frontend. Quello che c'è qui ha la precedenza su Pegasus, ES-DE e sui dati del gioco stesso.

```
DuoLauncher/
  3DS/metadata.txt
  3DS/media/<file name without extension>/logo.png   (also cover.png, background.png)
  NDS/metadata.txt
```

`metadata.txt` usa il formato di Pegasus. Le voci vengono abbinate tramite `file:`, oppure tramite `titleid:` per i giochi 3DS installati in un emulatore:

```
game: 勇者派遣会社
titleid: 0004000000065600
description: …
```

Nomi e descrizioni si possono indicare per lingua con `name.<language>:` e `description.<language>:` (`en`, `zh-CN`, `zh-TW`, `ja`, `ko`, `fr`, `es`, `de`, `it`, `pt`, `ru`; `zh` vale per entrambi i cinesi). Vengono usati quando il launcher è in quella lingua, così una stessa cartella può servire giocatori di lingue diverse:

```
game: 塞尔达传说 姆祖拉的假面
file: 塞尔达传说 姆祖拉的假面 汉化版.cci
name.en: The Legend of Zelda: Majora's Mask 3D
description.en: Link has three days before the moon falls on Termina…
```

## Temi

«Impostazioni → Aspetto e suono → Tema» offre sette temi di colore integrati («Azzurro», «Rosso ciliegia», «Verde menta», «Viola uva», «Mandarino», «Rosa sakura», «Grafite»), i **colori personalizzati** («Colori personali…»: scegli un colore principale e un colore di sfondo, e tutte le sfumature per la modalità chiara e scura vengono ricavate da quelli) e gli eventuali pacchetti di temi installati. Un tema colora entrambi gli schermi, compresi gli sfondi a linee dello schermo superiore.

Un pacchetto di temi è una cartella (o un suo `.zip`, che viene estratto al primo utilizzo) in `DuoLauncher/themes/` sulla scheda SD o nella memoria interna. Tutto è facoltativo; quello che un tema non contiene viene preso dall'aspetto integrato, quindi un tema può limitarsi a pochi colori.

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

`theme.txt` usa righe `chiave: valore`: `color.<name>` per la modalità chiara e `dark.color.<name>` per quella scura, con nomi come `accent`, `shelf-top`, `shelf-bottom`, `card`, `text`, `pill`, `footer`, `backdrop-paper` / `backdrop-ink` (i disegni a linee dello schermo superiore) e `folder.<type>` per le cartelle della Home. Basta `base-color: #RRGGBB` da solo per avere una combinazione di colori completa. [themes/sakura](themes/sakura/theme.txt) è un esempio completo con ogni chiave spiegata. «Impostazioni → Salvataggi, trasferimento, condivisione → Esporta tema attuale» salva quello che hai impostato come zip in `DuoLauncher/exports/`, che altri possono mettere nella propria cartella `themes/`; le tue immagini vengono incluse in `own/`. La voce «Impostazioni → Musica di sottofondo» compare solo quando un tema contiene della musica.

## Compilazione

Non servono né Android Studio né Gradle, solo gli strumenti a riga di comando dell'Android SDK (build-tools 35, piattaforma android-35) e JDK 17 o successivo:

```powershell
powershell -File build.ps1            # builds DuoLauncher.apk
powershell -File build.ps1 -Install   # also installs over adb, grants file access and starts it
```

Una chiave di firma di debug (`debug.keystore`) viene generata alla prima compilazione e non viene inclusa nel repository. `shot.ps1` invia pressioni di tasti via adb e salva gli screenshot di entrambi gli schermi, utile quando si lavora sull'interfaccia.

## Grafica

`assets/consoles/<type>.png` (icone di console e riquadri in plastilina) e `assets/backgrounds/<type>.jpg` (sfondi a linee in carta ritagliata, con le loro versioni per la modalità scura in `backgrounds/dark/`) sono stati realizzati per questo progetto. Gli sfondi sono in scala di grigi, così un tema di colore può tingerli. Le console senza immagine ripiegano su una console disegnata.

## Note sull'avvio

- AzaharPlus accetta solo un URI ad albero dentro una cartella a cui ha già accesso:
  `content://com.android.externalstorage.documents/tree/<volume>%3ARoms%2F3DS/document/<volume>%3ARoms%2F3DS%2F<file>`
- Citra MMJ: `org.citra.emu/org.citra.emu.ui.EmulationActivity` con `-e GamePath <path>`.
- Citra MMJ (modalità prestazioni) è la stessa build ripacchettizzata come `com.antutu.ABenchMark`; molte build di Android fanno girare quel nome di pacchetto con CPU e GPU alla massima frequenza. Viene proposta solo se quel pacchetto contiene davvero Citra.
- DraStic, nella versione installata sulle console Retroid, vuole `-e GAMEPATH "/sf<path>"`; per le build originali c'è una variante "percorso semplice".
- RetroArch: `com.retroarch.aarch64/...RetroActivityFuture` con `-e ROM`, `-e LIBRETRO <core>` e `-e CONFIGFILE`. Il file di configurazione indicato nella riga di avvio viene mantenuto: sul Duo, il `retroarch.cfg` di RetroArch mostra a piena dimensione le cornici per sistema di Retroid, mentre il `retroarch1920x1080.cfg` per schermo di Retroid disegna i giochi piccoli dentro una cornice a forma di TV.
- Dolphin (MMJR e ufficiale): `.../ui.main.MainActivity` con `--es AutoStartFile <path>`.
- ARMSX2, AetherSX2, Eden, PPSSPP e Mupen64Plus FZ ricevono il gioco come link in sola lettura `content://com.duo.launcher.rom/...` con un permesso di lettura una tantum, quindi non hanno bisogno di un proprio accesso alle cartelle.
- Vita3K: `org.vita3k.emulator/.Emulator` con l'extra di tipo array di stringhe `AppStartParameters = ["-r", "<title ID>"]`.
- Le righe di avvio presenti nei metadati Pegasus vengono usate così come sono.

### Citra MMJ su una console a doppio schermo

Citra MMJ mette di default lo schermo touch sul display superiore. Per avere l'immagine principale in alto e nascondere i pulsanti a schermo, chiudi Citra e imposta in `/sdcard/citra-emu/config/config-mmj.ini`:

```ini
[Controls]
input_overlay_hide = True
[Layout]
landscape_swap_screen = True
```

## Feedback

Duo Launcher prende forma da come viene usato. Se manca qualcosa, se qualcosa non è chiaro o non funziona, o se hai creato un tema che vale la pena condividere, apri una [issue](https://github.com/GenKnot/DuoLauncher/issues).

## Licenza

[MIT](LICENSE)
