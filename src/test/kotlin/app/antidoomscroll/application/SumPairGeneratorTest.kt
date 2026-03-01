package app.antidoomscroll.application

import app.antidoomscroll.domain.SumPairParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random

class SumPairGeneratorTest {

    private val generator = SumPairGenerator()

    @Test
    fun `single static produces one group with correct card count`() {
        val params = SumPairParams(
            staticNumbers = listOf(5),
            pairsPerRound = 4,
            minValue = 1,
            maxValue = 50
        )
        val result = generator.generateGroups(params, Random(42))
        assertEquals(1, result.groups.size)
        assertEquals(5, result.groups[0].static)
        assertEquals(8, result.groups[0].cards.size)
        assertEquals(8, result.deck.size)
    }

    @Test
    fun `single group cards form valid sum pairs`() {
        val params = SumPairParams(
            staticNumbers = listOf(3),
            pairsPerRound = 3,
            minValue = 1,
            maxValue = 30
        )
        val result = generator.generateGroups(params, Random(123))
        val cards = result.groups.single().cards
        val static = result.groups.single().static
        val values = cards.toMutableList()
        val matched = mutableListOf<Pair<Int, Int>>()
        while (values.size >= 2) {
            val a = values.minOrNull()!!
            val b = a + static
            assertTrue(values.contains(b)) { "No partner for $a (expected $b). Values: $values" }
            matched.add(a to b)
            values.remove(a)
            values.remove(b)
        }
        assertEquals(3, matched.size)
    }

    @Test
    fun `multi static produces one group per static`() {
        val params = SumPairParams(
            staticNumbers = listOf(2, 5, 7),
            pairsPerRound = 3,
            minValue = 1,
            maxValue = 99
        )
        val result = generator.generateGroups(params, Random(456))
        assertEquals(3, result.groups.size)
        assertEquals(2, result.groups[0].static)
        assertEquals(5, result.groups[1].static)
        assertEquals(7, result.groups[2].static)
        result.groups.forEach { g ->
            assertEquals(6, g.cards.size)
            assertTrue(g.cards.all { it in 1..99 })
        }
        assertEquals(18, result.deck.size)
    }

    @Test
    fun `multi static disjoint pools - no number from group i appears in group j`() {
        val params = SumPairParams(
            staticNumbers = listOf(3, 4),
            pairsPerRound = 4,
            minValue = 1,
            maxValue = 99
        )
        val result = generator.generateGroups(params, Random(789))
        val rounds = result.groups
        assertEquals(2, rounds.size)
        val pool0 = rounds[0].cards.toSet()
        val pool1 = rounds[1].cards.toSet()
        assertTrue(pool0.intersect(pool1).isEmpty()) { "Pools must be disjoint: $pool0 vs $pool1" }
    }

    @Test
    fun `multi static each group has valid sum pairs for its static`() {
        val params = SumPairParams(
            staticNumbers = listOf(2, 5),
            pairsPerRound = 3,
            minValue = 1,
            maxValue = 99
        )
        val rounds = generator.generateGroups(params, Random(111)).groups
        rounds.forEach { round ->
            val values = round.cards.toMutableList()
            while (values.size >= 2) {
                val a = values.minOrNull()!!
                val b = a + round.static
                assertTrue(values.contains(b)) { "Round static=${round.static}: no partner for $a. Values: $values" }
                values.remove(a)
                values.remove(b)
            }
        }
    }

    @Test
    fun `all cards in a group are distinct`() {
        val params = SumPairParams(
            staticNumbers = listOf(1),
            pairsPerRound = 5,
            minValue = 1,
            maxValue = 80
        )
        val result = generator.generateGroups(params, Random(222))
        val cards = result.groups.single().cards
        assertEquals(cards.size, cards.toSet().size)
    }

    @Test
    fun `range too small for pairs throws`() {
        val params = SumPairParams(
            staticNumbers = listOf(10),
            pairsPerRound = 10,
            minValue = 1,
            maxValue = 15
        )
        assertThrows<IllegalArgumentException> {
            generator.generateGroups(params, Random(0))
        }
    }

