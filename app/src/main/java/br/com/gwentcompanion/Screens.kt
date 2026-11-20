package br.com.gwentcompanion

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF090B09)
private val Panel = Color(0xFF151914)
private val PanelRaised = Color(0xFF20261F)
private val Gold = Color(0xFFD6B46A)
private val MutedGold = Color(0xFF88764B)
private val Parchment = Color(0xFFE4D7B9)
private val Moss = Color(0xFF587849)
private val SoftText = Color(0xFFB7B9AF)
private val Danger = Color(0xFF9F4B43)

enum class AppPage { HOME, SETUP, MATCH, RULES, CATALOG, FORCE_TRACK, HISTORY, SETTINGS, RECOGNITION }
enum class GameMode(val title: String) { SOLO("Solo"), ONE_V_ONE("1 vs 1"), TWO_V_TWO("2 vs 2"), FREE_FOR_ALL("Todos contra todos") }
enum class RuleMode(val title: String) { STANDARD("Padrão"), ADVANCED("Avançado"), TOURNAMENT("Torneio") }

data class BattleRecord(
    val date: String, val mode: String, val rules: String,
    val nameOne: String, val factionOne: String, val scoreOne: Int,
    val nameTwo: String, val factionTwo: String, val scoreTwo: Int,
    val usedCards: String
)

@Composable
fun TopBrand(onSettings: () -> Unit, onHome: () -> Unit, showHome: Boolean) {
    Box(Modifier.fillMaxWidth().height(62.dp).padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
        if (showHome) Text("‹ INÍCIO", color = SoftText, fontSize = 9.sp, modifier = Modifier.align(Alignment.CenterStart).clickable(onClick = onHome).padding(5.dp))
        Row(Modifier.clickable(onClick = onSettings), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.main_logo), null, Modifier.size(38.dp), contentScale = ContentScale.Fit)
            Spacer(Modifier.width(10.dp))
            Text("GWENT COMPANION", color = Parchment, fontFamily = FontFamily.Serif, fontSize = 18.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, maxLines = 1)
        }
        Text("⚙", color = Gold, fontSize = 22.sp, modifier = Modifier.align(Alignment.CenterEnd).clickable(onClick = onSettings).padding(4.dp))
    }
}

