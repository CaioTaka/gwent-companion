package br.com.gwentcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

private enum class AppPage { HOME, SETUP, MATCH }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(9, 11, 9)
        window.navigationBarColor = android.graphics.Color.rgb(9, 11, 9)
        setContent { GwentTheme { GwentApp() } }
    }
}

@Composable
private fun GwentTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Gold, onPrimary = Ink, secondary = Moss, background = Ink,
        surface = Panel, onSurface = Parchment, onBackground = Parchment,
        outline = MutedGold, error = Danger
    ), content = content)
}

@Composable
private fun GwentApp() {
    var page by remember { mutableStateOf(AppPage.HOME) }
    var factionOne by remember { mutableStateOf(Faction.NORTHERN) }
    var factionTwo by remember { mutableStateOf(Faction.NILFGAARD) }
    var playerOne by remember { mutableStateOf("Jogador 1") }
    var playerTwo by remember { mutableStateOf("Jogador 2") }
    var cards by remember { mutableStateOf(emptyList<UnitCard>()) }
    var weather by remember { mutableStateOf(emptySet<CombatRow>()) }
    var horns by remember { mutableStateOf(emptySet<String>()) }
    var round by remember { mutableIntStateOf(1) }
    var gemsOne by remember { mutableIntStateOf(2) }
    var gemsTwo by remember { mutableIntStateOf(2) }
    var lastRound by remember { mutableStateOf("") }
    var matchActive by remember { mutableStateOf(false) }
    var nextId by remember { mutableLongStateOf(1L) }
    var addCard by remember { mutableStateOf(false) }
    var infoDialog by remember { mutableStateOf(false) }

    fun score(player: Int): Int = GwentRules.totalStrength(player, cards, weather, horns)

    fun beginMatch() {
        cards = emptyList(); weather = emptySet(); horns = emptySet(); round = 1
        gemsOne = 2; gemsTwo = 2; lastRound = ""
        matchActive = true
        page = AppPage.MATCH
    }

    Box(Modifier.fillMaxSize().background(Ink)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            TopBrand(onHome = { page = AppPage.HOME })
            when (page) {
                AppPage.HOME -> HomeScreen(
                    hasMatch = matchActive,
                    onNew = { page = AppPage.SETUP }, onResume = { page = AppPage.MATCH },
                    onPhoto = { infoDialog = true }
                )
                AppPage.SETUP -> SetupScreen(
                    playerOne, playerTwo, factionOne, factionTwo,
                    onNameOne = { playerOne = it }, onNameTwo = { playerTwo = it },
                    onFactionOne = { factionOne = it }, onFactionTwo = { factionTwo = it },
                    onBack = { page = AppPage.HOME }, onStart = ::beginMatch
                )
                AppPage.MATCH -> MatchScreen(
                    playerOne, playerTwo, factionOne, factionTwo, cards, weather, horns, round,
                    gemsOne, gemsTwo, score(1), score(2), lastRound,
                    onAdd = { addCard = true },
                    onWeather = { row -> weather = if (row in weather) weather - row else weather + row },
                    onHorn = { player, row ->
                        val key = GwentRules.hornKey(player, row)
                        horns = if (key in horns) horns - key else horns + key
                    },
                    onClearWeather = { weather = emptySet() },
                    onRemove = { id -> cards = cards.filterNot { it.id == id } },
                    onEndRound = {
                        val a = score(1); val b = score(2)
                        when {
                            a > b -> { gemsTwo = (gemsTwo - 1).coerceAtLeast(0); lastRound = "$playerOne venceu a rodada" }
                            b > a -> { gemsOne = (gemsOne - 1).coerceAtLeast(0); lastRound = "$playerTwo venceu a rodada" }
                            else -> { gemsOne = (gemsOne - 1).coerceAtLeast(0); gemsTwo = (gemsTwo - 1).coerceAtLeast(0); lastRound = "Empate: cada lado perde uma gema" }
                        }
                        cards = emptyList(); weather = emptySet(); horns = emptySet()
                        if (gemsOne > 0 && gemsTwo > 0) round++ else matchActive = false
                    },
                    onNewGame = { page = AppPage.SETUP }
                )
            }
            Spacer(Modifier.height(26.dp))
        }
    }

    if (addCard) {
        AddUnitDialog(onDismiss = { addCard = false }, onAdd = { name, strength, row, player, hero, ability ->
            cards = cards + UnitCard(nextId++, name, strength, row, player, hero, ability)
            addCard = false
        })
    }
    if (infoDialog) {
        AlertDialog(onDismissRequest = { infoDialog = false }, containerColor = PanelRaised,
            title = { Text("Leitura por foto", color = Gold, fontFamily = FontFamily.Serif) },
            text = { Text("A leitura automática entra numa próxima etapa. O placar, as cartas e as regras continuarão disponíveis sem internet.", color = Parchment) },
            confirmButton = { TextButton(onClick = { infoDialog = false }) { Text("Entendi", color = Gold) } })
    }
}

