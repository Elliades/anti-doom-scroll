package app.antidoomscroll.web

import app.antidoomscroll.application.AnagramGenerator
import app.antidoomscroll.application.ImagePairDeckCache
import app.antidoomscroll.application.ImagePairGenerator
import app.antidoomscroll.application.MathChainGenerator
import app.antidoomscroll.application.MathFlashcardGenerator
import app.antidoomscroll.application.RememberNumberGenerator
import app.antidoomscroll.application.MemoryCardDeckCache
import app.antidoomscroll.application.SumPairGenerator
import app.antidoomscroll.application.SumPairRoundsCache
import app.antidoomscroll.application.WordleGenerator
import app.antidoomscroll.application.port.ExercisePort
import app.antidoomscroll.domain.Difficulty
import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.ExerciseType
import app.antidoomscroll.domain.WordleComplexity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.util.UUID

/**
 * Ensures N_BACK exercises always get nBackParams in the DTO (fixes "missing sequence" in app).
 * Also covers ESTIMATION type: estimationParams populated from exerciseParams.
 */
@ExtendWith(MockitoExtension::class)
class ExerciseDtoMapperTest {

    @Mock
    private lateinit var exercisePort: ExercisePort

    private val sumPairRoundsCache = SumPairRoundsCache(SumPairGenerator())
    private val memoryCardDeckCache = MemoryCardDeckCache()
    private val imagePairDeckCache = ImagePairDeckCache(ImagePairGenerator())
    private val mathFlashcardGenerator = MathFlashcardGenerator()
    private val mathChainGenerator = MathChainGenerator()
    private val rememberNumberGenerator = RememberNumberGenerator(mathFlashcardGenerator)
    private val anagramGenerator = AnagramGenerator()
    private val wordleGenerator = WordleGenerator()

    private val nBackId = UUID.fromString("c0000000-0000-0000-0000-000000000001")
    private val subjectId = UUID.fromString("b0000000-0000-0000-0000-000000000004")
    private val estimSubjectId = UUID.fromString("b0000000-0000-0000-0000-000000000013")

    private fun mapper() = ExerciseDtoMapper(
        exercisePort, sumPairRoundsCache, memoryCardDeckCache,
        imagePairDeckCache, mathFlashcardGenerator, mathChainGenerator, rememberNumberGenerator, anagramGenerator, wordleGenerator
    )

    @Test
    fun `when N_BACK exercise has null exerciseParams then findExerciseParamsById is used and DTO has nBackParams`() {
        val exerciseWithoutParams = Exercise(
            id = nBackId,
            subjectId = subjectId,
            type = ExerciseType.N_BACK,
            difficulty = Difficulty.ULTRA_EASY,
            prompt = "1-Back",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 30,
            exerciseParams = null
        )
        val paramsFromDb = mapOf(
            "n" to 1,
            "sequence" to listOf("A", "B", "A", "C"),
            "matchIndices" to listOf(2, 3)
        )
        `when`(exercisePort.findExerciseParamsById(nBackId)).thenReturn(paramsFromDb)

        val dto = mapper().toExerciseDto(exerciseWithoutParams, "B1")

        assertEquals("N_BACK", dto.type)
        assertNotNull(dto.nBackParams) { "N_BACK DTO must have nBackParams (app would show 'missing sequence')" }
        assertEquals(1, dto.nBackParams!!.n)
        assertEquals(listOf("A", "B", "A", "C"), dto.nBackParams!!.sequence)
        assertEquals(listOf(2, 3), dto.nBackParams!!.matchIndices)
    }

