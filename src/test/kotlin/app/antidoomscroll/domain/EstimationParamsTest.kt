package app.antidoomscroll.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.math.abs
import kotlin.math.ln

class EstimationParamsTest {

    // -------------------------------------------------------------------------
    // EstimationParams constructor validation
    // -------------------------------------------------------------------------

    @Test
    fun `constructor accepts valid params`() {
        val p = EstimationParams(
            correctAnswer = 330.0,
            unit = "m",
            toleranceFactor = 1.5,
            category = "geography"
        )
        assertEquals(330.0, p.correctAnswer)
        assertEquals("m", p.unit)
        assertEquals(1.5, p.toleranceFactor)
        assertEquals("geography", p.category)
    }

    @Test
    fun `constructor rejects non-positive correctAnswer`() {
        assertThrows<IllegalArgumentException> {
            EstimationParams(correctAnswer = 0.0, unit = "m", toleranceFactor = 1.5, category = "geography")
        }
        assertThrows<IllegalArgumentException> {
            EstimationParams(correctAnswer = -100.0, unit = "m", toleranceFactor = 1.5, category = "geography")
        }
    }

    @Test
    fun `constructor rejects toleranceFactor not greater than 1`() {
        assertThrows<IllegalArgumentException> {
            EstimationParams(correctAnswer = 100.0, unit = "m", toleranceFactor = 1.0, category = "geography")
        }
        assertThrows<IllegalArgumentException> {
            EstimationParams(correctAnswer = 100.0, unit = "m", toleranceFactor = 0.5, category = "geography")
        }
    }

    @Test
    fun `constructor rejects unknown category`() {
        assertThrows<IllegalArgumentException> {
            EstimationParams(correctAnswer = 100.0, unit = "m", toleranceFactor = 1.5, category = "sports")
        }
    }

    @Test
    fun `constructor accepts all valid categories`() {
        for (cat in listOf("math", "geography", "science", "history")) {
            val p = EstimationParams(correctAnswer = 1.0, unit = "x", toleranceFactor = 2.0, category = cat)
            assertEquals(cat, p.category)
        }
    }

    // -------------------------------------------------------------------------
    // Exercise.estimationParams() parsing from exerciseParams map
    // -------------------------------------------------------------------------

    private fun makeExercise(params: Map<String, Any?>): Exercise = Exercise(
        id = UUID.randomUUID(),
        subjectId = UUID.randomUUID(),
        type = ExerciseType.ESTIMATION,
        difficulty = Difficulty.EASY,
        prompt = "Test prompt",
        expectedAnswers = emptyList(),
        timeLimitSeconds = 30,
        exerciseParams = params
    )

    @Test
    fun `estimationParams parses all fields correctly`() {
        val ex = makeExercise(
            mapOf(
                "correctAnswer" to 8849.0,
                "unit" to "m",
                "toleranceFactor" to 1.3,
                "category" to "geography",
                "hint" to "Highest peak on Earth."
            )
        )
        val p = ex.estimationParams()!!
        assertEquals(8849.0, p.correctAnswer)
        assertEquals("m", p.unit)
        assertEquals(1.3, p.toleranceFactor)
        assertEquals("geography", p.category)
        assertEquals("Highest peak on Earth.", p.hint)
    }

    @Test
    fun `estimationParams parses int correctAnswer as double`() {
        val ex = makeExercise(
            mapOf(
                "correctAnswer" to 365,   // Int, not Double
                "unit" to "days",
                "toleranceFactor" to 1.03,
                "category" to "math"
            )
        )
        val p = ex.estimationParams()!!
        assertEquals(365.0, p.correctAnswer)
    }

    @Test
    fun `estimationParams returns null when correctAnswer missing`() {
        val ex = makeExercise(mapOf("unit" to "m", "toleranceFactor" to 1.5, "category" to "geography"))
        assertNull(ex.estimationParams())
    }

