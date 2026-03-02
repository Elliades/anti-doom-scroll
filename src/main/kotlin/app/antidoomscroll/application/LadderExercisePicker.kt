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
 * 2. Otherwise → query by [LadderLevel.subjectCodes] + [LadderLevel.allowedDifficulties]:
 *    - One or more subjects: pool candidates from each, deduplicate.
 *    - Empty subjectCodes: query all subjects (no subject filter).
 * 3. For ESTIMATION subject: add generated parametric exercises (pure math, speed/time, budget/days) to the pool.
 * 4. Apply in-memory filters in order:
 *    a. [LadderLevel.allowedTypes]         — gate on ExerciseType
 *    b. [LadderLevel.exerciseParamFilter]  — generic key/value filter on exercise.exerciseParams
 *       (any-match per key; values compared as strings)
 * 5. Return one random exercise from the filtered pool, or null if empty.
 *
 * This component is the single place that knows how to translate a LadderLevel into
 * an exercise. Both [StartLadderSessionUseCase] and [GetNextLadderExerciseUseCase] delegate here.
 */
@Component
class LadderExercisePicker(
    private val exercisePort: ExercisePort,
    private val subjectPort: SubjectPort,
    private val estimationExerciseGenerator: EstimationExerciseGenerator
) {

    fun pick(config: LadderConfig, level: LadderLevel): Exercise? {
        val candidates = buildCandidatePool(level) + buildGeneratedEstimationPool(level)
        val filtered = applyFilters(candidates, level)
        return filtered.randomOrNull()
    }

    // ------------------------------------------------------------------
    // Pool construction
    // ------------------------------------------------------------------

    /** Generated ESTIMATION exercises (parametric math + word problems) for variety. */
    private fun buildGeneratedEstimationPool(level: LadderLevel): List<Exercise> {
        if (level.exerciseIds != null && level.exerciseIds.isNotEmpty()) return emptyList()
        if (!level.subjectCodes.contains("ESTIMATION")) return emptyList()
        val subject = subjectPort.findByCode("ESTIMATION") ?: return emptyList()
        val difficulty = level.allowedDifficulties.firstOrNull() ?: return emptyList()
        val baseSeed = Random.nextLong()
        val generated = estimationExerciseGenerator.generatePool(
            levelIndex = level.levelIndex,
            difficulty = difficulty,
            baseSeed = baseSeed,
            count = 8
        )
        return generated.mapIndexed { i, g ->
            toExercise(g, subject.id, difficulty, level.levelIndex, baseSeed + i)
        }
    }

    private fun toExercise(
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

    private fun buildCandidatePool(level: LadderLevel): List<Exercise> {
        if (level.exerciseIds != null && level.exerciseIds.isNotEmpty()) {
            return exercisePort.findByIds(level.exerciseIds)
        }

        val subjectIds = level.subjectCodes
            .mapNotNull { subjectPort.findByCode(it)?.id }

        // If subjectCodes were specified but none resolved → no candidates (likely config error)
        if (level.subjectCodes.isNotEmpty() && subjectIds.isEmpty()) return emptyList()

        return exercisePort.findRandomBySubjectsAndDifficulties(
            subjectIds = subjectIds,      // empty = all subjects
            difficulties = level.allowedDifficulties,
            limit = 20
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

    /**
     * Returns true when every key in [filter] has at least one matching value in
     * the exercise's exerciseParams map (string-compared).
     *
     * Example: filter = {"operation": ["ADD", "SUBTRACT"]}
     *   → exercise.exerciseParams["operation"] must be "ADD" or "SUBTRACT"
     */
    private fun matchesParamFilter(exercise: Exercise, filter: Map<String, List<String>>): Boolean {
        val params = exercise.exerciseParams ?: return false
        return filter.all { (key, allowedValues) ->
            val actual = params[key]?.toString()
            actual != null && allowedValues.any { it == actual }
        }
    }
}