    @Test
    fun `when N_BACK exercise has parametric params n and suitCount then DTO has generated sequence`() {
        val params = mapOf("n" to 1, "suitCount" to 1)
        `when`(exercisePort.findExerciseParamsById(nBackId)).thenReturn(params)
        val exerciseParametric = Exercise(
            id = nBackId,
            subjectId = subjectId,
            type = ExerciseType.N_BACK,
            difficulty = Difficulty.ULTRA_EASY,
            prompt = "1-Back",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 30,
            exerciseParams = null
        )
        val dto = mapper().toExerciseDto(exerciseParametric, "B1")
        assertNotNull(dto.nBackParams) { "Parametric N_BACK must produce nBackParams" }
        assertEquals(1, dto.nBackParams!!.n)
        assertTrue(dto.nBackParams!!.sequence.size >= 3)
        assertTrue(dto.nBackParams!!.sequence.all { it.endsWith("C") }) { "suitCount=1: clubs only: ${dto.nBackParams!!.sequence}" }
        assertTrue(dto.nBackParams!!.matchIndices.all { it >= 1 && it < dto.nBackParams!!.sequence.size })
        assertTrue(dto.nBackParams!!.matchIndices.all { dto.nBackParams!!.sequence[it] == dto.nBackParams!!.sequence[it - 1] })
    }

    @Test
    fun `when N_BACK exercise has exerciseParams then DTO has nBackParams without calling port`() {
        val params = mapOf(
            "n" to 1,
            "sequence" to listOf("X", "Y", "X"),
            "matchIndices" to listOf(2)
        )
        val exerciseWithParams = Exercise(
            id = nBackId,
            subjectId = subjectId,
            type = ExerciseType.N_BACK,
            difficulty = Difficulty.ULTRA_EASY,
            prompt = "1-Back",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 30,
            exerciseParams = params
        )

        val dto = mapper().toExerciseDto(exerciseWithParams, "B1")

        assertNotNull(dto.nBackParams)
        assertEquals(1, dto.nBackParams!!.n)
        assertEquals(listOf("X", "Y", "X"), dto.nBackParams!!.sequence)
    }

    @Test
    fun `when MEMORY_CARD_PAIRS exercise has exerciseParams then DTO has memoryCardParams`() {
        val params = mapOf(
            "pairCount" to 4,
            "symbols" to listOf("🍎", "🍊", "🍋", "🍇")
        )
        val exercise = Exercise(
            id = nBackId,
            subjectId = subjectId,
            type = ExerciseType.MEMORY_CARD_PAIRS,
            difficulty = Difficulty.EASY,
            prompt = "Find the pairs.",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 120,
            exerciseParams = params
        )

        val dto = mapper().toExerciseDto(exercise, "MEMORY")

        assertEquals("MEMORY_CARD_PAIRS", dto.type)
        assertNotNull(dto.memoryCardParams)
        assertEquals(4, dto.memoryCardParams!!.pairCount)
        assertEquals(listOf("🍎", "🍊", "🍋", "🍇"), dto.memoryCardParams!!.symbols)
        assertNotNull(dto.memoryCardParams!!.shuffledDeck)
        assertEquals(8, dto.memoryCardParams!!.shuffledDeck!!.size)
    }

    @Test
    fun `when SUM_PAIR exercise has exerciseParams then DTO has sumPairParams, sumPairGroups and sumPairDeck`() {
        val params = mapOf(
            "staticNumbers" to listOf(5, 10),
            "pairsPerRound" to 3,
            "minValue" to 1,
            "maxValue" to 99
        )
        val exercise = Exercise(
            id = nBackId,
            subjectId = subjectId,
            type = ExerciseType.SUM_PAIR,
            difficulty = Difficulty.MEDIUM,
            prompt = "Find pairs where first + static = second.",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 120,
            exerciseParams = params
        )
        val dto = mapper().toExerciseDto(exercise, "MEMORY")

        assertEquals("SUM_PAIR", dto.type)
        assertNotNull(dto.sumPairParams)
        assertEquals(listOf(5, 10), dto.sumPairParams!!.staticNumbers)
        assertEquals(3, dto.sumPairParams!!.pairsPerRound)
        assertNotNull(dto.sumPairGroups)
        assertEquals(2, dto.sumPairGroups!!.size)
        assertEquals(5, dto.sumPairGroups!![0].static)
        assertEquals(10, dto.sumPairGroups!![1].static)
        dto.sumPairGroups!!.forEach { g ->
            assertEquals(6, g.cards.size)
            assertNotNull(g.color)
        }
        assertNotNull(dto.sumPairDeck)
        assertEquals(12, dto.sumPairDeck!!.size)
    }