    @Test
    fun `estimationParams returns null when unit missing`() {
        val ex = makeExercise(mapOf("correctAnswer" to 100.0, "toleranceFactor" to 1.5, "category" to "geography"))
        assertNull(ex.estimationParams())
    }

    @Test
    fun `estimationParams returns null for non-ESTIMATION exercise type`() {
        val ex = Exercise(
            id = UUID.randomUUID(),
            subjectId = UUID.randomUUID(),
            type = ExerciseType.FLASHCARD_QA,
            difficulty = Difficulty.EASY,
            prompt = "p",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 30,
            exerciseParams = mapOf("correctAnswer" to 100.0, "unit" to "m", "toleranceFactor" to 1.5, "category" to "geography")
        )
        assertNull(ex.estimationParams())
    }

    @Test
    fun `estimationParams returns null when exerciseParams is null`() {
        val ex = Exercise(
            id = UUID.randomUUID(),
            subjectId = UUID.randomUUID(),
            type = ExerciseType.ESTIMATION,
            difficulty = Difficulty.EASY,
            prompt = "p",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 30,
            exerciseParams = null
        )
        assertNull(ex.estimationParams())
    }

    @Test
    fun `estimationParams hint defaults to null when absent`() {
        val ex = makeExercise(
            mapOf("correctAnswer" to 365.0, "unit" to "days", "toleranceFactor" to 1.03, "category" to "math")
        )
        val p = ex.estimationParams()!!
        assertNull(p.hint)
    }

    // -------------------------------------------------------------------------
    // Logarithmic scoring formula validation (client-side formula, unit-tested here)
    // -------------------------------------------------------------------------

    /**
     * Reference implementation of the logarithmic scoring formula.
     * Frontend applies the same formula.
     */
    private fun logScore(userAnswer: Double, correctAnswer: Double, toleranceFactor: Double): Double {
        if (userAnswer <= 0) return 0.0
        val ratio = if (userAnswer > correctAnswer) userAnswer / correctAnswer else correctAnswer / userAnswer
        val logError = ln(ratio) / ln(toleranceFactor)
        return maxOf(0.0, 1.0 - logError)
    }

    @Test
    fun `logScore returns 1 for exact answer`() {
        assertEquals(1.0, logScore(330.0, 330.0, 1.5), 1e-9)
    }

    @Test
    fun `logScore returns 0 when off by exactly the tolerance factor`() {
        val score = logScore(330.0 * 1.5, 330.0, 1.5)
        assertEquals(0.0, score, 1e-9)
    }

    @Test
    fun `logScore returns 0 when off below by exactly the tolerance factor`() {
        val score = logScore(330.0 / 1.5, 330.0, 1.5)
        assertEquals(0.0, score, 1e-9)
    }

    @Test
    fun `logScore is symmetric - same score for over and under by same ratio`() {
        val over = logScore(1000.0 * 2.0, 1000.0, 5.0)
        val under = logScore(1000.0 / 2.0, 1000.0, 5.0)
        assertEquals(over, under, 1e-9)
    }

    @Test
    fun `logScore with tight tolerance 1_1 scores near-zero for 10pct off`() {
        val score = logScore(391.0 * 1.1, 391.0, 1.1)
        assertEquals(0.0, score, 1e-9)
    }

    @Test
    fun `logScore returns between 0 and 1 for answer within tolerance`() {
        val score = logScore(340.0, 330.0, 1.5) // slightly over
        assertTrue(score > 0.0 && score < 1.0, "Expected score in (0,1), got $score")
    }

    @Test
    fun `logScore returns 0 for negative or zero user answer`() {
        assertEquals(0.0, logScore(0.0, 330.0, 1.5))
        assertEquals(0.0, logScore(-10.0, 330.0, 1.5))
    }

    private fun assertTrue(condition: Boolean, message: String) {
        if (!condition) throw AssertionError(message)
    }
}
