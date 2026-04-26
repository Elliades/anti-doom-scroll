package app.antidoomscroll.application

import app.antidoomscroll.application.port.ExercisePort
import app.antidoomscroll.application.port.SubjectPort
import app.antidoomscroll.domain.Difficulty
import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.ExerciseType
import app.antidoomscroll.domain.LadderConfig
import app.antidoomscroll.domain.LadderLevel
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.random.Random

/**
 * Selects the next exercise for a ladder level.
 *
 * Selection pipeline:
 * 1. If [LadderLevel.exerciseIds] is set → fixed pool, skip DB query.
 * 2. Otherwise → query by [LadderLevel.subjectCodes] + [LadderLevel.allowedDifficulties] (limit 40).
 * 3. Add parametric pools (mirrors ESTIMATION): ESTIMATION, MEMORY_CARD_PAIRS, SUM_PAIR, FLASHCARD_QA on matching subjects.
 * 4. Apply in-memory filters: [allowedTypes], [exerciseParamFilter].
 * 5. Prefer exercises whose IDs are not in [excludeExerciseIds] when possible; otherwise fall back to full filtered pool.
 */
@Component
class LadderExercisePicker(
    private val exercisePort: ExercisePort,
    private val subjectPort: SubjectPort,
    private val estimationExerciseGenerator: EstimationExerciseGenerator
) {
    private val difficultyEaseMap = mapOf(
        Difficulty.ULTRA_EASY to 0.82,
        Difficulty.EASY to 0.72,
        Difficulty.MEDIUM to 0.60,
        Difficulty.HARD to 0.46,
        Difficulty.VERY_HARD to 0.35
    )

    private val earlyRampPortion = 0.2
    private val earlyRampStartEase = 0.86
    private val earlyRampEndEase = 0.64
    private val finalLinearEndEase = 0.26

    fun targetScoreForLevel(config: LadderConfig, level: LadderLevel): Double {
        val maxLevelIndex = config.levels.maxOfOrNull { it.levelIndex }?.coerceAtLeast(1) ?: 1
        val progress = (level.levelIndex.toDouble() / maxLevelIndex.toDouble()).coerceIn(0.0, 1.0)
        val curveEase = if (progress <= earlyRampPortion) {
            earlyRampStartEase -
                (earlyRampStartEase - earlyRampEndEase) * (progress / earlyRampPortion)
        } else {
            earlyRampEndEase -
                (earlyRampEndEase - finalLinearEndEase) * ((progress - earlyRampPortion) / (1 - earlyRampPortion))
        }
        val difficultyEase = level.allowedDifficulties
            .mapNotNull { difficultyEaseMap[it] }
            .let { values -> if (values.isEmpty()) 0.6 else values.average() }
        return (curveEase * 0.7 + difficultyEase * 0.3).coerceIn(0.0, 1.0)
    }

    fun blendTargetWithRecent(baseTargetScore: Double, recentScores: List<Double>): Double {
        if (recentScores.isEmpty()) return baseTargetScore.coerceIn(0.0, 1.0)
        val avgRecent = recentScores.average()
        return (baseTargetScore * 0.7 + avgRecent * 0.3).coerceIn(0.0, 1.0)
    }

    @Suppress("UNUSED_PARAMETER")
    fun pick(
        config: LadderConfig,
        level: LadderLevel,
        excludeExerciseIds: Set<UUID> = emptySet(),
        targetScore: Double = targetScoreForLevel(config, level)
    ): Exercise? {
        val candidates = buildCandidatePool(level) +
            buildGeneratedEstimationPool(level, targetScore) +
            buildGeneratedMemoryCardPool(level, targetScore) +
            buildGeneratedSumPairPool(level, targetScore) +
            buildGeneratedMathFlashcardPool(level, targetScore) +
            buildGeneratedMathChainPool(level, targetScore)
        val filtered = applyFilters(candidates, level)
        return chooseAvoidingRecent(filtered, excludeExerciseIds)
    }

    private fun chooseAvoidingRecent(filtered: List<Exercise>, excludeExerciseIds: Set<UUID>): Exercise? {
        if (filtered.isEmpty()) return null
        val preferred = filtered.filter { it.id !in excludeExerciseIds }
        val pool = if (preferred.isNotEmpty()) preferred else filtered
        return pool.randomOrNull()
    }

    // ------------------------------------------------------------------
    // Pool construction
    // ------------------------------------------------------------------

    /** Generated ESTIMATION exercises (parametric math + word problems) for variety. */
    private fun buildGeneratedEstimationPool(level: LadderLevel, targetScore: Double): List<Exercise> {
        if (level.exerciseIds != null && level.exerciseIds.isNotEmpty()) return emptyList()
        if (!level.subjectCodes.contains("ESTIMATION")) return emptyList()
        if (!allowsType(level, ExerciseType.ESTIMATION)) return emptyList()
        val subject = subjectPort.findByCode("ESTIMATION") ?: return emptyList()
        val difficulty = nearestDifficultyForTarget(level.allowedDifficulties, targetScore)
        val baseSeed = Random.nextLong()
        val generated = estimationExerciseGenerator.generatePool(
            levelIndex = level.levelIndex,
            difficulty = difficulty,
            baseSeed = baseSeed,
            count = 8
        )
        return generated.mapIndexed { i, g ->
            toEstimationExercise(g, subject.id, difficulty, level.levelIndex, baseSeed + i)
        }
    }

    private fun toEstimationExercise(
        g: EstimationExerciseGenerator.GeneratedEstimation,
        subjectId: UUID,
        difficulty: Difficulty,
        levelIndex: Int,
        seed: Long
    ): Exercise {
        val id = UUID.nameUUIDFromBytes("est-gen-$levelIndex-$seed".toByteArray())
        val params = mapOf(
            "correctAnswer" to g.correctAnswer,
            "unit" to g.unit,
            "toleranceFactor" to g.toleranceFactor,
            "category" to g.category,
            "hint" to g.hint,
            "timeWeightHigher" to g.timeWeightHigher
        )
        return Exercise(
            id = id,
            subjectId = subjectId,
            type = ExerciseType.ESTIMATION,
            difficulty = difficulty,
            prompt = g.prompt,
            expectedAnswers = listOf(g.correctAnswer.toString()),
            timeLimitSeconds = 30,
            exerciseParams = params
        )
    }

    /** Synthetic memory card exercises with random emoji sets (avoids a single DB row dominating). */
    private fun buildGeneratedMemoryCardPool(level: LadderLevel, targetScore: Double): List<Exercise> {
        if (level.exerciseIds != null && level.exerciseIds.isNotEmpty()) return emptyList()
        if (!level.subjectCodes.contains("MEMORY")) return emptyList()
        if (!allowsType(level, ExerciseType.MEMORY_CARD_PAIRS)) return emptyList()
        val subject = subjectPort.findByCode("MEMORY") ?: return emptyList()
        val difficulty = nearestDifficultyForTarget(level.allowedDifficulties, targetScore)
        val pairCount = ladderPickerMemoryPairCount(targetScore)
        val baseSeed = Random.nextLong()
        return (0 until 8).map { i ->
            val rng = Random(baseSeed + i * 31L)
            val symbols = ladderPickerPickDistinctEmojis(pairCount, rng)
            val id = UUID.nameUUIDFromBytes("mem-gen-${level.levelIndex}-${baseSeed}-$i".toByteArray())
            Exercise(
                id = id,
                subjectId = subject.id,
                type = ExerciseType.MEMORY_CARD_PAIRS,
                difficulty = difficulty,
                prompt = "Find all matching pairs. Flip two cards at a time.",
                expectedAnswers = emptyList(),
                timeLimitSeconds = 120,
                exerciseParams = mapOf(
                    "pairCount" to pairCount,
                    "symbols" to symbols
                )
            )
        }
    }

    /** Synthetic sum-pair templates with varied statics/ranges. */
    private fun buildGeneratedSumPairPool(level: LadderLevel, targetScore: Double): List<Exercise> {
        if (level.exerciseIds != null && level.exerciseIds.isNotEmpty()) return emptyList()
        if (!level.subjectCodes.contains("MEMORY")) return emptyList()
        if (!allowsType(level, ExerciseType.SUM_PAIR)) return emptyList()
        val subject = subjectPort.findByCode("MEMORY") ?: return emptyList()
        val difficulty = nearestDifficultyForTarget(level.allowedDifficulties, targetScore)
        val baseSeed = Random.nextLong()
        return (0 until 8).map { i ->
            val rng = Random(baseSeed + i * 37L)
            val (statics, pairs, minV, maxV) = ladderPickerSumPairParams(targetScore, rng)
            val id = UUID.nameUUIDFromBytes("sumpair-gen-${level.levelIndex}-${baseSeed}-$i".toByteArray())
            Exercise(
                id = id,
                subjectId = subject.id,
                type = ExerciseType.SUM_PAIR,
                difficulty = difficulty,
                prompt = "Find pairs where first + static = second.",
                expectedAnswers = emptyList(),
                timeLimitSeconds = 120,
                exerciseParams = mapOf(
                    "staticNumbers" to statics,
                    "pairsPerRound" to pairs,
                    "minValue" to minV,
                    "maxValue" to maxV
                )
            )
        }
    }

    /** Synthetic math flashcards so operation filters (e.g. ADD-only) still have a wide ID pool. */
    private fun buildGeneratedMathFlashcardPool(level: LadderLevel, targetScore: Double): List<Exercise> {
        if (level.exerciseIds != null && level.exerciseIds.isNotEmpty()) return emptyList()
        if (!level.subjectCodes.contains("default")) return emptyList()
        if (!allowsType(level, ExerciseType.FLASHCARD_QA)) return emptyList()
        val subject = subjectPort.findByCode("default") ?: return emptyList()
        val difficulty = nearestDifficultyForTarget(level.allowedDifficulties, targetScore)
        val filterOps = level.exerciseParamFilter?.get("operation")?.map { it.uppercase() }
        val ops = when {
            filterOps.isNullOrEmpty() -> listOf("ADD", "SUBTRACT", "MULTIPLY", "DIVIDE")
            else -> filterOps
        }
        if (ops.isEmpty()) return emptyList()
        val baseSeed = Random.nextLong()
        return (0 until 8).map { i ->
            val rng = Random(baseSeed + i * 41L)
            val op = ops[rng.nextInt(ops.size)]
            val (fMax, sMax) = ladderPickerMathOperandMaxes(targetScore, rng)
            val id = UUID.nameUUIDFromBytes("math-gen-${level.levelIndex}-${baseSeed}-$i".toByteArray())
            Exercise(
                id = id,
                subjectId = subject.id,
                type = ExerciseType.FLASHCARD_QA,
                difficulty = difficulty,
                prompt = "What is …?",
                expectedAnswers = listOf("0"),
                timeLimitSeconds = 60,
                exerciseParams = mapOf(
                    "operation" to op,
                    "firstMin" to 1,
                    "firstMax" to fMax,
                    "secondMin" to 1,
                    "secondMax" to sMax
                )
            )
        }
    }

    /** Synthetic MATH_CHAIN exercises so the type has a wide ID pool in ladders. */
    private fun buildGeneratedMathChainPool(level: LadderLevel, targetScore: Double): List<Exercise> {
        if (level.exerciseIds != null && level.exerciseIds.isNotEmpty()) return emptyList()
        if (!level.subjectCodes.contains("default")) return emptyList()
        if (!allowsType(level, ExerciseType.MATH_CHAIN)) return emptyList()
        val subject = subjectPort.findByCode("default") ?: return emptyList()
        val difficulty = nearestDifficultyForTarget(level.allowedDifficulties, targetScore)
        val baseSeed = Random.nextLong()
        return (0 until 8).map { i ->
            val id = UUID.nameUUIDFromBytes("mathchain-gen-${level.levelIndex}-${baseSeed}-$i".toByteArray())
            Exercise(
                id = id,
                subjectId = subject.id,
                type = ExerciseType.MATH_CHAIN,
                difficulty = difficulty,
                prompt = "Mental math chain",
                expectedAnswers = listOf("0"),
                timeLimitSeconds = 120,
                exerciseParams = null
            )
        }
    }

    private fun allowsType(level: LadderLevel, type: ExerciseType): Boolean {
        val types = level.allowedTypes
        return types == null || types.isEmpty() || type in types
    }

    private fun buildCandidatePool(level: LadderLevel): List<Exercise> {
        if (level.exerciseIds != null && level.exerciseIds.isNotEmpty()) {
            return exercisePort.findByIds(level.exerciseIds)
        }

        val subjectIds = level.subjectCodes
            .mapNotNull { subjectPort.findByCode(it)?.id }

        if (level.subjectCodes.isNotEmpty() && subjectIds.isEmpty()) return emptyList()

        return exercisePort.findRandomBySubjectsAndDifficulties(
            subjectIds = subjectIds,
            difficulties = level.allowedDifficulties,
            limit = 40
        )
    }

    // ------------------------------------------------------------------
    // In-memory filters
    // ------------------------------------------------------------------

    private fun applyFilters(candidates: List<Exercise>, level: LadderLevel): List<Exercise> {
        var result = candidates

        level.allowedTypes?.let { types ->
            if (types.isNotEmpty()) result = result.filter { it.type in types }
        }

        level.exerciseParamFilter?.let { filter ->
            if (filter.isNotEmpty()) result = result.filter { matchesParamFilter(it, filter) }
        }

        return result
    }

    private fun matchesParamFilter(exercise: Exercise, filter: Map<String, List<String>>): Boolean {
        val params = exercise.exerciseParams ?: return false
        return filter.all { (key, allowedValues) ->
            val actual = params[key]?.toString()
            actual != null && allowedValues.any { it == actual }
        }
    }
}

