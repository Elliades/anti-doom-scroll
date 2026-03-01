package app.antidoomscroll.application

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EstimationCuratedItemsTest {

    private val loader = EstimationCuratedItems(EstimationParametricConfig())

    @Test
    fun `items for level 0 are JSON level 1`() {
        val items = loader.itemsForLevel(0)
        assertTrue(items.isNotEmpty())
        assertEquals(1, items.first().level)
    }

    @Test
    fun `resolve level 0 returns math or fact`() {
        val g = loader.resolve(levelIndex = 0, seed = 1L)
        assertNotNull(g)
        assertTrue(g!!.prompt.startsWith("Estimate:"))
        assertTrue(g.correctAnswer >= 0)
    }

    @Test
    fun `same seed same result`() {
        val a = loader.resolve(levelIndex = 0, seed = 42L)
        val b = loader.resolve(levelIndex = 0, seed = 42L)
        assertNotNull(a)
        assertNotNull(b)
        assertEquals(a!!.prompt, b!!.prompt)
        assertEquals(a.correctAnswer, b.correctAnswer)
    }

    @Test
    fun `resolve level 3 returns item with two variables`() {
        val g = loader.resolve(levelIndex = 2, seed = 10L)
        assertNotNull(g)
        assertTrue(g!!.prompt.contains("Estimate:"))
    }

    @Test
    fun `resolve fact item has tolerance from JSON`() {
        val items = loader.itemsForLevel(3)
        val fact = items.filterIsInstance<EstimationCuratedItems.EstimationItem.Fact>().firstOrNull()
        assertNotNull(fact)
        val g = loader.resolve(levelIndex = 3, seed = 0L)
        assertNotNull(g)
        assertTrue(g!!.toleranceFactor >= 1.0)
    }
}
