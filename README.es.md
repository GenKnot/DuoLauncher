<p align="center"><img src="docs/logo.png" alt="Duo Launcher" width="520"></p>

# Duo Launcher

<p align="center"><a href="README.md">English</a> · <a href="README.fr.md">Français</a> · <a href="README.zh-CN.md">简体中文</a> · <a href="README.zh-TW.md">繁體中文</a> · <a href="README.ja.md">日本語</a> · <a href="README.ko.md">한국어</a> · <b>Español</b> · <a href="README.de.md">Deutsch</a> · <a href="README.it.md">Italiano</a> · <a href="README.pt.md">Português</a> · <a href="README.ru.md">Русский</a></p>

Un launcher de juegos para dos pantallas, pensado para la **Retroid Pocket Duo** y otras portátiles Android con segunda pantalla, al estilo del menú principal de la 3DS.
Le indicas tus carpetas de ROMs y él encuentra los juegos, los ordena por consola, género y saga, los muestra en la pantalla inferior con sus imágenes en la superior, y abre cada uno con el emulador que tú elijas.

**Descarga:** el APK más reciente está en la [página de versiones](https://github.com/GenKnot/DuoLauncher/releases/latest) (Android 11 o posterior). Si instalas una versión nueva encima de la anterior, conservas todos los ajustes y tu tiempo de juego.

## ¿Por qué otro launcher?

Duo Launcher empezó como el launcher de un solo jugador para una sola portátil, y sigue creciendo a partir de cómo se usa esa portátil en el día a día. Hay unas cuantas ideas detrás de todo:

- **Dos pantallas, dos tareas.** La pantalla de abajo sirve para elegir: carpetas grandes, una cuadrícula, una lista, menús. La de arriba sirve para mirar: la consola sobre su propio fondo ilustrado, el logo de un juego sobre su portada, un vídeo de muestra, cuánto tiempo llevas jugado, una nota que te dejaste o una conversación con la IA. Nada tiene que compartir espacio con otra cosa.
- **El mando va primero.** Todo funciona sin tocar la pantalla: un teclado pensado para la cruceta, direcciones que siguen avanzando si las mantienes, L / R para pasar página, mantener L para buscar por voz, mantener R para hablar con la IA. La pantalla táctil también funciona, solo que no hace falta.
- **Tu biblioteca, tal como ya la tienes.** Lee los metadatos y las imágenes de Pegasus y ES-DE que quizá ya tengas, guarda lo que añade en su propia carpeta `DuoLauncher` y nunca reescribe los archivos de otro frontend.
- **Una IA que trabaja para ti, con tu clave.** La traducción, la clasificación, las imágenes, los consejos y la conversación usan el servicio que *tú* eliges y pagas directamente. Las claves se quedan en la portátil y cada solicitud queda registrada en los ajustes con un coste estimado, así que nada se ejecuta a tus espaldas.
- **Todo se puede cambiar, ocultar o compartir.** Cada casilla del inicio se puede renombrar, cambiar de color, darle otra imagen, mover u ocultar. Toda tu configuración se puede exportar en un solo archivo y pasársela a un amigo, sin tus claves ni tu historial de juego.

Las sugerencias y los informes de errores son bienvenidos en el [gestor de incidencias](https://github.com/GenKnot/DuoLauncher/issues).

## Lo más destacado

- **Dos pantallas, un launcher**: eliges en la pantalla de abajo; en la de arriba ves logos, portadas, vídeos de muestra, tiempo de juego y descripciones.
- **Todos los emuladores en un sitio**: 3DS, NDS, GBA, GB/GBC, NES, SNES, N64, GameCube, Wii, Mega Drive, PS1, PS2, PSP, PS Vita y Switch, con el emulador elegido por consola o por juego. Los juegos de varios discos aparecen como uno solo.
- **Se ordena solo**: cada juego queda archivado en uno de 14 géneros y en su saga (Ys, Castlevania, Super Robot Wars…). El inicio muestra tu biblioteca por consola, por género o por saga, y las carpetas inteligentes mantienen al día «añadidos recientemente», «jugando», «pendiente», «terminado» y «mejor valorados».
- **Un compañero con IA**: mantén R en cualquier sitio y habla. Te recomienda juegos de tu propia biblioteca, te explica cómo pasar una parte de un juego, te cuenta una historia o simplemente charla contigo. Las respuestas se pueden leer en voz alta.
- **Trucos bien gestionados**: miles de trucos listos para 3DS, NDS, PSP, GBA y todos los sistemas de RetroArch, traducidos a tu idioma con un toque, editables y compartidos entre emuladores.
- **Completa lo que falta**: portadas reales de los paquetes de miniaturas de libretro, títulos oficiales en inglés y portadas, logos y fondos dibujados por IA con el estilo propio del launcher.
- **Hecho para una portátil**: salvapantallas seguro para OLED, transferencia por Wi-Fi desde un navegador, copias de las partidas guardadas, estadísticas de juego, progreso de RetroAchievements, un resumen del almacenamiento y una pantalla de inicio que está lista en un segundo.
- **A tu gusto**: temas de color y paquetes de temas, modo claro y oscuro, tamaño del texto, márgenes de las portadas, una página de ajustes como la de una consola y configuraciones que puedes exportar y compartir. 11 idiomas de interfaz.

## Capturas de pantalla

<p align="center"><img src="docs/screenshots/light-dark.png" alt="Light and dark mode"></p>

<table>
  <tr>
    <th>Ajustes al estilo 3DS</th>
    <th>Ajustes en modo oscuro</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_settings.png" alt="Ajustes al estilo 3DS"></td>
    <td><img src="docs/screenshots/en_settings_dark.png" alt="Ajustes en modo oscuro"></td>
  </tr>
  <tr>
    <th>Uso y coste de la IA</th>
    <th>Exportar e importar la configuración</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_ai_usage.png" alt="Uso y coste de la IA"></td>
    <td><img src="docs/screenshots/en_config.png" alt="Exportar e importar la configuración"></td>
  </tr>
  <tr>
    <th colspan="2">Ilustraciones a mano para el modo oscuro</th>
  </tr>
  <tr>
    <td colspan="2" align="center"><img src="docs/screenshots/en_dark_home.png" width="50%" alt="Ilustraciones a mano para el modo oscuro"></td>
  </tr>
  <tr>
    <th>Inicio por género, con iconos de plastilina</th>
    <th>Inicio por saga</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_genres.png" alt="Home by genre, with clay genre icons"></td>
    <td><img src="docs/screenshots/zh_series.png" alt="Home by series"></td>
  </tr>
  <tr>
    <th>Género y saga en la página del juego</th>
    <th>Nota, preguntar a la IA, estado, cómo se juega, partidas</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_game_series.png" alt="Genre and series on the game page"></td>
    <td><img src="docs/screenshots/zh_game_menu.png" alt="Note, Ask AI, status, how to play, saves"></td>
  </tr>
  <tr>
    <th>Teclado para mando; también encuentra títulos en inglés</th>
    <th>Pregúntale a la IA a qué jugar</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_keyboard.png" alt="Gamepad keyboard; English titles match too"></td>
    <td><img src="docs/screenshots/zh_ai_chat.png" alt="Ask the AI what to play"></td>
  </tr>
  <tr>
    <th>Herramientas de IA: sagas, sugerencias, almacenamiento, aviso de descanso</th>
    <th>Almacenamiento: tamaño por consola, juegos grandes sin jugar</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_tools_ai.png" alt="AI tools: series, picks, storage, break reminder"></td>
    <td><img src="docs/screenshots/zh_storage.png" alt="Storage: size per console, unplayed big games"></td>
  </tr>
  <tr>
    <th>Página del juego con vídeo de muestra</th>
    <th>Logo, portada o vídeo, para cada juego</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_game.png" alt="Game page"></td>
    <td><img src="docs/screenshots/en_menu.png" alt="Game menu"></td>
  </tr>
  <tr>
    <th>Vista de lista</th>
    <th>Estadísticas de juego</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_list.png" alt="List view"></td>
    <td><img src="docs/screenshots/en_stats.png" alt="Play statistics"></td>
  </tr>
  <tr>
    <th>Temas de color</th>
    <th>Los colores del tema llegan a los fondos</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_themes.png" alt="Theme menu"></td>
    <td><img src="docs/screenshots/en_pink.png" alt="Pink theme"></td>
  </tr>
  <tr>
    <th>Modo oscuro</th>
    <th>Modo oscuro</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_d3ds.png" alt="Dark mode, 3DS"></td>
    <td><img src="docs/screenshots/en_dstats.png" alt="Dark mode, statistics"></td>
  </tr>
  <tr>
    <th>Transferencia Wi-Fi</th>
    <th>Herramientas para ordenar</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_wifi.png" alt="Wi-Fi transfer"></td>
    <td><img src="docs/screenshots/en_tools.png" alt="Tools"></td>
  </tr>
  <tr>
    <th>GameCube, con Dolphin</th>
    <th>Mantén A para mover una carpeta</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_gamecube.png" alt="GameCube"></td>
    <td><img src="docs/screenshots/zh_move.png" alt="Moving a folder"></td>
  </tr>
  <tr>
    <th>Elige los trucos antes de importarlos, con los nombres traducidos</th>
    <th>Detalles del juego: tamaño, discos, capturas</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheat_pick.png" alt="Choosing cheats"></td>
    <td><img src="docs/screenshots/zh_details.png" alt="Game details"></td>
  </tr>
  <tr>
    <th>Trucos de 3DS, traducidos con un toque</th>
    <th>Trucos de NDS para DraStic, base de datos en chino</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheats.png" alt="3DS cheats"></td>
    <td><img src="docs/screenshots/zh_cheats_nds.png" alt="NDS cheats"></td>
  </tr>
  <tr>
    <th>Trucos de RetroArch (GBA)</th>
    <th>Editar, añadir, descargar</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheats_gba.png" alt="GBA cheats"></td>
    <td><img src="docs/screenshots/zh_cheat_menu.png" alt="Cheat menu"></td>
  </tr>
  <tr>
    <th>Renombrar o borrar un truco</th>
    <th>Traducir todos los trucos de una vez</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheat_edit.png" alt="Edit a cheat"></td>
    <td><img src="docs/screenshots/zh_tools.png" alt="Tools"></td>
  </tr>
  <tr>
    <th>Salvapantallas (seguro para OLED)</th>
    <th>11 idiomas</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_saver.png" alt="Screensaver"></td>
    <td><img src="docs/screenshots/en_lang.png" alt="Languages"></td>
  </tr>
</table>

## Primeros pasos

1. **Instala el APK** desde la [página de versiones](https://github.com/GenKnot/DuoLauncher/releases/latest) y ábrelo.
2. **Elige un idioma** y concede los permisos que te pide, todos en una misma página: acceso a los archivos (para encontrar tus juegos), acceso a los datos de uso (para que el tiempo de juego cuente solo el rato en que el juego está de verdad en pantalla) y, si quieres, el papel de pantalla de inicio. Puedes volver a esa página cuando quieras desde «Ajustes → Sistema → Permisos».
3. **Deja que encuentre tus juegos.** Cualquier carpeta `Roms` de la memoria interna o de una tarjeta SD se detecta sola. Puedes añadir otras carpetas en «Ajustes → Inicio y biblioteca → Carpetas de juegos». Las consolas se reconocen por el nombre de la carpeta (`gba`, `Nintendo DS`, `psx`…) y por la extensión de los archivos.
4. **Elige los emuladores** allí donde quieras algo distinto del predeterminado: pulsa Y sobre la carpeta de una consola para toda la consola, o sobre un juego solo para ese juego. Solo se ofrecen los emuladores que tienes instalados.
5. **Opcional: añade una clave de IA** en «Ajustes → IA y voz», desde la propia portátil o, más cómodo, desde un ordenador o un móvil por Wi-Fi («Ajustes → Partidas, transferencia, compartir → Transferencia Wi-Fi»). Todo funciona sin clave salvo las funciones de IA.
6. **Opcional: conviértelo en tu pantalla de inicio** («Ajustes → Sistema → Usar como pantalla de inicio») para que arranque junto con la portátil. La casilla «Inicio de Android» siempre te devuelve a la pantalla de inicio del sistema.

## Lo básico

**El inicio** (pantalla inferior) contiene las carpetas de tus consolas y unas cuantas casillas integradas: «Continuar» (vuelves directamente al último juego), «Jugados recientemente», «Favoritos», «Nueva carpeta», «Buscar», «Estadísticas», «Almacenamiento», «¿A qué juego?», «Ajustes» e «Inicio de Android». SELECT cambia el inicio entre consolas, géneros y sagas.

**Una carpeta** muestra sus juegos como una cuadrícula de portadas. X hace las portadas más pequeñas y, una vez más, convierte la cuadrícula en una lista con nombres, tiempo de juego y cuándo jugaste por última vez. SELECT ordena (orden predeterminado, nombre, jugados recientemente, más jugados) y puede mostrar solo las versiones en chino simplificado, chino tradicional o inglés. L2 / R2 saltan a la inicial anterior o siguiente; los títulos en chino cuentan por la inicial de su pinyin.

**La pantalla superior** sigue a lo que tengas seleccionado: una consola sobre su fondo, con su número de juegos y su emulador; o el logo de un juego sobre su portada difuminada, con su consola, género · saga, emulador, tiempo de juego y una descripción breve, un vídeo de muestra pasado un momento, o la imagen del punto donde lo dejaste.

**Y** abre el menú de lo que tengas seleccionado. En un juego: emulador, favorito, carpetas, trucos, detalles, género, nota, preguntar a la IA, estado y valoración, cómo se juega, partidas, qué muestra la pantalla superior y ocultar. En la carpeta de una consola: emulador predeterminado, cambiar nombre, unir carpetas y ocultar. En una casilla integrada: nombre, color, icono e imagen de fondo.

### Controles

| Botón | Acción |
|---|---|
| Cruceta / stick izquierdo | Moverse; mantén para seguir avanzando |
| Izquierda / derecha, L / R en una lista o un menú | Página a página |
| A | Abrir / jugar; mantén A sobre una carpeta del inicio para cogerla y moverla |
| B | Atrás |
| L / R | Consola anterior / siguiente (en los ajustes: categoría) |
| Mantener L | Buscar por voz |
| Mantener R | Hablar con la IA |
| L2 / R2 | Inicial anterior / siguiente |
| X | Tamaño de las portadas y, después, vista de lista |
| Y | Menú del juego, la carpeta o la casilla seleccionada |
| START | Ajustes (desde cualquier punto del inicio, aunque la casilla «Ajustes» esté oculta); en un menú, lo mismo que A |
| SELECT | En el inicio: consolas / géneros / sagas; en una carpeta: orden y filtro de idioma |
| R3 | Buscar |
| En el teclado de búsqueda | A escribe, B borra, X espacio, Y teclado del sistema, SELECT limpia, START o R3 terminan, mantén L para hablar |
| En el chat con la IA | Mantén R para hablar, Y para escribir, X para una charla nueva, SELECT para el historial, L2 / R2 para volver atrás en la conversación, B para cerrar; la cruceta y A eligen entre los juegos de abajo |

Todo funciona también con la pantalla táctil: toca para seleccionar, vuelve a tocar para abrir, desliza para desplazarte y toca las pastillas de la parte superior en lugar de L, R, X, SELECT o Buscar. A/B y X/Y se pueden intercambiar para tener los botones de confirmar al estilo Nintendo o Xbox («Ajustes → Juego → Botones»).

## Tu biblioteca

- **Encuentra tus juegos solo.** Busca carpetas `Roms` en la memoria interna y en las tarjetas SD; puedes añadir más en «Ajustes → Inicio y biblioteca → Carpetas de juegos». El último escaneo se guarda en caché, así que el inicio aparece en un segundo y el siguiente escaneo se hace en silencio en segundo plano.
- **Consolas**: 3DS, NDS, GBA (y las carpetas de GBA con vibración y de hacks), GBC, GB, NES, SNES, N64, GameCube, Wii, Mega Drive, PlayStation 1/2/3, PSP, PS Vita y Switch, con imágenes también para PS4, PS5, Xbox 360 y Xbox Series.
- **Juegos de varios discos como uno solo.** `Disc A/B`, `Disc 1/2`, `CD1/CD2`, `(Disc 2 of 3)` en una misma carpeta, o una lista `files:` de Pegasus, se convierten en una sola entrada. RetroArch y DuckStation reciben una lista `.m3u` con los discos para que puedas cambiar de disco dentro del juego.
- **Juegos y aplicaciones de Android**: los juegos instalados tienen su propia carpeta automáticamente; cualquier otra aplicación (vídeo, navegador…) se puede añadir a una carpeta «Aplicaciones».
- **Funciona con lo que ya tienes.** Lee `metadata.pegasus.txt` de Pegasus, `gamelist.xml` y `downloaded_media` de ES-DE / EmulationStation, las miniaturas de RetroArch y las carpetas `media/`, `covers/` e `images/` que haya junto a los juegos. No toca nada de lo suyo; lo que añade el launcher va a su propia carpeta `DuoLauncher` (consulta [Tu propia información de juegos](#tu-propia-información-de-juegos)).
- **Se da cuenta de los juegos nuevos.** Copia juegos a la tarjeta y el siguiente escaneo los encuentra; llevan la etiqueta NEW durante una semana (o hasta que los juegues) y te propone clasificarlos por género y saga. Esa pregunta se puede desactivar. Y → «Marcar como visto» quita la etiqueta de un juego; «Marcar todo como visto» (Y sobre una consola o carpeta, o «Ajustes → Inicio y biblioteca») la quita de muchos a la vez, y el interruptor «Etiquetas NEW» de ahí las desactiva del todo.
- **Unir carpetas.** Dos carpetas de la misma consola en sitios distintos (por ejemplo, dos de PS1) se pueden mostrar como una sola: «Y → Unir carpetas» sobre una consola. Cada juego conserva su emulador, las carpetas con el mismo nombre se unen solas y «Separar» lo deshace.
- **Tus propias carpetas.** La casilla «Nueva carpeta» crea una carpeta con su propio nombre, color, icono e imagen de fondo. Añade cualquier juego o aplicación con Y, y sácalo de nuevo con «Y → Quitar de esta carpeta».
- **Las carpetas inteligentes** se llenan solas: añadidos recientemente (los últimos 30 días), sin jugar, más jugados, jugando, pendiente, terminado y mejor valorados. Y sobre una de ellas muestra su regla.
- **Continuar y favoritos.** La primera casilla del inicio te devuelve directamente al último juego. Marca juegos con Y para reunirlos en una carpeta «Favoritos».
- **Oculta lo que no juegas**: la carpeta de una consola, o un solo juego o aplicación («Y → Ocultar este juego»); los recuperas en «Ajustes → Inicio y biblioteca».
- **Tu catálogo.** Tras cada escaneo, toda la biblioteca se escribe en `DuoLauncher/catalog.tsv` (consola, archivo, título, título en inglés, género, saga, tiempo de juego, estado, valoración), para hojas de cálculo, otras herramientas o la IA.

### Géneros y sagas

Recorrer dos mil juegos solo por consola acaba cansando, así que cada juego recibe además uno de 14 géneros (rol, acción, aventura, plataformas, táctica, estrategia, disparos, puzles, carreras, deportes, lucha, simulación, party y música, otros) y su saga.

- **Primero, lo que ya sabe.** Una tabla incluida en el launcher (`assets/gamedb.tsv`, 3860 nombres de archivo que cubren todos los juegos de las listas de los packs G de Pegasus para estas consolas, con los nombres `.iso` y `.cso` cuando un pack trae los dos) ya conoce el género, la saga y el título oficial en inglés de los sets de ROMs habituales por consola y nombre de archivo. Los juegos que conoce no necesitan IA ni conexión a internet.
- **La IA para el resto.** «Ajustes → IA y voz → IA: ordenar por género» / «IA: sagas de juegos» clasifica los juegos que faltan, y los resultados se guardan en `DuoLauncher/<console>/genres.txt` y `series.txt` en la tarjeta, así que se hace una sola vez y sobrevive a las reinstalaciones. Los géneros que ya hayan escrito Pegasus o ES-DE («Action RPG», «角色扮演»…) se asignan a los 14 géneros.
- **A mano**: «Y → Género» en cualquier juego.
- **En pantalla.** La pantalla superior muestra el género · saga de cada juego, y SELECT en el inicio convierte las carpetas en géneros o sagas. Los nombres de las sagas siguen el idioma de la interfaz.

### Búsqueda

R3, la casilla «Buscar» o el botón de búsqueda abren en la pantalla superior un teclado pensado para la cruceta, con tus búsquedas recientes y una fila de géneros encima; los resultados llenan la pantalla inferior mientras escribes. Los títulos en chino coinciden con su pinyin, con sus iniciales y con el principio de cada sílaba (`kdyg`, `mlao` → 马里奥), y cada juego coincide también con su título oficial en inglés, así que `zelda` encuentra 塞尔达传说. Si la abres desde dentro de una consola, busca solo en esa consola, con un botón «Todas las plataformas» para ampliarla. Mantén L para decir un nombre en lugar de escribirlo.

### Imágenes que se completan solas

- **Portadas reales**: «Ajustes → Ordenar → Descargar portadas» baja las portadas de los paquetes de miniaturas de libretro. Los juegos de NDS y GBA se identifican por el código de juego que lleva la ROM (también las ROMs comprimidas en zip, con pantallas de título), así que los archivos traducidos o renombrados siguen encontrando sus imágenes; los de PlayStation 1/2, PSP, N64, GameCube, Wii, 3DS, GB/GBC, NES, SNES y Mega Drive se identifican por su título en inglés.
- **Iconos sacados de las propias ROMs**: los iconos de banner de NDS y los iconos SMDH de 3DS; los títulos de 3DS instalados muestran su nombre en el idioma de la interfaz.
- **Portadas uniformes**: las portadas llenan su casilla, o todas quedan con el mismo margen pequeño, mediano o grande («Ajustes → Aspecto y sonido → Margen de portadas»); los marcos blancos que traen algunas imágenes de portada se recortan.
- **Imágenes con IA**: con una clave de imágenes se pueden dibujar portadas, logos de título y fondos para los juegos que no tienen («Y → Detalles → Crear portada con IA», o una consola entera de una vez en «Ajustes → IA y voz → Imágenes con IA»). Consulta [IA](#ia).

## Jugar

- **Emulador por juego o por consola** (Y). Solo se ofrecen los emuladores instalados:

  | Consola | Emuladores (el primero es el predeterminado) |
  |---|---|
  | 3DS | AzaharPlus, Azahar, Citra MMJ, Citra MMJ (modo rendimiento) |
  | NDS | DraStic, DraStic (ruta simple), RetroArch · melonDS DS |
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
  | PS Vita | Vita3K (por ID de título, p. ej. un archivo llamado `PCSE00000.psvita` o un nombre que lo contenga) |
  | Switch | Eden |

- **Citra MMJ (modo rendimiento)**: la misma versión de Citra MMJ reempaquetada con el nombre de paquete del benchmark AnTuTu, que muchos sistemas Android ejecutan a frecuencia máxima. Solo se ofrece cuando ese paquete es realmente Citra.
- **Tiempo de juego y estadísticas**: cuánto tiempo has jugado a cada juego y cuándo fue la última vez, al estilo de Switch; con el acceso a los datos de uso concedido, solo cuenta el tiempo en que el juego estuvo de verdad en pantalla. La casilla «Estadísticas» muestra el total, esta semana día a día, el tiempo por consola y los juegos más jugados.
- **Donde lo dejaste**: la captura o la imagen del estado guardado más reciente de un juego que ya has jugado pasa a ser su fondo en la pantalla superior.
- **Estado, valoración y notas**: marca un juego como jugando, pendiente, terminado o abandonado y dale de una a cinco estrellas («Y → Estado»); escribe o dicta una frase sobre dónde vas y qué te toca hacer después («Y → Nota»). Todo aparece en la pantalla superior la próxima vez que vuelvas al juego.
- **Detalles del juego** («Y → Detalles»): archivo, tamaño, discos, carpeta, emulador, tiempo de juego, el título oficial en inglés y las capturas del juego en la pantalla superior.
- **Vídeos de muestra y sonidos**: el vídeo de un juego empieza en la pantalla superior pasado un momento (o elige logo o portada para cada juego: «Y → Pantalla superior»). Los sonidos suaves de la interfaz y la melodía del banner de cada juego de 3DS se pueden desactivar.
- **Cuando un juego no arranca**: si falta el archivo, el emulador no está instalado, no puede abrir ese tipo de archivo o el juego se cierra al instante, aparece un mensaje que te dice qué hacer en cada caso.
- **Aviso de descanso** («Ajustes → Juego»): tras una, dos o tres horas con un juego, el launcher te sugiere un descanso cuando vuelves.
- **RetroAchievements**: con tu nombre de usuario y tu clave Web API, ves los juegos jugados recientemente con su progreso de logros, y una etiqueta 🏆 en los juegos que tienen logros.

## Tu compañero con IA

Todo esto es opcional y usa el servicio de IA que elijas con tu propia clave (consulta [IA](#ia)).

- **Háblale desde cualquier sitio.** Mantén R en el inicio, en una carpeta o en el teclado de búsqueda y habla, o pulsa Y para escribir. La conversación ocupa la pantalla superior; los juegos que te recomienda o encuentra llenan la inferior, listos para empezar con A. Pregúntale a qué jugar («un RPG tranquilo y no muy largo»), afina la búsqueda («algo con más acción»), pregúntale cómo pasar la parte del juego que tienes seleccionado, por una historia o un personaje, o simplemente charla. Recuerda la conversación durante media hora.
  *Por qué:* la pantalla de abajo es donde se eligen los juegos, así que las sugerencias de la IA aparecen ahí; la de arriba queda libre para leer. Y en una portátil, mantener un gatillo y hablar es más rápido que cualquier menú.
- **Charla nueva, historial guardado.** X en el chat empieza una charla nueva; SELECT muestra las anteriores, la más reciente arriba, para volver a abrirlas, borrarlas una a una o todas a la vez (también en «Ajustes → IA y voz → Historial de charlas con la IA»). Mientras el chat está abierto, las vistas previas en vídeo, las melodías de los banners de 3DS y los sonidos de los botones se callan, y las respuestas habladas suenan con el volumen multimedia.
  *Por qué:* una pregunta nueva no debería arrastrar el tema anterior, una respuesta que vale la pena no debería perderse al reiniciar, y una respuesta en voz alta no sirve si encima suena el tráiler de un juego. El historial se queda en la consola, nunca forma parte de una configuración exportada y «Guardar charlas con la IA» lo desactiva.
- **Respuestas en voz alta**: «Ajustes → IA y voz → Respuestas de voz de la IA»: Soniox (con su propia clave) u OpenAI (la clave de imágenes), con elección de voz y velocidad y un botón para probar. Desactivado a menos que lo actives.
- **A qué jugar hoy**: Y sobre la casilla «¿A qué juego?» hace que la IA elija seis juegos de tu biblioteca según tus gustos, con el motivo de cada uno. Si solo pulsas la casilla, elige uno al azar, dando preferencia a los que nunca has jugado.
- **Pregunta sobre un juego** («Y → Preguntar a la IA»): una pregunta sobre el juego seleccionado, escrita o dicha, con la respuesta en la pantalla superior.
- **Cómo se juega** («Y → Cómo se juega»): los controles (incluidos los botones de confirmar / cancelar intercambiados de las versiones japonesas) y lo básico que necesita alguien que empieza, escrito una vez por juego y guardado.
- **¿Aún no tienes clave?** Al mantener R te explica cómo añadir una, desde la portátil o desde un ordenador por Wi-Fi.

## Trucos

«Y → Trucos» sobre un juego muestra su lista de trucos; A activa o desactiva cada uno.

| | 3DS (Azahar, Citra) | NDS (DraStic) | PSP (PPSSPP) | RetroArch (GBA, GB, GBC, NES, SNES, N64, MD, PS1, …) |
|---|---|---|---|---|
| Dónde se guardan | `azahar/cheats/` y `citra-emu/cheats/`, `<title ID>.txt`; se escriben para los dos, así que no se pierden si cambias de emulador | la lista completa en `DuoLauncher/cheats/NDS/`; los activados van a los trucos personalizados de DraStic (`DraStic/cheats/<game file name>.cht`), que también funcionan con ROMs traducidas | `PSP/Cheats/<game ID>.ini`, los archivos de trucos del propio PPSSPP; el ID del juego se lee de la ISO, el CSO o el EBOOT.PBP | `RetroArch/cheats/<core>/<game file name>.cht`, que RetroArch carga al iniciar el juego |
| Ya en el dispositivo | – | el `usrcheat.dat` de DraStic (en chino en las portátiles Retroid), identificado solo por el código de juego, así que las ROMs parcheadas y traducidas encuentran sus trucos incluso cuando el propio DraStic no muestra ninguno | el `cheat.db` de PPSSPP y, en las portátiles Retroid, archivos de trucos en chino para unos 4300 juegos | Retroid incluye trucos en chino para cientos de juegos (`000 中文金手指` y las carpetas de los cores); un juego sin archivo propio recibe una copia, identificada por el nombre |
| Descarga | [Sharkive](https://github.com/FlagBrew/Sharkive), unos 650 juegos, por el ID de título leído de la ROM | [libretro-database](https://github.com/libretro/libretro-database), unos 4200 juegos, por el código de juego de la cabecera de la ROM | [CWCheat-Database-Plus+](https://github.com/Saramagrean/CWCheat-Database-Plus-), unos 4300 juegos, por ID de juego | [libretro-database](https://github.com/libretro/libretro-database) para GBA, por el código de juego de la cabecera de la ROM |

- **Elige antes de importar**: una descarga primero muestra lo que ha encontrado, ya traducido, con «Seleccionar todo» / «Quitar todo»; solo se añaden los marcados. *Por qué:* una base de datos puede tener decenas de códigos por juego, y la mayoría de los jugadores quiere tres.
- **Traducir**: los nombres de trucos en inglés pasan a tu idioma de interfaz con un toque, para un juego o para todos a la vez («Ajustes → Ordenar»). Los trucos habituales salen de un glosario integrado (无限血量, 金钱最大, 主代码…); el resto va al servicio gratuito MyMemory, con el servicio gratuito de Google como segundo intento. Las etiquetas de versión (`v1.2`) y las indicaciones de botones (`(START+L)`) se conservan, y el nombre original en inglés queda en el archivo como nota.
- **Traducir con IA**: con una clave de IA, los trucos de un juego se envían al modelo junto con el nombre del juego, para que use los nombres oficiales de personajes y objetos (鬼神林克, 心之容器…).
- **Editar**: renombra o borra cualquier truco, borra todos los trucos de un juego de una vez o añade los tuyos con sus códigos (los códigos de 3DS, NDS y PSP se comprueban con el formato `XXXXXXXX YYYYYYYY`).
- **Seguro para DraStic**: los parches de flashcart (saltos de protección anticopia, arreglos de overlay y de Wi-Fi) se dejan fuera de las descargas de NDS porque DraStic se cuelga si están activados, y como mucho puede haber 256 líneas de código activas a la vez, el máximo que DraStic puede cargar.
- **Seguro para tus archivos**: la primera vez que se modifica un archivo de trucos de 3DS escrito a mano, el original se guarda como `.txt.bak`.
- **RetroArch, una sola vez**: activa una vez en RetroArch «Menú rápido → Trucos → Auto-Apply Cheats During Game Load» (aplicar los trucos al cargar el juego), y los trucos que actives aquí funcionarán desde que empiece el juego.

## Aspecto

- **La pantalla superior (1920×1080)**: cada consola sobre su propio fondo de papel recortado, o el logo del juego seleccionado sobre su portada difuminada, con etiquetas y una descripción breve; el reloj y la batería están en la esquina.
- **La pantalla inferior (1280×960)**: carpetas de consolas, una cuadrícula de portadas con zoom y una vista de lista, con indicaciones de botones abajo que se mantienen cortas y dejan fuera lo que no cabe cuando el texto es grande.
- **Temas**: siete combinaciones de colores, colores propios y paquetes de temas con imágenes de consolas, fondos, una fuente, sonidos y música. El tema también colorea los fondos de líneas de la pantalla superior. Consulta [Temas](#temas).
- **Modo claro y oscuro**: sigue al sistema o lo eliges tú, con un juego de fondos dibujado aparte para cada modo.
- **Todo lo del inicio es tuyo.** Cada casilla integrada y cada carpeta de consola se puede renombrar, cambiar de color, darle su propio icono (una imagen tuya, o una dibujada por el servicio de imágenes con el estilo de plastilina del launcher), mover (mantén A o pulsación larga) u ocultar, y recuperarla en «Ajustes → Inicio y biblioteca → Carpetas ocultas». Las casillas integradas como «Estadísticas», «Almacenamiento» y «¿A qué juego?» también pueden tener su propio fondo en la pantalla superior.
- **Aspecto de cada consola**: la imagen de cada consola, su fondo claro y oscuro y el color de su carpeta se pueden sustituir («Ajustes → Aspecto y sonido → Aspecto de plataformas»). El servicio de imágenes puede redibujar un fondo oscuro a partir del claro, para que los dos siempre casen.
- **Tamaño del texto**: cuatro tamaños para las dos pantallas.
- **Salvapantallas**: tras unos minutos sin tocar nada, la pantalla superior muestra tus portadas con un reloj y la inferior se apaga. El texto cambia de esquina con cada imagen y se desplaza poco a poco, para que nada se quede marcado en un panel OLED.

## Ajustes y compartir

- **Una página de ajustes como la de una consola.** START abre una sola página: las categorías a la izquierda («Aspecto y sonido», «Inicio y biblioteca», «Juego», «IA y voz», «Partidas, transferencia, compartir», «Ordenar», «Sistema»), las opciones de la categoría elegida a la derecha con sus valores e interruptores, y la pantalla superior con la versión, el espacio libre de cada unidad y qué servicios de IA están activos. L / R cambian de categoría.
- **Comparte tu configuración.** «Ajustes → Partidas, transferencia, compartir → Exportar mi configuración» crea un único zip con todos los ajustes (aspecto, distribución y orden del inicio, carpetas, cosas ocultas, emuladores elegidos, servicios de IA) y tus propias imágenes. «Importar una configuración» carga una desde `DuoLauncher/exports` o la carpeta Descargas, tanto si te llegó por Wi-Fi como por una app de mensajería o en una tarjeta.
  *Por qué es seguro compartirla:* las claves, las contraseñas y el historial de juego nunca entran en una configuración, al importar una conservas las tuyas, y una clave tuya nunca se dirige a una dirección que venga de la configuración de otra persona.
- **11 idiomas**: 简体中文, 繁體中文, English, 日本語, 한국어, Français, Español, Deutsch, Italiano, Português, Русский.

## Herramientas útiles

- **Transferencia Wi-Fi**: «Ajustes → Partidas, transferencia, compartir → Transferencia Wi-Fi» muestra una dirección y un PIN. Abre la dirección en un navegador de la misma red, escribe el PIN y suelta ROMs o paquetes de temas directamente en la carpeta de cualquier consola. Esa misma página reúne los ajustes de IA, voz y RetroAchievements tras una contraseña de administración (seis dígitos al azar, o la que tú pongas), para que puedas pegar las claves largas desde un ordenador. La pantalla de la portátil se mantiene encendida mientras dura la transferencia, porque con la pantalla apagada no se puede acceder a la página.
- **Almacenamiento**: una casilla del inicio muestra lo llena que está la tarjeta, y el espacio libre de cada unidad en la pantalla superior. Dentro: el tamaño de los juegos de cada consola, los juegos más grandes que nunca has jugado y la opción de borrar los archivos de un juego (todos los discos, y los archivos que nombra un `.cue` o un `.m3u`) tras una confirmación.
- **Copias de las partidas guardadas**: copia y restaura las partidas de DraStic, RetroArch, PPSSPP, AetherSX2, DuckStation, Mupen64Plus FZ, Azahar y Citra en un solo zip, guardado en `DuoLauncher/backups` en la tarjeta SD (o en la memoria interna si no hay tarjeta). Tras tres horas de juego sin copia, el launcher te lo recuerda o, si lo prefieres, la hace él solo (se guardan las diez copias automáticas más recientes; las tuyas nunca se borran). Al restaurar, cada partida solo se sustituye cuando su copia está completa. «Y → Partidas» copia y restaura un solo juego por separado.
- **Ordenar**: busca juegos duplicados, lista los juegos sin imágenes, descarga las portadas que faltan, traduce todos los trucos de una vez y ofrece una página de diagnóstico; las herramientas de IA (nombres y descripciones de una consola entera, géneros, sagas, imágenes, iconos de géneros) están en «IA y voz».
- **Actualizaciones**: «Ajustes → Sistema → Acerca de» comprueba una vez al día la última versión publicada en GitHub y enlaza a ella.
- **Pantalla de inicio**: ponlo como aplicación de inicio predeterminada para que arranque con la portátil; la casilla «Inicio de Android» (o «Ajustes → Sistema → Salir al inicio de Android») te devuelve a la pantalla de inicio del sistema, y «Ajustes → Sistema → Ajustes de Android» abre los ajustes del propio sistema.

## IA

Todo lo de este apartado es opcional y usa tu propia cuenta. Las claves se guardan en los ajustes del launcher, en la portátil, y solo se envían al servicio que hayas elegido. Introdúcelas en «Ajustes → IA y voz», o en la página Wi-Fi desde un ordenador o un móvil.

- **Traducción con IA**: DeepSeek, Qwen, OpenAI, Claude, Gemini o cualquier dirección compatible con OpenAI. Esta clave es la que conversa: el chat, las sugerencias de juegos, «Preguntar a la IA», «Cómo se juega», géneros y sagas, nombres de trucos (un juego entero en una sola solicitud), y nombres y descripciones de juegos en tu idioma (que se escriben en los metadatos propios del launcher junto con el título oficial en inglés, para que una biblioteca de títulos en chino se lea con naturalidad en inglés, y al revés). Sin clave, los trucos se traducen con servicios gratuitos.
- **Imágenes con IA**: una clave de imágenes de OpenAI (se elige automáticamente el modelo de imagen más reciente que permita la clave). Portadas, logos de título, fondos, imágenes de consolas, iconos y fondos de carpetas y casillas, todo con el mismo estilo de iconos de plastilina y papel recortado que el arte integrado; los iconos se dibujan con fondo transparente para que sus partes blancas se mantengan. Cada imagen tarda alrededor de medio minuto. La misma clave convierte tu voz en texto (`/audio/transcriptions`), ya que la portátil no tiene reconocimiento de voz propio.
- **Respuestas de voz de la IA**: Soniox con su propia clave, u OpenAI con la clave de imágenes; voz y velocidad («Normal», «Rápido», «Muy rápido»; «Rápido» por defecto, porque los dos leen el chino despacio).
- **Uso y coste de la IA**: cada solicitud que hace el launcher se cuenta en la portátil: solicitudes, tokens, imágenes y minutos de voz, con un coste estimado de hoy, esta semana, este mes y en total. La estimación usa los precios públicos de los servicios para lo que usas; cada precio se puede cambiar por el tuyo (otro modelo, un revendedor, un descuento). Lo que cuenta es la factura de tu proveedor.

## Tu propia información de juegos

Crea una carpeta `DuoLauncher` en la raíz de la tarjeta SD (o de la memoria interna) para añadir o corregir nombres, descripciones e imágenes sin tocar los archivos de otros frontends. Lo que pongas aquí tiene prioridad sobre Pegasus, ES-DE y los datos del propio juego.

```
DuoLauncher/
  3DS/metadata.txt
  3DS/media/<file name without extension>/logo.png   (also cover.png, background.png)
  NDS/metadata.txt
```

`metadata.txt` usa el formato de Pegasus. Las entradas se identifican por `file:`, o por `titleid:` en el caso de juegos de 3DS instalados en un emulador:

```
game: 勇者派遣会社
titleid: 0004000000065600
description: …
```

Los nombres y las descripciones se pueden dar por idioma con `name.<language>:` y `description.<language>:` (`en`, `zh-CN`, `zh-TW`, `ja`, `ko`, `fr`, `es`, `de`, `it`, `pt`, `ru`; `zh` vale para los dos chinos). Se usan mientras el launcher esté en ese idioma, así que una misma carpeta sirve para jugadores de varios idiomas:

```
game: 塞尔达传说 姆祖拉的假面
file: 塞尔达传说 姆祖拉的假面 汉化版.cci
name.en: The Legend of Zelda: Majora's Mask 3D
description.en: Link has three days before the moon falls on Termina…
```

## Temas

«Ajustes → Aspecto y sonido → Tema» ofrece siete temas de color integrados (azul, rojo, verde, morado, naranja, rosa, grafito), **colores propios** (eliges un color principal y un color de fondo, y a partir de ellos se calculan todos los tonos del modo claro y del oscuro) y los paquetes de temas que tengas instalados. Un tema colorea las dos pantallas, incluidos los fondos de líneas de la pantalla superior.

Un paquete de temas es una carpeta (o un `.zip` de una, que se descomprime la primera vez que se usa) dentro de `DuoLauncher/themes/` en la tarjeta SD o la memoria interna. Todo es opcional; lo que un tema no incluya se toma del aspecto integrado, así que un tema puede ser tan pequeño como unos cuantos colores.

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

`theme.txt` usa líneas `key: value`: `color.<name>` para el modo claro y `dark.color.<name>` para el oscuro, con nombres como `accent`, `shelf-top`, `shelf-bottom`, `card`, `text`, `pill`, `footer`, `backdrop-paper` / `backdrop-ink` (las líneas de los fondos de la pantalla superior) y `folder.<type>` para las carpetas del inicio. Con `base-color: #RRGGBB` basta para tener una combinación de colores completa. [themes/sakura](themes/sakura/theme.txt) es un ejemplo completo con todas las claves explicadas. «Ajustes → Partidas, transferencia, compartir → Exportar tema actual» guarda lo que tengas configurado como un zip en `DuoLauncher/exports/`, que otros pueden soltar en su propia carpeta `themes/`; tus propias imágenes van incluidas en `own/`. La opción «Ajustes → Música de fondo» aparece cuando un tema trae música.

## Compilar

No hacen falta Android Studio ni Gradle, solo las herramientas de línea de comandos del Android SDK (build-tools 35, plataforma android-35) y JDK 17 o posterior:

```powershell
powershell -File build.ps1            # builds DuoLauncher.apk
powershell -File build.ps1 -Install   # also installs over adb, grants file access and starts it
```

La primera compilación genera una clave de firma de depuración (`debug.keystore`), que no se sube al repositorio. `shot.ps1` envía pulsaciones de botones por adb y guarda capturas de las dos pantallas, algo muy práctico mientras se trabaja en la interfaz.

## Arte

`assets/consoles/<type>.png` (iconos de plastilina de consolas y casillas) y `assets/backgrounds/<type>.jpg` (fondos de líneas en papel recortado, con sus versiones para el modo oscuro en `backgrounds/dark/`) se hicieron para este proyecto. Los fondos están en escala de grises, para que un tema de color pueda teñirlos. Las consolas sin imagen usan una consola dibujada en su lugar.

## Notas de arranque

- AzaharPlus solo acepta una URI de árbol dentro de una carpeta a la que ya tenga acceso:
  `content://com.android.externalstorage.documents/tree/<volume>%3ARoms%2F3DS/document/<volume>%3ARoms%2F3DS%2F<file>`
- Citra MMJ: `org.citra.emu/org.citra.emu.ui.EmulationActivity` con `-e GamePath <path>`.
- Citra MMJ (modo rendimiento) es la misma versión reempaquetada como `com.antutu.ABenchMark`; muchas versiones de Android ejecutan ese nombre de paquete con la CPU y la GPU a frecuencia máxima. Solo se ofrece cuando ese paquete contiene realmente Citra.
- DraStic, tal como viene en las portátiles Retroid, espera `-e GAMEPATH "/sf<path>"`; para las versiones estándar se ofrece la variante «ruta simple».
- RetroArch: `com.retroarch.aarch64/...RetroActivityFuture` con `-e ROM`, `-e LIBRETRO <core>` y `-e CONFIGFILE`. Se respeta el archivo de configuración que indica la línea de arranque: en la Duo, el `retroarch.cfg` propio de RetroArch muestra a tamaño completo los marcos por sistema de Retroid, mientras que el `retroarch1920x1080.cfg` por pantalla de Retroid dibuja los juegos pequeños dentro de un marco de televisor.
- Dolphin (MMJR y oficial): `.../ui.main.MainActivity` con `--es AutoStartFile <path>`.
- ARMSX2, AetherSX2, Eden, PPSSPP y Mupen64Plus FZ reciben el juego como un enlace `content://com.duo.launcher.rom/...` de solo lectura con un permiso de lectura de un solo uso, así que no necesitan acceso propio a ninguna carpeta.
- Vita3K: `org.vita3k.emulator/.Emulator` con el extra de tipo array de cadenas `AppStartParameters = ["-r", "<title ID>"]`.
- Las líneas de arranque de los metadatos de Pegasus se usan sin cambios.

### Citra MMJ en una consola de doble pantalla

Citra MMJ pone por defecto la pantalla táctil en la pantalla de arriba. Para tener la imagen principal arriba y ocultar los botones en pantalla, cierra Citra y pon esto en `/sdcard/citra-emu/config/config-mmj.ini`:

```ini
[Controls]
input_overlay_hide = True
[Layout]
landscape_swap_screen = True
```

## Comentarios

Duo Launcher toma forma según cómo se usa. Si falta algo, hay algo confuso o roto, o has hecho un tema que merece la pena compartir, abre una [incidencia](https://github.com/GenKnot/DuoLauncher/issues).

## Licencia

[MIT](LICENSE)