private val LADDER_PICKER_MEMORY_EMOJI_POOL = listOf(
    "⭐", "❤️", "🔵", "🍎", "🍊", "🍋", "🍇", "🐶", "🐱", "🐰", "🐻", "🦊", "🐼", "🌙", "☀️", "🌧️", "⚡", "🔥", "💧", "🌿",
    "🎵", "🎮", "📚", "✏️", "🎯", "🏀", "⚽", "🎲", "🧩", "🔑", "🧠", "💡", "🌈", "🦋", "🍀", "🌻", "🍄", "🪐", "🎸", "🎹",
    "🚲", "⛵", "🎈", "🎁", "🏠", "🌍", "🧁", "🍪", "🥝", "🫐", "🦆", "🦉", "🐢", "🦀", "🐙", "🦩", "🪁", "🧿", "📌", "🧸"
)

private fun ladderPickerPickDistinctEmojis(pairCount: Int, rng: Random): List<String> {
    val shuffled = LADDER_PICKER_MEMORY_EMOJI_POOL.shuffled(rng)
    return shuffled.take(pairCount.coerceAtMost(LADDER_PICKER_MEMORY_EMOJI_POOL.size))
}

private fun ladderPickerMemoryPairCount(targetScore: Double): Int {
    return when {
        targetScore >= 0.78 -> 3
        targetScore >= 0.62 -> 4
        targetScore >= 0.48 -> 5
        else -> 6
    }
}

