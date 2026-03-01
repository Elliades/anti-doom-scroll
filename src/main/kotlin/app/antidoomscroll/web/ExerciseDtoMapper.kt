package app.antidoomscroll.web

import app.antidoomscroll.application.AnagramGenerator
import app.antidoomscroll.application.AnagramParamsResolver
import app.antidoomscroll.application.MathFlashcardGenerator
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
import app.antidoomscroll.web.dto.SumPairCardDto
import app.antidoomscroll.web.dto.SumPairGroupDto
import app.antidoomscroll.web.dto.SumPairParamsDto
import org.springframework.stereotype.Component

/**
 * Maps Exercise to ExerciseDto. When an N_BACK exercise has no nBackParams (e.g. from cache),
 * re-fetches by level so the API always returns sequence for the app.
 * Sum Pair rounds are cached per exercise ID so all rounds stay the same for a session
 * (e.g. list → play, or multiple requests for same exercise).
 */
@Component
class ExerciseDtoMapper(
    private val exercisePort: ExercisePort,
    private val sumPairRoundsCache: SumPairRoundsCache,
    private val memoryCardDeckCache: MemoryCardDeckCache,
    private val imagePairDeckCache: ImagePairDeckCache,
    private val mathFlashcardGenerator: MathFlashcardGenerator,
    private val anagramGenerator: AnagramGenerator
) {

    fun toExerciseDto(ex: Exercise, subjectCode: String?): ExerciseDto {
        val exerciseWithParams = resolveNBackParamsIfNeeded(ex)
        val mathParams = exerciseWithParams.mathFlashcardParams()
        val (prompt, expectedAnswers) = if (mathParams != null) {
            val (p, a) = mathFlashcardGenerator.generate(mathParams)
            Pair(p, listOf(a))
        } else {
            Pair(exerciseWithParams.prompt, exerciseWithParams.expectedAnswers)
        }
        val nBackParams = resolveNBackParams(exerciseWithParams)
        val nBackGridParams = exerciseWithParams.nBackGridParams()
        val dualNBackGridParams = exerciseWithParams.dualNBackGridParams()
        val dualNBackCardParams = exerciseWithParams.dualNBackCardParams()
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
            nBackParams = nBackParams,
            nBackGridParams = nBackGridParams?.let { p ->
                NBackGridParamsDto(
                    n = p.n,
                    sequence = p.sequence,
                    matchIndices = p.matchIndices,
                    gridSize = p.gridSize
                )
            },
            dualNBackGridParams = dualNBackGridParams?.let { p ->
                DualNBackGridParamsDto(
                    n = p.n,
                    sequence = p.sequence.map { GridStimulusDto(position = it.position, color = it.color) },
                    matchPositionIndices = p.matchPositionIndices,
                    matchColorIndices = p.matchColorIndices,
                    colors = p.colors,
                    gridSize = p.gridSize
                )
            },
            dualNBackCardParams = dualNBackCardParams?.let { p ->
                DualNBackCardParamsDto(
                    n = p.n,
                    sequence = p.sequence,
                    matchColorIndices = p.matchColorIndices,
                    matchNumberIndices = p.matchNumberIndices
                )
            },
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
            anagramParams = anagramParams
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