    @Test
    fun `when IMAGE_PAIR exercise has exerciseParams then DTO has imagePairParams and imagePairDeck`() {
        val params = mapOf(
            "pairCount" to 4,
            "maxPairsPerBackground" to 2,
            "colorCount" to 1
        )
        val exercise = Exercise(
            id = UUID.fromString("f0000000-0000-0000-0000-000000000001"),
            subjectId = subjectId,
            type = ExerciseType.IMAGE_PAIR,
            difficulty = Difficulty.EASY,
            prompt = "Find pairs: same background and same image.",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 120,
            exerciseParams = params
        )
        val dto = mapper().toExerciseDto(exercise, "MEMORY")
        assertEquals("IMAGE_PAIR", dto.type)
        assertNotNull(dto.imagePairParams)
        assertEquals(4, dto.imagePairParams!!.pairCount)
        assertEquals(2, dto.imagePairParams!!.maxPairsPerBackground)
        assertEquals(1, dto.imagePairParams!!.colorCount)
        assertNotNull(dto.imagePairDeck)
        assertEquals(8, dto.imagePairDeck!!.size)
        dto.imagePairDeck!!.forEach { c ->
            assertTrue(c.imageId.isNotBlank())
            assertTrue(c.backgroundId in 0..1)
        }
    }

    @Test
    fun `when FLASHCARD_QA has math params then DTO has generated prompt and expectedAnswers`() {
        val params = mapOf(
            "operation" to "ADD",
            "firstMax" to 9,
            "secondMax" to 9
        )
        val exercise = Exercise(
            id = nBackId,
            subjectId = subjectId,
            type = ExerciseType.FLASHCARD_QA,
            difficulty = Difficulty.ULTRA_EASY,
            prompt = "Placeholder",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 30,
            exerciseParams = params
        )

        val dto = mapper().toExerciseDto(exercise, "default")

        assertEquals("FLASHCARD_QA", dto.type)
        assertNotNull(dto.prompt)
        assertNotNull(dto.expectedAnswers)
        assertNotNull(dto.mathComplexityScore) { "Math flashcard DTO should include complexity score" }
        assertTrue(dto.mathComplexityScore!! >= 0.0)
        assertEquals(1, dto.expectedAnswers.size)
        assert(dto.prompt.startsWith("What is ")) { "Generated prompt should start with 'What is '" }
        assert(dto.prompt.contains(" + ")) { "Addition prompt should contain ' + '" }
        assert(dto.prompt.endsWith("?")) { "Generated prompt should end with '?'" }
    }

    @Test
    fun `when FLASHCARD_QA has MULTIPLY with firstValues then DTO has mathOperation MULTIPLY and prompt with times`() {
        val params = mapOf(
            "operation" to "MULTIPLY",
            "firstMin" to 1,
            "firstMax" to 10,
            "firstValues" to listOf(2, 5, 10),
            "secondMin" to 1,
            "secondMax" to 9
        )
        val exercise = Exercise(
            id = nBackId,
            subjectId = subjectId,
            type = ExerciseType.FLASHCARD_QA,
            difficulty = Difficulty.ULTRA_EASY,
            prompt = "Solve the multiplication.",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 30,
            exerciseParams = params
        )
        val dto = mapper().toExerciseDto(exercise, "default")
        assertEquals("FLASHCARD_QA", dto.type)
        assertEquals("MULTIPLY", dto.mathOperation)
        assertNotNull(dto.prompt)
        assert(dto.prompt.contains(" × ")) { "Multiply prompt should contain ×" }
        assertEquals(1, dto.expectedAnswers.size)
    }

