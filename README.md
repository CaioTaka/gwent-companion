# Gwent Companion

Aplicativo Android gratuito para acompanhar partidas de **Gwent: O Jogo de Cartas Lendário — 2ª edição**. Projeto pessoal, sem anúncios, contas, compras ou serviços pagos. O placar funciona offline.

## Baixar e instalar no Samsung Galaxy

Baixe o arquivo `gwent-companion-v0.1.0.apk` na página [Releases do app](https://github.com/CaioTaka/gwent-companion/releases/latest).

1. No celular, abra a página do projeto no GitHub e entre em **Releases**. Abra a versão mais recente e toque no arquivo `gwent-companion-v0.1.0.apk` para baixá-lo.
2. Quando o download terminar, abra **Meus Arquivos** > **Downloads** e toque no APK. Também é possível abrir o aviso de download do navegador.
3. Se o Samsung pedir autorização, toque em **Configurações**. Ative **Permitir desta fonte** para o aplicativo que abriu o arquivo (por exemplo, Samsung Internet, Chrome ou Meus Arquivos). Essa permissão é por aplicativo de origem; os nomes dos menus podem variar conforme o modelo e a versão do One UI.
4. Volte à tela do instalador e toque em **Instalar**. Depois, toque em **Abrir**.
5. Após instalar, se quiser, volte à permissão **Instalar apps desconhecidos** e desative **Permitir desta fonte**.

Se não encontrar a opção, abra **Configurações** e pesquise por **Instalar apps desconhecidos**. Em algumas versões Samsung ela fica em **Segurança e privacidade** > **Mais configurações de segurança**. A [ajuda da Samsung](https://www.samsung.com/us/support/troubleshoot/TSG10001913/) também explica esse ajuste.

O Android pode exibir avisos porque o APK é baixado fora da Play Store. Instale somente o arquivo publicado nas Releases deste repositório. Se o Bloqueador Automático da Samsung impedir a instalação, não mude essa proteção sem antes confirmar que o arquivo veio deste repositório.

### Atualizar para uma versão futura

Baixe o APK mais recente em **Releases** e toque nele para instalar a atualização. Não desinstale a versão anterior: versões publicadas pelo mesmo projeto devem manter a mesma identidade de assinatura. Se o Android disser que não foi possível atualizar por conflito de assinatura, pare e peça ajuda; desinstalar pode apagar dados locais.

## O que esta versão faz

- Configura dois jogadores, nomes e facções.
- Mostra o placar e as três fileiras de combate.
- Permite registrar unidades manualmente e editar a mesa removendo uma carta.
- Calcula os efeitos implementados na ordem do manual: clima, Laço, Moral e Trompa do Comandante; Heróis são imunes a efeitos.
- Resolve a rodada com as duas gemas de cada jogador.
- Funciona sem internet durante a partida.

## Ainda não incluído

O catálogo das 436 cartas, todas as habilidades e cartas especiais, captura e reconhecimento por foto, consulta detalhada de cartas e gravação permanente da partida ainda não estão implementados. Os atalhos de catálogo e leitura na tela inicial são apenas indicativos nesta versão. A partida fica na memória do app e se perde quando o processo é encerrado.

## Compatibilidade e privacidade

- Android 8.0 (API 26) ou posterior.
- O app não solicita acesso à câmera nem acesso à internet nesta versão.
- O APK atual é uma compilação de depuração para avaliação pessoal; ainda não foi verificado em aparelhos Samsung físicos.

## Construir a partir do código

Abra esta pasta no Android Studio ou no VS Code com as ferramentas Android instaladas. Use JDK 17 e Android SDK Platform 36; o wrapper do Gradle 8.13 está incluído. No Windows, execute `gradlew.bat assembleDebug` para compilar um APK de depuração em `app/build/outputs/apk/debug/`.

## Regras

As regras implementadas foram consultadas no manual em inglês da 2ª edição. A resolução de efeitos ainda é parcial; o app não substitui o manual completo.


