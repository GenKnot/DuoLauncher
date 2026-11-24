<p align="center"><img src="docs/logo.png" alt="Duo Launcher" width="520"></p>

# Duo Launcher

<p align="center"><a href="README.md">English</a> · <a href="README.fr.md">Français</a> · <a href="README.zh-CN.md">简体中文</a> · <a href="README.zh-TW.md">繁體中文</a> · <a href="README.ja.md">日本語</a> · <a href="README.ko.md">한국어</a> · <a href="README.es.md">Español</a> · <a href="README.de.md">Deutsch</a> · <a href="README.it.md">Italiano</a> · <b>Português</b> · <a href="README.ru.md">Русский</a></p>

Um launcher de jogos de duas telas para o **Retroid Pocket Duo** e outros portáteis Android com uma segunda tela, inspirado no menu inicial do 3DS.
Você aponta para as suas pastas de ROMs e ele encontra os jogos, separa por console, gênero e série, mostra tudo na tela de baixo com a arte na de cima e abre cada jogo no emulador que você escolher.
Os cheats também já vêm embutidos: quando um jogo não tem nenhum no portátil, ele baixa dos grandes bancos de cheats da comunidade (Sharkive, libretro, CWCheat), deixa você marcar só os que quiser, traduz os nomes para o idioma da interface com um toque e grava tudo onde o emulador lê, então eles funcionam assim que o jogo abre.

