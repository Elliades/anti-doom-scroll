package app.antidoomscroll.application

import app.antidoomscroll.domain.AnagramParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

class AnagramGeneratorTest {

    private val generator = AnagramGenerator()

    @Test
    fun `generate returns valid anagram for ULTRA_EASY 3 letters`() {
        val params = AnagramParams(minLetters = 3, maxLetters = 3, language = "fr")
        repeat(10) {
            val result = generator.generate(params, Random(it))
            assertNotNull(result) { "Should always find words for 3 letters" }
            assertNotNull(result!!.answer)
            assertTrue(result.answer.length == 3) { "Answer length: ${result.answer.length}" }
            assertEquals(result.answer.length, result.scrambledLetters.size) { "Scrambled must match answer length" }
        }
    }

    @Test
    fun `generate returns valid anagram for HARD 6-7 letters`() {
        val params = AnagramParams(minLetters = 6, maxLetters = 7, language = "fr")
        repeat(5) {
            val result = generator.generate(params, Random(it))
            assertNotNull(result) { "Should find words for 6-7 letters (or fallback to >=6)" }
            assertNotNull(result!!.answer)
            assertTrue(result.answer.length >= 6) { "Answer length: ${result.answer.length}" }
            assertEquals(result.answer.length, result.scrambledLetters.size) { "Scrambled must match answer length" }
        }
    }

    @Test
    fun `generate returns valid anagram for VERY_HARD 8-15 letters`() {
        val params = AnagramParams(minLetters = 8, maxLetters = 15, language = "fr")
        repeat(5) {
            val result = generator.generate(params, Random(it))
            assertNotNull(result) { "Should find words for 8+ letters (or fallback to >=8)" }
            assertNotNull(result!!.answer)
            assertTrue(result.answer.length >= 8) { "Answer length: ${result.answer.length}" }
            assertEquals(result.answer.length, result.scrambledLetters.size) { "Scrambled must match answer length" }
        }
    }

    @Test
    fun `generate returns different words with different random seeds`() {
        val params = AnagramParams(minLetters = 3, maxLetters = 3, language = "fr")
        val results = (0..19).map { generator.generate(params, Random(it))!!.answer }.toSet()
        assertTrue(results.size > 1) { "Different seeds should produce different words, got ${results.size} unique" }
    }
}