@Composable
fun HomeScreen(hasMatch: Boolean, onNew: () -> Unit, onResume: () -> Unit, onNavigate: (AppPage) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(8.dp))
        PanelCard {
            Image(painterResource(R.drawable.gwent_logo), "Logo de Gwent", Modifier.fillMaxWidth().height(118.dp), contentScale = ContentScale.Fit)
            Text("CAMPO DE BATALHA", color = Gold, fontSize = 10.sp, letterSpacing = 2.3.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(7.dp))
            Text("A guerra se decide\nna contagem.", color = Parchment, textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif, fontSize = 27.sp, lineHeight = 32.sp, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))
            PrimaryButton(if (hasMatch) "NOVA PARTIDA" else "COMEÇAR PARTIDA", onNew)
            if (hasMatch) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onResume, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("RETOMAR PARTIDA", color = Parchment) }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("ATALHOS DE MESA", color = MutedGold, fontSize = 10.sp, letterSpacing = 1.8.sp)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ShortcutCard("REGRAS DE BATALHA", "Regras, facções e habilidades", "R", { onNavigate(AppPage.RULES) }, Modifier.weight(1f))
            ShortcutCard("CARTAS DE GUERRA", "Catálogo e filtros", "C", { onNavigate(AppPage.CATALOG) }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ShortcutCard("RECONHECIMENTO DE CAMPO", "Conferir o tabuleiro por foto", "◉", { onNavigate(AppPage.RECOGNITION) }, Modifier.weight(1f))
            ShortcutCard("TRILHA DE FORÇA", "Contagem manual de pontos", "III", { onNavigate(AppPage.FORCE_TRACK) }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        ShortcutCard("REGISTRO DE BATALHAS", "Partidas concluídas neste celular", "✦", { onNavigate(AppPage.HISTORY) }, Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun SetupScreen(
    nameOne: String, nameTwo: String, factionOne: Faction, factionTwo: Faction,
    mode: GameMode, rules: RuleMode,
    onNameOne: (String) -> Unit, onNameTwo: (String) -> Unit,
    onFactionOne: (Faction) -> Unit, onFactionTwo: (Faction) -> Unit,
    onMode: (GameMode) -> Unit, onRules: (RuleMode) -> Unit,
    cards: List<CatalogCard>, leaderOne: String, leaderTwo: String,
    onLeaderOne: (String) -> Unit, onLeaderTwo: (String) -> Unit,
    onBack: () -> Unit, onStart: () -> Unit
) {
    var choosingLeader by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        ScreenHeading("Preparar a partida", "Escolha jogadores, facções e formato.")
        PlayerSetup("JOGADOR / LADO 1", nameOne, factionOne, leaderOne, cards, onNameOne, onFactionOne, { choosingLeader = 1 })
        Spacer(Modifier.height(12.dp))
        PlayerSetup("JOGADOR / LADO 2", nameTwo, factionTwo, leaderTwo, cards, onNameTwo, onFactionTwo, { choosingLeader = 2 })
        Spacer(Modifier.height(14.dp))
        PanelCard {
            Text("FORMATO DA PARTIDA", color = Gold, fontSize = 10.sp, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(8.dp))
            ChipWrap(GameMode.entries.map { it.title to (it == mode) }) { index -> onMode(GameMode.entries[index]) }
            Spacer(Modifier.height(12.dp))
            Text("CONJUNTO DE REGRAS", color = Gold, fontSize = 10.sp, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(8.dp))
            ChipWrap(RuleMode.entries.map { it.title to (it == rules) }) { index -> onRules(RuleMode.entries[index]) }
            if (mode != GameMode.ONE_V_ONE) {
                Spacer(Modifier.height(10.dp))
                Text("O registro guarda este formato. O placar por gemas desta versão ainda acompanha dois lados; 2 vs 2 e todos contra todos não têm resolução de equipe implementada.", color = SoftText, fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        InfoStrip("Duas gemas por lado", "No modo padrão, a pessoa com menor força perde a rodada; em empate, ambos perdem uma gema.")
        Spacer(Modifier.height(18.dp))
        PrimaryButton("ABRIR O TABULEIRO", onStart)
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("VOLTAR", color = SoftText) }
    }
    if (choosingLeader != 0) {
        val targetFaction = if (choosingLeader == 1) factionOne.label else factionTwo.label
        val leaders = cards.filter { it.faction == targetFaction && it.category == "Líder" }
        AlertDialog(onDismissRequest = { choosingLeader = 0 }, containerColor = PanelRaised,
            title = { Text("Escolher líder · $targetFaction", color = Gold) },
            text = { Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                leaders.forEach { card ->
                    TextButton(onClick = {
                        if (choosingLeader == 1) onLeaderOne(card.name) else onLeaderTwo(card.name)
                        choosingLeader = 0
                    }, modifier = Modifier.fillMaxWidth()) { Text(card.name, color = Parchment, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth()) }
                }
            } }, confirmButton = { TextButton(onClick = { choosingLeader = 0 }) { Text("FECHAR", color = Gold) } })
    }
}

@Composable
private fun PlayerSetup(title: String, name: String, faction: Faction, leader: String, cards: List<CatalogCard>, onName: (String) -> Unit, onFaction: (Faction) -> Unit, onPickLeader: () -> Unit) {
    PanelCard {
        Text(title, color = Gold, fontSize = 9.sp, letterSpacing = 1.4.sp)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = name, onValueChange = onName, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Nome na mesa") })
        Spacer(Modifier.height(11.dp))
        Text("FACÇÃO", color = SoftText, fontSize = 9.sp, letterSpacing = 1.1.sp)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Faction.entries.forEach { option -> FactionChip(option, option == faction) { onFaction(option) } }
        }
        Spacer(Modifier.height(8.dp))
        val available = cards.any { it.name == leader && it.faction == faction.label }
        OutlinedButton(onClick = onPickLeader, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Text(if (available) "LÍDER  ·  $leader" else "ESCOLHER LÍDER", color = Gold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun CatalogScreen(cards: List<CatalogCard>, onBack: () -> Unit) {
    var faction by remember { mutableStateOf("Todas") }
    var category by remember { mutableStateOf("Todas") }
    var ability by remember { mutableStateOf("Todas") }
    var row by remember { mutableStateOf("Todas") }
    var query by remember { mutableStateOf("") }
    var strengthOrder by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf<CatalogCard?>(null) }
    val filtered = remember(cards, faction, category, ability, row, query, strengthOrder) {
        var result = cards
        if (faction != "Todas") result = result.filter { it.faction == faction }
        if (category != "Todas") result = result.filter { it.category == category }
        if (ability != "Todas") result = result.filter { it.ability == ability }
        if (row != "Todas") result = result.filter { it.row == row }
        result = CardCatalog.search(result, query)
        if (strengthOrder != 0) result.sortedWith(compareBy<CatalogCard> { it.strength == null }.thenBy { if (strengthOrder > 0) it.strength else -(it.strength ?: 0) }) else result
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        ScreenHeading("Cartas de guerra", "${cards.size} registros · 436 cópias de cartas nas planilhas fornecidas")
        OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Pesquisar nome ou habilidade") })
        Spacer(Modifier.height(7.dp))
        FilterLine("FACÇÃO", listOf("Todas") + Faction.entries.map { it.label }, faction) { faction = it }
        FilterLine("TIPO", listOf("Todas", "Líder", "Herói", "Unidade", "Especial", "Reserva", "Carta bônus"), category) { category = it }
        FilterLine("HABILIDADE", listOf("Todas") + cards.map { it.ability }.distinct().sorted(), ability) { ability = it }
        FilterLine("ALCANCE", listOf("Todas") + cards.map { it.row }.filter(String::isNotBlank).distinct(), row) { row = it }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("ORDENAR POR FORÇA", color = MutedGold, fontSize = 9.sp, modifier = Modifier.weight(1f))
            SmallChoice("↑ CRESCENTE", strengthOrder > 0) { strengthOrder = if (strengthOrder == 1) 0 else 1 }
            Spacer(Modifier.width(5.dp))
            SmallChoice("↓ DECRESCENTE", strengthOrder < 0) { strengthOrder = if (strengthOrder == -1) 0 else -1 }
        }
        Spacer(Modifier.height(5.dp))
        Text("${filtered.size} resultado(s)", color = SoftText, fontSize = 10.sp)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            items(filtered, key = { it.id }) { card -> CatalogCardRow(card) { selected = card } }
        }
    }
    selected?.let { CardDetailsDialog(it) { selected = null } }
}

@Composable
private fun FilterLine(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
        Text(label, color = MutedGold, fontSize = 8.sp, modifier = Modifier.width(62.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            options.forEach { option -> SmallChoice(option.uppercase(), option == selected) { onSelect(option) } }
        }
    }
}

@Composable
fun BattleRulesScreen(cards: List<CatalogCard>) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        ScreenHeading("Regras de batalha", "Resumo consultável da 2ª edição, organizado por assunto.")
        RuleSection("Regras padrão", listOf(
            "Monte um baralho de uma só facção: 1 líder, pelo menos 22 cartas de unidade e no máximo 10 cartas especiais.",
            "Cada jogador começa com 10 cartas na mão. Em seu turno, jogue uma carta ou ative uma habilidade de líder; também é possível passar.",
            "Quando ambos passam, compare a força total. O menor total perde uma gema; empate remove uma gema de cada lado.",
            "As cartas saem do campo no fim da rodada e vão para a pilha de descarte de quem as controlava. O vencedor inicia a rodada seguinte.",
            "A partida termina quando alguém fica sem gemas. Se ambos perderem a última gema ao mesmo tempo, há empate."
        ))
        RuleSection("Ordem de cálculo da força", listOf(
            "1. Clima: reduz a 1 a força das unidades comuns na fileira afetada, dos dois lados.",
            "2. Laço estreito: multiplica a força pelo número de unidades aliadas com o mesmo nome na mesma fileira.",
            "3. Moral: cada carta com Moral acrescenta 1 às outras unidades comuns da própria fileira.",
            "4. Trompa do Comandante: dobra a força das unidades comuns daquela fileira; só uma trompa por fileira.",
            "Heróis ignoram todos esses efeitos."
        ))
        RuleSection("Regras de facção", listOf(
            "Monstros: ao fim de cada rodada, embaralhe as unidades comuns que ficaram no campo, escolha uma ao acaso e mantenha-a para a próxima rodada; descarte as demais.",
            "Nilfgaard: vence a rodada quando o resultado de força termina empatado.",
            "Reinos do Norte: ao vencer uma rodada, compra uma carta do baralho.",
            "Scoia'tael: decide quem começa a primeira rodada.",
            "Skellige: no início da terceira rodada, embaralha o descarte, escolhe duas unidades comuns ao acaso, joga-as e aplica seus efeitos."
        ))
        RuleSection("Habilidades de unidade", listOf(
            "Agilidade: pode entrar na fileira corpo a corpo ou à distância; depois de colocada, não pode ser movida.",
            "Trompa do Comandante: dobra as outras unidades comuns da fileira; não combina com outra trompa nem com líder de mesmo efeito.",
            "Médico: escolhe uma unidade comum do descarte e joga-a imediatamente.",
            "Moral: acrescenta 1 às outras unidades comuns na mesma fileira.",
            "Convocação: busca as cartas indicadas na mão e no baralho, joga-as e embaralha o baralho.",
            "Laço estreito: multiplica a força desta unidade pelo número de aliadas de mesmo nome.",
            "Queimar: se a força total da fileira oposta chegar a 10 ou mais, descarta a(s) unidade(s) comum(ns) mais fortes dessa fileira.",
            "Espião: é jogado no campo adversário, soma força para o adversário e permite comprar duas cartas.",
            "Invocador: quando descartada, chama para o campo a carta correspondente do baralho.",
            "Berserker/Mardroeme: o Mardroeme ativa os Berserkers daquela fileira e os efeitos de Berserker jogados nela nesta rodada."
        ))
        RuleSection("Cartas especiais e clima", listOf(
            "Trompa: dobra as unidades comuns da fileira escolhida.",
            "Isca: devolve uma unidade comum do seu lado para a mão e ocupa o lugar dela.",
            "Queimar: descarta as unidades comuns mais fortes do campo quando a força da fileira oposta alcança o limite impresso.",
            "Geada, neblina, chuva e tempestade de Skellige reduzem as unidades afetadas a 1; tempo bom remove todos os climas.",
            "Os efeitos das cartas especiais aplicam-se conforme o texto impresso na carta; cartas de Herói são imunes."
        ))
        RuleSection("Líderes por facção", emptyList())
        Faction.entries.forEach { faction ->
            val leaders = cards.filter { it.faction == faction.label && it.category == "Líder" }
            PanelCard {
                Text(faction.label.uppercase(), color = faction.tint, fontSize = 10.sp, letterSpacing = 1.2.sp)
                leaders.forEach { leader ->
                    Text(leader.name, color = Parchment, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.padding(top = 9.dp))
                    Text("Ative no seu turno em vez de jogar uma carta. A descrição individual da habilidade deve ser conferida no texto impresso do líder; as planilhas usadas para o catálogo identificam os líderes, mas não incluem esse texto.", color = SoftText, fontSize = 10.sp, lineHeight = 14.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        RuleSection("Regras avançadas", listOf(
            "Baralho: 1 líder, mínimo de 22 unidades, máximo de 5 especiais, até 2 Espiões e 2 Médicos. A força combinada das unidades não pode passar de 130.",
            "Cartas de reserva não contam para a força total do baralho."
        ))
        RuleSection("Modo de torneio", listOf(
            "Cada pessoa monta três baralhos de facções diferentes; o mesmo Herói não pode aparecer em mais de um.",
            "Cada duelo é melhor de três. A moeda decide quem escolhe primeiro o baralho; depois, quem venceu escolhe um novo e quem perdeu decide se mantém o atual.",
            "Um baralho vencedor não pode ser usado novamente no mesmo torneio."
        ))
        RuleSection("2 vs 2", listOf(
            "Cada equipe compartilha campo, descarte e gemas; cada carta jogada soma à força da equipe.",
            "As equipes alternam turnos e cada integrante joga uma carta por vez. A equipe continua jogando mesmo depois que um integrante passa.",
            "Parceiros usam facções diferentes. Algumas habilidades têm aplicação especial no modo de equipes; consulte o manual da edição."
        ))
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun RuleSection(title: String, items: List<String>) {
    PanelCard {
        Text(title.uppercase(), color = Gold, fontFamily = FontFamily.Serif, fontSize = 14.sp)
        items.forEach { Text("•  $it", color = Parchment, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 9.dp)) }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
fun ForceTrackScreen(scores: List<List<Int>>, onChange: (Int, Int, Int) -> Unit, onReset: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        ScreenHeading("Trilha de força", "Tabuleiro manual. Estes pontos não somam cartas nem aplicam regras automaticamente.")
        CombatRow.entries.forEachIndexed { rowIndex, row ->
            PanelCard {
                Text(row.short, color = Gold, fontFamily = FontFamily.Serif, fontSize = 15.sp)
                Spacer(Modifier.height(7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ManualScore("LADO 1", scores[0][rowIndex], Modifier.weight(1f), { onChange(0, rowIndex, -1) }, { onChange(0, rowIndex, 1) })
                    ManualScore("LADO 2", scores[1][rowIndex], Modifier.weight(1f), { onChange(1, rowIndex, -1) }, { onChange(1, rowIndex, 1) })
                }
            }
            Spacer(Modifier.height(9.dp))
        }
        InfoStrip("TOTAL LADO 1", scores[0].sum().toString())
        Spacer(Modifier.height(7.dp))
        InfoStrip("TOTAL LADO 2", scores[1].sum().toString())
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("ZERAR TRILHA", color = Parchment) }
    }
}

@Composable
private fun ManualScore(name: String, value: Int, modifier: Modifier, onMinus: () -> Unit, onPlus: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(13.dp)).background(PanelRaised).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(name, color = SoftText, fontSize = 9.sp)
        Text(value.toString(), color = Gold, fontSize = 29.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { SmallChoice("−", false, onMinus); SmallChoice("+", false, onPlus) }
    }
}

@Composable
fun HistoryScreen(records: List<BattleRecord>, onClear: () -> Unit) {
    var confirmClear by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        ScreenHeading("Registro de batalhas", "Partidas salvas localmente neste celular.")
        if (records.isEmpty()) {
            PanelCard { Text("Ainda não há partidas encerradas. O registro é gravado quando uma partida termina.", color = SoftText, fontSize = 12.sp, lineHeight = 18.sp) }
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                items(records.asReversed()) { record ->
                    PanelCard {
                        Text(record.date, color = Gold, fontSize = 10.sp)
                        Text("${record.mode} · ${record.rules}", color = SoftText, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("${record.nameOne} (${record.factionOne})  ${record.scoreOne}  :  ${record.scoreTwo}  ${record.nameTwo} (${record.factionTwo})", color = Parchment, fontSize = 12.sp, lineHeight = 17.sp)
                        Text(record.usedCards, color = SoftText, fontSize = 9.sp, modifier = Modifier.padding(top = 7.dp))
                    }
                }
            }
            TextButton(onClick = { confirmClear = true }, modifier = Modifier.fillMaxWidth()) { Text("APAGAR HISTÓRICO", color = Danger) }
        }
    }
    if (confirmClear) AlertDialog(onDismissRequest = { confirmClear = false }, containerColor = PanelRaised,
        title = { Text("Apagar o histórico?", color = Gold) }, text = { Text("As partidas salvas neste celular serão removidas.", color = Parchment) },
        confirmButton = { TextButton(onClick = { onClear(); confirmClear = false }) { Text("APAGAR", color = Danger) } },
        dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("CANCELAR", color = SoftText) } })
}

@Composable
fun SettingsScreen(updateStatus: String, onCheckUpdates: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        ScreenHeading("Configurações", "Preferências e atualização do aplicativo.")
        PanelCard {
            Text("ATUALIZAÇÃO", color = Gold, fontSize = 10.sp, letterSpacing = 1.4.sp)
            Spacer(Modifier.height(7.dp))
            Text("Verifica a última release pública no GitHub. Se houver versão nova, o app baixa o APK e pede autorização do Android para instalar.", color = Parchment, fontSize = 11.sp, lineHeight = 16.sp)
            Spacer(Modifier.height(10.dp))
            PrimaryButton("BUSCAR ATUALIZAÇÕES", onCheckUpdates)
            Text(updateStatus, color = SoftText, fontSize = 10.sp, lineHeight = 14.sp, modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(12.dp))
        PanelCard {
            Text("FEITO PARA A MESA", color = Gold, fontFamily = FontFamily.Serif, fontSize = 17.sp)
            Spacer(Modifier.height(5.dp))
            Text("Sem anúncios, sem conta e sem compras. O registro de partidas e as preferências ficam apenas neste aparelho.", color = SoftText, fontSize = 12.sp, lineHeight = 17.sp)
        }
        Spacer(Modifier.height(12.dp))
        PanelCard {
            Text("SOBRE A VERSÃO", color = Gold, fontSize = 10.sp, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(6.dp))
            Text("Reconhecimento por foto e textos individuais de líderes ainda estão em construção. O APK de teste exige confirmação do instalador do Android.", color = SoftText, fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
fun RecognitionScreen(onInfo: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        ScreenHeading("Reconhecimento de campo", "Conferência por câmera em desenvolvimento.")
        PanelCard {
            Image(painterResource(R.drawable.gwent_playmat), null, Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
            Spacer(Modifier.height(12.dp))
            Text("A leitura de cartas ainda não está conectada. O app não pede permissão de câmera nesta versão.", color = Parchment, fontSize = 12.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onInfo, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("SAIBA COMO VAI FUNCIONAR", color = Gold) }
        }
    }
}

@Composable
fun MatchScreen(
    playerOne: String, playerTwo: String, factionOne: Faction, factionTwo: Faction,
    leaderOne: String, leaderTwo: String, cards: List<UnitCard>, weather: Set<CombatRow>, horns: Set<String>, round: Int,
    gemsOne: Int, gemsTwo: Int, scoreOne: Int, scoreTwo: Int, lastRound: String,
    onPlayCard: (Int, CatalogCard, CombatRow) -> Unit, onWeather: (CombatRow) -> Unit, onHorn: (Int, CombatRow) -> Unit,
    onClearWeather: () -> Unit, onRemove: (Long) -> Unit, onReorder: (Long, Int) -> Unit,
    onEndRound: () -> Unit, onNewGame: () -> Unit, catalog: List<CatalogCard>
) {
    var pickerPlayer by remember { mutableIntStateOf(0) }
    var selectedCard by remember { mutableStateOf<UnitCard?>(null) }
    val finished = gemsOne == 0 || gemsTwo == 0
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenHeading(if (finished) "Partida encerrada" else "Rodada $round", "Toque em uma carta para ver seus detalhes. Segure e use as setas para reorganizar.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScorePanel(playerOne, factionOne, leaderOne, scoreOne, gemsOne, Modifier.weight(1f))
            ScorePanel(playerTwo, factionTwo, leaderTwo, scoreTwo, gemsTwo, Modifier.weight(1f))
        }
        if (lastRound.isNotBlank()) { Spacer(Modifier.height(8.dp)); InfoStrip(if (finished) "FIM DE JOGO" else "RODADA ANTERIOR", lastRound) }
        Spacer(Modifier.height(8.dp))
        Text("TABULEIRO DE BATALHA", color = Gold, fontSize = 9.sp, letterSpacing = 1.2.sp)
        Box(Modifier.fillMaxWidth().weight(1f, fill = false).heightIn(min = 230.dp, max = 345.dp)) {
            PlaymatBackground()
            Column(Modifier.fillMaxSize().padding(vertical = 7.dp), verticalArrangement = Arrangement.SpaceEvenly) {
                CombatRow.entries.forEach { row ->
                    val inRow = cards.filter { it.row == row }
                    val rowTotalOne = GwentRules.rowStrength(1, row, cards, if (row in weather) setOf(row) else emptySet(), horns)
                    val rowTotalTwo = GwentRules.rowStrength(2, row, cards, if (row in weather) setOf(row) else emptySet(), horns)
                    Column(Modifier.fillMaxWidth().padding(horizontal = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(row.short, color = Parchment, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("$rowTotalOne : $rowTotalTwo", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            SmallChoice(if (row in weather) "TEMPO" else "CLIMA", row in weather) { onWeather(row) }
                            Spacer(Modifier.width(3.dp))
                            SmallChoice("T1", GwentRules.hornKey(1, row) in horns) { onHorn(1, row) }
                            Spacer(Modifier.width(3.dp))
                            SmallChoice("T2", GwentRules.hornKey(2, row) in horns) { onHorn(2, row) }
                        }
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
                            val rowCards = inRow
                            if (rowCards.isEmpty()) Text("Toque em JOGAR CARTA para preencher esta fileira", color = Ink.copy(alpha = .65f), fontSize = 8.sp)
                            rowCards.forEachIndexed { index, card ->
                                CardTile(card, GwentRules.effectiveStrength(card, cards, if (row in weather) setOf(row) else emptySet(), horns), { selectedCard = card },
                                    { onReorder(card.id, -1) }, { onReorder(card.id, 1) }, { onRemove(card.id) }, index, rowCards.lastIndex)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            PlayerActionButton("JOGAR CARTA · LADO 1", Modifier.weight(1f)) { pickerPlayer = 1 }
            PlayerActionButton("JOGAR CARTA · LADO 2", Modifier.weight(1f)) { pickerPlayer = 2 }
        }
        Spacer(Modifier.height(5.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            OutlinedButton(onClick = onClearWeather, modifier = Modifier.weight(1f), enabled = weather.isNotEmpty(), shape = RoundedCornerShape(12.dp)) { Text("LIMPAR CLIMA", color = Parchment, fontSize = 9.sp) }
            Button(onClick = onEndRound, modifier = Modifier.weight(1f), enabled = !finished, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink)) { Text("ENCERRAR RODADA", fontSize = 9.sp) }
        }
        if (finished) { Spacer(Modifier.height(6.dp)); OutlinedButton(onClick = onNewGame, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("NOVA PARTIDA", color = Gold) } }
        Spacer(Modifier.height(8.dp))
    }
    if (pickerPlayer != 0) {
        val faction = if (pickerPlayer == 1) factionOne else factionTwo
        CardPickerDialog(catalog, faction.label, pickerPlayer, onDismiss = { pickerPlayer = 0 }) { card, row ->
            onPlayCard(pickerPlayer, card, row); pickerPlayer = 0
        }
    }
    selectedCard?.let { card ->
        AlertDialog(onDismissRequest = { selectedCard = null }, containerColor = PanelRaised,
            title = { Text(card.name, color = Gold) },
            text = { Text("Força impressa: ${card.strength}\nFileira: ${card.row.short}\nHabilidade: ${card.ability.label}\n${if (card.hero) "Herói: imune aos efeitos." else "Força atual: ${GwentRules.effectiveStrength(card, cards, weather, horns)}"}", color = Parchment, lineHeight = 19.sp) },
            confirmButton = { TextButton(onClick = { selectedCard = null }) { Text("FECHAR", color = Gold) } })
    }
}

@Composable
private fun PlaymatBackground() {
    val image = ImageBitmap.imageResource(R.drawable.gwent_playmat)
    Canvas(Modifier.fillMaxSize().clip(RoundedCornerShape(15.dp)).border(1.dp, MutedGold.copy(alpha = .65f), RoundedCornerShape(15.dp))) {
        drawImage(image, srcOffset = androidx.compose.ui.unit.IntOffset(0, 218), srcSize = androidx.compose.ui.unit.IntSize(image.width, 364), dstSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt()))
    }
}

@Composable
private fun CardPickerDialog(cards: List<CatalogCard>, faction: String, player: Int, onDismiss: () -> Unit, onPick: (CatalogCard, CombatRow) -> Unit) {
    var category by remember { mutableStateOf("Unidade") }
    var row by remember { mutableStateOf(CombatRow.CLOSE) }
    var query by remember { mutableStateOf("") }
    var specialInfo by remember { mutableStateOf<CatalogCard?>(null) }
    val categories = listOf("Líder", "Herói", "Especial", "Unidade")
    val filtered = CardCatalog.search(cards.filter { it.faction == faction && it.category == category }, query)
    AlertDialog(onDismissRequest = onDismiss, containerColor = PanelRaised,
        title = { Text("Cartas · ${if (player == 1) "Lado 1" else "Lado 2"}", color = Gold) },
        text = {
            Column(Modifier.heightIn(max = 540.dp)) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.forEach { item -> SmallChoice(item.uppercase(), item == category) { category = item } }
                }
                Spacer(Modifier.height(7.dp))
                if (category == "Unidade" || category == "Herói") {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { CombatRow.entries.forEach { option -> SmallChoice(option.short, option == row) { row = option } } }
                }
                OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Pesquisar") })
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
                    items(filtered, key = { it.id }) { card ->
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Panel).clickable {
                            if (category == "Líder") specialInfo = card else onPick(card, row)
                        }.padding(9.dp)) {
                            Text(card.name, color = Parchment, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("${card.strength?.let { "Força $it · " } ?: ""}${card.row.ifBlank { card.category }} · ${card.ability}", color = SoftText, fontSize = 9.sp, maxLines = 2)
                        }
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = onDismiss) { Text("FECHAR", color = SoftText) } })
    specialInfo?.let { card ->
        AlertDialog(onDismissRequest = { specialInfo = null }, containerColor = PanelRaised,
            title = { Text(card.name, color = Gold) },
            text = { Text("${card.category} · ${card.ability}\n\nEsta carta não é uma unidade de força. Consulte as regras e o texto da carta física para aplicar seu efeito.", color = Parchment) },
            confirmButton = { TextButton(onClick = { specialInfo = null }) { Text("OK", color = Gold) } })
    }
}

@Composable
private fun CardTile(card: UnitCard, strength: Int, onInspect: () -> Unit, onLeft: () -> Unit, onRight: () -> Unit, onRemove: () -> Unit, index: Int, lastIndex: Int) {
    Column(Modifier.padding(end = 4.dp).clip(RoundedCornerShape(8.dp)).background(if (card.player == 1) Color(0xFF33412E) else Color(0xFF4C342C)).clickable(onClick = onInspect).padding(horizontal = 5.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("${if (card.player == 1) "L1" else "L2"} · $strength", color = Gold, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Text(card.name, color = Parchment, fontSize = 7.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 72.dp))
        Row {
            if (index > 0) Text("‹", color = Parchment, fontSize = 12.sp, modifier = Modifier.clickable(onClick = onLeft).padding(horizontal = 4.dp))
            if (index < lastIndex) Text("›", color = Parchment, fontSize = 12.sp, modifier = Modifier.clickable(onClick = onRight).padding(horizontal = 4.dp))
            Text("×", color = SoftText, fontSize = 10.sp, modifier = Modifier.clickable(onClick = onRemove).padding(horizontal = 4.dp))
        }
    }
}

@Composable
fun ScorePanel(name: String, faction: Faction, leader: String, score: Int, gems: Int, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(15.dp)).background(Panel).border(1.dp, faction.tint.copy(alpha = .55f), RoundedCornerShape(15.dp)).padding(10.dp)) {
        Text(name, color = Parchment, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(faction.label.uppercase(), color = faction.tint, fontSize = 8.sp, letterSpacing = .5.sp)
        if (leader.isNotBlank()) Text(leader, color = SoftText, fontSize = 7.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(score.toString(), color = Gold, fontSize = 28.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("● ".repeat(gems.coerceAtLeast(0)).trim(), color = Danger, fontSize = 12.sp)
        }
    }
}

@Composable
private fun PlayerActionButton(label: String, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.height(44.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Moss, contentColor = Parchment)) {
        Text(label, fontSize = 8.sp, letterSpacing = .3.sp, textAlign = TextAlign.Center, lineHeight = 10.sp)
    }
}

@Composable
private fun CatalogCardRow(card: CatalogCard, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(11.dp)).background(Panel).clickable(onClick = onClick).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(card.name, color = Parchment, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text("${card.faction} · ${card.category} · ${card.ability}${card.row.takeIf(String::isNotBlank)?.let { " · $it" } ?: ""}", color = SoftText, fontSize = 8.sp, lineHeight = 12.sp)
        }
        Text(card.strength?.toString() ?: "—", color = Gold, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CardDetailsDialog(card: CatalogCard, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, containerColor = PanelRaised,
        title = { Text(card.name, color = Gold, fontFamily = FontFamily.Serif) },
        text = { Text("Facção: ${card.faction}\nTipo: ${card.category}\nForça: ${card.strength ?: "—"}\nAlcance: ${card.row.ifBlank { "Especial" }}\nHabilidade: ${card.ability}\nCópias no conjunto: ${card.count}", color = Parchment, lineHeight = 20.sp) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("FECHAR", color = Gold) } })
}

@Composable
fun PanelCard(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Panel).border(1.dp, Color(0xFF30362C), RoundedCornerShape(17.dp)).padding(14.dp), content = content)
}

@Composable
fun ScreenHeading(title: String, subtitle: String) {
    Text(title, color = Parchment, fontFamily = FontFamily.Serif, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(4.dp))
    Text(subtitle, color = SoftText, fontSize = 11.sp, lineHeight = 16.sp)
    Spacer(Modifier.height(12.dp))
}

@Composable
fun InfoStrip(title: String, body: String) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(Color(0xFF1C241B)).border(1.dp, Moss.copy(alpha = .7f), RoundedCornerShape(13.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(4.dp, 32.dp).clip(RoundedCornerShape(3.dp)).background(Moss))
        Spacer(Modifier.width(10.dp))
        Column { Text(title, color = Gold, fontSize = 9.sp, letterSpacing = 1.sp); Text(body, color = Parchment, fontSize = 10.sp, lineHeight = 14.sp) }
    }
}

@Composable
fun PrimaryButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink)) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
    }
}

@Composable
fun SmallChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(label, color = if (selected) Ink else Parchment, fontSize = 8.sp, letterSpacing = .25.sp,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (selected) Gold else Panel).clickable(onClick = onClick).padding(horizontal = 7.dp, vertical = 8.dp), maxLines = 1)
}