    @Test
    fun `when FLASHCARD_QA has DIVIDE with secondValues then DTO has mathOperation DIVIDE and prompt with divide`() {
        val params = mapOf(
            "operation" to "DIVIDE",
            "firstMin" to 1,
            "firstMax" to 9,
            "secondMax" to 10,
            "secondValues" to listOf(2, 5, 10)
        )
        val exercise = Exercise(
            id = nBackId,
            subjectId = subjectId,
            type = ExerciseType.FLASHCARD_QA,
            difficulty = Difficulty.ULTRA_EASY,
            prompt = "Solve the division.",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 30,
            exerciseParams = params
        )
        val dto = mapper().toExerciseDto(exercise, "default")
        assertEquals("FLASHCARD_QA", dto.type)
        assertEquals("DIVIDE", dto.mathOperation)
        assertNotNull(dto.prompt)
        assert(dto.prompt.contains(" ÷ ")) { "Divide prompt should contain ÷" }
        assertEquals(1, dto.expectedAnswers.size)
    }

    @Test
    fun `when N_BACK_GRID exercise has exerciseParams then DTO has nBackGridParams`() {
        val params = mapOf(
            "n" to 1,
            "sequence" to listOf(0, 4, 2, 4, 8),
            "matchIndices" to listOf(3),
            "gridSize" to 3
        )
        val exercise = Exercise(
            id = nBackId,
            subjectId = subjectId,
            type = ExerciseType.N_BACK_GRID,
            difficulty = Difficulty.ULTRA_EASY,
            prompt = "Grid 1-Back",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 45,
            exerciseParams = params
        )
        val dto = mapper().toExerciseDto(exercise, "B1")
        assertEquals("N_BACK_GRID", dto.type)
        assertNotNull(dto.nBackGridParams)
        assertEquals(1, dto.nBackGridParams!!.n)
        assertEquals(listOf(0, 4, 2, 4, 8), dto.nBackGridParams!!.sequence)
        assertEquals(listOf(3), dto.nBackGridParams!!.matchIndices)
        assertEquals(3, dto.nBackGridParams!!.gridSize)
    }

    @Test
    fun `when DUAL_NBACK_GRID exercise has exerciseParams then DTO has dualNBackGridParams`() {
        val params = mapOf(
            "n" to 1,
            "sequence" to listOf(
                mapOf("position" to 0, "color" to "#4285F4"),
                mapOf("position" to 4, "color" to "#EA4335"),
                mapOf("position" to 0, "color" to "#FBBC04")
            ),
            "matchPositionIndices" to listOf(2),
            "matchColorIndices" to emptyList<Int>(),
            "colors" to listOf("#4285F4", "#EA4335", "#FBBC04", "#34A853"),
            "gridSize" to 3
        )
        val exercise = Exercise(
            id = nBackId,
            subjectId = subjectId,
            type = ExerciseType.DUAL_NBACK_GRID,
            difficulty = Difficulty.ULTRA_EASY,
            prompt = "Dual Grid 1-Back",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 60,
            exerciseParams = params
        )
        val dto = mapper().toExerciseDto(exercise, "B1")
        assertEquals("DUAL_NBACK_GRID", dto.type)
        assertNotNull(dto.dualNBackGridParams)
        assertEquals(1, dto.dualNBackGridParams!!.n)
        assertEquals(3, dto.dualNBackGridParams!!.sequence.size)
        assertEquals(listOf(2), dto.dualNBackGridParams!!.matchPositionIndices)
        assertEquals(4, dto.dualNBackGridParams!!.colors.size)
    }

    @Test
    fun `when DUAL_NBACK_CARD exercise has exerciseParams then DTO has dualNBackCardParams`() {
        val params = mapOf(
            "n" to 1,
            "sequence" to listOf("AC", "2D", "2C", "3H"),
            "matchColorIndices" to emptyList<Int>(),
            "matchNumberIndices" to listOf(2)
        )
        val exercise = Exercise(
            id = nBackId,
            subjectId = subjectId,
            type = ExerciseType.DUAL_NBACK_CARD,
            difficulty = Difficulty.ULTRA_EASY,
            prompt = "Dual Card 1-Back",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 60,
            exerciseParams = params
        )
        val dto = mapper().toExerciseDto(exercise, "B1")
        assertEquals("DUAL_NBACK_CARD", dto.type)
        assertNotNull(dto.dualNBackCardParams)
        assertEquals(1, dto.dualNBackCardParams!!.n)
        assertEquals(listOf("AC", "2D", "2C", "3H"), dto.dualNBackCardParams!!.sequence)
        assertEquals(listOf(2), dto.dualNBackCardParams!!.matchNumberIndices)
    }

