package br.com.gwentcompanion

import android.content.Context
import org.json.JSONArray
import java.text.Normalizer

data class CatalogCard(
    val id: String,
    val faction: String,
    val name: String,
    val strength: Int?,
    val row: String,
    val category: String,
    val ability: String,
    val count: Int
)

object CardCatalog {
    fun load(context: Context): List<CatalogCard> = runCatching {
        val source = context.assets.open("cards.json").bufferedReader().use { it.readText() }
        val json = JSONArray(source)
        List(json.length()) { index ->
            val item = json.getJSONObject(index)
            CatalogCard(
                id = item.getString("id"), faction = item.getString("faction"), name = item.getString("name"),
                strength = if (item.isNull("strength")) null else item.getInt("strength"),
                row = item.optString("row"), category = item.getString("category"),
                ability = item.getString("ability"), count = item.optInt("count", 1)
            )
        }
    }.getOrDefault(emptyList())

    fun normalize(value: String): String = Normalizer.normalize(value.lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "").trim()

    /** All search terms may match any part of a name, independent of word order. */
    fun search(cards: List<CatalogCard>, query: String): List<CatalogCard> {
        val terms = normalize(query).split(Regex("\\s+")).filter(String::isNotBlank)
        if (terms.isEmpty()) return cards
        return cards.filter { card ->
            val haystack = normalize("${card.name} ${card.ability} ${card.category} ${card.faction}")
            terms.all { term -> haystack.contains(term) || fuzzySubsequence(haystack, term) }
        }
    }

    private fun fuzzySubsequence(text: String, term: String): Boolean {
        if (term.length < 4) return false
        var position = 0
        for (character in term) {
            position = text.indexOf(character, position)
            if (position < 0) return false
            position++
        }
        return true
    }
}

fun CatalogCard.toUnitCard(id: Long, player: Int, selectedRow: CombatRow? = null): UnitCard? {
    if (category != "Unidade" && category != "Herói" && category != "Reserva") return null
    val combatRow = when (selectedRow) {
        CombatRow.CLOSE -> CombatRow.CLOSE
        CombatRow.RANGED -> CombatRow.RANGED
        CombatRow.SIEGE -> CombatRow.SIEGE
        null -> when {
            "corpo a corpo" in row.lowercase() -> CombatRow.CLOSE
            "distância" in row.lowercase() -> CombatRow.RANGED
            else -> CombatRow.SIEGE
        }
    }
    val ability = when (this.ability) {
        "Laço estreito" -> UnitAbility.TIGHT_BOND
        "Moral", "Agilidade + Moral" -> UnitAbility.MORALE_BOOST
        else -> UnitAbility.NONE
    }
    return UnitCard(id, name, strength ?: 0, combatRow, player, category == "Herói", ability, category)
}