    @Test
    fun `static too large for range throws in generateRoundCards`() {
        // Range [1, 10]: valid a in [1, 10-5]=[1,5], only 5 values; we need 3 pairs so 3 values of a -> ok.
        // Range [1, 8], static 5: valid a in [1, 3], only 3 values -> exactly 3 pairs, ok.
        // Range [1, 7], static 5: valid a in [1, 2], only 2 values -> need 3 pairs, fails.
        assertThrows<IllegalArgumentException> {
            generator.generateRoundCards(static = 5, pairCount = 3, low = 1, high = 7, random = Random(0))
        }
    }

    /**
     * The dangerous case: K=2, numbers 2,4,6,8. Both (2,4) and (4,6) are valid!
     * If user matches (4,6) first, 2 and 8 remain — 2+2≠8, unsolvable.
     * Generator must ensure: for chosen a values, a_i + K never equals a_j (no value is both
     * the "b" of one pair and the "a" of another).
     */
    @Test
    fun `no value is both b of one pair and a of another - avoids 4 6 8 trap`() {
        val params = SumPairParams(
            staticNumbers = listOf(2),
            pairsPerRound = 3,
            minValue = 1,
            maxValue = 50
        )
        repeat(100) { seed ->
            val result = generator.generateGroups(params, Random(seed))
            val cards = result.groups.single().cards
            val k = 2
            // For every pair (a, a+k), ensure no other pair has a+k as its "a"
            val asValues = cards.map { v -> v to (v - k) }.filter { (b, potentialA) -> potentialA in cards }
            val allA = mutableSetOf<Int>()
            for ((b, a) in asValues) {
                assertTrue(a !in allA) { "Seed $seed: $a appears as both 'a' and 'b' - overlap! Cards: $cards" }
                allA.add(a)
            }
            // Explicit check: no consecutive chain a, a+K, a+2K all in cards (would allow wrong match)
            val sorted = cards.sorted()
            for (i in 0 until sorted.size - 2) {
                val x = sorted[i]
                val y = sorted[i + 1]
                val z = sorted[i + 2]
                assertFalse(x + k == y && y + k == z) {
                    "Seed $seed: chain $x, $y, $z with K=$k allows wrong match. Cards: $cards"
                }
            }
        }
    }

    /**
     * CRITICAL: Every value must have exactly one valid pair partner.
     * If (6,8) is valid and user matches it first, (4,10) would be left but 4+2≠10 — unsolvable!
     * Generator must ensure no overlapping pairs: each number appears in exactly one pair.
     */
    @Test
    fun `every card value has exactly one valid pair - no overlapping pairs`() {
        repeat(200) { seed ->
            val params = SumPairParams(
                staticNumbers = listOf(2, 3, 5, 7),
                pairsPerRound = 4,
                minValue = 1,
                maxValue = 99
            )
            val result = generator.generateGroups(params, Random(seed))
            result.groups.forEach { g ->
                val cards = g.cards
                val k = g.static
                // Build the unique pair partition: each value must appear in exactly one valid pair
                val remaining = cards.toMutableSet()
                val matchedPairs = mutableListOf<Pair<Int, Int>>()
                while (remaining.size >= 2) {
                    val a = remaining.minOrNull()!!
                    val b = a + k
                    assertTrue(remaining.contains(b)) {
                        "Seed $seed static=$k: no partner for $a (expected $b). Remaining: $remaining. Matched: $matchedPairs"
                    }
                    matchedPairs.add(a to b)
                    remaining.remove(a)
                    remaining.remove(b)
                }
                assertEquals(0, remaining.size) { "Seed $seed static=$k: unmatched values $remaining" }
                assertEquals(g.cards.size / 2, matchedPairs.size)
                // Verify no overlap: no value in one pair appears in another
                val allInPairs = matchedPairs.flatMap { (x, y) -> listOf(x, y) }
                assertEquals(allInPairs.size, allInPairs.toSet().size) { "Overlapping pairs!" }
            }
        }
    }

    @Test
    fun `multi round with large static relative to range throws`() {
        // 2 rounds, rangeSize = 99/2 = 49. Need rangeSize >= maxStatic + n; 49 >= 45+5 = 50 fails.
        val params = SumPairParams(
            staticNumbers = listOf(5, 45),
            pairsPerRound = 5,
            minValue = 1,
            maxValue = 99
        )
        assertThrows<IllegalArgumentException> {
            generator.generateGroups(params, Random(0))
        }
    }
}
