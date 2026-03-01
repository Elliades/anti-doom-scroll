package app.antidoomscroll.application

import app.antidoomscroll.domain.ImagePairParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random

class ImagePairGeneratorTest {

    private val generator = ImagePairGenerator()

    @Test
    fun `generates deck with 2 times pairCount cards`() {
        val params = ImagePairParams(pairCount = 4, maxPairsPerBackground = 2, colorCount = 1)
        val result = generator.generate(params, Random(42))
        assertEquals(8, result.deck.size)
    }

    @Test
    fun `each pair has two cards with same backgroundId and imageId`() {
        val params = ImagePairParams(pairCount = 3, maxPairsPerBackground = 2, colorCount = 1)
        val result = generator.generate(params, Random(123))
        val deck = result.deck
        assertEquals(6, deck.size)
        val byKey = deck.groupBy { "${it.backgroundId}-${it.imageId}" }
        byKey.values.forEach { cards ->
            assertEquals(2, cards.size) { "Each (background, image) must appear exactly twice" }
            assertEquals(cards[0].backgroundId, cards[1].backgroundId)
            assertEquals(cards[0].imageId, cards[1].imageId)
        }
    }

    @Test
    fun `no background is used more than maxPairsPerBackground times`() {
        val params = ImagePairParams(pairCount = 6, maxPairsPerBackground = 2, colorCount = 2)
        val result = generator.generate(params, Random(456))
        val pairsByBackground = result.deck.asSequence()
            .map { it.backgroundId to it.imageId }
            .distinct()
            .groupBy { it.first }
        pairsByBackground.values.forEach { pairs ->
            assertTrue(pairs.size <= 2) { "Background ${pairs.first().first} has ${pairs.size} pairs, max 2" }
        }
    }

    @Test
    fun `colorCount 0 means only backgroundId 0`() {
        val params = ImagePairParams(pairCount = 2, maxPairsPerBackground = 2, colorCount = 0)
        val result = generator.generate(params, Random(789))
        assertTrue(result.deck.all { it.backgroundId == 0 })
        assertTrue(result.deck.all { it.backgroundColorHex == null })
    }

    @Test
    fun `invalid params throw`() {
        assertThrows<IllegalArgumentException> {
            ImagePairParams(pairCount = 10, maxPairsPerBackground = 2, colorCount = 0)
        }
    }
}
