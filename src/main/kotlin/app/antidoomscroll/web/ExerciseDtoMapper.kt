package app.antidoomscroll.web

import app.antidoomscroll.application.AnagramGenerator
import app.antidoomscroll.application.AnagramParamsResolver
import app.antidoomscroll.application.DualNBackCardSequenceGenerator
import app.antidoomscroll.application.DualNBackGridSequenceGenerator
import app.antidoomscroll.application.NBackGridSequenceGenerator
import app.antidoomscroll.application.WordleGenerator
import app.antidoomscroll.application.WordleParamsResolver
import app.antidoomscroll.application.MathChainGenerator
import app.antidoomscroll.application.MathFlashcardGenerator
import app.antidoomscroll.application.RememberNumberGenerator
import app.antidoomscroll.application.NBackSequenceGenerator
import app.antidoomscroll.application.ImagePairDeckCache
import app.antidoomscroll.application.MemoryCardDeckCache
import app.antidoomscroll.application.SumPairParamsResolver
import app.antidoomscroll.application.SumPairRoundsCache
import app.antidoomscroll.application.port.ExercisePort
import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.ExerciseType
import app.antidoomscroll.domain.NBackParams
import app.antidoomscroll.web.dto.AnagramParamsDto
import app.antidoomscroll.web.dto.DigitSpanParamsDto
import app.antidoomscroll.web.dto.EstimationParamsDto
import app.antidoomscroll.web.dto.WordleParamsDto
import app.antidoomscroll.web.dto.DualNBackCardParamsDto
import app.antidoomscroll.web.dto.DualNBackGridParamsDto
import app.antidoomscroll.web.dto.ExerciseDto
import app.antidoomscroll.web.dto.GridStimulusDto
import app.antidoomscroll.web.dto.ImagePairCardDto
import app.antidoomscroll.web.dto.ImagePairParamsDto
import app.antidoomscroll.web.dto.MemoryCardParamsDto
import app.antidoomscroll.web.dto.NBackGridParamsDto
import app.antidoomscroll.web.dto.NBackParamsDto
import app.antidoomscroll.application.SumPairResult
import app.antidoomscroll.web.dto.MathChainParamsDto
import app.antidoomscroll.web.dto.MathChainStepDto
import app.antidoomscroll.web.dto.RememberNumberParamsDto
import app.antidoomscroll.web.dto.SumPairCardDto
import app.antidoomscroll.web.dto.SumPairGroupDto
import app.antidoomscroll.web.dto.SumPairParamsDto
import org.springframework.stereotype.Component

/**
 * Maps Exercise to ExerciseDto. When an N_BACK exercise has no nBackParams (e.g. from cache),
 * re-fetches by level so the API always returns sequence for the app.
 * Sum pair rounds and memory card deck order are generated on each mapping; [SumPairRoundsCache]
 * and [MemoryCardDeckCache] use `@Cacheable(condition = "false")` so Spring does not cache them.
 */