    @Test
    fun `when ESTIMATION exercise has full params then DTO has estimationParams with all fields`() {
        val params = mapOf(
            "correctAnswer" to 330.0,
            "unit" to "m",
            "toleranceFactor" to 1.5,
            "category" to "geography",
            "hint" to "Built in 1889."
        )
        val exercise = Exercise(
            id = UUID.fromString("a0000000-0000-0000-0000-000000000301"),
            subjectId = estimSubjectId,
            type = ExerciseType.ESTIMATION,
            difficulty = Difficulty.ULTRA_EASY,
            prompt = "How tall is the Eiffel Tower (in meters)?",
            expectedAnswers = listOf("330"),
            timeLimitSeconds = 25,
            exerciseParams = params
        )
        val dto = mapper().toExerciseDto(exercise, "ESTIMATION")

        assertEquals("ESTIMATION", dto.type)
        assertNotNull(dto.estimationParams)
        assertEquals(330.0, dto.estimationParams!!.correctAnswer)
        assertEquals("m", dto.estimationParams!!.unit)
        assertEquals(1.5, dto.estimationParams!!.toleranceFactor)
        assertEquals("geography", dto.estimationParams!!.category)
        assertEquals("Built in 1889.", dto.estimationParams!!.hint)
        assertEquals(false, dto.estimationParams!!.timeWeightHigher)
    }

    @Test
    fun `when ESTIMATION exercise has timeWeightHigher then DTO has timeWeightHigher true`() {
        val params = mapOf(
            "correctAnswer" to 124.0,
            "unit" to "",
            "toleranceFactor" to 1.15,
            "category" to "math",
            "timeWeightHigher" to true
        )
        val exercise = Exercise(
            id = UUID.fromString("a0000000-0000-0000-0000-000000000321"),
            subjectId = estimSubjectId,
            type = ExerciseType.ESTIMATION,
            difficulty = Difficulty.ULTRA_EASY,
            prompt = "Estimate: 372 ÷ 3 = ?",
            expectedAnswers = listOf("124"),
            timeLimitSeconds = 20,
            exerciseParams = params
        )
        val dto = mapper().toExerciseDto(exercise, "ESTIMATION")
        assertNotNull(dto.estimationParams)
        assertEquals(true, dto.estimationParams!!.timeWeightHigher)
    }

    @Test
    fun `when ESTIMATION exercise has no hint then estimationParams hint is null`() {
        val params = mapOf(
            "correctAnswer" to 365.0,
            "unit" to "days",
            "toleranceFactor" to 1.03,
            "category" to "math"
        )
        val exercise = Exercise(
            id = UUID.fromString("a0000000-0000-0000-0000-000000000300"),
            subjectId = estimSubjectId,
            type = ExerciseType.ESTIMATION,
            difficulty = Difficulty.ULTRA_EASY,
            prompt = "How many days are in a year?",
            expectedAnswers = listOf("365"),
            timeLimitSeconds = 20,
            exerciseParams = params
        )
        val dto = mapper().toExerciseDto(exercise, "ESTIMATION")

        assertEquals("ESTIMATION", dto.type)
        assertNotNull(dto.estimationParams)
        assertEquals(365.0, dto.estimationParams!!.correctAnswer)
        assertEquals("days", dto.estimationParams!!.unit)
        assertNull(dto.estimationParams!!.hint)
    }

    @Test
    fun `when ESTIMATION exercise has missing required param then estimationParams is null`() {
        val params = mapOf(
            "unit" to "m",
            "toleranceFactor" to 1.5,
            "category" to "geography"
            // correctAnswer is missing
        )
        val exercise = Exercise(
            id = UUID.fromString("a0000000-0000-0000-0000-000000000399"),
            subjectId = estimSubjectId,
            type = ExerciseType.ESTIMATION,
            difficulty = Difficulty.EASY,
            prompt = "Missing answer?",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 30,
            exerciseParams = params
        )
        val dto = mapper().toExerciseDto(exercise, "ESTIMATION")

        assertEquals("ESTIMATION", dto.type)
        assertNull(dto.estimationParams)
    }

