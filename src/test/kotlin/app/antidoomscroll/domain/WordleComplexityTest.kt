package app.antidoomscroll.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WordleComplexityTest {

    @Test
    fun `longer words and tighter time increase score`() {
        val easy = WordleComplexity.compute(3, 6, 300, "en")
        val hard = WordleComplexity.compute(7, 4, 60, "en")
        assertTrue(hard.difficultyScore0To100 > easy.difficultyScore0To100)
        assertTrue(easy.difficultyScore0To100 in 0..100)
        assertTrue(hard.difficultyScore0To100 in 0..100)
    }

    @Test
    fun `seeded FR exercises match expected scores`() {
        assertEquals(25, WordleComplexity.compute(3, 6, 120, "fr").difficultyScore0To100)
        assertEquals(43, WordleComplexity.compute(5, 6, 180, "fr").difficultyScore0To100)
        assertEquals(48, WordleComplexity.compute(6, 6, 240, "fr").difficultyScore0To100)
        assertEquals(53, WordleComplexity.compute(7, 6, 300, "fr").difficultyScore0To100)
    }

    @Test
    fun `seeded EN exercises match expected scores`() {
        assertEquals(24, WordleComplexity.compute(3, 6, 120, "en").difficultyScore0To100)
        assertEquals(42, WordleComplexity.compute(5, 6, 180, "en").difficultyScore0To100)
        assertEquals(47, WordleComplexity.compute(6, 6, 240, "en").difficultyScore0To100)
        assertEquals(52, WordleComplexity.compute(7, 6, 300, "en").difficultyScore0To100)
    }
}