private data class LadderPickerSumPairParams(
    val statics: List<Int>,
    val pairs: Int,
    val minV: Int,
    val maxV: Int
)

private fun ladderPickerSumPairParams(targetScore: Double, rng: Random): LadderPickerSumPairParams {
    return when {
        targetScore >= 0.78 ->
            LadderPickerSumPairParams(
                statics = listOf(2, 3, 5).shuffled(rng).take(1),
                pairs = 3 + rng.nextInt(2),
                minV = 1,
                maxV = 30
            )
        targetScore >= 0.60 ->
            LadderPickerSumPairParams(
                statics = listOf(5, 7).shuffled(rng).take(1 + rng.nextInt(2)),
                pairs = 4 + rng.nextInt(2),
                minV = 1,
                maxV = 50
            )
        else ->
            LadderPickerSumPairParams(
                statics = listOf(2, 5, 10).shuffled(rng).take(2 + rng.nextInt(2)),
                pairs = 4 + rng.nextInt(3),
                minV = 1,
                maxV = 99
            )
    }
}

private fun ladderPickerMathOperandMaxes(targetScore: Double, rng: Random): Pair<Int, Int> {
    val cap = when {
        targetScore >= 0.78 -> 9
        targetScore >= 0.62 -> 20
        targetScore >= 0.48 -> 30
        else -> 99
    }
    val a = rng.nextInt(4, cap + 1)
    val b = rng.nextInt(4, cap + 1)
    return a to b
}

private fun nearestDifficultyForTarget(allowed: List<Difficulty>, targetScore: Double): Difficulty {
    val difficultyEaseMap = mapOf(
        Difficulty.ULTRA_EASY to 0.82,
        Difficulty.EASY to 0.72,
        Difficulty.MEDIUM to 0.60,
        Difficulty.HARD to 0.46,
        Difficulty.VERY_HARD to 0.35
    )
    return allowed.minByOrNull { difficulty ->
        kotlin.math.abs((difficultyEaseMap[difficulty] ?: 0.6) - targetScore)
    } ?: Difficulty.MEDIUM
}