    @Test
    fun `when ESTIMATION exercise has math category then DTO type is ESTIMATION and no other params are set`() {
        val params = mapOf(
            "correctAnswer" to 391.0,
            "unit" to "",
            "toleranceFactor" to 1.1,
            "category" to "math",
            "hint" to "Decompose: 17×20 + 17×3"
        )
        val exercise = Exercise(
            id = UUID.fromString("a0000000-0000-0000-0000-000000000308"),
            subjectId = estimSubjectId,
            type = ExerciseType.ESTIMATION,
            difficulty = Difficulty.EASY,
            prompt = "Estimate: 17 × 23 = ?",
            expectedAnswers = listOf("391"),
            timeLimitSeconds = 20,
            exerciseParams = params
        )
        val dto = mapper().toExerciseDto(exercise, "ESTIMATION")

        assertEquals("ESTIMATION", dto.type)
        assertNotNull(dto.estimationParams)
        assertEquals(391.0, dto.estimationParams!!.correctAnswer)
        assertEquals("math", dto.estimationParams!!.category)
        assertNull(dto.nBackParams)
        assertNull(dto.memoryCardParams)
        assertNull(dto.sumPairParams)
        assertNull(dto.anagramParams)
        assertNull(dto.wordleParams)
    }

    @Test
    fun `when WORDLE exercise then DTO has wordleParams and wordleComplexity`() {
        val exercise = Exercise(
            id = UUID.fromString("a0000000-0000-0000-0000-000000000201"),
            subjectId = UUID.fromString("b0000000-0000-0000-0000-000000000011"),
            type = ExerciseType.WORDLE,
            difficulty = Difficulty.MEDIUM,
            prompt = "Devinez le mot de 5 lettres en 6 essais.",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 180,
            exerciseParams = mapOf("language" to "fr", "maxAttempts" to 6)
        )
        val dto = mapper().toExerciseDto(exercise, "WORDLE_FR")
        assertEquals("WORDLE", dto.type)
        assertNotNull(dto.wordleParams) { "Word list must be present for FR length 5" }
        assertEquals(5, dto.wordleParams!!.wordLength)
        assertEquals(6, dto.wordleParams!!.maxAttempts)
        assertNotNull(dto.wordleComplexity)
        assertEquals(43, dto.wordleComplexity!!.difficultyScore0To100)
        val expected = WordleComplexity.compute(5, 6, 180, "fr")
        assertEquals(expected.searchSpaceLog10, dto.wordleComplexity!!.searchSpaceLog10, 1e-9)
        assertEquals(expected.entropyBits, dto.wordleComplexity!!.entropyBits, 1e-9)
    }

    @Test
    fun `when REMEMBER_NUMBER exercise has params then DTO has rememberNumberParams`() {
        val params = mapOf(
            "numberDigits" to 3,
            "displayTimeMs" to 2500,
            "mathOperation" to "ADD",
            "mathFirstMax" to 99,
            "mathSecondMax" to 9
        )
        val exercise = Exercise(
            id = UUID.fromString("f3000000-0000-0000-0000-000000000002"),
            subjectId = UUID.fromString("b0000000-0000-0000-0000-000000000008"),
            type = ExerciseType.REMEMBER_NUMBER,
            difficulty = Difficulty.EASY,
            prompt = "Remember the number.",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 60,
            exerciseParams = params
        )
        val dto = mapper().toExerciseDto(exercise, "MEMORY")
        assertEquals("REMEMBER_NUMBER", dto.type)
        assertNotNull(dto.rememberNumberParams)
        val rn = dto.rememberNumberParams!!
        assertTrue(rn.numberToRemember in 100..999) { "3-digit number expected, got ${rn.numberToRemember}" }
        assertEquals(2500, rn.displayTimeMs)
        assertTrue(rn.mathPrompt.startsWith("What is ")) { "Math prompt format: ${rn.mathPrompt}" }
        assertTrue(rn.mathExpectedAnswer.toIntOrNull() != null)
    }
}
