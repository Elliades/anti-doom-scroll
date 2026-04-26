package app.antidoomscroll.web.dto

import java.util.UUID

/**
 * Immutable DTO for exercise in API responses.
 * Exercises belong to a subject (subjectId / subjectCode).
 * For N_BACK type, nBackParams contains sequence and matchIndices.
 * For MEMORY_CARD_PAIRS type, memoryCardParams contains pairCount and symbols.
 * For SUM_PAIR type, sumPairParams and sumPairGroups (generated at response time).
 * For FLASHCARD_QA math: mathOperation is ADD, SUBTRACT, MULTIPLY, or DIVIDE (for UI labels).
 */
data class ExerciseDto(
    val id: UUID,
    val subjectId: UUID,
    val subjectCode: String?,
    val type: String,
    val difficulty: String,
    val prompt: String,
    val expectedAnswers: List<String>,
    val timeLimitSeconds: Int,
    /** When type is FLASHCARD_QA and exercise has math params: ADD, SUBTRACT, MULTIPLY, DIVIDE. */
    val mathOperation: String? = null,
    /** Human arithmetic complexity score (0–5 very easy, 5–15 elementary, 15–30 intermediate, 30+ advanced). */
    val mathComplexityScore: Double? = null,
    val nBackParams: NBackParamsDto? = null,
    val nBackGridParams: NBackGridParamsDto? = null,
    val dualNBackGridParams: DualNBackGridParamsDto? = null,
    val dualNBackCardParams: DualNBackCardParamsDto? = null,
    val memoryCardParams: MemoryCardParamsDto? = null,
    val sumPairParams: SumPairParamsDto? = null,
    val sumPairRounds: List<SumPairRoundDto>? = null,
    /** Groups: static + color + cards (for displaying colored statics). */
    val sumPairGroups: List<SumPairGroupDto>? = null,
    /** Flat shuffled deck: all cards in display order, stable per session. */
    val sumPairDeck: List<SumPairCardDto>? = null,
    /** IMAGE_PAIR: config and generated deck (background + image; match same background + same image). */
    val imagePairParams: ImagePairParamsDto? = null,
    val imagePairDeck: List<ImagePairCardDto>? = null,
    /** ANAGRAM: scrambled letters + answer, generated at response time. */
    val anagramParams: AnagramParamsDto? = null,
    /** WORDLE: secret answer + word length + max attempts, generated at response time. */
    val wordleParams: WordleParamsDto? = null,
    /** ESTIMATION: correct answer, unit, tolerance factor and category for logarithmic scoring. */
    val estimationParams: EstimationParamsDto? = null,
    /** DIGIT_SPAN: progressive digit recall with challenge modes. */
    val digitSpanParams: DigitSpanParamsDto? = null,
    /** MATH_CHAIN: sequential operations starting from a number. */
    val mathChainParams: MathChainParamsDto? = null
)

data class ImagePairParamsDto(
    val pairCount: Int,
    val maxPairsPerBackground: Int,
    val colorCount: Int
)

data class ImagePairCardDto(
    val backgroundId: Int,
    val imageId: String,
    val backgroundColorHex: String?
)

data class AnagramParamsDto(
    val scrambledLetters: List<String>,
    val answer: String,
    /** Hint every N seconds of inactivity; default 10. */
    val hintIntervalSeconds: Int = 10,
    /** When true, filled slots show green (correct) or red (wrong) — another kind of hint. */
    val letterColorHint: Boolean = true
)

data class NBackParamsDto(
    val n: Int,
    val sequence: List<String>,
    val matchIndices: List<Int>
)

data class NBackGridParamsDto(
    val n: Int,
    val sequence: List<Int>,
    val matchIndices: List<Int>,
    val gridSize: Int = 3
)

data class GridStimulusDto(val position: Int, val color: String)

data class DualNBackGridParamsDto(
    val n: Int,
    val sequence: List<GridStimulusDto>,
    val matchPositionIndices: List<Int>,
    val matchColorIndices: List<Int>,
    val colors: List<String>,
    val gridSize: Int = 3
)

data class DualNBackCardParamsDto(
    val n: Int,
    val sequence: List<String>,
    val matchColorIndices: List<Int>,
    val matchNumberIndices: List<Int>
)

data class MemoryCardParamsDto(
    val pairCount: Int,
    val symbols: List<String>,
    /** Pre-shuffled deck: 2× each symbol, stable per session (cached by exercise ID). */
    val shuffledDeck: List<String>? = null
)

data class SumPairParamsDto(
    val staticNumbers: List<Int>,
    val pairsPerRound: Int,
    val minValue: Int = 1,
    val maxValue: Int = 99
)

data class SumPairRoundDto(val static: Int, val cards: List<Int>)

data class SumPairGroupDto(val static: Int, val color: String, val cards: List<Int>)

/** Single card with group info for flat deck display. */
data class SumPairCardDto(val value: Int, val static: Int, val color: String)

data class WordleParamsDto(
    val answer: String,
    val wordLength: Int,
    val maxAttempts: Int = 6,
    val language: String = "fr"
)

/**
 * Params for ESTIMATION exercise.
 * Score = max(0, 1 − |ln(userAnswer/correctAnswer)| / ln(toleranceFactor)).
 * category: "math" | "geography" | "science" | "history".
 * When timeWeightHigher is true (e.g. pure arithmetic), frontend weights time more than precision.
 */
data class EstimationParamsDto(
    val correctAnswer: Double,
    val unit: String,
    val toleranceFactor: Double,
    val category: String,
    val hint: String? = null,
    val timeWeightHigher: Boolean = false
)

/**
 * Params for DIGIT_SPAN exercise.
 * The exercise is fully client-driven; the backend provides timing and length config.
 */
data class DigitSpanParamsDto(
    val startLength: Int,
    val displayTimeMs: Int,
    val maxLength: Int
)

/**
 * Params for MATH_CHAIN exercise: sequential operations on a starting number.
 * User sees operations one at a time, then types the final result.
 */
data class MathChainParamsDto(
    val startNumber: Int,
    val steps: List<MathChainStepDto>,
    val expectedAnswer: Int,
    val totalComplexity: Double
)

data class MathChainStepDto(
    val operation: String,
    val operand: Int,
    val complexity: Double
)
