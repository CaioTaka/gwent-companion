package br.com.gwentcompanion

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AppInk = Color(0xFF090B09)
private val AppGold = Color(0xFFD6B46A)
private val AppMoss = Color(0xFF587849)
private val AppParchment = Color(0xFFE4D7B9)
private val AppSoftText = Color(0xFFB7B9AF)

class MainActivity : ComponentActivity() {
    private var pendingUpdateFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(9, 11, 9)
        window.navigationBarColor = android.graphics.Color.rgb(9, 11, 9)
        setContent { GwentTheme { GwentApp(this) } }
    }

    override fun onResume() {
        super.onResume()
        val pending = pendingUpdateFile
        if (pending != null && pending.exists() && packageManager.canRequestPackageInstalls()) {
            pendingUpdateFile = null
            installApk(pending)
        }
    }

    fun checkForUpdates(onResult: (String, UpdateInfo?) -> Unit) {
        lifecycleScope.launch {
            onResult("Consultando a release pública do GitHub…", null)
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val connection = URL("https://api.github.com/repos/CaioTaka/gwent-companion/releases/latest").openConnection() as HttpURLConnection
                    connection.connectTimeout = 15_000
                    connection.readTimeout = 20_000
                    connection.setRequestProperty("Accept", "application/vnd.github+json")
                    connection.inputStream.bufferedReader().use { reader ->
                        val json = JSONObject(reader.readText())
                        val tag = json.getString("tag_name").removePrefix("v")
                        val assetList = json.getJSONArray("assets")
                        val apk = (0 until assetList.length()).map { assetList.getJSONObject(it) }
                            .firstOrNull { it.optString("name").endsWith(".apk", true) }
                            ?: error("A release mais recente não tem APK anexado.")
                        val installed = packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()
                        val candidate = UpdateInfo(tag, apk.getString("browser_download_url"), apk.optLong("size"))
                        if (compareVersions(tag, installed) <= 0) Pair("O Gwent Companion já está atualizado (v$installed).", null)
                        else Pair("Nova versão v$tag disponível (${"%.1f".format(Locale.US, candidate.size / 1_000_000.0)} MB).", candidate)
                    }
                }.getOrElse { Pair("Não foi possível consultar o GitHub: ${it.message ?: "erro de rede"}", null) }
            }
            onResult(result.first, result.second)
        }
    }

    fun downloadAndInstall(info: UpdateInfo, onStatus: (String) -> Unit) {
        lifecycleScope.launch {
            onStatus("Baixando v${info.version} do GitHub…")
            val fileResult = withContext(Dispatchers.IO) {
                runCatching {
                    require(info.url.startsWith("https://github.com/")) { "O link da release não é confiável." }
                    val connection = URL(info.url).openConnection() as HttpURLConnection
                    connection.connectTimeout = 20_000
                    connection.readTimeout = 60_000
                    val file = File(cacheDir, "gwent-companion-${info.version}.apk")
                    connection.inputStream.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
                    file
                }
            }
            fileResult.onSuccess { file ->
                if (!packageManager.canRequestPackageInstalls()) {
                    pendingUpdateFile = file
                    onStatus("APK baixado. O Android precisa permitir instalações vindas deste app; ative a permissão e volte para continuar.")
                    startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:$packageName")))
                } else {
                    onStatus("Abrindo o instalador do Android. Confirme a atualização na tela do sistema.")
                    installApk(file)
                }
            }.onFailure { onStatus("Falha no download da atualização: ${it.message ?: "erro de rede"}") }
        }
    }

    private fun installApk(file: File) {
        runCatching {
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}

data class UpdateInfo(val version: String, val url: String, val size: Long)

private fun compareVersions(left: String, right: String): Int {
    val a = Regex("\\d+").findAll(left).map { it.value.toIntOrNull() ?: 0 }.toList()
    val b = Regex("\\d+").findAll(right).map { it.value.toIntOrNull() ?: 0 }.toList()
    for (index in 0 until maxOf(a.size, b.size)) {
        val difference = (a.getOrNull(index) ?: 0).compareTo(b.getOrNull(index) ?: 0)
        if (difference != 0) return difference
    }
    return 0
}

private object BattleHistoryStore {
    private const val PREFS = "gwent-companion"
    private const val HISTORY = "battle-history"
    fun read(context: Context): List<BattleRecord> = runCatching {
        val source = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(HISTORY, "[]") ?: "[]"
        val json = JSONArray(source)
        List(json.length()) { i ->
            val item = json.getJSONObject(i)
            BattleRecord(item.optString("date"), item.optString("mode"), item.optString("rules"),
                item.optString("nameOne"), item.optString("factionOne"), item.optInt("scoreOne"),
                item.optString("nameTwo"), item.optString("factionTwo"), item.optInt("scoreTwo"), item.optString("usedCards"))
        }
    }.getOrDefault(emptyList())

    fun add(context: Context, record: BattleRecord) {
        val old = read(context)
        val json = JSONArray()
        (old + record).takeLast(100).forEach { item ->
            json.put(JSONObject().put("date", item.date).put("mode", item.mode).put("rules", item.rules)
                .put("nameOne", item.nameOne).put("factionOne", item.factionOne).put("scoreOne", item.scoreOne)
                .put("nameTwo", item.nameTwo).put("factionTwo", item.factionTwo).put("scoreTwo", item.scoreTwo)
                .put("usedCards", item.usedCards))
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(HISTORY, json.toString()).apply()
    }

    fun clear(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(HISTORY).apply()
}

@Composable
private fun GwentTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(
        primary = AppGold, onPrimary = AppInk, secondary = AppMoss, background = AppInk,
        surface = Color(0xFF151914), onSurface = AppParchment, onBackground = AppParchment,
        outline = Color(0xFF88764B), error = Color(0xFF9F4B43)
    ), content = content)
}

@Composable
private fun GwentApp(activity: MainActivity) {
    val context = LocalContext.current
    val catalog = remember { CardCatalog.load(context) }
    var page by remember { mutableStateOf(AppPage.HOME) }
    var factionOne by remember { mutableStateOf(Faction.NORTHERN) }
    var factionTwo by remember { mutableStateOf(Faction.NILFGAARD) }
    var playerOne by remember { mutableStateOf("Jogador 1") }
    var playerTwo by remember { mutableStateOf("Jogador 2") }
    var leaderOne by remember { mutableStateOf("") }
    var leaderTwo by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(GameMode.ONE_V_ONE) }
    var rules by remember { mutableStateOf(RuleMode.STANDARD) }
    var cards by remember { mutableStateOf(emptyList<UnitCard>()) }
    var playedCardCounts by remember { mutableStateOf(emptyMap<String, Int>()) }
    var weather by remember { mutableStateOf(emptySet<CombatRow>()) }
    var horns by remember { mutableStateOf(emptySet<String>()) }
    var round by remember { mutableIntStateOf(1) }
    var gemsOne by remember { mutableIntStateOf(2) }
    var gemsTwo by remember { mutableIntStateOf(2) }
    var lastRound by remember { mutableStateOf("") }
    var matchActive by remember { mutableStateOf(false) }
    var historySaved by remember { mutableStateOf(false) }
    var nextId by remember { mutableLongStateOf(1L) }
    var infoDialog by remember { mutableStateOf(false) }
    var updateStatus by remember { mutableStateOf("Toque para consultar a página pública de Releases.") }
    var availableUpdate by remember { mutableStateOf<UpdateInfo?>(null) }
    var updateDialog by remember { mutableStateOf(false) }
    var forceScores by remember { mutableStateOf(loadForceTrack(context)) }

    fun score(player: Int): Int = GwentRules.totalStrength(player, cards, weather, horns)

    fun beginMatch() {
        cards = emptyList(); weather = emptySet(); horns = emptySet(); round = 1
        playedCardCounts = buildMap {
            if (leaderOne.isNotBlank()) put("Líder", 1)
            if (leaderTwo.isNotBlank()) put("Líder", getOrDefault("Líder", 0) + 1)
        }
        gemsOne = 2; gemsTwo = 2; lastRound = ""; historySaved = false; nextId = 1L
        matchActive = true; page = AppPage.MATCH
    }

    fun endRound() {
        val a = score(1); val b = score(2)
        val nilfgaardTieWinner = if (factionOne == Faction.NILFGAARD && factionTwo != Faction.NILFGAARD) 1 else if (factionTwo == Faction.NILFGAARD && factionOne != Faction.NILFGAARD) 2 else 0
        when {
            a > b || (a == b && nilfgaardTieWinner == 1) -> { gemsTwo = (gemsTwo - 1).coerceAtLeast(0); lastRound = "$playerOne venceu a rodada" }
            b > a || (a == b && nilfgaardTieWinner == 2) -> { gemsOne = (gemsOne - 1).coerceAtLeast(0); lastRound = "$playerTwo venceu a rodada" }
            else -> { gemsOne = (gemsOne - 1).coerceAtLeast(0); gemsTwo = (gemsTwo - 1).coerceAtLeast(0); lastRound = "Empate: cada lado perde uma gema" }
        }
        val gameFinished = gemsOne == 0 || gemsTwo == 0
        if (gameFinished && !historySaved) {
            val countSummary = playedCardCounts.entries.sortedBy { it.key }.joinToString(" · ") { "${it.key}: ${it.value}" }.ifBlank { "Cartas registradas: 0" }
            val record = BattleRecord(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()), mode.title, rules.title,
                playerOne, factionOne.label, a, playerTwo, factionTwo.label, b, countSummary)
            BattleHistoryStore.add(context, record)
            historySaved = true
        }
        cards = emptyList(); weather = emptySet(); horns = emptySet()
        if (gameFinished) matchActive = false else round++
    }

    Box(Modifier.fillMaxSize().background(AppInk)) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            TopBrand(onSettings = { page = AppPage.SETTINGS }, onHome = { page = AppPage.HOME }, showHome = page != AppPage.HOME)
            when (page) {
                AppPage.HOME -> HomeScreen(matchActive, { page = AppPage.SETUP }, { page = AppPage.MATCH }) { page = it }
                AppPage.SETUP -> SetupScreen(playerOne, playerTwo, factionOne, factionTwo, mode, rules,
                    { playerOne = it }, { playerTwo = it }, { factionOne = it; if (leaderOne.isNotBlank() && catalog.none { card -> card.name == leaderOne && card.faction == it.label }) leaderOne = "" },
                    { factionTwo = it; if (leaderTwo.isNotBlank() && catalog.none { card -> card.name == leaderTwo && card.faction == it.label }) leaderTwo = "" },
                    { mode = it }, { rules = it }, catalog, leaderOne, leaderTwo, { leaderOne = it }, { leaderTwo = it },
                    { page = AppPage.HOME }, ::beginMatch)
                AppPage.MATCH -> MatchScreen(playerOne, playerTwo, factionOne, factionTwo, leaderOne, leaderTwo, cards, weather, horns, round,
                    gemsOne, gemsTwo, score(1), score(2), lastRound,
                    onPlayCard = { player, card, selectedRow ->
                        playedCardCounts = playedCardCounts + (card.category to ((playedCardCounts[card.category] ?: 0) + 1))
                        when {
                            card.category == "Líder" -> infoDialog = true
                            card.category == "Especial" && card.ability == "Clima" -> {
                                val name = CardCatalog.normalize(card.name)
                                when {
                                    "gutes wetter" in name -> weather = emptySet()
                                    "sturm" in name -> weather = weather + CombatRow.RANGED + CombatRow.SIEGE
                                    "frost" in name -> weather = weather + CombatRow.CLOSE
                                    "nebel" in name -> weather = weather + CombatRow.RANGED
                                    "regen" in name -> weather = weather + CombatRow.SIEGE
                                    else -> weather = weather + selectedRow
                                }
                            }
                            card.category == "Especial" && card.ability == "Trompa do Comandante" -> {
                                val key = GwentRules.hornKey(player, selectedRow); horns = if (key in horns) horns - key else horns + key
                            }
                            card.category == "Especial" -> infoDialog = true
                            else -> card.toUnitCard(nextId++, player, selectedRow)?.let { cards = cards + it }
                        }
                    },
                    onWeather = { row -> weather = if (row in weather) weather - row else weather + row },
                    onHorn = { player, row -> val key = GwentRules.hornKey(player, row); horns = if (key in horns) horns - key else horns + key },
                    onClearWeather = { weather = emptySet() }, onRemove = { id -> cards = cards.filterNot { it.id == id } },
                    onReorder = { id, delta -> cards = reorderCard(cards, id, delta) }, onEndRound = ::endRound,
                    onNewGame = { page = AppPage.SETUP }, catalog = catalog)
                AppPage.RULES -> BattleRulesScreen(catalog)
                AppPage.CATALOG -> CatalogScreen(catalog) { page = AppPage.HOME }
                AppPage.FORCE_TRACK -> ForceTrackScreen(forceScores, { player, row, delta ->
                    val mutable = forceScores.map { it.toMutableList() }.toMutableList()
                    mutable[player][row] = (mutable[player][row] + delta).coerceAtLeast(0)
                    forceScores = mutable.map { it.toList() }; saveForceTrack(context, forceScores)
                }, {
                    forceScores = listOf(listOf(0, 0, 0), listOf(0, 0, 0)); saveForceTrack(context, forceScores)
                })
                AppPage.HISTORY -> HistoryScreen(BattleHistoryStore.read(context)) { BattleHistoryStore.clear(context); page = AppPage.HISTORY }
                AppPage.SETTINGS -> SettingsScreen(updateStatus) {
                    availableUpdate = null
                    activity.checkForUpdates { status, update -> updateStatus = status; availableUpdate = update; updateDialog = update != null }
                }
                AppPage.RECOGNITION -> RecognitionScreen { infoDialog = true }
            }
        }
    }
    if (infoDialog) AlertDialog(onDismissRequest = { infoDialog = false }, containerColor = Color(0xFF20261F),
        title = { Text("Em desenvolvimento", color = AppGold) },
        text = { Text("Reconhecimento por imagem e leitura detalhada do texto das cartas ainda não estão disponíveis nesta versão.", color = AppParchment) },
        confirmButton = { TextButton(onClick = { infoDialog = false }) { Text("ENTENDI", color = AppGold) } })
    if (updateDialog && availableUpdate != null) AlertDialog(onDismissRequest = { updateDialog = false }, containerColor = Color(0xFF20261F),
        title = { Text("Atualização disponível", color = AppGold) },
        text = { Text("A versão v${availableUpdate!!.version} está disponível. O Android vai abrir a confirmação oficial de instalação depois do download.", color = AppParchment) },
        confirmButton = { TextButton(onClick = { updateDialog = false; activity.downloadAndInstall(availableUpdate!!) { updateStatus = it } }) { Text("BAIXAR E INSTALAR", color = AppGold) } },
        dismissButton = { TextButton(onClick = { updateDialog = false }) { Text("DEPOIS", color = AppSoftText) } })
}

