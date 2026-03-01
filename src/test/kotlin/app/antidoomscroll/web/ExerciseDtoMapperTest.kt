package app.antidoomscroll.web

import app.antidoomscroll.application.AnagramGenerator
import app.antidoomscroll.application.ImagePairDeckCache
import app.antidoomscroll.application.ImagePairGenerator
import app.antidoomscroll.application.MathFlashcardGenerator
import app.antidoomscroll.application.MemoryCardDeckCache
import app.antidoomscroll.application.SumPairGenerator
import app.antidoomscroll.application.SumPairRoundsCache
import app.antidoomscroll.application.port.ExercisePort
import app.antidoomscroll.domain.Difficulty
import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.ExerciseType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.util.UUID

/**
 * Ensures N_BACK exercises always get nBackParams in the DTO (fixes "missing sequence" in app).
 */
@ExtendWith(MockitoExtension::class)
class ExerciseDtoMapperTest {

    @Mock
    private lateinit var exercisePort: ExercisePort

    private val sumPairRoundsCache = SumPairRoundsCache(SumPairGenerator())
    private val memoryCardDeckCache = MemoryCardDeckCache()
    private val imagePairDeckCache = ImagePairDeckCache(ImagePairGenerator())
    private val mathFlashcardGenerator = MathFlashcardGenerator()
    private val anagramGenerator = AnagramGenerator()

    private val nBackId = UUID.fromString("c0000000-0000-0000-0000-000000000001")
    private val subjectId = UUID.fromString("b0000000-0000-0000-0000-000000000004")

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

        val mapper = ExerciseDtoMapper(exercisePort, sumPairRoundsCache, memoryCardDeckCache, imagePairDeckCache, mathFlashcardGenerator, anagramGenerator)
        val dto = mapper.toExerciseDto(exerciseWithoutParams, "B1")

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
        val mapper = ExerciseDtoMapper(exercisePort, sumPairRoundsCache, memoryCardDeckCache, imagePairDeckCache, mathFlashcardGenerator, anagramGenerator)
        val dto = mapper.toExerciseDto(exerciseParametric, "B1")
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

        val mapper = ExerciseDtoMapper(exercisePort, sumPairRoundsCache, memoryCardDeckCache, imagePairDeckCache, mathFlashcardGenerator, anagramGenerator)
        val dto = mapper.toExerciseDto(exerciseWithParams, "B1")

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

        val mapper = ExerciseDtoMapper(exercisePort, sumPairRoundsCache, memoryCardDeckCache, imagePairDeckCache, mathFlashcardGenerator, anagramGenerator)
        val dto = mapper.toExerciseDto(exercise, "MEMORY")

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
        val mapper = ExerciseDtoMapper(exercisePort, sumPairRoundsCache, memoryCardDeckCache, imagePairDeckCache, mathFlashcardGenerator, anagramGenerator)

        val dto = mapper.toExerciseDto(exercise, "MEMORY")

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
        val mapper = ExerciseDtoMapper(exercisePort, sumPairRoundsCache, memoryCardDeckCache, imagePairDeckCache, mathFlashcardGenerator, anagramGenerator)
        val dto = mapper.toExerciseDto(exercise, "MEMORY")
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

        val mapper = ExerciseDtoMapper(exercisePort, sumPairRoundsCache, memoryCardDeckCache, imagePairDeckCache, mathFlashcardGenerator, anagramGenerator)
        val dto = mapper.toExerciseDto(exercise, "default")

        assertEquals("FLASHCARD_QA", dto.type)
        assertNotNull(dto.prompt)
        assertNotNull(dto.expectedAnswers)
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
        val mapper = ExerciseDtoMapper(exercisePort, sumPairRoundsCache, memoryCardDeckCache, imagePairDeckCache, mathFlashcardGenerator, anagramGenerator)
        val dto = mapper.toExerciseDto(exercise, "default")
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
        val mapper = ExerciseDtoMapper(exercisePort, sumPairRoundsCache, memoryCardDeckCache, imagePairDeckCache, mathFlashcardGenerator, anagramGenerator)
        val dto = mapper.toExerciseDto(exercise, "default")
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
        val mapper = ExerciseDtoMapper(exercisePort, sumPairRoundsCache, memoryCardDeckCache, imagePairDeckCache, mathFlashcardGenerator, anagramGenerator)
        val dto = mapper.toExerciseDto(exercise, "B1")
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
        val mapper = ExerciseDtoMapper(exercisePort, sumPairRoundsCache, memoryCardDeckCache, imagePairDeckCache, mathFlashcardGenerator, anagramGenerator)
        val dto = mapper.toExerciseDto(exercise, "B1")
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
        val mapper = ExerciseDtoMapper(exercisePort, sumPairRoundsCache, memoryCardDeckCache, imagePairDeckCache, mathFlashcardGenerator, anagramGenerator)
        val dto = mapper.toExerciseDto(exercise, "B1")
        assertEquals("DUAL_NBACK_CARD", dto.type)
        assertNotNull(dto.dualNBackCardParams)
        assertEquals(1, dto.dualNBackCardParams!!.n)
        assertEquals(listOf("AC", "2D", "2C", "3H"), dto.dualNBackCardParams!!.sequence)
        assertEquals(listOf(2), dto.dualNBackCardParams!!.matchNumberIndices)
    }
}
