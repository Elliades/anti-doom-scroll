package app.antidoomscroll.application

import app.antidoomscroll.domain.Difficulty
import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.ExerciseType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.random.Random

class SumPairParamsResolverTest {

    private val subjectId = UUID.fromString("b0000000-0000-0000-0000-000000000008")

    @Test
    fun `resolves fixed staticNumbers from exercise_params`() {
        val exercise = Exercise(
            id = UUID.randomUUID(),
            subjectId = subjectId,
            type = ExerciseType.SUM_PAIR,
            difficulty = Difficulty.EASY,
            prompt = "Find pairs",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 120,
            exerciseParams = mapOf(
                "staticNumbers" to listOf(5, 10),
                "pairsPerRound" to 3,
                "minValue" to 1,
                "maxValue" to 99
            )
        )
        val params = SumPairParamsResolver.resolve(exercise, Random(42))
        assertNotNull(params)
        assertEquals(listOf(5, 10), params!!.staticNumbers)
        assertEquals(3, params.pairsPerRound)
        assertEquals(1, params.minValue)
        assertEquals(99, params.maxValue)
    }

    @Test
    fun `resolves random config - generates staticCount distinct values in range sorted ascending`() {
        val exercise = Exercise(
            id = UUID.randomUUID(),
            subjectId = subjectId,
            type = ExerciseType.SUM_PAIR,
            difficulty = Difficulty.MEDIUM,
            prompt = "Find pairs",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 180,
            exerciseParams = mapOf(
                "staticCount" to 3,
                "staticMin" to 2,
                "staticMax" to 10,
                "pairsPerRound" to 3,
                "minValue" to 1,
                "maxValue" to 99
            )
        )
        val params = SumPairParamsResolver.resolve(exercise, Random(123))
        assertNotNull(params)
        assertEquals(3, params!!.staticNumbers.size)
        assertTrue(params.staticNumbers.all { it in 2..10 })
        assertEquals(params.staticNumbers.toSet().size, params.staticNumbers.size) { "Statics must be distinct" }
        assertEquals(params.staticNumbers.sorted(), params.staticNumbers) { "Statics must be ordered ascending" }
    }

    @Test
    fun `different seeds produce different staticNumbers for random config`() {
        val exercise = Exercise(
            id = UUID.randomUUID(),
            subjectId = subjectId,
            type = ExerciseType.SUM_PAIR,
            difficulty = Difficulty.MEDIUM,
            prompt = "Find pairs",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 180,
            exerciseParams = mapOf(
                "staticCount" to 2,
                "staticMin" to 2,
                "staticMax" to 8,
                "pairsPerRound" to 3,
                "minValue" to 1,
                "maxValue" to 99
            )
        )
        val p1 = SumPairParamsResolver.resolve(exercise, Random(1))!!.staticNumbers
        val p2 = SumPairParamsResolver.resolve(exercise, Random(2))!!.staticNumbers
        assertTrue(p1 != p2) { "Different seeds should produce different statics" }
    }

    @Test
    fun `returns null for non SUM_PAIR type`() {
        val exercise = Exercise(
            id = UUID.randomUUID(),
            subjectId = subjectId,
            type = ExerciseType.N_BACK,
            difficulty = Difficulty.EASY,
            prompt = "N-back",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 60,
            exerciseParams = mapOf("staticCount" to 1, "staticMin" to 2, "staticMax" to 5, "pairsPerRound" to 2, "minValue" to 1, "maxValue" to 50)
        )
        assertEquals(null, SumPairParamsResolver.resolve(exercise))
    }

    @Test
    fun `resolves minDigits and maxDigits to minValue and maxValue`() {
        val exercise = Exercise(
            id = UUID.randomUUID(),
            subjectId = subjectId,
            type = ExerciseType.SUM_PAIR,
            difficulty = Difficulty.EASY,
            prompt = "Find pairs",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 120,
            exerciseParams = mapOf(
                "staticNumbers" to listOf(5),
                "pairsPerRound" to 4,
                "minDigits" to 1,
                "maxDigits" to 2
            )
        )
        val params = SumPairParamsResolver.resolve(exercise, Random(42))
        assertNotNull(params)
        assertEquals(1, params!!.minValue)   // 10^0 = 1
        assertEquals(99, params.maxValue)   // 10^2 - 1 = 99
    }

    @Test
    fun `minDigits 2 maxDigits 2 gives range 10 to 99`() {
        val exercise = Exercise(
            id = UUID.randomUUID(),
            subjectId = subjectId,
            type = ExerciseType.SUM_PAIR,
            difficulty = Difficulty.MEDIUM,
            prompt = "Find pairs",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 180,
            exerciseParams = mapOf(
                "staticNumbers" to listOf(3),
                "pairsPerRound" to 3,
                "minDigits" to 2,
                "maxDigits" to 2
            )
        )
        val params = SumPairParamsResolver.resolve(exercise, Random(0))
        assertNotNull(params)
        assertEquals(10, params!!.minValue)
        assertEquals(99, params.maxValue)
    }

    @Test
    fun `when minDigits maxDigits present explicit minValue maxValue are ignored`() {
        val exercise = Exercise(
            id = UUID.randomUUID(),
            subjectId = subjectId,
            type = ExerciseType.SUM_PAIR,
            difficulty = Difficulty.EASY,
            prompt = "Find pairs",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 120,
            exerciseParams = mapOf(
                "staticNumbers" to listOf(1),
                "pairsPerRound" to 2,
                "minDigits" to 1,
                "maxDigits" to 1,
                "minValue" to 100,
                "maxValue" to 200
            )
        )
        val params = SumPairParamsResolver.resolve(exercise, Random(0))
        assertNotNull(params)
        assertEquals(1, params!!.minValue)  // from digits
        assertEquals(9, params.maxValue)   // 10^1 - 1
    }
}
