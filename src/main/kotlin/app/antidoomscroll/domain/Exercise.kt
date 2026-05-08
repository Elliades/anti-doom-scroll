package app.antidoomscroll.domain

import java.util.UUID

/**
 * Pure domain: an exercise with prompt, type, difficulty, and scoring params.
 * Belongs to one Subject. No JPA — infrastructure maps to/from this.
 * For N_BACK type, exerciseParams contains: n, sequence, matchIndices.
 */
data class Exercise(
    val id: UUID,
    val subjectId: UUID,
    val type: ExerciseType,
    val difficulty: Difficulty,
    val prompt: String,
    val expectedAnswers: List<String>,
    val timeLimitSeconds: Int,
    val exerciseParams: Map<String, Any?>? = null
) {
    fun isUltraEasy(): Boolean = difficulty == Difficulty.ULTRA_EASY

    /**
     * Parses N_BACK params from exerciseParams. Returns null if type is not N_BACK,
     * params are missing, or JSON/DB shape causes parse errors (avoids 500s).
     */
    fun nBackParams(): NBackParams? = runCatching {
        when (type) {
            ExerciseType.N_BACK -> {
                val p = exerciseParams ?: return@runCatching null
                val n = (p["n"] as? Number)?.toInt() ?: return@runCatching null
                @Suppress("UNCHECKED_CAST")
                val sequence = (p["sequence"] as? List<*>)?.map { it.toString() } ?: return@runCatching null
                @Suppress("UNCHECKED_CAST")
                val matchIndices = (p["matchIndices"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: return@runCatching null
                NBackParams(n = n, sequence = sequence, matchIndices = matchIndices)
            }
            else -> null
        }
    }.getOrNull()

    /**
     * Parses MEMORY_CARD_PAIRS params from exerciseParams. Returns null if type is not MEMORY_CARD_PAIRS,
     * params are missing, or JSON/DB shape causes parse errors (avoids 500s).
     */
    fun memoryCardParams(): MemoryCardParams? = runCatching {
        when (type) {
            ExerciseType.MEMORY_CARD_PAIRS -> {
                val p = exerciseParams ?: return@runCatching null
                val pairCount = (p["pairCount"] as? Number)?.toInt() ?: return@runCatching null
                @Suppress("UNCHECKED_CAST")
                val symbols = (p["symbols"] as? List<*>)?.map { it.toString() } ?: return@runCatching null
                if (symbols.size != pairCount) return@runCatching null
                MemoryCardParams(pairCount = pairCount, symbols = symbols)
            }
            else -> null
        }
    }.getOrNull()

    /**
     * Parses IMAGE_PAIR params from exerciseParams. Returns null if type is not IMAGE_PAIR or params invalid.
     */
    fun imagePairParams(): ImagePairParams? = runCatching {
        when (type) {
            ExerciseType.IMAGE_PAIR -> {
                val p = exerciseParams ?: return@runCatching null
                val pairCount = (p["pairCount"] as? Number)?.toInt() ?: return@runCatching null
                val maxPairsPerBackground = (p["maxPairsPerBackground"] as? Number)?.toInt() ?: 2
                val colorCount = (p["colorCount"] as? Number)?.toInt() ?: 1
                ImagePairParams(
                    pairCount = pairCount,
                    maxPairsPerBackground = maxPairsPerBackground,
                    colorCount = colorCount
                )
            }
            else -> null
        }
    }.getOrNull()

    /**
     * Parses MATH_FLASHCARD params from exerciseParams. Returns null if params are missing.
     * Used for FLASHCARD_QA with generated addition/subtraction/multiplication/division.
     */
    fun mathFlashcardParams(): MathFlashcardParams? = runCatching {
        val p = exerciseParams ?: return@runCatching null
        val opStr = (p["operation"] as? String) ?: return@runCatching null
        val operation = when (opStr.uppercase()) {
            "ADD" -> MathOperation.ADD
            "SUBTRACT" -> MathOperation.SUBTRACT
            "MULTIPLY" -> MathOperation.MULTIPLY
            "DIVIDE" -> MathOperation.DIVIDE
            else -> return@runCatching null
        }
        val firstMax = (p["firstMax"] as? Number)?.toInt() ?: return@runCatching null
        val secondMax = (p["secondMax"] as? Number)?.toInt() ?: return@runCatching null
        val firstMin = (p["firstMin"] as? Number)?.toInt() ?: 1
        val secondMin = (p["secondMin"] as? Number)?.toInt() ?: 1
        @Suppress("UNCHECKED_CAST")
        val firstValues = (p["firstValues"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }?.takeIf { it.isNotEmpty() }
        @Suppress("UNCHECKED_CAST")
        val secondValues = (p["secondValues"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }?.takeIf { it.isNotEmpty() }
        MathFlashcardParams(
            operation = operation,
            firstMin = firstMin,
            firstMax = firstMax,
            secondMin = secondMin,
            secondMax = secondMax,
            firstValues = firstValues,
            secondValues = secondValues
        )
    }.getOrNull()

    fun sumPairParams(): SumPairParams? = runCatching {
        when (type) {
            ExerciseType.SUM_PAIR -> {
                val p = exerciseParams ?: return@runCatching null
                @Suppress("UNCHECKED_CAST")
                val staticNumbers = (p["staticNumbers"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: return@runCatching null
                if (staticNumbers.isEmpty()) return@runCatching null
                val pairsPerRound = (p["pairsPerRound"] as? Number)?.toInt() ?: return@runCatching null
                val minValue = (p["minValue"] as? Number)?.toInt() ?: 1
                val maxValue = (p["maxValue"] as? Number)?.toInt() ?: 99
                SumPairParams(
                    staticNumbers = staticNumbers,
                    pairsPerRound = pairsPerRound,
                    minValue = minValue,
                    maxValue = maxValue
                )
            }
            else -> null
        }
    }.getOrNull()

    fun nBackGridParams(): NBackGridParams? = runCatching {
        when (type) {
            ExerciseType.N_BACK_GRID -> {
                val p = exerciseParams ?: return@runCatching null
                val n = (p["n"] as? Number)?.toInt() ?: return@runCatching null
                @Suppress("UNCHECKED_CAST")
                val sequence = (p["sequence"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: return@runCatching null
                @Suppress("UNCHECKED_CAST")
                val matchIndices = (p["matchIndices"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: return@runCatching null
                val gridSize = (p["gridSize"] as? Number)?.toInt() ?: 3
                NBackGridParams(n = n, sequence = sequence, matchIndices = matchIndices, gridSize = gridSize)
            }
            else -> null
        }
    }.getOrNull()

    fun dualNBackGridParams(): DualNBackGridParams? = runCatching {
        when (type) {
            ExerciseType.DUAL_NBACK_GRID -> {
                val p = exerciseParams ?: return@runCatching null
                val n = (p["n"] as? Number)?.toInt() ?: return@runCatching null
                @Suppress("UNCHECKED_CAST")
                val seqRaw = (p["sequence"] as? List<*>) ?: return@runCatching null
                val sequence = seqRaw.mapNotNull { item ->
                    val m = item as? Map<*, *> ?: return@mapNotNull null
                    val pos = (m["position"] as? Number)?.toInt() ?: return@mapNotNull null
                    val col = m["color"]?.toString() ?: return@mapNotNull null
                    GridStimulus(position = pos, color = col)
                }
                if (sequence.size != seqRaw.size) return@runCatching null
                @Suppress("UNCHECKED_CAST")
                val matchPositionIndices = (p["matchPositionIndices"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: return@runCatching null
                @Suppress("UNCHECKED_CAST")
                val matchColorIndices = (p["matchColorIndices"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: return@runCatching null
                @Suppress("UNCHECKED_CAST")
                val colors = (p["colors"] as? List<*>)?.map { it.toString() } ?: return@runCatching null
                val gridSize = (p["gridSize"] as? Number)?.toInt() ?: 3
                DualNBackGridParams(
                    n = n,
                    sequence = sequence,
                    matchPositionIndices = matchPositionIndices,
                    matchColorIndices = matchColorIndices,
                    colors = colors,
                    gridSize = gridSize
                )
            }
            else -> null
        }
    }.getOrNull()

    fun anagramParams(): AnagramParams? = runCatching {
        when (type) {
            ExerciseType.ANAGRAM -> {
                val p = exerciseParams ?: return@runCatching null
                val minLetters = (p["minLetters"] as? Number)?.toInt() ?: return@runCatching null
                val maxLetters = (p["maxLetters"] as? Number)?.toInt() ?: return@runCatching null
                val language = (p["language"] as? String)?.lowercase() ?: "fr"
                AnagramParams(minLetters = minLetters, maxLetters = maxLetters, language = language)
            }
            else -> null
        }
    }.getOrNull()

    fun wordleParams(): WordleParams? = runCatching {
        when (type) {
            ExerciseType.WORDLE -> {
                val p = exerciseParams ?: return@runCatching null
                val wordLength = (p["wordLength"] as? Number)?.toInt() ?: return@runCatching null
                val language = (p["language"] as? String)?.lowercase() ?: "fr"
                val maxAttempts = (p["maxAttempts"] as? Number)?.toInt() ?: 6
                WordleParams(wordLength = wordLength, language = language, maxAttempts = maxAttempts)
            }
            else -> null
        }
    }.getOrNull()

    fun estimationParams(): EstimationParams? = runCatching {
        when (type) {
            ExerciseType.ESTIMATION -> {
                val p = exerciseParams ?: return@runCatching null
                val correctAnswer = (p["correctAnswer"] as? Number)?.toDouble() ?: return@runCatching null
                val unit = (p["unit"] as? String) ?: return@runCatching null
                val toleranceFactor = (p["toleranceFactor"] as? Number)?.toDouble() ?: return@runCatching null
                val category = (p["category"] as? String) ?: return@runCatching null
                val hint = p["hint"] as? String
                val timeWeightHigher = (p["timeWeightHigher"] as? Boolean) ?: false
                EstimationParams(
                    correctAnswer = correctAnswer,
                    unit = unit,
                    toleranceFactor = toleranceFactor,
                    category = category,
                    hint = hint,
                    timeWeightHigher = timeWeightHigher
                )
            }
            else -> null
        }
    }.getOrNull()

    fun digitSpanParams(): DigitSpanParams? = runCatching {
        when (type) {
            ExerciseType.DIGIT_SPAN -> {
                val p = exerciseParams ?: return@runCatching null
                val startLength = (p["startLength"] as? Number)?.toInt() ?: return@runCatching null
                val displayTimeMs = (p["displayTimeMs"] as? Number)?.toInt() ?: 3000
                val maxLength = (p["maxLength"] as? Number)?.toInt() ?: 15
                DigitSpanParams(startLength = startLength, displayTimeMs = displayTimeMs, maxLength = maxLength)
            }
            else -> null
        }
    }.getOrNull()

    fun rememberNumberParams(): RememberNumberParams? = runCatching {
        when (type) {
            ExerciseType.REMEMBER_NUMBER -> {
                val p = exerciseParams ?: return@runCatching null
                val numberDigits = (p["numberDigits"] as? Number)?.toInt() ?: return@runCatching null
                val displayTimeMs = (p["displayTimeMs"] as? Number)?.toInt() ?: 3000
                val mathOperation = (p["mathOperation"] as? String) ?: "ADD"
                val mathFirstMax = (p["mathFirstMax"] as? Number)?.toInt() ?: 9
                val mathSecondMax = (p["mathSecondMax"] as? Number)?.toInt() ?: 9
                RememberNumberParams(
                    numberDigits = numberDigits,
                    displayTimeMs = displayTimeMs,
                    mathOperation = mathOperation,
                    mathFirstMax = mathFirstMax,
                    mathSecondMax = mathSecondMax
                )
            }
            else -> null
        }
    }.getOrNull()

    fun mathChainDifficulty(): Difficulty? = runCatching {
        when (type) {
            ExerciseType.MATH_CHAIN -> difficulty
            else -> null
        }
    }.getOrNull()

    fun dualNBackCardParams(): DualNBackCardParams? = runCatching {
        when (type) {
            ExerciseType.DUAL_NBACK_CARD -> {
                val p = exerciseParams ?: return@runCatching null
                val n = (p["n"] as? Number)?.toInt() ?: return@runCatching null
                @Suppress("UNCHECKED_CAST")
                val sequence = (p["sequence"] as? List<*>)?.map { it.toString() } ?: return@runCatching null
                @Suppress("UNCHECKED_CAST")
                val matchColorIndices = (p["matchColorIndices"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: return@runCatching null
                @Suppress("UNCHECKED_CAST")
                val matchNumberIndices = (p["matchNumberIndices"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: return@runCatching null
                DualNBackCardParams(
                    n = n,
                    sequence = sequence,
                    matchColorIndices = matchColorIndices,
                    matchNumberIndices = matchNumberIndices
                )
            }
            else -> null
        }
    }.getOrNull()
}
