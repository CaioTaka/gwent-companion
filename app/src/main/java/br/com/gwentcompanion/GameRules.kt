package br.com.gwentcompanion

/** Reference model for the tabletop 2nd-edition rulebook. */
enum class CombatRow(val label: String, val short: String) {
    CLOSE("Combate corpo a corpo", "CORPO A CORPO"),
    RANGED("Combate à distância", "À DISTÂNCIA"),
    SIEGE("Cerco", "CERCO")
}

enum class Faction(val label: String, val mark: String, val tint: androidx.compose.ui.graphics.Color) {
    NORTHERN("Reinos do Norte", "N", androidx.compose.ui.graphics.Color(0xFF7189B8)),
    NILFGAARD("Nilfgaard", "N", androidx.compose.ui.graphics.Color(0xFFC19B4F)),
    MONSTERS("Monstros", "M", androidx.compose.ui.graphics.Color(0xFFB8524A)),
    SCOIATAEL("Scoia'tael", "S", androidx.compose.ui.graphics.Color(0xFF6C9A59)),
    SKELLIGE("Skellige", "S", androidx.compose.ui.graphics.Color(0xFF9B74A8))
}

enum class UnitAbility(val label: String) {
    NONE("Sem habilidade"), TIGHT_BOND("Laço estreito"), MORALE_BOOST("Moral")
}

data class UnitCard(
    val id: Long,
    val name: String,
    val strength: Int,
    val row: CombatRow,
    val player: Int,
    val hero: Boolean,
    val ability: UnitAbility = UnitAbility.NONE,
    val category: String = "Unidade"
)

/** Applies ongoing effects in the order specified on page 14 of the rulebook. */
object GwentRules {
    fun totalStrength(player: Int, cards: List<UnitCard>, weather: Set<CombatRow>, hornRows: Set<String>): Int =
        cards.filter { it.player == player }.sumOf { effectiveStrength(it, cards, weather, hornRows) }

    fun rowStrength(player: Int, row: CombatRow, cards: List<UnitCard>, weather: Set<CombatRow>, hornRows: Set<String>): Int =
        cards.filter { it.player == player && it.row == row }.sumOf { effectiveStrength(it, cards, weather, hornRows) }

    fun effectiveStrength(card: UnitCard, cards: List<UnitCard>, weather: Set<CombatRow>, hornRows: Set<String>): Int {
        // Heroes ignore card and weather effects. Weather sets each other unit's base strength to 1.
        var result = if (card.hero) card.strength else if (card.row in weather) 1 else card.strength
        if (card.hero) return result

        // Tight Bond multiplies this unit by the number of allied units with the same name.
        if (card.ability == UnitAbility.TIGHT_BOND) {
            val allies = cards.count { !it.hero && it.player == card.player && it.row == card.row && it.name.equals(card.name, ignoreCase = true) }
            result *= allies.coerceAtLeast(1)
        }

        // Each Morale Boost adds one to every other non-Hero unit in the same player's row.
        val moraleSources = cards.count {
            it.player == card.player && it.row == card.row && !it.hero && it.ability == UnitAbility.MORALE_BOOST && it.id != card.id
        }
        result += moraleSources

        // A Commander's Horn doubles non-Hero strength after the earlier ongoing effects.
        if (hornKey(card.player, card.row) in hornRows) result *= 2
        return result
    }

    fun hornKey(player: Int, row: CombatRow) = "$player:${row.name}"
}