@Component
class ExerciseDtoMapper(
    private val exercisePort: ExercisePort,
    private val sumPairRoundsCache: SumPairRoundsCache,
    private val memoryCardDeckCache: MemoryCardDeckCache,
    private val imagePairDeckCache: ImagePairDeckCache,
    private val mathFlashcardGenerator: MathFlashcardGenerator,
    private val mathChainGenerator: MathChainGenerator,
    private val rememberNumberGenerator: RememberNumberGenerator,
    private val anagramGenerator: AnagramGenerator,
    private val wordleGenerator: WordleGenerator
) {

    fun toExerciseDto(ex: Exercise, subjectCode: String?): ExerciseDto {
        val exerciseWithParams = resolveNBackParamsIfNeeded(ex)
        val mathParams = exerciseWithParams.mathFlashcardParams()
        val (prompt, expectedAnswers, mathComplexityScore) = if (mathParams != null) {
            val result = mathFlashcardGenerator.generate(mathParams, exerciseWithParams.difficulty)
            Triple(result.prompt, listOf(result.expectedAnswer), result.complexityScore)
        } else {
            Triple(exerciseWithParams.prompt, exerciseWithParams.expectedAnswers, null)
        }
        val nBackParams = resolveNBackParams(exerciseWithParams)
        val nBackGridParams = resolveNBackGridParams(exerciseWithParams)
        val dualNBackGridParams = resolveDualNBackGridParams(exerciseWithParams)
        val dualNBackCardParams = resolveDualNBackCardParams(exerciseWithParams)
        val memoryCardParams = exerciseWithParams.memoryCardParams()
        val imagePairParams = exerciseWithParams.imagePairParams()
        val sumPairParams = SumPairParamsResolver.resolve(exerciseWithParams)
        val anagramParams = AnagramParamsResolver.resolve(exerciseWithParams)?.let { p ->
            anagramGenerator.generate(p)?.let { r ->
                val ep = exerciseWithParams.exerciseParams
                val hintIntervalSeconds = (ep?.get("hintIntervalSeconds") as? Number)?.toInt() ?: 10
                val letterColorHint = (ep?.get("letterColorHint") as? Boolean) ?: true
                AnagramParamsDto(
                    scrambledLetters = r.scrambledLetters,
                    answer = r.answer,
                    hintIntervalSeconds = hintIntervalSeconds,
                    letterColorHint = letterColorHint
                )
            }
        }
        val estimationParams = exerciseWithParams.estimationParams()?.let { p ->
            EstimationParamsDto(
                correctAnswer = p.correctAnswer,
                unit = p.unit,
                toleranceFactor = p.toleranceFactor,
                category = p.category,
                hint = p.hint,
                timeWeightHigher = p.timeWeightHigher
            )
        }
        val digitSpanParams = exerciseWithParams.digitSpanParams()?.let { p ->
            DigitSpanParamsDto(
                startLength = p.startLength,
                displayTimeMs = p.displayTimeMs,
                maxLength = p.maxLength
            )
        }
        val mathChainParams = if (exerciseWithParams.type == ExerciseType.MATH_CHAIN) {
            val chainResult = mathChainGenerator.generate(exerciseWithParams.difficulty)
            MathChainParamsDto(
                startNumber = chainResult.startNumber,
                steps = chainResult.steps.map { s ->
                    MathChainStepDto(
                        operation = s.operation.name,
                        operand = s.operand,
                        complexity = s.complexity
                    )
                },
                expectedAnswer = chainResult.expectedAnswer,
                totalComplexity = chainResult.totalComplexity
            )
        } else null
        val rememberNumberParams = exerciseWithParams.rememberNumberParams()?.let { p ->
            val result = rememberNumberGenerator.generate(p, exerciseWithParams.difficulty)
            RememberNumberParamsDto(
                numberToRemember = result.numberToRemember,
                displayTimeMs = result.displayTimeMs,
                mathPrompt = result.mathPrompt,
                mathExpectedAnswer = result.mathExpectedAnswer,
                mathComplexityScore = result.mathComplexityScore
            )
        }
        val wordleParams = WordleParamsResolver.resolve(exerciseWithParams)?.let { p ->
            wordleGenerator.generate(p)?.let { r ->
                WordleParamsDto(
                    answer = r.answer,
                    wordLength = r.wordLength,
                    maxAttempts = r.maxAttempts,
                    language = p.language
                )
            }
        }
        val memoryCardShuffledDeck = memoryCardParams?.let { p ->
            memoryCardDeckCache.getOrGenerate(exerciseWithParams.id.toString(), p)
        }
        val imagePairResult = imagePairParams?.let { p ->
            imagePairDeckCache.getOrGenerate(exerciseWithParams.id.toString(), p)
        }
        val sumPairResult = sumPairParams?.let { p ->
            sumPairRoundsCache.getOrGenerate(exerciseWithParams.id.toString(), p)
        }
        val sumPairGroups = sumPairResult?.groups?.map { g ->
            SumPairGroupDto(static = g.static, color = g.color, cards = g.cards)
        }
        val sumPairDeck = sumPairResult?.deck?.map { c ->
            SumPairCardDto(value = c.value, static = c.static, color = c.color)
        }
        return ExerciseDto(
            id = exerciseWithParams.id,
            subjectId = exerciseWithParams.subjectId,
            subjectCode = subjectCode,
            type = exerciseWithParams.type.name,
            difficulty = exerciseWithParams.difficulty.name,
            prompt = prompt,
            expectedAnswers = expectedAnswers,
            timeLimitSeconds = exerciseWithParams.timeLimitSeconds,
            mathOperation = mathParams?.operation?.name,
            mathComplexityScore = mathComplexityScore,
            nBackParams = nBackParams,
            nBackGridParams = nBackGridParams,
            dualNBackGridParams = dualNBackGridParams,
            dualNBackCardParams = dualNBackCardParams,
            memoryCardParams = memoryCardParams?.let { p ->
                MemoryCardParamsDto(
                    pairCount = p.pairCount,
                    symbols = p.symbols,
                    shuffledDeck = memoryCardShuffledDeck
                )
            },
            sumPairParams = sumPairParams?.let { p ->
                SumPairParamsDto(
                    staticNumbers = p.staticNumbers,
                    pairsPerRound = p.pairsPerRound,
                    minValue = p.minValue,
                    maxValue = p.maxValue
                )
            },
            sumPairGroups = sumPairGroups,
            sumPairDeck = sumPairDeck,
            imagePairParams = imagePairParams?.let { p ->
                ImagePairParamsDto(
                    pairCount = p.pairCount,
                    maxPairsPerBackground = p.maxPairsPerBackground,
                    colorCount = p.colorCount
                )
            },
            imagePairDeck = imagePairResult?.deck?.map { c ->
                ImagePairCardDto(
                    backgroundId = c.backgroundId,
                    imageId = c.imageId,
                    backgroundColorHex = c.backgroundColorHex
                )
            },
            anagramParams = anagramParams,
            wordleParams = wordleParams,
            estimationParams = estimationParams,
            digitSpanParams = digitSpanParams,
            mathChainParams = mathChainParams,
            rememberNumberParams = rememberNumberParams
        )
    }

    /**
     * Resolves N_BACK_GRID params: from stored static sequence, or dynamically from (n, gridSize).
     * Parametric exercises store only n + gridSize (+ optional sequenceLength); sequence is generated fresh each call.
     */
    private fun resolveNBackGridParams(ex: Exercise): NBackGridParamsDto? {
        if (ex.type != ExerciseType.N_BACK_GRID) return null
        val stored = ex.nBackGridParams()
        if (stored != null) {
            return NBackGridParamsDto(
                n = stored.n,
                sequence = stored.sequence,
                matchIndices = stored.matchIndices,
                gridSize = stored.gridSize
            )
        }
        val p = ex.exerciseParams ?: return null
        val n = (p["n"] as? Number)?.toInt() ?: return null
        val gridSize = (p["gridSize"] as? Number)?.toInt() ?: 3
        val sequenceLength = (p["sequenceLength"] as? Number)?.toInt() ?: 12
        val (sequence, matchIndices) = NBackGridSequenceGenerator.generate(
            n = n,
            gridSize = gridSize,
            sequenceLength = sequenceLength.coerceAtLeast(n + 2),
            seed = kotlin.random.Random.Default.nextInt()
        )
        return NBackGridParamsDto(n = n, sequence = sequence, matchIndices = matchIndices, gridSize = gridSize)
    }

    /**
     * Resolves DUAL_NBACK_GRID params: from stored static sequence, or dynamically from (n, gridSize, colorCount).
     */
    private fun resolveDualNBackGridParams(ex: Exercise): DualNBackGridParamsDto? {
        if (ex.type != ExerciseType.DUAL_NBACK_GRID) return null
        val stored = ex.dualNBackGridParams()
        if (stored != null) {
            return DualNBackGridParamsDto(
                n = stored.n,
                sequence = stored.sequence.map { GridStimulusDto(position = it.position, color = it.color) },
                matchPositionIndices = stored.matchPositionIndices,
                matchColorIndices = stored.matchColorIndices,
                colors = stored.colors,
                gridSize = stored.gridSize
            )
        }
        val p = ex.exerciseParams ?: return null
        val n = (p["n"] as? Number)?.toInt() ?: return null
        val gridSize = (p["gridSize"] as? Number)?.toInt() ?: 3
        val colorCount = (p["colorCount"] as? Number)?.toInt() ?: 4
        val sequenceLength = (p["sequenceLength"] as? Number)?.toInt() ?: 12
        val result = DualNBackGridSequenceGenerator.generate(
            n = n,
            gridSize = gridSize,
            colorCount = colorCount,
            sequenceLength = sequenceLength.coerceAtLeast(n + 2),
            seed = kotlin.random.Random.Default.nextInt()
        )
        return DualNBackGridParamsDto(
            n = n,
            sequence = result.sequence.map { s ->
                GridStimulusDto(
                    position = (s["position"] as? Number)?.toInt() ?: 0,
                    color = s["color"]?.toString() ?: "#000000"
                )
            },
            matchPositionIndices = result.matchPositionIndices,
            matchColorIndices = result.matchColorIndices,
            colors = result.colors,
            gridSize = gridSize
        )
    }

    /**
     * Resolves DUAL_NBACK_CARD params: from stored static sequence, or dynamically from (n, suitCount).
     */
    private fun resolveDualNBackCardParams(ex: Exercise): DualNBackCardParamsDto? {
        if (ex.type != ExerciseType.DUAL_NBACK_CARD) return null
        val stored = ex.dualNBackCardParams()
        if (stored != null) {
            return DualNBackCardParamsDto(
                n = stored.n,
                sequence = stored.sequence,
                matchColorIndices = stored.matchColorIndices,
                matchNumberIndices = stored.matchNumberIndices
            )
        }
        val p = ex.exerciseParams ?: return null
        val n = (p["n"] as? Number)?.toInt() ?: return null
        val suitCount = (p["suitCount"] as? Number)?.toInt() ?: 4
        val sequenceLength = (p["sequenceLength"] as? Number)?.toInt() ?: 12
        val result = DualNBackCardSequenceGenerator.generate(
            n = n,
            suitCount = suitCount,
            sequenceLength = sequenceLength.coerceAtLeast(n + 2),
            seed = kotlin.random.Random.Default.nextInt()
        )
        return DualNBackCardParamsDto(
            n = n,
            sequence = result.sequence,
            matchColorIndices = result.matchColorIndices,
            matchNumberIndices = result.matchNumberIndices
        )
    }

    /**
     * Resolves NBackParams: from stored sequence, or by generating from (n, suitCount).
     * Parametric mode: exercise has n + suitCount (1-4) only; sequence is generated.
     */
    private fun resolveNBackParams(ex: Exercise): NBackParamsDto? {
        val parsed = ex.nBackParams() ?: run {
            if (ex.type != ExerciseType.N_BACK) return@run null
            val p = ex.exerciseParams ?: return@run null
            val n = (p["n"] as? Number)?.toInt() ?: return@run null
            val suitCount = (p["suitCount"] as? Number)?.toInt() ?: return@run null
            if (suitCount !in 1..4) return@run null
            val sequenceLength = (p["sequenceLength"] as? Number)?.toInt() ?: 12
            val (sequence, matchIndices) = NBackSequenceGenerator.generate(
                n = n,
                suitCount = suitCount,
                sequenceLength = sequenceLength.coerceAtLeast(n + 2),
                seed = kotlin.random.Random.Default.nextInt()
            )
            NBackParamsDto(n = n, sequence = sequence, matchIndices = matchIndices)
        }
        return when (parsed) {
            is NBackParams -> NBackParamsDto(n = parsed.n, sequence = parsed.sequence, matchIndices = parsed.matchIndices)
            is NBackParamsDto -> parsed
            else -> null
        }
    }

    /**
     * If exercise is N_BACK but nBackParams() is null, try findNBackByLevel by id, then raw DB load.
     * Fallback: generate from (n, suitCount) if present, else minimal valid sequence.
     */
    private fun resolveNBackParamsIfNeeded(ex: Exercise): Exercise {
        if (ex.type != ExerciseType.N_BACK) return ex
        if (ex.nBackParams() != null) return ex
        for (level in 1..3) {
            val loaded = exercisePort.findNBackByLevel(level) ?: continue
            if (loaded.id == ex.id && loaded.nBackParams() != null) return loaded
        }
        val paramsMap = exercisePort.findExerciseParamsById(ex.id)
        if (paramsMap != null) return ex.copy(exerciseParams = paramsMap)
        // Fallback: parametric (n=1, suitCount=1) so app never shows "missing sequence"
        return ex.copy(
            exerciseParams = mapOf("n" to 1, "suitCount" to 1)
        )
    }
}