@Composable
private fun TopBrand(onHome: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 18.dp).clickable(onClick = onHome), verticalAlignment = Alignment.CenterVertically) {
        Crest(42.dp)
        Spacer(Modifier.width(11.dp))
        Column {
            Text("GWENT", color = Parchment, fontFamily = FontFamily.Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Text("COMPANION  ·  2ª EDIÇÃO", color = Gold, fontSize = 9.sp, letterSpacing = 1.6.sp)
        }
        Spacer(Modifier.weight(1f))
        Text("●", color = Moss, fontSize = 12.sp)
        Spacer(Modifier.width(6.dp))
        Text("OFFLINE", color = SoftText, fontSize = 9.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun Crest(size: androidx.compose.ui.unit.Dp) {
    Canvas(Modifier.size(size)) {
        val c = center
        val r = this.size.minDimension * .45f
        drawCircle(MutedGold, r, c, style = Stroke(width = 1.3.dp.toPx()))
        drawCircle(Color(0xFF26281E), r * .78f, c)
        val shield = Path().apply {
            moveTo(c.x, c.y - r * .56f); lineTo(c.x + r * .48f, c.y - r * .28f)
            lineTo(c.x + r * .37f, c.y + r * .28f); lineTo(c.x, c.y + r * .62f)
            lineTo(c.x - r * .37f, c.y + r * .28f); lineTo(c.x - r * .48f, c.y - r * .28f); close()
        }
        drawPath(shield, Gold, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
        drawLine(Gold, Offset(c.x - r * .25f, c.y + r * .12f), Offset(c.x + r * .25f, c.y - r * .12f), 1.dp.toPx(), StrokeCap.Round)
        drawLine(Gold, Offset(c.x - r * .25f, c.y - r * .12f), Offset(c.x + r * .25f, c.y + r * .12f), 1.dp.toPx(), StrokeCap.Round)
    }
}

@Composable
private fun HomeScreen(hasMatch: Boolean, onNew: () -> Unit, onResume: () -> Unit, onPhoto: () -> Unit) {
    Spacer(Modifier.height(12.dp))
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Panel).border(1.dp, MutedGold.copy(alpha = .55f), RoundedCornerShape(24.dp)).padding(22.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Crest(100.dp)
            Spacer(Modifier.height(16.dp))
            Text("CAMPO DE BATALHA", color = Gold, fontSize = 10.sp, letterSpacing = 2.3.sp)
            Spacer(Modifier.height(8.dp))
            Text("A guerra se decide\nna contagem.", color = Parchment, textAlign = TextAlign.Center, fontFamily = FontFamily.Serif, fontSize = 29.sp, lineHeight = 34.sp)
            Spacer(Modifier.height(11.dp))
            Text("Placar e regras para Gwent: O Jogo de Cartas Lendário", color = SoftText, textAlign = TextAlign.Center, fontSize = 12.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(23.dp))
            PrimaryButton(if (hasMatch) "NOVA PARTIDA" else "COMEÇAR PARTIDA", onNew)
            if (hasMatch) {
                Spacer(Modifier.height(9.dp))
                OutlinedButton(onClick = onResume, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("RETOMAR PARTIDA", color = Parchment, letterSpacing = 1.sp) }
            }
        }
    }
    Spacer(Modifier.height(17.dp))
    Text("ATALHOS DE MESA", color = MutedGold, fontSize = 10.sp, letterSpacing = 1.8.sp)
    Spacer(Modifier.height(9.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ShortcutCard("LEITURA", "Conferir o tabuleiro", "◉", onPhoto, Modifier.weight(1f))
        ShortcutCard("CARTAS", "Consultar catálogo", "✦", onPhoto, Modifier.weight(1f))
    }
    Spacer(Modifier.height(15.dp))
    Text("FEITO PARA A MESA", color = Gold, fontFamily = FontFamily.Serif, fontSize = 17.sp)
    Spacer(Modifier.height(5.dp))
    Text("Sem anúncios. Sem conta. O núcleo da partida funciona offline.", color = SoftText, fontSize = 12.sp, lineHeight = 18.sp)
}

@Composable
private fun ShortcutCard(title: String, subtitle: String, mark: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(PanelRaised).border(1.dp, Color(0xFF343B31), RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(14.dp)) {
        Text(mark, color = Gold, fontFamily = FontFamily.Serif, fontSize = 24.sp)
        Spacer(Modifier.height(10.dp))
        Text(title, color = Gold, fontSize = 9.sp, letterSpacing = 1.4.sp)
        Text(subtitle, color = Parchment, fontSize = 12.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun SetupScreen(
    nameOne: String, nameTwo: String, factionOne: Faction, factionTwo: Faction,
    onNameOne: (String) -> Unit, onNameTwo: (String) -> Unit,
    onFactionOne: (Faction) -> Unit, onFactionTwo: (Faction) -> Unit,
    onBack: () -> Unit, onStart: () -> Unit
) {
    ScreenHeading("Preparar a partida", "Escolha os comandantes e as facções.")
    PlayerSetup("LADO DO JOGADOR 1", nameOne, factionOne, onNameOne, onFactionOne)
    Spacer(Modifier.height(12.dp))
    PlayerSetup("LADO DO JOGADOR 2", nameTwo, factionTwo, onNameTwo, onFactionTwo)
    Spacer(Modifier.height(16.dp))
    InfoStrip("2 gemas por jogador", "A primeira pessoa a perder as duas gemas perde a partida.")
    Spacer(Modifier.height(20.dp))
    PrimaryButton("ABRIR O TABULEIRO", onStart)
    Spacer(Modifier.height(8.dp))
    TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("VOLTAR", color = SoftText, letterSpacing = 1.sp) }
}

@Composable
private fun PlayerSetup(title: String, name: String, faction: Faction, onName: (String) -> Unit, onFaction: (Faction) -> Unit) {
    PanelCard {
        Text(title, color = Gold, fontSize = 9.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(value = name, onValueChange = onName, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Nome na mesa") })
        Spacer(Modifier.height(12.dp))
        Text("FACÇÃO", color = SoftText, fontSize = 9.sp, letterSpacing = 1.3.sp)
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Faction.entries.chunked(3).forEach { line ->
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    line.forEach { option -> FactionChip(option, option == faction, { onFaction(option) }, Modifier.weight(1f)) }
                    repeat(3 - line.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun FactionChip(faction: Faction, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.clip(RoundedCornerShape(12.dp)).background(if (selected) faction.tint.copy(alpha = .19f) else PanelRaised).border(1.dp, if (selected) faction.tint else Color(0xFF343B31), RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(horizontal = 9.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Box(Modifier.size(20.dp).clip(CircleShape).background(faction.tint.copy(alpha = .24f)), contentAlignment = Alignment.Center) { Text(faction.mark, color = faction.tint, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(5.dp))
        Text(faction.label, color = if (selected) Parchment else SoftText, fontSize = 9.sp, maxLines = 1)
    }
}

@Composable
private fun MatchScreen(
    playerOne: String, playerTwo: String, factionOne: Faction, factionTwo: Faction,
    cards: List<UnitCard>, weather: Set<CombatRow>, horns: Set<String>, round: Int,
    gemsOne: Int, gemsTwo: Int, scoreOne: Int, scoreTwo: Int, lastRound: String,
    onAdd: () -> Unit, onWeather: (CombatRow) -> Unit, onHorn: (Int, CombatRow) -> Unit, onClearWeather: () -> Unit,
    onRemove: (Long) -> Unit, onEndRound: () -> Unit, onNewGame: () -> Unit
) {
    val finished = gemsOne == 0 || gemsTwo == 0
    ScreenHeading(if (finished) "Partida encerrada" else "Rodada $round", if (finished) "Resultado final" else "Ajuste as cartas e acompanhe a força total.")
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        ScorePanel(playerOne, factionOne, scoreOne, gemsOne, Modifier.weight(1f))
        ScorePanel(playerTwo, factionTwo, scoreTwo, gemsTwo, Modifier.weight(1f))
    }
    Spacer(Modifier.height(12.dp))
    if (lastRound.isNotBlank()) InfoStrip(if (finished) "FIM DE JOGO" else "RODADA ANTERIOR", if (finished) if (gemsOne == 0 && gemsTwo == 0) "A partida terminou empatada." else "${if (gemsOne > 0) playerOne else playerTwo} venceu." else lastRound)
    Spacer(Modifier.height(13.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("FILEIRAS DE COMBATE", color = Gold, fontSize = 10.sp, letterSpacing = 1.7.sp)
        Spacer(Modifier.weight(1f))
        Text("CLIMA ATIVO ${if (weather.isEmpty()) "·" else "${weather.size}"}", color = SoftText, fontSize = 9.sp)
    }
    Spacer(Modifier.height(8.dp))
    CombatRow.entries.forEach { row ->
        RowPanel(row, cards.filter { it.row == row }, weather.contains(row), horns, playerOne, playerTwo, onWeather, onHorn, onRemove)
        Spacer(Modifier.height(8.dp))
    }
    Spacer(Modifier.height(7.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        OutlinedButton(onClick = onClearWeather, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp), enabled = weather.isNotEmpty()) { Text("LIMPAR CLIMA", color = Parchment, fontSize = 10.sp, letterSpacing = .8.sp) }
        Button(onClick = onAdd, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Moss, contentColor = Parchment), enabled = !finished) { Text("＋  ADICIONAR CARTA", fontSize = 10.sp, letterSpacing = .6.sp) }
    }
    Spacer(Modifier.height(9.dp))
    Button(onClick = onEndRound, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink), enabled = !finished) { Text("ENCERRAR RODADA", fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }
    Spacer(Modifier.height(5.dp))
    Text("Clima → Laço → Moral → Trompa · heróis são imunes a efeitos.", color = SoftText, fontSize = 10.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    if (finished) {
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onNewGame, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("NOVA PARTIDA", color = Gold, letterSpacing = 1.sp) }
    }
}

@Composable
private fun ScorePanel(name: String, faction: Faction, score: Int, gems: Int, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(17.dp)).background(Panel).border(1.dp, faction.tint.copy(alpha = .55f), RoundedCornerShape(17.dp)).padding(13.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(24.dp).clip(CircleShape).background(faction.tint.copy(alpha = .20f)), contentAlignment = Alignment.Center) { Text(faction.mark, color = faction.tint, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.width(7.dp))
            Column {
                Text(name, color = Parchment, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(faction.label.uppercase(), color = faction.tint, fontSize = 8.sp, letterSpacing = .7.sp, maxLines = 1)
            }
        }
        Spacer(Modifier.height(9.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(score.toString(), color = Gold, fontSize = 32.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(5.dp)); Text("FORÇA", color = SoftText, fontSize = 8.sp, modifier = Modifier.padding(bottom = 5.dp), letterSpacing = 1.sp)
            Spacer(Modifier.weight(1f))
            Text("● ".repeat(gems).trim(), color = Color(0xFFB84C43), fontSize = 12.sp)
        }
    }
}

@Composable
private fun RowPanel(row: CombatRow, cards: List<UnitCard>, weather: Boolean, horns: Set<String>, playerOne: String, playerTwo: String, onWeather: (CombatRow) -> Unit, onHorn: (Int, CombatRow) -> Unit, onRemove: (Long) -> Unit) {
    PanelCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(row.short, color = Parchment, fontFamily = FontFamily.Serif, fontSize = 14.sp, letterSpacing = .6.sp)
                Text("${cards.count { it.player == 1 }} + ${cards.count { it.player == 2 }} cartas", color = SoftText, fontSize = 10.sp)
            }
            Text("${GwentRules.rowStrength(1, row, cards, if (weather) setOf(row) else emptySet(), horns)} : ${GwentRules.rowStrength(2, row, cards, if (weather) setOf(row) else emptySet(), horns)}", color = Gold, fontSize = 15.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = { onWeather(row) }) { Text(if (weather) "TEMPO" else "CLIMA", color = if (weather) Gold else SoftText, fontSize = 9.sp) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SmallChoice("TROMPA P1", GwentRules.hornKey(1, row) in horns) { onHorn(1, row) }
            SmallChoice("TROMPA P2", GwentRules.hornKey(2, row) in horns) { onHorn(2, row) }
        }
        if (weather) Text("EFEITO DE CLIMA · unidades comuns valem 1", color = Gold, fontSize = 8.sp, letterSpacing = .7.sp)
        cards.forEach { card ->
            Row(Modifier.fillMaxWidth().padding(top = 7.dp).clip(RoundedCornerShape(10.dp)).background(PanelRaised).padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(if (card.player == 1) "P1" else "P2", color = if (card.player == 1) Moss else factionAccent(card.player), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(card.name + if (card.hero) "  ·  HERÓI" else "", color = Parchment, fontSize = 11.sp, maxLines = 1)
                    Text(if (card.player == 1) playerOne else playerTwo, color = SoftText, fontSize = 8.sp)
                }
                Text(GwentRules.effectiveStrength(card, cards, if (weather) setOf(row) else emptySet(), horns).toString(), color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Text("×", color = SoftText, fontSize = 16.sp, modifier = Modifier.clickable { onRemove(card.id) }.padding(horizontal = 4.dp))
            }
        }
    }
}

private fun factionAccent(player: Int) = if (player == 1) Moss else Color(0xFFB8524A)

@Composable
private fun AddUnitDialog(onDismiss: () -> Unit, onAdd: (String, Int, CombatRow, Int, Boolean, UnitAbility) -> Unit) {
    var name by remember { mutableStateOf("") }
    var strength by remember { mutableStateOf("4") }
    var row by remember { mutableStateOf(CombatRow.CLOSE) }
    var player by remember { mutableIntStateOf(1) }
    var hero by remember { mutableStateOf(false) }
    var ability by remember { mutableStateOf(UnitAbility.NONE) }
    AlertDialog(onDismissRequest = onDismiss, containerColor = PanelRaised,
        title = { Text("Registrar carta", color = Gold, fontFamily = FontFamily.Serif) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, label = { Text("Nome da carta") })
                OutlinedTextField(value = strength, onValueChange = { value -> strength = value.filter(Char::isDigit).take(2) }, singleLine = true, label = { Text("Força impressa") })
                Text("FILEIRA", color = Gold, fontSize = 9.sp, letterSpacing = 1.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { CombatRow.entries.forEach { option -> SmallChoice(option.short, row == option) { row = option } } }
                Text("LADO", color = Gold, fontSize = 9.sp, letterSpacing = 1.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { SmallChoice("JOGADOR 1", player == 1) { player = 1 }; SmallChoice("JOGADOR 2", player == 2) { player = 2 } }
                Text("HABILIDADE", color = Gold, fontSize = 9.sp, letterSpacing = 1.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    UnitAbility.entries.forEach { option -> SmallChoice(option.label.uppercase(), ability == option) { ability = option } }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hero, onCheckedChange = { hero = it })
                    Text("Herói (imune a efeitos)", color = Parchment, fontSize = 12.sp)
                }
            }
        },
        confirmButton = { TextButton(onClick = { onAdd(name.ifBlank { "Unidade" }, strength.toIntOrNull() ?: 0, row, player, hero, ability) }) { Text("ADICIONAR", color = Gold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR", color = SoftText) } })
}

@Composable
private fun SmallChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(label, color = if (selected) Ink else Parchment, fontSize = 8.sp, letterSpacing = .3.sp,
        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(if (selected) Gold else Panel).clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 9.dp))
}

@Composable
private fun ScreenHeading(title: String, subtitle: String) {
    Spacer(Modifier.height(4.dp))
    Text(title, color = Parchment, fontFamily = FontFamily.Serif, fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(4.dp))
    Text(subtitle, color = SoftText, fontSize = 12.sp, lineHeight = 17.sp)
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun PanelCard(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Panel).border(1.dp, Color(0xFF30362C), RoundedCornerShape(17.dp)).padding(14.dp), content = content)
}

@Composable
private fun InfoStrip(title: String, body: String) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF1C241B)).border(1.dp, Moss.copy(alpha = .7f), RoundedCornerShape(14.dp)).padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(4.dp, 35.dp).clip(RoundedCornerShape(3.dp)).background(Moss))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, color = Gold, fontSize = 9.sp, letterSpacing = 1.sp)
            Text(body, color = Parchment, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(51.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink)) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.1.sp)
    }
}