private fun reorderCard(cards: List<UnitCard>, id: Long, delta: Int): List<UnitCard> {
    val index = cards.indexOfFirst { it.id == id }
    if (index < 0) return cards
    val related = cards.indices.filter { cards[it].row == cards[index].row && cards[it].player == cards[index].player }
    val position = related.indexOf(index)
    val targetPosition = (position + delta).coerceIn(0, related.lastIndex)
    if (targetPosition == position) return cards
    return cards.toMutableList().also { list ->
        val target = related[targetPosition]
        val current = list[index]
        list[index] = list[target]
        list[target] = current
    }
}

private fun loadForceTrack(context: Context): List<List<Int>> {
    val raw = context.getSharedPreferences("gwent-companion", Context.MODE_PRIVATE).getString("force-track", "0,0,0|0,0,0") ?: "0,0,0|0,0,0"
    return runCatching { raw.split('|').map { side -> side.split(',').map { it.toInt().coerceAtLeast(0) }.take(3).let { if (it.size == 3) it else listOf(0, 0, 0) } }.take(2).let { if (it.size == 2) it else listOf(listOf(0, 0, 0), listOf(0, 0, 0)) } }.getOrDefault(listOf(listOf(0, 0, 0), listOf(0, 0, 0)))
}

private fun saveForceTrack(context: Context, scores: List<List<Int>>) {
    val serialized = scores.joinToString("|") { side -> side.joinToString(",") }
    context.getSharedPreferences("gwent-companion", Context.MODE_PRIVATE).edit().putString("force-track", serialized).apply()
}
