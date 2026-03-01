package app.antidoomscroll.application

import app.antidoomscroll.domain.Difficulty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EstimationExerciseGeneratorTest {

    private val parametricConfig = EstimationParametricConfig()
    private val generator = EstimationExerciseGenerator(
        EstimationCuratedItems(parametricConfig),
        parametricConfig
    )

    @Test
    fun `same seed produces same exercise`() {
        val a = generator.generate(levelIndex = 0, difficulty = Difficulty.ULTRA_EASY, seed = 42L)
        val b = generator.generate(levelIndex = 0, difficulty = Difficulty.ULTRA_EASY, seed = 42L)
        assertEquals(a.prompt, b.prompt)
        assertEquals(a.correctAnswer, b.correctAnswer)
        assertEquals(a.unit, b.unit)
        assertEquals(a.timeWeightHigher, b.timeWeightHigher)
    }

    @Test
    fun `different seed can produce different exercise`() {
        val a = generator.generate(levelIndex = 0, difficulty = Difficulty.ULTRA_EASY, seed = 1L)
        val b = generator.generate(levelIndex = 0, difficulty = Difficulty.ULTRA_EASY, seed = 2L)
        assertTrue(a.prompt != b.prompt || a.correctAnswer != b.correctAnswer)
    }

    @Test
    fun `complexity band for score 1-25 is elementary`() {
        val (min, max) = generator.complexityBandForScore(15)
        assertEquals(5.0, min)
        assertEquals(15.0, max)
    }

    @Test
    fun `complexity band for score 26-50 is intermediate`() {
        val (min, max) = generator.complexityBandForScore(40)
        assertEquals(15.0, min)
        assertEquals(30.0, max)
    }

    @Test
    fun `difficulty score from level increases with level`() {
        assertEquals(10, generator.difficultyScoreFromLevel(0))
        assertTrue(generator.difficultyScoreFromLevel(10) > generator.difficultyScoreFromLevel(0))
        assertEquals(100, generator.difficultyScoreFromLevel(30))
    }

    @Test
    fun `generated exercise has valid params`() {
        val g = generator.generate(levelIndex = 5, difficulty = Difficulty.EASY, seed = 123L)
        assertTrue(g.prompt.isNotBlank())
        assertTrue(g.correctAnswer > 0)
        assertTrue(g.toleranceFactor >= 1.05)
        assertTrue(g.category in listOf("math", "geography", "science", "history"))
    }

    @Test
    fun `pure math has timeWeightHigher true`() {
        for (seed in 0L..20L) {
            val g = generator.generate(levelIndex = 0, difficulty = Difficulty.ULTRA_EASY, seed = seed)
            if (g.prompt.contains("÷") || g.prompt.contains("×")) {
                assertTrue(g.timeWeightHigher) { "Pure math prompt should have timeWeightHigher: ${g.prompt}" }
            }
        }
    }

    @Test
    fun `speed-time problem has correct formula`() {
        var found = false
        for (seed in 0L..80L) {
            val g = generator.generate(levelIndex = 0, difficulty = Difficulty.ULTRA_EASY, seed = seed)
            if (g.prompt.contains("km/h") && (g.unit == "hours" || g.unit == "minutes")) {
                found = true
                assertTrue(g.correctAnswer > 0)
                assertTrue(!g.timeWeightHigher)
                break
            }
        }
        assertTrue(found) { "Should generate speed/time problem in 80 seeds" }
    }

    @Test
    fun `conversion h-min problem has correct formula`() {
        var found = false
        for (seed in 0L..150L) {
            val g = generator.generate(levelIndex = 0, difficulty = Difficulty.ULTRA_EASY, seed = seed)
            if (g.prompt.contains("minutes") && g.prompt.contains("hours") && g.unit == "minutes" && !g.prompt.contains("km/h")) {
                found = true
                assertTrue(g.correctAnswer > 0 && g.correctAnswer <= 180)
                assertTrue(!g.timeWeightHigher)
                break
            }
        }
        assertTrue(found) { "Should generate conversion h/min problem in 150 seeds" }
    }

    @Test
    fun `budget-days problem has correct formula and only from level 10`() {
        for (seed in 0L..80L) {
            val g = generator.generate(levelIndex = 0, difficulty = Difficulty.ULTRA_EASY, seed = seed)
            assertTrue(!g.prompt.contains("Budget")) { "Budget should not appear at level 0; got: ${g.prompt}" }
        }
        var found = false
        for (seed in 0L..80L) {
            val g = generator.generate(levelIndex = 10, difficulty = Difficulty.MEDIUM, seed = seed)
            if (g.prompt.contains("Budget") && (g.unit == "days" || g.unit == "months")) {
                found = true
                assertTrue(g.correctAnswer > 0)
                assertTrue(!g.timeWeightHigher)
                break
            }
        }
        assertTrue(found) { "Should generate budget/days problem at level 10 in 80 seeds" }
    }

    @Test
    fun `first level has greater tolerance factor`() {
        var firstLevelTol = 0.0
        var laterLevelTol = 0.0
        var firstCount = 0
        var laterCount = 0
        for (seed in 0L..30L) {
            val g0 = generator.generate(levelIndex = 0, difficulty = Difficulty.ULTRA_EASY, seed = seed)
            firstLevelTol += g0.toleranceFactor
            firstCount++
            val g10 = generator.generate(levelIndex = 10, difficulty = Difficulty.EASY, seed = seed)
            laterLevelTol += g10.toleranceFactor
            laterCount++
        }
        val avgFirst = firstLevelTol / firstCount
        val avgLater = laterLevelTol / laterCount
        assertTrue(avgFirst > avgLater) { "First level avg tol ($avgFirst) should be > later ($avgLater)" }
    }

    @Test
    fun `generatePool returns at least requested count and may include curated`() {
        val pool = generator.generatePool(levelIndex = 0, difficulty = Difficulty.ULTRA_EASY, baseSeed = 1L, count = 5)
        assertTrue(pool.size >= 5) { "Pool should have at least 5 parametric; got ${pool.size}" }
    }
}
