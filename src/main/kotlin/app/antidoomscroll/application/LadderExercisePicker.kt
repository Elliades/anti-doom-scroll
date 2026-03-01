package app.antidoomscroll.application

import app.antidoomscroll.application.port.ExercisePort
import app.antidoomscroll.application.port.SubjectPort
import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.LadderConfig
import app.antidoomscroll.domain.LadderLevel
import org.springframework.stereotype.Component

/**
 * Selects the next exercise for a ladder level.
 *
 * Selection pipeline:
 * 1. If [LadderLevel.exerciseIds] is set → fixed pool, skip DB query.
 * 2. Otherwise → query by [LadderLevel.subjectCodes] + [LadderLevel.allowedDifficulties]:
 *    - One or more subjects: pool candidates from each, deduplicate.
 *    - Empty subjectCodes: query all subjects (no subject filter).
 * 3. Apply in-memory filters in order:
 *    a. [LadderLevel.allowedTypes]         — gate on ExerciseType
 *    b. [LadderLevel.exerciseParamFilter]  — generic key/value filter on exercise.exerciseParams
 *       (any-match per key; values compared as strings)
 * 4. Return one random exercise from the filtered pool, or null if empty.
 *
 * This component is the single place that knows how to translate a LadderLevel into
 * an exercise. Both [StartLadderSessionUseCase] and [GetNextLadderExerciseUseCase] delegate here.
 */
@Component
class LadderExercisePicker(
    private val exercisePort: ExercisePort,
    private val subjectPort: SubjectPort
) {

    fun pick(config: LadderConfig, level: LadderLevel): Exercise? {
        val candidates = buildCandidatePool(level)
        val filtered = applyFilters(candidates, level)
        return filtered.randomOrNull()
    }

    // ------------------------------------------------------------------
    // Pool construction
    // ------------------------------------------------------------------

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