**Download:** o APK mais recente está na [página de Releases](https://github.com/GenKnot/DuoLauncher/releases/latest) (Android 11 ou mais novo). Instalar uma versão nova por cima da antiga mantém todas as configurações e o seu tempo de jogo.

## Por que mais um launcher?

O Duo Launcher começou como o launcher de um jogador para um portátil, e até hoje cresce a partir do jeito como esse portátil é usado de verdade. Ele se apoia em quatro promessas:

1. **Funciona com a biblioteca que você já tem** – não importa com qual frontend você montou, nada precisa ser refeito. Hoje ele lê o Pegasus e o ES-DE; outros frontends estão a caminho.
2. **IA quando você quiser, do jeito que você quiser** – vários serviços para escolher, a sua própria chave, um custo que você enxerga, e todo recurso de IA é opcional.
3. **O trabalho chato fica com ele** – trapaças, imagens, traduções, copiar arquivos, arrumar a casa.
4. **Tudo fica do seu jeito** – uma escolha para quase todo detalhe, e tudo o que mostra alguma coisa pode ser desligado.

Além de encontrar e abrir seus jogos, isto é o que essas promessas viram na prática:

- **Trapaças, do download ao primeiro quadro.** Quando um jogo não tem trapaças no portátil, ele busca nos grandes bancos de dados da comunidade – Sharkive para 3DS, libretro para NDS e GBA, CWCheat para PSP –, deixa você marcar só as que quiser, traduz os nomes para o seu idioma com um toque (com uma chave de IA, até com os nomes oficiais de personagens e itens do jogo) e grava tudo nos arquivos de trapaça de cada emulador: Azahar e Citra, DraStic, PPSSPP e RetroArch. Veja [Trapaças](#trapaças).
- **Uma IA que conhece a sua biblioteca.** Segure R e pergunte o que jogar agora, como passar de um chefe ou do que trata uma história – ela responde com base nos jogos que você realmente tem. A mesma IA traduz nomes e descrições, coloca cada jogo num gênero e numa série e desenha as capas, logos e fundos que estiverem faltando. Ela usa o serviço que *você* escolhe, com a sua própria chave, e cada pedido é contado nas Configurações com um custo estimado. Veja [Seu companheiro de IA](#seu-companheiro-de-ia) e [IA](#ia).
- **Configure pelo celular.** Ligue a Transferência Wi-Fi e um QR code aparece na tela de cima. Escaneie, e abre uma página no idioma do seu celular onde dá para mandar jogos, digitar chaves de API num teclado de verdade, importar e exportar configurações e baixar backups – sem cabo, sem gerenciador de arquivos. Veja [Ferramentas úteis](#ferramentas-úteis).
- **Um lugar só para tudo o que merece uma olhada.** A Central de problemas reúne os arquivos que ele não conseguiu reconhecer, consoles sem emulador, jogos sem capa e duplicados, e resolve a maior parte ali mesmo. Veja [Ferramentas úteis](#ferramentas-úteis).
- **Os dados de cada frontend, usados e deixados em paz.** Ele lê os metadados e as imagens do Pegasus e do ES-DE lado a lado, guarda o que acrescenta numa pasta própria, `DuoLauncher`, e nunca reescreve os arquivos de outro frontend, então todos podem dividir o mesmo cartão. Veja [De onde vêm os nomes, as imagens e os detalhes](#de-onde-vêm-os-nomes-as-imagens-e-os-detalhes).
- **Do seu jeito, até nos detalhes.** Temas de cores e pacotes de tema para compartilhar, modo claro e escuro, imagem e cor próprias para qualquer bloco, tamanho do texto, margem das capas, regras para arrumar os nomes, a sua própria ordem de jogos, onde ficam os dados e o Duo Lite, que alivia o trabalho de cada tela. Toda a sua configuração pode ser exportada num arquivo só e passada para um amigo – sem as suas chaves e sem o seu histórico de jogo. Veja [Aparência](#aparência) e [Temas](#temas).
- **Organizado por série, não só por console.** Ys, Castlevania, Super Robot Wars … cada jogo fica junto com o resto da sua série e entra num dos 14 gêneros, pela tabela de jogos embutida ou pela IA. Veja [Gêneros e séries](#gêneros-e-séries).

E algumas ideias que atravessam tudo:

- **Duas telas, duas funções.** A tela de baixo é para escolher: pastas grandes, grade, lista, menus. A de cima é para ver: o console no seu próprio fundo ilustrado, o logo do jogo sobre a capa, uma prévia em vídeo, quanto tempo você já jogou, uma nota para você mesmo ou uma conversa com a IA. Nada precisa disputar espaço com nada.
- **O controle vem primeiro.** Tudo funciona sem tocar na tela: um teclado feito para o direcional, direções que continuam rolando enquanto você segura, L / R para virar a página, segurar L para pesquisar por voz, segurar R para falar com a IA. O toque também funciona, só não é obrigatório.

### Ao lado de outros frontends

| | Duo Launcher | Outros frontends |
|---|:-:|:-:|
| Trapaças baixadas, escolhidas, traduzidas e configuradas para cada emulador | ✅ | raro |
| IA embutida que conhece sua biblioteca | ✅ | raro |
| Configuração pelo celular via Wi-Fi, com QR code | ✅ | raro |
| Central de problemas | ✅ | raro |
| Dados do Pegasus e do ES-DE lidos lado a lado | ✅ | raro |
| Agrupado por série | ✅ | raro |
| Agrupado por gênero | ✅ | comum |
| Lista de jogos e imagens em duas telas | ✅ | comum |
| Tempo de jogo e RetroAchievements | ✅ | comum |

Um panorama aproximado, com base no que outros frontends ofereciam em novembro de 2026. Muitos deles são excelentes naquilo em que focam; o Duo Launcher só coloca o esforço dele nas coisas acima. Se algo aqui estiver desatualizado, avise no [issue tracker](https://github.com/GenKnot/DuoLauncher/issues).

Sugestões e relatos de bugs são bem-vindos no [issue tracker](https://github.com/GenKnot/DuoLauncher/issues).

## Destaques

- **Cheats prontos para usar** – milhares de cheats para 3DS, NDS, PSP, GBA e todos os sistemas do RetroArch, do próprio portátil ou, quando um jogo não tem, baixados dos grandes bancos de cheats (Sharkive, libretro, CWCheat), onde você marca só os que quiser. Os nomes são traduzidos para o seu idioma com um toque, cada cheat pode ser editado e tudo vai direto para os arquivos de cheats do próprio emulador, funcionando assim que o jogo abre.
- **Um companheiro de IA** – segure R em qualquer lugar e fale: ele recomenda jogos da sua própria biblioteca, explica como passar de um trecho, conta sobre a história ou só bate papo. As respostas podem ser lidas em voz alta.
- **Duas telas, um launcher** – você escolhe na tela de baixo e vê logos, capas, prévias em vídeo, tempo de jogo e descrições na de cima.
- **Todos os emuladores num lugar só** – 3DS, NDS, GBA, GB/GBC, NES, SNES, N64, GameCube, Wii, Mega Drive, PS1, PS2, PSP, PS Vita e Switch, além de 22 consoles mais antigos pelo RetroArch (do Master System ao Dreamcast, PC Engine, Neo Geo, WonderSwan, Atari, arcade, DOS …), com o emulador escolhido por console ou por jogo. Jogos de vários discos aparecem como um só.
- **Já vem organizado** – cada jogo entra num de 14 gêneros e na sua série (Ys, Castlevania, Super Robot Wars …). A tela inicial mostra a biblioteca por console, gênero ou série, e as pastas inteligentes mantêm sempre em dia “Adicionados recentemente”, “Jogando”, “Quero jogar”, “Zerado” e “Mais bem avaliados”.
- **Completa o que falta** – capas reais dos pacotes de thumbnails da libretro, títulos oficiais em inglês e capas, logos e fundos desenhados por IA no estilo do próprio launcher.
- **Feito para um portátil** – protetor de tela seguro para OLED, transferência por Wi-Fi pelo navegador, backup de saves, estatísticas de jogo, progresso no RetroAchievements, um panorama do armazenamento e uma tela inicial que abre em um segundo.
- **Do seu jeito** – temas de cores e pacotes de tema, modo claro e escuro, tamanho do texto, margem das capas, uma tela de configurações como a de um console e configurações que você exporta e compartilha. 11 idiomas na interface.

## Capturas de tela

<p align="center"><img src="docs/screenshots/light-dark.png" alt="Modo claro e escuro"></p>

<table>
  <tr>
    <th>Definições ao estilo 3DS</th>
    <th>Definições no modo escuro</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_settings.png" alt="Definições ao estilo 3DS"></td>
    <td><img src="docs/screenshots/en_settings_dark.png" alt="Definições no modo escuro"></td>
  </tr>
  <tr>
    <th>Uso e custo da IA</th>
    <th>Exportar e importar a configuração</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_ai_usage.png" alt="Uso e custo da IA"></td>
    <td><img src="docs/screenshots/en_config.png" alt="Exportar e importar a configuração"></td>
  </tr>
  <tr>
    <th colspan="2">Ilustrações desenhadas para o modo escuro</th>
  </tr>
  <tr>
    <td colspan="2" align="center"><img src="docs/screenshots/en_dark_home.png" width="50%" alt="Ilustrações desenhadas para o modo escuro"></td>
  </tr>
  <tr>
    <th>Início por gênero, com ícones de massinha</th>
    <th>Início por série</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_genres.png" alt="Início por gênero, com ícones de massinha"></td>
    <td><img src="docs/screenshots/zh_series.png" alt="Início por série"></td>
  </tr>
  <tr>
    <th>Gênero e série na página do jogo</th>
    <th>Nota, Perguntar à IA, status, como jogar, saves</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_game_series.png" alt="Gênero e série na página do jogo"></td>
    <td><img src="docs/screenshots/zh_game_menu.png" alt="Nota, Perguntar à IA, status, como jogar, saves"></td>
  </tr>
  <tr>
    <th>Teclado para o controle; títulos em inglês também valem</th>
    <th>Perguntando à IA o que jogar</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_keyboard.png" alt="Teclado para o controle; títulos em inglês também valem"></td>
    <td><img src="docs/screenshots/zh_ai_chat.png" alt="Perguntando à IA o que jogar"></td>
  </tr>
  <tr>
    <th>Ferramentas de IA: séries, sugestões, armazenamento, lembrete de pausa</th>
    <th>Armazenamento: tamanho por console, jogos grandes nunca jogados</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_tools_ai.png" alt="Ferramentas de IA: séries, sugestões, armazenamento, lembrete de pausa"></td>
    <td><img src="docs/screenshots/zh_storage.png" alt="Armazenamento: tamanho por console, jogos grandes nunca jogados"></td>
  </tr>
  <tr>
    <th>Página do jogo com prévia em vídeo</th>
    <th>Logo, capa ou vídeo, por jogo</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_game.png" alt="Página do jogo"></td>
    <td><img src="docs/screenshots/en_menu.png" alt="Menu do jogo"></td>
  </tr>
  <tr>
    <th>Modo lista</th>
    <th>Estatísticas de jogo</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_list.png" alt="Modo lista"></td>
    <td><img src="docs/screenshots/en_stats.png" alt="Estatísticas de jogo"></td>
  </tr>
  <tr>
    <th>Temas de cores</th>
    <th>As cores do tema chegam aos fundos</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_themes.png" alt="Menu de temas"></td>
    <td><img src="docs/screenshots/en_pink.png" alt="Tema rosa"></td>
  </tr>
  <tr>
    <th>Modo escuro</th>
    <th>Modo escuro</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_d3ds.png" alt="Modo escuro, 3DS"></td>
    <td><img src="docs/screenshots/en_dstats.png" alt="Modo escuro, estatísticas"></td>
  </tr>
  <tr>
    <th>Transferência Wi-Fi</th>
    <th>Ferramentas de organização</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_wifi.png" alt="Transferência Wi-Fi"></td>
    <td><img src="docs/screenshots/en_tools.png" alt="Ferramentas"></td>
  </tr>
  <tr>
    <th>GameCube, com o Dolphin</th>
    <th>Segure A para mover uma pasta</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_gamecube.png" alt="GameCube"></td>
    <td><img src="docs/screenshots/zh_move.png" alt="Movendo uma pasta"></td>
  </tr>
  <tr>
    <th>Escolha as trapaças antes de importar, com nomes traduzidos</th>
    <th>Detalhes do jogo: tamanho, discos, capturas</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheat_pick.png" alt="Escolhendo trapaças"></td>
    <td><img src="docs/screenshots/zh_details.png" alt="Detalhes do jogo"></td>
  </tr>
  <tr>
    <th>Trapaças de 3DS, traduzidas com um toque</th>
    <th>Trapaças de NDS para o DraStic, base de dados chinesa</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheats.png" alt="Trapaças de 3DS"></td>
    <td><img src="docs/screenshots/zh_cheats_nds.png" alt="Trapaças de NDS"></td>
  </tr>
  <tr>
    <th>Trapaças do RetroArch (GBA)</th>
    <th>Editar, adicionar, baixar</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheats_gba.png" alt="Trapaças de GBA"></td>
    <td><img src="docs/screenshots/zh_cheat_menu.png" alt="Menu de trapaças"></td>
  </tr>
  <tr>
    <th>Renomear ou excluir uma trapaça</th>
    <th>Traduzir todas as trapaças de uma vez</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/zh_cheat_edit.png" alt="Editar uma trapaça"></td>
    <td><img src="docs/screenshots/zh_tools.png" alt="Ferramentas"></td>
  </tr>
  <tr>
    <th>Protetor de tela (seguro para OLED)</th>
    <th>11 idiomas</th>
  </tr>
  <tr>
    <td><img src="docs/screenshots/en_saver.png" alt="Protetor de tela"></td>
    <td><img src="docs/screenshots/en_lang.png" alt="Idiomas"></td>
  </tr>
</table>

## Primeiros passos

1. **Instale o APK** da [página de Releases](https://github.com/GenKnot/DuoLauncher/releases/latest) e abra.
2. **Siga o guia de boas-vindas.** Na primeira vez que você abre o launcher, um guia curto passa pela configuração, uma tela de cada vez:
   - **“Encontramos suas configurações e registros anteriores”** – aparece primeiro, e só quando a cópia automática do launcher está em algum armazenamento (depois de reinstalar, ou num portátil novo com o mesmo cartão). “Restaurar” deixa você escolher o que volta, como em “Importar uma configuração”; “Começar do zero” segue com o guia normalmente. Veja [Configurações e compartilhamento](#configurações-e-compartilhamento).
   - **Idioma.**
   - **Permissões**, todas numa tela só: acesso aos arquivos (para achar os seus jogos), acesso ao uso (para que o tempo de jogo conte só quando o jogo está de fato na tela) e o papel de tela inicial, marcado como “opcional”. Essa tela também fica em Configurações → Sistema → Permissões.
   - **O que foi encontrado** – quantos consoles e jogos a busca achou, e onde ela procurou: na pasta `Roms` na raiz de cada armazenamento e em todas as pastas que você adicionou. Se a busca ainda estiver rodando, a tela avisa e se completa quando ela terminar. Uma porta leva à [Central de problemas](#ferramentas-úteis) para tudo o que ela não conseguiu identificar.
   - **IA (opcional)** – o que os recursos de IA fazem, e que você paga ao serviço que escolher pelo que usar. Depois, *Digitar as chaves no celular (Wi-Fi)*, *Configurar no portátil* ou *Depois*.
   - **Os botões** – o que cada um faz, numa tela só.

   Toda tela tem *Pular o guia*, e o B volta uma tela. Configurações → Sistema → Ver o guia de boas-vindas de novo mostra o guia outra vez.
   *Por quê:* os primeiros minutos decidem se o launcher faz sentido, e quem acabou de chegar não deveria precisar ler esta página para chegar lá. Ver logo de cara quantos jogos foram encontrados também responde a primeira pergunta de todo mundo (“ele achou os meus jogos?”). Quem já sabe o que está fazendo pode pular o guia em qualquer tela.
3. **Deixe ele achar os seus jogos.** Qualquer pasta `Roms` no armazenamento interno ou no cartão SD é encontrada sozinha. Outras pastas podem ser adicionadas em Configurações → Início e biblioteca → Pastas de jogos. Os consoles são reconhecidos pelo nome da pasta (`gba`, `Nintendo DS`, `psx` …) e pela extensão dos arquivos.
4. **Escolha os emuladores** onde você quiser algo diferente do padrão: aperte Y numa pasta de console para o console inteiro, ou num jogo só para aquele jogo. Só aparecem os emuladores que estão instalados.
5. **Opcional: coloque uma chave de IA** em Configurações → IA e voz → Serviços de IA e chaves – no próprio portátil ou, com mais conforto, pelo computador ou celular via Wi-Fi (Configurações → Saves, transferência, compartilhamento → Transferência Wi-Fi, ou *Digitar no computador ou celular* no mesmo menu). Tudo funciona sem chave, menos os recursos de IA.
6. **Opcional: use como tela inicial** (Configurações → Sistema → Definir como tela inicial (ao ligar)) para que ele abra junto com o portátil. O bloco “Início do Android” sempre leva de volta à tela inicial do sistema.

## O básico

**A tela inicial** (tela de baixo) tem as suas pastas de consoles e alguns blocos fixos: “Continuar” (volta direto para o último jogo), “Jogados recentemente”, “Favoritos”, “Nova pasta”, “Pesquisar”, “Estatísticas”, “Armazenamento”, “O que jogar?”, “Configurações” e “Início do Android”. SELECT alterna a tela inicial entre consoles, gêneros e séries.

**Uma pasta** mostra os jogos numa grade de capas. X deixa as capas menores e, apertando mais uma vez, a grade vira uma lista com nomes, tempo de jogo e quando você jogou pela última vez. SELECT ordena (“Padrão”, “Nome”, “Jogados recentemente”, “Mais jogados”) e pode mostrar só versões em chinês simplificado, chinês tradicional ou inglês. L2 / R2 pulam para a inicial anterior ou a próxima – títulos chineses contam pela inicial do pinyin.

**A tela de cima** acompanha o que está selecionado: um console no seu fundo, com o número de jogos e o emulador; ou o logo do jogo sobre a capa desfocada, com console, gênero · série, emulador, tempo de jogo e uma descrição curta, uma prévia em vídeo depois de um instante, ou a imagem de onde você parou.

**Y** é o menu do que estiver selecionado. Num jogo ele é curto, e o que se usa menos fica uma porta mais para dentro: emulador, favorito, adicionar a / remover de uma pasta, “Trapaças” ›, “Ajuda da IA” › (“Perguntar à IA”, “Como jogar”), “Informações do jogo” › (“Detalhes”, “Gênero”, “Status” e nota de estrelas, “Nota”, “Tela superior” – o que a tela de cima mostra, “Imagens com IA para este jogo”) e “Mais” › (“Saves”, “Marcar como visto”, ocultar, “Apagar o arquivo”). Numa pasta de console: emulador padrão, “Renomear”, “Juntar pastas” ›, “Marcar todos como vistos”, ocultar. Num bloco fixo: nome, “Cor”, “Ícone” e fundo. A linha selecionada se explica numa frase curta no rodapé do menu.

*Por quê:* um menu com quinze linhas é difícil de ler num portátil. As portas deixam na frente o que se usa todo dia, e a dica de uma linha poupa qualquer um de adivinhar o que uma opção faz antes de apertar.

**Apagar o arquivo** (Y → “Mais”) pergunta antes, mostrando o tamanho e avisando que os saves ficam; o cursor começa em “Cancelar”, então apertar A duas vezes rápido demais não apaga nada. Um jogo de vários discos perde todos os arquivos de uma vez, e um jogo apagado de uma lista da Central de problemas (“Jogos duplicados”, por exemplo) some dessa lista na hora.

*Por quê:* duplicados e jogos que você já terminou saem sem precisar de computador.

**Dicas que aparecem uma vez só.** Na primeira vez que um recurso fica ao seu alcance, uma linha aparece logo acima da legenda dos botões, na tela de baixo. Ela aparece uma vez só e some depois de uns nove segundos ou quando você aperta um botão (menos nos primeiros dois segundos, mais ou menos, para não sumir antes de dar tempo de ler; o botão continua fazendo o que faz). Por enquanto são quatro:

- na tela inicial: segure A para pegar um bloco e mudá-lo de lugar, Y para as opções daquele bloco;
- de volta à tela inicial depois de sair de uma pasta: SELECT mostra a tela inicial por console, por gênero ou por série;
- num jogo dentro de uma pasta de console, na ordem padrão: segure A para levar o jogo aonde você quiser, Y para emulador, trapaças, saves e mais;
- na pasta “Não reconhecidos”: aperte A num arquivo para dizer de qual console ele é.

Configurações → Sistema → Dicas de recursos novos desliga as dicas, e “Mostrar todas as dicas de novo” traz de volta as que você já viu.
*Por quê:* segurar A para mover, ou o SELECT na tela inicial, são fáceis de passar despercebidos e difíceis de adivinhar. Uma linha na hora em que dá para usar o recurso fica mais na memória do que um manual, e uma vez basta: dica que volta toda hora vira ruído.

### Controles

| Botão | Ação |
|---|---|
| Direcional / analógico esquerdo | Mover; segure para continuar andando |
| Esquerda / direita, L / R numa lista ou menu | Uma página por vez |
| A | Abrir / jogar; segure A para pegar e mudar de lugar uma pasta da tela inicial ou um jogo dentro de uma pasta de console |
| B | Voltar |
| L / R | Console anterior / seguinte (nas Configurações: categoria) |
| Segurar L | Pesquisar por voz |
| Segurar R | Falar com a IA |
| L2 / R2 | Inicial anterior / seguinte |
| X | Tamanho das capas, depois modo lista |
| Y | Menu do jogo, pasta ou bloco selecionado |
| START | Configurações (de qualquer ponto da tela inicial, mesmo com o bloco “Configurações” oculto); num menu, o mesmo que A |
| SELECT | Na tela inicial: consoles / gêneros / séries; numa pasta: ordem e filtro de idioma |
| R3 | Pesquisar |
| No teclado de pesquisa | A digita, B apaga, X espaço, Y teclado do sistema, SELECT limpa, START ou R3 conclui, segure L para falar |
| No chat com a IA | Segure R para falar, Y para digitar, X para uma conversa nova, SELECT para o histórico, L2 / R2 para rolar para trás, B para fechar; o direcional e o A escolhem entre os jogos embaixo |

Tudo também funciona por toque: toque para selecionar, toque de novo para abrir, deslize para rolar e toque nos botõezinhos do topo no lugar de L, R, X, SELECT ou Pesquisar. A/B e X/Y podem ser trocados para ter a confirmação no estilo Nintendo ou Xbox (Configurações → Jogo → Botões).

## Sua biblioteca

- **Acha os seus jogos sozinho.** O armazenamento interno e os cartões SD são vasculhados atrás de pastas `Roms`; outras podem ser adicionadas em Configurações → Início e biblioteca → Pastas de jogos. A última busca fica em cache, então a tela inicial abre em um segundo e a próxima busca roda quietinha em segundo plano.
- **Consoles** – 3DS, NDS, GBA (e as pastas de GBA com vibração e de hacks), GBC, GB, NES, SNES, N64, GameCube, Wii, Mega Drive, PlayStation 1/2/3, PSP, PS Vita e Switch, com arte também para PS4, PS5, Xbox 360 e Xbox Series. Todos os outros consoles do pack Pegasus G também são reconhecidos: Virtual Boy, Master System, Game Gear, 32X, Mega-CD, Saturn, Dreamcast, NAOMI, PC Engine e o seu CD, PC-FX, Neo Geo CD, Neo Geo Pocket Color, WonderSwan e WonderSwan Color, Pokémon Mini, Atari Lynx, Atari 2600, Game & Watch, 3DO, DOS e arcade.
- **Jogos de vários discos como um só.** `Disc A/B`, `Disc 1/2`, `CD1/CD2`, `(Disc 2 of 3)` na mesma pasta, ou uma lista `files:` do Pegasus, viram uma entrada só. RetroArch e DuckStation recebem uma playlist `.m3u` dos discos, para você trocar de disco durante o jogo.
- **Jogos e apps de Android** – os jogos instalados ganham uma pasta própria automaticamente; outros apps (vídeo, navegador …) podem ser adicionados a uma pasta “Apps”.
- **Funciona com o que você já tem.** Ele lê o `metadata.pegasus.txt` do Pegasus, o `gamelist.xml` e a `downloaded_media` do ES-DE / EmulationStation, as thumbnails do RetroArch e as pastas `media/`, `covers/` e `images/` ao lado dos jogos. Nada disso é alterado; o que o launcher acrescenta vai para a pasta `DuoLauncher` dele (veja [Suas informações de jogo](#suas-informações-de-jogo)).
- **Percebe jogos novos.** Copie jogos para o cartão e a próxima busca encontra; eles ganham um selo NEW por uma semana (ou até serem jogados) e o launcher oferece separá-los em gêneros e séries. A pergunta pode ser desligada. Y → "Marcar como visto" tira o selo de um jogo; "Marcar todos como vistos" (Y num console ou pasta, ou Configurações → Início e biblioteca) tira de vários de uma vez, e a chave "Selos NEW" ali desliga os selos de vez.
- **Juntar pastas.** Duas pastas do mesmo console em lugares diferentes (duas pastas de PS1, por exemplo) podem aparecer como uma só: Y → “Juntar pastas” num console. Cada jogo mantém o seu emulador, pastas com o mesmo nome se juntam sozinhas e “Separar” desfaz a junção.
- **Suas próprias pastas.** O bloco “Nova pasta” cria uma pasta com nome, cor, ícone e fundo próprios. Adicione qualquer jogo ou app a ela com Y e tire de novo com Y → “Remover desta pasta”.
- **Pastas inteligentes** se enchem sozinhas: “Adicionados recentemente” (os últimos 30 dias), “Ainda não jogados”, “Mais jogados”, “Jogando”, “Quero jogar”, “Zerado” e “Mais bem avaliados”. Y numa delas mostra a regra.
- **Continuar e favoritos.** O primeiro bloco da tela inicial volta direto para o último jogo. Marque jogos com estrela pelo Y para juntá-los na pasta “Favoritos”.
- **Esconda o que você não joga** – uma pasta de console, ou um único jogo ou app (Y → “Ocultar esta pasta” / “Ocultar este jogo”); traga de volta em Configurações → Início e biblioteca.
- **Limpe os nomes dos jogos.** Nomes tirados direto dos arquivos de ROM muitas vezes ficam assim: `0153 - Legend of Zelda, The - The Minish Cap (USA) [!]`. Configurações → Início e biblioteca → Limpar nomes dos jogos tem seis chaves, todas desligadas no começo: “Tirar região e idiomas”, “Tirar versão e marcas de dump”, “Tirar o número do início”, “Sublinhados e pontos viram espaços”, “‘Zelda, The’ vira ‘The Zelda’” e “Tirar tudo o que está entre parênteses”. Ao lado de cada chave aparece um exemplo da sua própria biblioteca com o nome antes e depois, e “Ver todas as mudanças” lista todos os jogos que seriam renomeados. *Por quê:* só muda o nome na tela. Os arquivos no cartão continuam exatamente como estão, então emuladores, saves e outros frontends não são afetados. Títulos que vêm do Pegasus, do ES-DE ou das suas próprias edições ficam como estão, porque alguém já os escolheu. Tags que diferenciam um jogo do outro, como `(Beta)` ou `(Hack)`, são mantidas, a não ser que você ligue a chave dos parênteses.
- **Coloque os jogos na sua ordem.** Dentro de uma pasta de console, segure A num jogo (ou toque e segure) para pegá-lo, leve-o com o direcional até onde quiser e solte para deixá-lo ali. A ordem fica guardada para aquele console, sobrevive a novas buscas e entra no backup completo. Jogos adicionados depois ficam no lugar de sempre. Y no console → “Voltar à ordem original” desfaz tudo. Só dá para mudar jogos de lugar na ordem “Padrão” e sem filtro de idioma, porque numa ordem por nome ou por tempo de jogo a sua ordem não faria sentido. *Por quê:* os jogos que você está jogando agora, ou uma série na ordem de lançamento, ficam melhor juntos no topo, e nenhuma ordenação automática tem como saber disso.
- **O seu catálogo.** Depois de cada busca, a biblioteca inteira é gravada em `DuoLauncher/catalog.tsv` (console, arquivo, título, título em inglês, gênero, série, tempo de jogo, status, nota) – para planilhas, outras ferramentas ou para a IA.

### Gêneros e séries

Navegar por dois mil jogos só por console cansa, então cada jogo também ganha um de 14 gêneros (RPG, ação, aventura, plataforma, tático, estratégia, tiro, quebra-cabeça, corrida, esporte, luta, simulação, party e música, outros) e a sua série.

- **Primeiro, o que já vem embutido.** Uma tabela dentro do launcher (`assets/gamedb.tsv`, 3.860 nomes de arquivo que cobrem todos os jogos das listas dos packs Pegasus G para esses consoles, com o nome `.iso` e o `.cso` quando o pack tem os dois) já conhece o gênero, a série e o título oficial em inglês dos romsets comuns, por console e nome de arquivo. Jogos que ela conhece não precisam de IA nem de internet.
- **IA para o resto.** Configurações → IA e voz → Deixar a IA organizar a biblioteca → “IA: separar por gênero” / “IA: séries de jogos” classifica os jogos que sobraram, e o resultado fica em `DuoLauncher/<console>/genres.txt` e `series.txt` no cartão – é feito uma vez só e sobrevive a reinstalações. Gêneros já escritos pelo Pegasus ou pelo ES-DE (“Action RPG”, “角色扮演” …) são encaixados nos 14 gêneros.
- **À mão** – Y → “Gênero” em qualquer jogo.
- **Na tela.** A tela de cima mostra o gênero · série do jogo, e SELECT na tela inicial transforma as pastas em gêneros ou séries. Os nomes das séries seguem o idioma da interface.

### Pesquisa

R3, o bloco “Pesquisar” ou o botão de pesquisa abrem na tela de cima um teclado feito para o direcional, com as suas pesquisas recentes e uma fileira de gêneros logo acima; os resultados vão enchendo a tela de baixo enquanto você digita. Títulos chineses batem com o pinyin, com as iniciais e com o começo de cada sílaba (`kdyg`, `mlao` → 马里奥), e todo jogo também bate com o título oficial em inglês, então `zelda` acha 塞尔达传说. Aberta de dentro de um console, a pesquisa procura só naquele console, com um botão “Todas as plataformas” para ampliar. Segure L para falar o nome em vez de digitar.

### Imagens que se preenchem sozinhas

- **Capas reais** – Configurações → Organizar → “Baixar capas” (também na Central de problemas, em “Jogos sem capa”) baixa as capas dos pacotes de thumbnails da libretro. Jogos de NDS e GBA são identificados pelo código de jogo dentro da ROM (ROMs zipadas também, com telas de título), então arquivos traduzidos e renomeados continuam achando as suas imagens; jogos de PlayStation 1/2, PSP, N64, GameCube, Wii, 3DS, GB/GBC, NES, SNES e Mega Drive são identificados pelo título em inglês. “Sem capa” quer dizer a mesma coisa em todo lugar: o jogo não tem capa (só logo ou fundo não conta), então a Central de problemas, “Baixar capas” e o menu de imagens por IA mostram o mesmo número.
- **Ícones direto das ROMs** – ícones de banner do NDS e ícones SMDH do 3DS; títulos de 3DS instalados mostram o nome no idioma da interface.
- **Capas que combinam** – as capas preenchem os blocos, ou ficam todas com a mesma margem pequena, média ou grande (Configurações → Aparência e som → Margem das capas); bordas brancas embutidas nas imagens das capas são cortadas.
- **Imagens por IA** – com uma chave de imagens, a IA desenha capa, fundo e logo de título. Para um jogo: Y → “Informações do jogo” → “Imagens com IA para este jogo”, que mostra se o jogo já tem cada uma; a nova substitui a atual, e o logo de título precisa do título em inglês (“IA: nomes e descrições” encontra). Para vários jogos: Configurações → IA e voz → Deixar a IA organizar a biblioteca → “IA: desenhar imagens que faltam”, que mostra quantos jogos estão sem cada tipo; a lista de consoles começa com “Todas as plataformas”, e dois consoles com o mesmo nome mostram o nome da pasta (uma pasta de hacks de GBA, por exemplo). Veja [IA](#ia).

## Jogando

- **Emulador por jogo ou por console** (Y). Só aparecem os emuladores instalados:

  | Console | Emuladores (o primeiro é o padrão) |
  |---|---|
  | 3DS | AzaharPlus, Azahar, Citra MMJ, Citra MMJ (modo desempenho) |
  | NDS | DraStic, DraStic (caminho simples), RetroArch · melonDS DS |
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
  | PS Vita | Vita3K (pelo ID do título, por exemplo um arquivo chamado `PCSE00000.psvita` ou um nome que contenha esse ID) |
  | Switch | Eden |
  | Virtual Boy | RetroArch · Beetle VB |
  | Master System / Game Gear | RetroArch · Genesis Plus GX, PicoDrive |
  | 32X | RetroArch · PicoDrive |
  | Mega-CD | RetroArch · Genesis Plus GX, PicoDrive |
  | Saturn | RetroArch · Yaba Sanshiro, Yabause |
  | Dreamcast / NAOMI | RetroArch · Flycast |
  | PC Engine / PC Engine CD | RetroArch · Beetle PCE Fast, Beetle PCE |
  | PC-FX | RetroArch · Beetle PC-FX |
  | Neo Geo CD | RetroArch · NeoCD |
  | Neo Geo Pocket Color | RetroArch · Beetle NeoPop, RACE |
  | WonderSwan / Color | RetroArch · Beetle Cygne |
  | Pokémon Mini | RetroArch · PokeMini |
  | Atari Lynx | RetroArch · Handy, Beetle Lynx |
  | Atari 2600 | RetroArch · Stella |
  | Game & Watch | RetroArch · Game & Watch |
  | 3DO | RetroArch · Opera |
  | DOS | RetroArch · DOSBox Pure |
  | Arcade | RetroArch · FinalBurn Neo, MAME 2003-Plus |

  Um `metadata.pegasus.txt` do Pegasus normalmente já traz a própria linha de inicialização para cada console; ela é usada primeiro, e os cores acima aparecem como opção ao lado dela.

- **Citra MMJ (modo desempenho)** – a mesma build do Citra MMJ reempacotada com o nome de pacote do benchmark AnTuTu, que muitos sistemas Android rodam com o clock no máximo. Só aparece quando esse pacote é mesmo o Citra.
- **Tempo de jogo e estatísticas** – quanto tempo e quando você jogou cada jogo pela última vez, no estilo do Switch; com o acesso ao uso liberado, conta só o tempo em que o jogo estava de fato na tela. O bloco “Estatísticas” mostra o total, a semana atual dia a dia, o tempo por console e os jogos mais jogados.
- **Onde você parou** – a captura de tela ou a imagem de save state mais recente de um jogo vira o fundo dele na tela de cima.
- **Status, nota e anotações** – marque um jogo como “Jogando”, “Quero jogar”, “Zerado” ou “Abandonado” e dê de uma a cinco estrelas (Y → “Status”); escreva ou fale uma linha sobre onde você está e o que fazer a seguir (Y → “Nota”). Tudo isso aparece na tela de cima da próxima vez que você pegar o jogo.
- **Detalhes do jogo** (Y → “Detalhes”) – arquivo, tamanho, discos, pasta, emulador, tempo de jogo, o título oficial em inglês e as capturas de tela do jogo na tela de cima.
- **Prévias em vídeo e sons** – o vídeo de um jogo começa na tela de cima depois de um instante (ou escolha logo ou capa por jogo: Y → “Tela superior”). Os sons suaves da interface e a musiquinha de banner de cada jogo de 3DS podem ser desligados.
- **Sai da frente enquanto você joga.** Assim que um jogo abre, a tela de baixo fica com o emulador, e vídeo, música e músicas de banner param. Além disso, o launcher libera as imagens que guarda na memória (medido: de uns 167 MB para 109 MB), deixa uma varredura do cartão que esteja em andamento esperando até você voltar, para não disputar o cartão com o emulador, e para a atualização do relógio e as animações da tela de cima. Na volta, as capas reaparecem em menos de um segundo.
  *Por quê:* o emulador fica com a memória e o cartão só para ele.
- **Quando um jogo não abre** – arquivo sumido, emulador não instalado, emulador que não abre aquele tipo de arquivo ou jogo que fecha na hora: cada caso ganha uma mensagem dizendo o que fazer. A [Central de problemas](#ferramentas-úteis) lista os consoles sem emulador instalado antes que você dê de cara com isso.
- **Lembrete de pausa** (Configurações → Jogo) – depois de uma, duas ou três horas num jogo, o launcher sugere uma pausa quando você volta.
- **RetroAchievements** – com o seu nome de usuário e a chave Web API, mostra os jogos recentes com o progresso das conquistas, e uma etiqueta 🏆 nos jogos que têm conquistas.

## Seu companheiro de IA

Tudo isso é opcional e usa o serviço de IA que você escolher, com a sua própria chave (veja [IA](#ia)).

- **Fale com ele de qualquer lugar.** Segure R na tela inicial, numa pasta ou no teclado de pesquisa e fale – ou aperte Y para digitar. A conversa ocupa a tela de cima; os jogos que ele recomenda ou encontra aparecem na de baixo, prontos para abrir com A. Pergunte o que jogar (“um RPG tranquilo, não muito longo”), refine (“algo com mais ação”), pergunte como passar daquele trecho do jogo selecionado, sobre a história ou um personagem, ou só bata papo. Ele lembra da conversa por meia hora.
  *Por quê:* a tela de baixo é onde se escolhem os jogos, então as sugestões da IA caem ali; a de cima fica livre para ler. E num portátil, segurar um botão de ombro e falar é mais rápido que qualquer menu.
- **Conversa nova, histórico salvo.** X no chat começa uma conversa nova; SELECT lista as anteriores, a mais recente no topo, para abrir de novo, apagar uma por uma ou apagar todas (também em Configurações → IA e voz → Histórico de conversas com a IA). Enquanto o chat está aberto, as prévias em vídeo, as músicas de banner do 3DS e os sons dos botões ficam em silêncio, e as respostas faladas tocam no volume de mídia.
  *Por quê:* uma pergunta nova não deve arrastar o assunto anterior, uma resposta que vale guardar não deve sumir ao reiniciar, e uma resposta falada não adianta nada com o trailer de um jogo tocando por cima. O histórico nunca vai junto numa configuração exportada com *Compartilhar*, mas entra na cópia automática em `DuoLauncher/profile/`, então reinstalar não apaga as conversas; e a chave “Salvar conversas com a IA”, dentro de Histórico de conversas com a IA, desliga o recurso.
- **Respostas em voz alta** – Configurações → IA e voz → Serviços de IA e chaves → Respostas de voz da IA: Soniox (com chave própria) ou OpenAI (a chave de imagens), com escolha de voz e de velocidade e um botão de teste. Fica desligado até você ligar.
- **O que jogar hoje** – Y no bloco “O que jogar?” faz a IA escolher seis jogos da sua biblioteca de acordo com o seu gosto, com um motivo para cada um. Um aperto simples no bloco sorteia um jogo, dando preferência aos que você nunca jogou.
- **Pergunte sobre um jogo** (Y → “Perguntar à IA”) – uma pergunta sobre o jogo selecionado, digitada ou falada, respondida na tela de cima.
- **Como jogar** (Y → “Como jogar”) – os controles (inclusive os botões de confirmar / cancelar trocados das versões japonesas) e o básico que um iniciante precisa saber, escritos uma vez por jogo e guardados.
- **Ainda sem chave?** Segurar R explica como colocar uma – no portátil ou pelo computador via Wi-Fi.

## Trapaças

Y → “Trapaças” num jogo lista os cheats dele; A liga ou desliga cada um.

| | 3DS (Azahar, Citra) | NDS (DraStic) | PSP (PPSSPP) | RetroArch (GBA, GB, GBC, NES, SNES, N64, MD, PS1, …) |
|---|---|---|---|---|
| Onde ficam | `azahar/cheats/` e `citra-emu/cheats/`, `<title ID>.txt` – gravados para os dois, então trocar de emulador não perde nada | a lista completa em `DuoLauncher/cheats/NDS/`; os que estão ligados vão para os cheats personalizados do próprio DraStic (`DraStic/cheats/<game file name>.cht`), que funcionam também com ROMs traduzidas | `PSP/Cheats/<game ID>.ini`, os arquivos de cheat do próprio PPSSPP; o ID do jogo é lido do ISO, CSO ou EBOOT.PBP | `RetroArch/cheats/<core>/<game file name>.cht`, carregados pelo RetroArch quando o jogo abre |
| O que já vem no aparelho | – | o `usrcheat.dat` do DraStic (em chinês nos portáteis Retroid), identificado só pelo código do jogo, então ROMs com patch e traduzidas acham os seus cheats mesmo quando o próprio DraStic não mostra nenhum | o `cheat.db` do PPSSPP e, nos portáteis Retroid, arquivos de cheat em chinês para cerca de 4.300 jogos | a Retroid traz cheats em chinês para centenas de jogos (`000 中文金手指` e as pastas dos cores); um jogo sem arquivo próprio recebe uma cópia, identificada pelo nome |
| Download | [Sharkive](https://github.com/FlagBrew/Sharkive), cerca de 650 jogos, pelo ID do título lido da ROM | [libretro-database](https://github.com/libretro/libretro-database), cerca de 4.200 jogos, pelo código do jogo no cabeçalho da ROM | [CWCheat-Database-Plus+](https://github.com/Saramagrean/CWCheat-Database-Plus-), cerca de 4.300 jogos, pelo ID do jogo | [libretro-database](https://github.com/libretro/libretro-database) para GBA, pelo código do jogo no cabeçalho da ROM |

- **Escolha antes de importar** – um download primeiro lista o que encontrou, já traduzido, com “Selecionar tudo” / “Desmarcar tudo”; só os marcados são adicionados. *Por quê:* uma base de dados pode ter dezenas de códigos por jogo, e a maioria dos jogadores quer uns três.
- **Traduzir** – os nomes dos cheats em inglês passam para o idioma da interface com um toque, para um jogo ou para todos de uma vez (Configurações → Organizar). Os cheats comuns vêm de um glossário embutido (无限血量, 金钱最大, 主代码 …); o resto vai para o serviço gratuito MyMemory, com o endpoint gratuito do Google como segunda tentativa. Marcas de versão (`v1.2`) e dicas de botão (`(START+L)`) são mantidas, e o nome original em inglês continua no arquivo como comentário.
- **Traduzir com IA** – com uma chave de IA, os cheats de um jogo vão para o modelo junto com o nome do jogo, para que ele use os nomes oficiais de personagens e itens (鬼神林克, 心之容器 …).
- **Editar** – renomeie ou exclua qualquer cheat, apague todos os cheats de um jogo de uma vez, ou adicione os seus com os códigos (códigos de 3DS, NDS e PSP são conferidos no formato `XXXXXXXX YYYYYYYY`).
- **Seguro para o DraStic** – patches de flashcart (desvios de antipirataria, correções de overlay e de Wi-Fi) ficam de fora dos downloads de NDS porque o DraStic trava com eles ligados, e no máximo 256 linhas de código podem estar ligadas ao mesmo tempo, o limite que o DraStic consegue carregar.
- **Seguro para os seus arquivos** – na primeira vez que um arquivo de cheat de 3DS feito à mão é alterado, o original é guardado como `.txt.bak`.
- **RetroArch, uma vez só** – ative uma vez no RetroArch Quick Menu → Cheats → “Auto-Apply Cheats During Game Load”, e os cheats que você ligar aqui ficam ativos desde o momento em que o jogo abre.

## Aparência

- **A tela de cima (1920×1080)** – cada console no seu próprio fundo de papel recortado, ou o logo do jogo selecionado sobre a capa desfocada, com etiquetas e uma descrição curta; o relógio e a bateria ficam no canto.
- **A tela de baixo (1280×960)** – pastas de consoles, uma grade de capas com zoom e um modo lista, com dicas de botões no rodapé que continuam curtas e deixam de fora o que não cabe quando o texto está grande.
- **Temas** – sete esquemas de cores, cores próprias e pacotes de tema com arte de consoles, fundos, fonte, sons e música. Um tema também colore os fundos de traço da tela de cima. Veja [Temas](#temas).
- **Modo claro e escuro** – segue o sistema ou você define, com um conjunto separado de fundos desenhado para cada um.
- **Tudo na tela inicial é seu.** Cada bloco fixo e cada pasta de console pode ser renomeado, ganhar outra cor, um ícone próprio (uma imagem sua ou uma desenhada pelo serviço de imagens no estilo massinha do launcher), mudar de lugar (segure A, ou toque e segure) ou ser ocultado, e volta em Configurações → Início e biblioteca → Pastas ocultas. Blocos fixos como “Estatísticas”, “Armazenamento” e “O que jogar?” também podem ter o seu próprio fundo na tela de cima.
- **Visual dos consoles** – a imagem, o fundo claro e escuro e a cor da pasta de cada console podem ser trocados (Configurações → Aparência e som → Aparência das plataformas). Um fundo escuro pode ser redesenhado pelo serviço de imagens a partir do claro, para que os dois sempre combinem.
- **Tamanho do texto** – quatro tamanhos para as duas telas.
- **Protetor de tela** – depois de alguns minutos parado, a tela de cima mostra as suas capas com um relógio e a de baixo fica preta. O texto muda de canto a cada imagem e se desloca devagar, para nada marcar um painel OLED.
- **Duo Lite** (Configurações → Aparência e som, primeira linha) – uma chave só que desliga as prévias em vídeo e a música dos banners do 3DS, a arte desfocada atrás de um jogo, os pontinhos que flutuam na tela de cima e o leve balanço da imagem do console, a animação de entrada e a moldura de seleção que pulsa. Enquanto ela está ligada, as chaves “Prévias em vídeo” e “Música dos banners do 3DS” aparecem desligadas e explicam o motivo quando você aperta; as suas escolhas voltam quando o Duo Lite é desligado. Medido no Duo, com o launcher parado na tela inicial: a tela de cima normalmente redesenha uns 600 quadros em 5 segundos, e no Duo Lite, nenhum.
  *Por quê:* para sessões longas na bateria, ou se você simplesmente prefere uma tela parada.

## Configurações e compartilhamento

- **Uma tela de configurações como a de um console.** START abre uma página só: as categorias à esquerda (Aparência e som, Início e biblioteca, Jogo, IA e voz, Saves, transferência, compartilhamento, Organizar, Sistema), as opções de uma categoria à direita com os valores atuais e as chaves de liga/desliga, e a tela de cima mostrando a versão, o espaço livre em cada unidade e quais serviços de IA estão ligados. L / R trocam de categoria.
  Algumas opções são portas para um menu menor (Serviços de IA e chaves, Deixar a IA organizar a biblioteca, Central de problemas …). Com o cursor numa delas, uma linha no rodapé do cartão diz o que tem lá dentro, e o B, de dentro, volta para a mesma opção com os valores já atualizados.
  *Por quê:* uma categoria com uma dúzia de opções vira um paredão numa tela de 1280×960. Agrupar as configurações menos usadas atrás de uma porta deixa cada categoria curta, e a nota de uma linha mostra o que uma porta guarda antes de você entrar.
- **Faça backup ou compartilhe a sua configuração.** Configurações → Saves, transferência, compartilhamento → Exportar minha configuração pergunta o que incluir: as configurações e as suas imagens, o histórico de jogo, status, avaliações e notas dos jogos, chaves e contas, o uso da IA e as conversas, e os dados dos jogos nas pastas `DuoLauncher` – ou um de dois atalhos, *Compartilhar* (só configurações e imagens) e *Backup completo* (tudo). “Importar uma configuração” pega uma de `DuoLauncher/exports` ou da pasta Download – enviada por Wi-Fi, por um app de mensagens ou num cartão – e mostra o que ela traz, para você desmarcar o que deve ficar como está. Na página Wi-Fi, mandar um arquivo para *Configurações para importar* coloca o arquivo direto em `DuoLauncher/exports`, e ele já fica esperando em “Importar uma configuração”.
  *Por quê:* uma configuração passada para um amigo nunca pode levar as suas chaves nem o seu histórico, enquanto um backup seu deve trazer de volta exatamente o mesmo launcher num portátil novo ou depois de reinstalar. Um backup completo guarda as suas chaves, então a exportação avisa para você não passá-lo adiante, e uma chave sua nunca é apontada para um endereço que veio da configuração de outra pessoa.
- **Onde ficam os dados** (Configurações → Saves, transferência, compartilhamento) – para onde vai a pasta `DuoLauncher`: gêneros, séries e nomes dados pela IA de cada console, imagens baixadas ou desenhadas pela IA, backups dos saves, exportações, temas e a cópia das configurações descrita abaixo. “Junto com os jogos” é o padrão e o jeito de sempre: os dados de cada console no armazenamento onde estão os jogos dele, os backups no cartão. “Cartão” põe tudo no cartão TF, que leva os seus dados junto para outro portátil (sem cartão, o armazenamento interno quebra o galho por enquanto). “Interno” deixa tudo no portátil, mesmo quando você troca de cartão. O que foi gravado antes da mudança continua sendo lido onde está. Com “Cartão” ou “Interno” escolhido, “Mover os dados existentes para cá” mostra o tamanho e então traz as pastas `DuoLauncher` dos outros armazenamentos; os arquivos que já estão lá são mantidos, e as listas de cheats do NDS e as playlists de jogos com vários discos ficam onde os emuladores esperam.
  *Por quê:* quem decide onde ficam os seus dados é você; nada disso vem fixo no código.
- **Uma cópia automática das configurações e registros.** “Manter uma cópia das configurações e registros” (no mesmo menu, ligado por padrão) copia as configurações, as suas imagens, o tempo de jogo, status, avaliações e notas, a ordem dos jogos, o uso da IA e as conversas para `DuoLauncher/profile/` no armazenamento escolhido – a primeira vez em até um minuto, depois sempre que algo muda. A verificação acontece mais ou menos uma vez por minuto enquanto o launcher está na tela, nunca durante um jogo, e só a cópia mais recente é guardada. As chaves de API ficam de fora, a não ser que “Incluir as chaves de API na cópia” esteja ligado, com o aviso de que qualquer pessoa com o cartão poderia lê-las. “Salvar uma cópia agora” faz uma na hora e mostra quando foi feita a última. Depois de reinstalar, ou num portátil novo com o mesmo cartão, o guia de boas-vindas começa com “Encontramos suas configurações e registros anteriores”: “Restaurar” deixa você escolher as partes, como em “Importar uma configuração”, e “Começar do zero” deixa tudo como está.
  *Por quê:* até agora, desinstalar ou restaurar o aparelho para os padrões de fábrica levava junto o tempo de jogo, as notas e as configurações, a menos que você tivesse lembrado de exportar tudo na mão.
- **11 idiomas** – 简体中文, 繁體中文, English, 日本語, 한국어, Français, Español, Deutsch, Italiano, Português, Русский.

## Ferramentas úteis

- **Central de problemas** – a primeira opção de Configurações → Organizar. Ela verifica a biblioteca em segundo plano toda vez que você entra e junta tudo o que talvez mereça uma olhada, com uma contagem em cada linha:
  - **Permissões que faltam** – abre a tela de permissões.
  - **Arquivos não reconhecidos** – abre a pasta “Não reconhecidos”, onde A num arquivo escolhe o console dele.
  - **Consoles sem emulador instalado** – escolha um console para ver os emuladores instalados (A passa a usar um deles) e os que o launcher sabe que funcionariam depois de instalados, além de *Abrir este console*.
  - **Arquivos que não abrem** – arquivos vazios e arquivos que uma lista de jogos do Pegasus ou do ES-DE cita, mas que não estão no cartão.
  - **Jogos sem capa** – um menuzinho com “Ver quais são os N jogos”, “Baixar capas” (roda ali mesmo com a linha “Procurando capas online…”, e depois o menu volta com o novo número e quantas foram achadas, ou avisa que nada foi encontrado online) e “Desenhar capas para estes N jogos com IA”.
  - **Jogos duplicados** – mesmo console, mesmo nome e mesmo tamanho: quase certamente um jogo copiado duas vezes.

  A primeira linha avisa que nada disso impede o launcher de funcionar, e só aparecem os problemas que existem de verdade. A tela *O que foi encontrado* do guia de boas-vindas também leva até aqui. B numa lista aberta daqui (Jogos sem capa, Jogos duplicados, Arquivos que não abrem) volta para a Central de problemas, não para a tela inicial.
  *Por quê:* essas verificações viviam espalhadas pelas configurações, e um emulador faltando só aparecia quando um jogo não abria. Um lugar só, verificado quando você vai até ele e que nunca aparece sozinho, deixa você arrumar as coisas quando estiver a fim e ignorar o resto.
- **Transferência Wi-Fi** – Configurações → Saves, transferência, compartilhamento → Transferência Wi-Fi mostra na tela de cima o endereço da página com um QR code ao lado, a senha de administração (seis dígitos aleatórios, ou uma sua) e uma listinha do que dá para fazer na página. Aponte a câmera do celular para o código, ou digite o endereço num navegador na mesma rede. O QR code é gerado no próprio portátil, então não precisa de internet. O B no menu Wi-Fi volta para onde você o abriu.
  A página segue o modo claro ou escuro do celular ou do computador, e aparece no primeiro idioma preferido do navegador entre os onze do launcher (chinês tradicional para Taiwan, Hong Kong e Macau), ou em inglês se nenhum servir. Tudo nela é traduzido, com os mesmos nomes do app. Antes de ser desbloqueada, ela só pede a senha de administração e diz o que dá para fazer ali. Depois de desbloqueada (recarregar não manda a senha de novo), ela tem:
  - **Enviar arquivos** – escolha uma pasta e solte os arquivos: ROMs em qualquer pasta de console, pacotes de tema, ou uma configuração em *Configurações para importar*, que vai para `DuoLauncher/exports` e aparece em Configurações → Importar uma configuração.
  - **As configurações de Tradução por IA, Imagens por IA, Respostas de voz da IA e RetroAchievements**, para colar chaves longas pelo computador. Chaves salvas nunca aparecem inteiras.
  - **Downloads** – toda configuração exportada, tema exportado e backup de saves, do mais novo para o mais antigo, com tamanho e data.

  A tela do portátil fica ligada enquanto a transferência está rodando, porque com a tela apagada não dá para acessar a página.
  *Por quê:* digitar um endereço no celular é a parte chata, e a câmera resolve isso num segundo. Os downloads ficam atrás da senha porque um backup completo pode ter as suas chaves de API, e uma página que qualquer um na mesma rede Wi-Fi consegue abrir não deve entregar isso.
- **Armazenamento** – um bloco da tela inicial mostra o quanto o cartão está cheio, e o espaço livre de cada unidade na tela de cima. Lá dentro: o tamanho dos jogos de cada console, os maiores jogos nunca jogados e a opção de apagar os arquivos de um jogo (todos os discos e os arquivos citados num `.cue` ou `.m3u`) depois de uma confirmação.
- **Backup de saves** – faça backup e restaure os saves do DraStic, RetroArch, PPSSPP, AetherSX2, DuckStation, Mupen64Plus FZ, Azahar e Citra num zip, guardado em `DuoLauncher/backups` no cartão SD (no armazenamento interno quando não há cartão). Depois de três horas de jogo sem backup o launcher avisa, ou faz um sozinho se você preferir (os dez backups automáticos mais recentes são mantidos; os seus nunca são apagados). Uma restauração só substitui cada save quando a cópia dele está completa. Y → “Saves” faz backup e restaura um jogo sozinho.
- **Organizar** – a Central de problemas, Armazenamento, “Baixar capas”, “Traduzir todas as trapaças” e uma tela de Diagnóstico. “Jogos duplicados” e “Jogos sem capa” agora ficam dentro da Central de problemas. As ferramentas de IA (nomes e descrições de um console inteiro, gêneros, séries, ícones de gêneros, imagens para a arte que falta) ficam em IA e voz → Deixar a IA organizar a biblioteca.
- **Atualizações** – Configurações → Sistema → Sobre confere a release mais recente no GitHub uma vez por dia e leva até ela.
- **Tela inicial** – defina como app de tela inicial padrão para ele abrir junto com o portátil; o bloco “Início do Android” (ou Configurações → Sistema → Sair para o início do Android) leva de volta à tela inicial do sistema, e Configurações → Sistema → Configurações do Android abre as configurações do próprio sistema.

## IA

Tudo aqui é opcional e usa a sua própria conta. As chaves ficam guardadas nas configurações do launcher, no portátil, e são enviadas só para o serviço que você escolheu. Coloque-as em Configurações → IA e voz → Serviços de IA e chaves, ou na página Wi-Fi pelo computador ou celular.

Configurações → IA e voz tem seis opções:

- **Escolher com a IA** e **IA: o que jogar hoje** – o chat e as seis sugestões (veja [Seu companheiro de IA](#seu-companheiro-de-ia)).
- **Serviços de IA e chaves** – Tradução por IA, Imagens por IA e Respostas de voz da IA, cada uma com o seu serviço e a sua chave, e *Digitar no computador ou celular (página Wi-Fi)*.
- **Deixar a IA organizar a biblioteca** – “IA: nomes e descrições”, “IA: separar por gênero”, “IA: séries de jogos”, “IA: ícones de gêneros” e “IA: desenhar imagens que faltam”.
- **Histórico de conversas com a IA** – as conversas anteriores e a chave “Salvar conversas com a IA”.
- **Uso e custo da IA.**

*Por quê:* essa categoria tinha treze opções numa lista só, misturando coisas do dia a dia com chaves que você coloca uma vez. Agora as duas coisas que as pessoas vêm buscar ficam no topo, a configuração de uma vez só fica atrás de uma porta, e as tarefas que passam pela biblioteca inteira ficam atrás de outra.

- **Tradução por IA** – DeepSeek, Qwen, OpenAI, Claude, Gemini ou qualquer endereço compatível com a OpenAI. É essa chave que conversa: o chat, as sugestões de jogos, “Perguntar à IA”, “Como jogar”, gêneros e séries, nomes de cheats (um jogo inteiro num pedido só) e nomes e descrições de jogos no seu idioma (gravados nos metadados do próprio launcher junto com o título oficial em inglês, então uma biblioteca de títulos chineses fica natural em inglês e vice-versa). Sem chave, os cheats são traduzidos por serviços gratuitos.
- **Imagens por IA** – uma chave de imagens da OpenAI (o modelo de imagem mais novo que a chave pode usar é escolhido automaticamente). Capas, logos de título, fundos, imagens de consoles, ícones e fundos de pastas e blocos, tudo no mesmo estilo de ícones de massinha e papel recortado da arte embutida; os ícones são desenhados com fundo transparente, para que as partes brancas fiquem. Uma imagem leva cerca de meio minuto. A mesma chave transforma a sua voz em texto (`/audio/transcriptions`), já que o portátil não tem reconhecimento de voz próprio.
- **Respostas de voz da IA** – Soniox com chave própria, ou OpenAI com a chave de imagens; voz e velocidade (“Normal”, “Rápido”, “Muito rápido” – “Rápido” é o padrão, porque os dois leem chinês devagar).
- **Uso e custo da IA** – cada pedido que o launcher faz é contado no portátil: pedidos, tokens, imagens e minutos de voz, com um custo estimado para hoje, esta semana, este mês e o total. A estimativa usa os preços públicos dos serviços para o que você usa; cada preço pode ser ajustado ao seu (outro modelo, um revendedor, um desconto). O que vale é a fatura do seu provedor.

## Como ele encontra os seus jogos

Para começar, você não precisa de arquivo de metadados, de scraper nem de uma organização de pastas específica: copie os jogos para o cartão e o launcher resolve o resto. Esta seção explica exatamente como, para você saber onde colocar as coisas e por que um jogo aparece onde aparece.

### Onde ele procura

- **Toda pasta `Roms` na raiz de um armazenamento** – o armazenamento interno e cada cartão SD. Maiúsculas não fazem diferença (`ROMs`, `roms`). Dá para desligar em Configurações → Início e biblioteca → Pastas de jogos → Encontrar pastas Roms automaticamente.
- **Qualquer pasta que você adicionar** em Configurações → Início e biblioteca → Pastas de jogos, para jogos guardados em outro lugar.
- **Até três níveis de pastas abaixo** dali. Pastas com imagens ou arquivos de emulador são puladas: `media`, `images`, `videos`, `covers`, `boxart`, `screenshots`, `manuals`, `marquees`, `wheel`, `saves`, `states`, `savestates`, `bios`, `system`, `cheats`, `downloaded_media`.
- **Jogos de 3DS instalados no Azahar ou no Citra** (a partir da pasta `sdmc` deles) e **jogos de Android** também entram.

O resultado de uma busca fica guardado, então a tela inicial abre em um segundo; jogos copiados depois são encontrados na próxima busca.

*Por quê:* a maioria das pessoas já tem uma pasta `Roms` de outro frontend ou de um pack de ROMs. Partir dela significa não ter que mover, renomear nem configurar nada antes.

### Organização recomendada

Uma pasta por console, com o nome do console:

```
Roms/
  3DS/      NDS/      GBA/      GBC/      GB/
  NES/      SNES/     N64/      NGC/      WII/
  MD/       PS1/      PS2/      PSP/      PSV/
  SWITCH/   SATURN/   DC/       PCE/      ARCADE/  …
```

Os nomes das pastas são comparados de forma flexível (espaços, hífens e maiúsculas são ignorados), e os nomes de costume do Pegasus, do ES-DE e do RetroArch também funcionam:

| Console | Nomes de pasta que ele entende | Tipos de arquivo que ele reconhece em qualquer lugar |
|---|---|---|
| Nintendo 3DS | `3ds`, `n3ds`, `nintendo3ds` | `.3ds` `.cci` `.cxi` `.cia` `.3dsx` |
| Nintendo DS | `nds`, `ds`, `nintendods` | `.nds` `.dsi` |
| Game Boy Advance | `gba`, `gameboyadvance` | `.gba` |
| Game Boy Color | `gbc`, `gameboycolor` | `.gbc` |
| Game Boy | `gb`, `gameboy` | `.gb` |
| NES / Famicom | `nes`, `fc`, `famicom` | `.nes` `.fds` `.unf` |
| SNES / Super Famicom | `snes`, `sfc`, `superfamicom`, `supernintendo` | `.sfc` `.smc` |
| Nintendo 64 | `n64`, `nintendo64` | `.n64` `.z64` `.v64` |
| GameCube | `ngc`, `gc`, `gamecube`, `nintendogamecube` | `.gcm` `.gcz` `.rvz` `.ciso` |
| Wii | `wii`, `nintendowii` | `.wbfs` `.wad` |
| Mega Drive / Genesis | `md`, `genesis`, `megadrive`, `segagenesis` | `.md` `.gen` `.smd` |
| PlayStation | `ps1`, `psx`, `playstation` | `.pbp` |
| PlayStation 2 | `ps2`, `playstation2` | – (precisa da pasta dele) |
| PSP | `psp` | `.cso` |
| PS Vita | `psv`, `psvita`, `vita` | `.vpk` |
| Switch | `switch`, `ns`, `nintendoswitch` | `.nsp` `.xci` `.nsz` `.xcz` |
| Virtual Boy | `vb`, `virtualboy` | `.vb` `.vboy` |
| Master System | `sms`, `mastersystem` | `.sms` |
| Game Gear | `gg`, `gamegear` | `.gg` |
| 32X | `32x`, `sega32x`, `md32x` | `.32x` |
| Mega-CD / Sega CD | `segacd`, `megacd`, `mdcd` | – (pelo conteúdo) |
| Saturn | `saturn`, `ss`, `segasaturn` | – (pelo conteúdo) |
| Dreamcast | `dc`, `dreamcast` | `.cdi` `.gdi` |
| NAOMI / Atomiswave | `naomi`, `naomi2`, `atomiswave` | – (precisa da pasta dele) |
| PC Engine / TurboGrafx-16 | `pce`, `pcengine`, `tg16` | `.pce` |
| PC Engine CD | `pcecd`, `pcenginecd`, `tgcd` | – (precisa da pasta dele) |
| PC-FX | `pcfx` | – (precisa da pasta dele) |
| Neo Geo CD | `neogeocd`, `ngcd` | – (precisa da pasta dele) |
| Neo Geo Pocket Color | `ngpc`, `ngp`, `neogeopocket` | `.ngp` `.ngc` |
| WonderSwan | `ws`, `wonderswan` | `.ws` |
| WonderSwan Color | `wsc`, `wonderswancolor` | `.wsc` |
| Pokémon Mini | `pokemini` | `.min` |
| Atari Lynx | `lynx`, `atarilynx` | `.lnx` |
| Atari 2600 | `atari`, `atari2600`, `2600` | `.a26` |
| Game & Watch | `gw`, `gameandwatch` | `.mgw` |
| 3DO | `3do` | – (pelo conteúdo) |
| DOS | `dos`, `msdos`, `pc` | `.dosz` |
| Arcade | `arcade`, `mame`, `fbneo`, `fba`, `neogeo`, `cps1`/`cps2`/`cps3` | – (precisa da pasta dele) |

### Pastas misturadas, e o que precisa de pasta própria

Você também pode jogar tudo numa pasta só. Para cada arquivo, o launcher se pergunta, nesta ordem:

1. **Você já disse de qual console ele é?** (veja o último ponto abaixo)
2. **O nome da pasta diz?**
3. **O tipo de arquivo pertence a um console só?** (a coluna da direita acima)
4. **O que diz o próprio arquivo?** Nos tipos de arquivo que vários consoles usam, ele lê alguns bytes do começo do arquivo – é rápido mesmo num cartão grande:
   - `.iso` – discos de GameCube, Wii, PSP, PlayStation, Saturn, Mega-CD e 3DO trazem cada um a sua própria marca; um disco de PlayStation com mais de 900 MB conta como PS2.
   - `.rvz`, `.wia`, `.ciso` – o cabeçalho do disco diz se é GameCube ou Wii. (`.gcz` não dá para distinguir e conta como GameCube.)
   - `.cue` – a primeira faixa do `.bin` que ele indica: PlayStation, Saturn, Mega-CD ou 3DO.
   - `.chd` – um GD-ROM de Dreamcast ou um DVD de PS2; imagens de CD de outros consoles precisam da pasta deles.
   - `.zip` – um único jogo dentro (até quatro arquivos) cujo tipo pertence a um console só, como um `.gba` dentro de um `.zip`. Sets de arcade, que têm muitas partes, precisam de uma pasta de arcade.
   - `.m3u` – vale o que for o primeiro disco dele.

O que continuar sem resposta depois disso (um `.7z`, por exemplo, ou o `.zip` de um jogo de arcade fora de uma pasta de arcade) não some: vai para uma pasta **Não reconhecidos** na tela inicial. Aperte A ou Y num desses jogos e escolha o console uma vez; a escolha fica guardada, continua valendo depois de uma nova busca e pode ser desfeita com Y → “De qual console é?” → “Restaurar padrão”. Nos pares `.cue` + `.bin` de PlayStation, só o `.cue` aparece na lista. A [Central de problemas](#ferramentas-úteis) conta esses arquivos em “Arquivos não reconhecidos” e abre a pasta para você.

*Por quê:* chutar errado é pior do que não chutar – um jogo de PS2 tomado por PSP não iniciaria. Por isso o launcher só decide quando a pasta, o tipo de arquivo ou o cabeçalho do próprio arquivo dá uma resposta clara, e pergunta a você sobre o resto em vez de escondê-lo.

### De onde vêm os nomes, as imagens e os detalhes

Para cada jogo, vale a primeira fonte que tiver alguma coisa:

1. **A pasta do próprio launcher, `DuoLauncher/`** (veja abaixo) – suas correções, traduções da IA, gêneros e séries. Nada passa por cima dela.
2. **Pegasus** – um `metadata.pegasus.txt` na pasta dos jogos, com títulos, descrições, imagens, linhas de inicialização e listas de vários discos. Jogos copiados depois sem entrada continuam aparecendo.
3. **ES-DE / EmulationStation** – `gamelist.xml` ao lado dos jogos ou em `ES-DE/gamelists/`, e as imagens em `downloaded_media/`.
4. **Imagens ao lado dos jogos** – `media/<file name without extension>/`, ou uma imagem com o mesmo nome em `covers/`, `images/` ou `boxart/`.
5. **Thumbnails do RetroArch.**
6. **A tabela de jogos embutida** (3.860 nomes de arquivo conhecidos) – título oficial em inglês, gênero e série.
7. **O próprio nome do arquivo.**

Os arquivos de outros frontends só são lidos, nunca alterados, então Pegasus, ES-DE e Duo Launcher podem dividir o mesmo cartão.

*Por quê:* as suas edições devem sempre ganhar, mas tudo o que você ou outro frontend já juntaram deve ser aproveitado, não jogado fora.

### O que fica na pasta `DuoLauncher`

O launcher guarda os próprios arquivos numa pasta `DuoLauncher` na raiz de um armazenamento. Por padrão, o que é de um console fica no armazenamento onde estão os jogos dele (normalmente o cartão SD); em Configurações → Saves, transferência, compartilhamento → Onde ficam os dados dá para pôr tudo no cartão ou no armazenamento interno (veja [Configurações e compartilhamento](#configurações-e-compartilhamento)). Listas de cheats e listas de discos ficam sempre no armazenamento interno:

| Caminho | O que é |
|---|---|
| `DuoLauncher/<console>/metadata.txt` | Nomes, descrições e títulos em inglês que você ou a IA adicionaram (formato Pegasus, veja [Suas informações de jogo](#suas-informações-de-jogo)) |
| `DuoLauncher/<console>/media/<file name without extension>/` | Logos, capas e fundos seus ou baixados |
| `DuoLauncher/<console>/genres.txt`, `series.txt` | Gênero e série de cada jogo |
| `DuoLauncher/cheats/NDS/` | As listas completas de cheats de NDS (armazenamento interno) |
| `DuoLauncher/m3u/` | Listas de discos dos jogos de vários discos (armazenamento interno) |
| `DuoLauncher/themes/` | Pacotes de temas (em qualquer armazenamento) |
| `DuoLauncher/backups/` | Backups dos saves (no cartão SD quando ele tem uma pasta `DuoLauncher`) |
| `DuoLauncher/exports/` | Configurações e temas exportados |
| `DuoLauncher/profile/` | A cópia automática das suas configurações e registros (no armazenamento escolhido em “Onde ficam os dados”) |
| `DuoLauncher/catalog.tsv` | Uma lista da sua biblioteca inteira, gravada para uso seu |

Configurações como o layout da tela inicial, as pastas, as escolhas de emulador e o tempo de jogo ficam guardadas dentro do app, com uma cópia em `DuoLauncher/profile/` que se atualiza sozinha; Configurações → Saves, transferência, compartilhamento → Exportar minha configuração pode salvá-las num arquivo só, junto com tudo o que foi descrito acima (veja [Configurações e compartilhamento](#configurações-e-compartilhamento)).

## Suas informações de jogo

Coloque uma pasta `DuoLauncher` na raiz do cartão SD (ou do armazenamento interno) para acrescentar ou corrigir nomes, descrições e imagens sem mexer nos arquivos de outros frontends. O que estiver aqui tem prioridade sobre o Pegasus, o ES-DE e os dados do próprio jogo.

```
DuoLauncher/
  3DS/metadata.txt
  3DS/media/<file name without extension>/logo.png   (also cover.png, background.png)
  NDS/metadata.txt
```

O `metadata.txt` usa o formato do Pegasus. As entradas são identificadas por `file:`, ou por `titleid:` para jogos de 3DS instalados num emulador:

```
game: 勇者派遣会社
titleid: 0004000000065600
description: …
```

Nomes e descrições podem ser dados por idioma com `name.<language>:` e `description.<language>:` (`en`, `zh-CN`, `zh-TW`, `ja`, `ko`, `fr`, `es`, `de`, `it`, `pt`, `ru`; `zh` vale para os dois chineses). Eles são usados enquanto o launcher está naquele idioma, então uma mesma pasta pode atender jogadores de vários idiomas:

```
game: 塞尔达传说 姆祖拉的假面
file: 塞尔达传说 姆祖拉的假面 汉化版.cci
name.en: The Legend of Zelda: Majora's Mask 3D
description.en: Link has three days before the moon falls on Termina…
```

## Temas

Configurações → Aparência e som → Tema oferece sete temas de cores embutidos (“Azul céu”, “Vermelho cereja”, “Verde menta”, “Roxo uva”, “Tangerina”, “Rosa sakura”, “Grafite”), **“Cores próprias”** (escolha uma cor principal e uma cor de fundo; todos os tons para o modo claro e o escuro são calculados a partir delas) e os pacotes de tema que você tiver instalado. Um tema colore as duas telas, incluindo os fundos de traço da tela de cima.

Um pacote de tema é uma pasta (ou um `.zip` dela, descompactado no primeiro uso) em `DuoLauncher/themes/` no cartão SD ou no armazenamento interno. Tudo é opcional; o que o tema deixar de fora vem do visual embutido, então um tema pode ser só algumas cores.

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

O `theme.txt` usa linhas `chave: valor`: `color.<name>` para o modo claro e `dark.color.<name>` para o escuro, com nomes como `accent`, `shelf-top`, `shelf-bottom`, `card`, `text`, `pill`, `footer`, `backdrop-paper` / `backdrop-ink` (o traço dos fundos da tela de cima) e `folder.<type>` para as pastas da tela inicial. Só `base-color: #RRGGBB` já basta para um esquema de cores completo. [themes/sakura](themes/sakura/theme.txt) é um exemplo completo, com cada chave explicada. Configurações → Saves, transferência, compartilhamento → Exportar tema atual grava o que você montou num zip em `DuoLauncher/exports/`, que outras pessoas podem soltar na pasta `themes/` delas; as suas próprias imagens vão junto em `own/`. (A opção Configurações → Música de fundo aparece quando o tema tem música.)

## Compilação

Não precisa de Android Studio nem de Gradle – só das ferramentas de linha de comando do Android SDK (build-tools 35, plataforma android-35) e do JDK 17 ou mais novo:

```powershell
powershell -File build.ps1            # builds DuoLauncher.apk
powershell -File build.ps1 -Install   # also installs over adb, grants file access and starts it
```

Uma chave de assinatura de debug (`debug.keystore`) é gerada na primeira compilação e não vai para o repositório. O `shot.ps1` envia toques de botão pelo adb e salva capturas das duas telas, o que ajuda bastante enquanto se mexe na interface.

## Arte

`assets/consoles/<type>.png` (ícones de massinha dos consoles e dos blocos) e `assets/backgrounds/<type>.jpg` (fundos de papel recortado com traço, com versões próprias para o modo escuro em `backgrounds/dark/`) foram feitos para este projeto. Os fundos são em tons de cinza, para que um tema de cores possa tingi-los. Consoles sem imagem usam um console desenhado no lugar.

## Notas de inicialização

- O AzaharPlus só aceita uma URI de árvore dentro de uma pasta à qual ele já tem acesso:
  `content://com.android.externalstorage.documents/tree/<volume>%3ARoms%2F3DS/document/<volume>%3ARoms%2F3DS%2F<file>`
- Citra MMJ: `org.citra.emu/org.citra.emu.ui.EmulationActivity` com `-e GamePath <path>`.
- O Citra MMJ (modo desempenho) é a mesma build reempacotada como `com.antutu.ABenchMark`; muitas builds de Android rodam esse nome de pacote com CPU/GPU no clock máximo. Só aparece quando esse pacote contém mesmo o Citra.
- O DraStic que vem nos portáteis Retroid quer `-e GAMEPATH "/sf<path>"`; uma variante “caminho simples” é oferecida para as builds originais.
- RetroArch: `com.retroarch.aarch64/...RetroActivityFuture` com `-e ROM`, `-e LIBRETRO <core>` e `-e CONFIGFILE`. O arquivo de configuração indicado na linha de inicialização é mantido: no Duo, o `retroarch.cfg` do próprio RetroArch mostra as molduras por sistema da Retroid em tamanho cheio, enquanto o `retroarch1920x1080.cfg` por tela da Retroid desenha os jogos pequenos dentro de uma moldura de TV.
- Dolphin (MMJR e oficial): `.../ui.main.MainActivity` com `--es AutoStartFile <path>`.
- ARMSX2, AetherSX2, Eden, PPSSPP e Mupen64Plus FZ recebem o jogo como um link `content://com.duo.launcher.rom/...` somente leitura, com permissão de leitura de uso único, então não precisam de acesso próprio a pastas.
- Vita3K: `org.vita3k.emulator/.Emulator` com o extra de array de strings `AppStartParameters = ["-r", "<title ID>"]`.
- Linhas de inicialização dos metadados do Pegasus são usadas sem alteração.

### Citra MMJ num portátil de duas telas

O Citra MMJ coloca a tela de toque no display de cima por padrão. Para ter a imagem principal em cima e esconder os botões na tela, feche o Citra e defina em `/sdcard/citra-emu/config/config-mmj.ini`:

```ini
[Controls]
input_overlay_hide = True
[Layout]
landscape_swap_screen = True
```

## Feedback

O Duo Launcher toma forma pelo jeito como é usado. Se algo estiver faltando, confuso ou quebrado – ou se você fez um tema que vale a pena compartilhar – abra uma [issue](https://github.com/GenKnot/DuoLauncher/issues).

## Licença

[MIT](LICENSE)
