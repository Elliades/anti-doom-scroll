package app.antidoomscroll.application

import app.antidoomscroll.domain.SumPairParams
import org.springframework.stereotype.Component
import kotlin.random.Random

/** Color palette for statics; each static gets one color for its group. */
private val STATIC_COLORS = listOf("#3b82f6", "#22c55e", "#f59e0b", "#8b5cf6", "#ec4899")

/**
 * Generates groups for SUM_PAIR exercises. Each group has a static K and cards (pairs a,a+K).
 * All groups appear on one board; statics and their cards are colored for visual grouping.
 * For multiple statics, uses disjoint number pools so no cross-group matches are possible.
 */
@Component
class SumPairGenerator {

    /**
     * Produces groups (for display of statics) and a flat shuffled deck.
     * Deck order is stable per session (all cards from all groups, shuffled).
     */
    fun generateGroups(params: SumPairParams, random: Random = Random.Default): SumPairResult {
        val statics = params.staticNumbers
        val n = params.pairsPerRound
        val minVal = params.minValue
        val maxVal = params.maxValue

        val groups = if (statics.size == 1) {
            val k = statics.single()
            val cards = generateGroupCards(k, n, minVal, maxVal, random)
            val color = STATIC_COLORS[0]
            listOf(SumPairGroup(static = k, color = color, cards = cards))
        } else {
            val rangeSize = (maxVal - minVal + 1) / statics.size
            require(rangeSize >= 2 * n) {
                "Range too small for $n pairs across ${statics.size} groups; need at least ${2 * n} values per group"
            }
            val maxStatic = statics.maxOrNull() ?: 0
            require(rangeSize >= maxStatic + n) {
                "Per-group range size $rangeSize too small for $n pairs with static up to $maxStatic"
            }
            statics.mapIndexed { i, k ->
                val low = minVal + i * rangeSize
                val high = minVal + (i + 1) * rangeSize - 1
                val cards = generateGroupCards(k, n, low, high, random)
                val color = STATIC_COLORS[i % STATIC_COLORS.size]
                SumPairGroup(static = k, color = color, cards = cards)
            }
        }
        val deck = groups.flatMap { g ->
            g.cards.map { v -> SumPairCard(value = v, static = g.static, color = g.color) }
        }.shuffled(random)
        return SumPairResult(groups = groups, deck = deck)
    }

    /** Legacy alias for cache compatibility. */
    fun generateRounds(params: SumPairParams, random: Random = Random.Default): List<SumPairRound> =
        generateGroups(params, random).groups.map { g -> SumPairRound(static = g.static, cards = g.cards) }

    /**
     * Generates 2*n distinct card values for one group: n pairs (a, a+K) with all values in [low, high].
     */
    fun generateGroupCards(
        static: Int,
        pairCount: Int,
        low: Int,
        high: Int,
        random: Random = Random.Default
    ): List<Int> = generateRoundCards(static, pairCount, low, high, random)

    fun generateRoundCards(
        static: Int,
        pairCount: Int,
        low: Int,
        high: Int,
        random: Random = Random.Default
    ): List<Int> {
        require(high - low + 1 >= 2 * pairCount) { "Range must fit 2 * pairCount distinct values" }
        require(high - low >= static) { "Range must allow a and a+static" }
        // Need at least pairCount valid 'a' values: a in [low, high-static] => count = high - static - low + 1
        require(high - static - low + 1 >= pairCount) {
            "Range [$low, $high] too small for $pairCount pairs with static $static (need at least $pairCount values a with a+static <= $high)"
        }
        val pool = (low..high - static).toList().shuffled(random)
        val chosenA = mutableSetOf<Int>()
        val usedValues = mutableSetOf<Int>() // a and a+static already used
        for (a in pool) {
            if (chosenA.size >= pairCount) break
            if (a + static > high) continue
            if (a in usedValues || (a + static) in usedValues) continue
            // CRITICAL: no alternative valid pair. If we have (15,17) and (19,21), then (17,19)
            // is also valid—user could match (17,19) first, leaving 15,21 unsolvable.
            // Reject a when a±2K in chosenA (either order of iteration would create chain).
            if ((a - 2 * static) in chosenA || (a + 2 * static) in chosenA) continue
            chosenA.add(a)
            usedValues.add(a)
            usedValues.add(a + static)
        }
        require(chosenA.size == pairCount) {
            "Could not find $pairCount valid pairs in [$low, $high] for static $static"
        }
        val pairs = chosenA.flatMap { listOf(it, it + static) }
        // Postcondition: no chain x, x+K, x+2K in cards (ensures exactly one valid match per value)
        val cardSet = pairs.toSet()
        for (x in cardSet) {
            check((x + static) !in cardSet || (x - static) !in cardSet) {
                "Generator invariant violated: chain found at $x"
            }
        }
        return pairs.shuffled(random)
    }
}

/** One group: static K, color for UI, and card values. All groups shown on one board. */
data class SumPairGroup(val static: Int, val color: String, val cards: List<Int>)

/** Card with group info for flat shuffled deck (stable per session). */
data class SumPairCard(val value: Int, val static: Int, val color: String)

/** Groups + flat deck in display order. */
data class SumPairResult(val groups: List<SumPairGroup>, val deck: List<SumPairCard>)

/** Legacy: round without color (used by cache adapter). */
data class SumPairRound(val static: Int, val cards: List<Int>)