@Composable
private fun ShortcutCard(title: String, subtitle: String, mark: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(PanelRaised).border(1.dp, Color(0xFF343B31), RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(13.dp)) {
        Text(mark, color = Gold, fontFamily = FontFamily.Serif, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(7.dp))
        Text(title, color = Gold, fontSize = 8.sp, letterSpacing = .8.sp, maxLines = 2, lineHeight = 11.sp)
        Text(subtitle, color = Parchment, fontSize = 10.sp, lineHeight = 14.sp)
    }
}

@Composable
private fun FactionChip(faction: Faction, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.clip(RoundedCornerShape(10.dp)).background(if (selected) faction.tint.copy(alpha = .19f) else PanelRaised).border(1.dp, if (selected) faction.tint else Color(0xFF343B31), RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(17.dp).clip(CircleShape).background(faction.tint.copy(alpha = .24f)), contentAlignment = Alignment.Center) { Text(faction.mark, color = faction.tint, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(4.dp)); Text(faction.label, color = if (selected) Parchment else SoftText, fontSize = 8.sp, maxLines = 1)
    }
}

@Composable
private fun ChipWrap(values: List<Pair<String, Boolean>>, onClick: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        values.chunked(2).forEachIndexed { rowIndex, pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                pair.forEachIndexed { index, (label, selected) -> SmallChoice(label.uppercase(), selected) { onClick(rowIndex * 2 + index) } }
            }
        }
    }
}
