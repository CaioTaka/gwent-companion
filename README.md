# Gwent Companion

Aplicativo Android pessoal e gratuito para acompanhar partidas de **Gwent: O Jogo de Cartas Lendário — 2ª edição**. Sem anúncios, conta ou compras. A partida funciona offline; a internet é usada somente para consultar e baixar atualizações pelo GitHub.

## Baixar e instalar no Samsung Galaxy

Baixe o arquivo `gwent-companion-v0.2.0.apk` na página [Releases do app](https://github.com/CaioTaka/gwent-companion/releases/latest).

1. No celular, abra a página do projeto no GitHub e entre em **Releases**. Abra a versão mais recente e toque no arquivo `gwent-companion-v0.2.0.apk` para baixá-lo.
2. Quando o download terminar, abra **Meus Arquivos** > **Downloads** e toque no APK. Também é possível abrir o aviso de download do navegador.
3. Se o Samsung pedir autorização, toque em **Configurações**. Ative **Permitir desta fonte** para o aplicativo que abriu o arquivo (por exemplo, Samsung Internet, Chrome ou Meus Arquivos). Essa permissão é por aplicativo de origem; os nomes dos menus podem variar conforme o modelo e a versão do One UI.
4. Volte à tela do instalador e toque em **Instalar**. Depois, toque em **Abrir**.
5. Após instalar, se quiser, volte à permissão **Instalar apps desconhecidos** e desative **Permitir desta fonte**.

Se não encontrar a opção, abra **Configurações** e pesquise por **Instalar apps desconhecidos**. Em algumas versões Samsung ela fica em **Segurança e privacidade** > **Mais configurações de segurança**. A [ajuda da Samsung](https://www.samsung.com/us/support/troubleshoot/TSG10001913/) também explica esse ajuste.

O Android pode exibir avisos porque o APK é baixado fora da Play Store. Instale somente o arquivo publicado nas Releases deste repositório. Se o Bloqueador Automático da Samsung impedir a instalação, não desative essa proteção sem antes confirmar que o arquivo veio deste repositório.

## Buscar atualizações pelo app

Abra **Configurações** tocando no logo do cabeçalho e escolha **Buscar atualizações**. O app consulta a release mais recente e só baixa o APK quando existe versão mais nova. O Android abrirá a própria confirmação de instalação; o app não pode instalar APKs silenciosamente. Se o Android pedir autorização para instalar apps desconhecidos, permita a instalação para o Gwent Companion e retorne ao app.

Não desinstale a versão anterior para atualizar: isso pode apagar o histórico local. As releases deste projeto devem conservar a mesma assinatura. Se o Android informar conflito de assinatura, interrompa a instalação e peça ajuda.

## O que esta versão faz

- Corrige a área segura para que a interface não fique sob a barra de status ou de navegação do aparelho.
- Mostra o logo enviado no cabeçalho e no ícone Android, além do logo de Gwent na tela inicial.
- Oferece telas de regras, catálogo de cartas, reconhecimento de campo, trilha manual de força, registro de batalhas e configurações.
- Importa 284 entradas das planilhas fornecidas, correspondentes a 436 cópias de cartas. O catálogo filtra por facção, categoria, habilidade, alcance e força. A busca responde enquanto se digita e não exige o começo do nome.
- Permite escolher cartas de unidade/herói da facção na rodada, aplicar clima e Trompa do Comandante, inspecionar uma carta, reorganizar cartas na fileira e removê-las.
- Salva localmente as partidas encerradas, incluindo horário local, nomes, facções, formato, conjunto de regras, placares e contagem das cartas escolhidas por tipo.
- Consulta a última release pública do GitHub e encaminha a instalação pelo instalador oficial do Android.

## Limitações conhecidas

- Os nomes das cartas no catálogo mantêm o idioma das planilhas, que estão em alemão. As planilhas listam nomes, força, categoria, alcance e rótulos de habilidade; não trazem o texto completo de todas as cartas nem a descrição individual das habilidades dos líderes. A tela de regras informa essa limitação.
- O placar automático de gemas acompanha duas pessoas/lados. Solo, 2 vs 2 e todos contra todos podem ser selecionados e salvos no registro, mas a resolução de equipes e de três ou mais participantes ainda não está implementada.
- Agilidade, Laço estreito, Moral, Heróis, clima, Trompa e a habilidade de empate de Nilfgaard estão parcialmente representados no cálculo. O app não aplica todas as habilidades, líder, especiais, descarte, compra de cartas ou regras de facção automaticamente. Consulte o manual e o texto impresso das cartas.
- O Reconhecimento de Campo é uma tela preparatória. Captura e identificação automática por câmera ainda não estão disponíveis e o app não solicita permissão de câmera.
- A trilha manual mantém os pontos por fileira e lado, mas não aplica efeitos automaticamente.
- O histórico é local e limitado aos dados gravados pelo app; se apagar os dados do aplicativo, trocar de celular ou remover o app, o histórico pode ser perdido.
- O APK é uma compilação de depuração para avaliação pessoal e ainda não foi verificado em aparelhos Samsung físicos.

## Construir a partir do código

Abra a pasta no Android Studio ou no VS Code com as ferramentas Android instaladas. Use JDK 17 e Android SDK Platform 36; o wrapper do Gradle 8.13 está incluído. No Windows, execute `gradlew.bat assembleDebug` para gerar `app/build/outputs/apk/debug/app-debug.apk`.

## Fonte das regras

O resumo de regras foi preparado a partir do manual em inglês da 2ª edição, especialmente as seções de pontuação, facções, habilidades de carta e modos avançado/torneio/equipe. A resolução automática de regras é parcial; o app não substitui o manual completo.
