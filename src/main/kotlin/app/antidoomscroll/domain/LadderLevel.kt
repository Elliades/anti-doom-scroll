package app.antidoomscroll.domain

import java.util.UUID

/**
 * Definition of one level in a ladder.
 *
 * Exercise selection priority:
 * 1. [exerciseIds] — fixed set of exercises (exact IDs from DB)
 * 2. Otherwise: query by [subjectCodes] + [allowedDifficulties], then filter in memory by
 *    [allowedTypes] and [exerciseParamFilter].
 *
 * [subjectCodes] — one or more subjects to pool candidates from.
 *   Empty list = pick from ANY subject (cross-subject combo levels).
 *
 * [exerciseParamFilter] — generic param filter applied to exercise.exerciseParams.
 *   Keys are exerciseParams JSON keys; values are the allowed string values (any-match).
 *   Example: {"operation": ["ADD", "SUBTRACT"]}  →  only ADD or SUBTRACT math exercises.
 *   Example: {"n": ["2"]}                         →  only 2-back N-back exercises.
 *   Example: {"language": ["fr"]}                 →  only French anagrams.
 *
 * [allowedTypes] — optional gate on ExerciseType (e.g. only N_BACK from a multi-type subject).
 */
data class LadderLevel(
    val levelIndex: Int,
    val allowedDifficulties: List<Difficulty>,
    val subjectCodes: List<String> = emptyList(),
    val exerciseIds: List<UUID>? = null,
    val exerciseParamFilter: Map<String, List<String>>? = null,
    val allowedTypes: List<ExerciseType>? = null
) {
    init {
        require(levelIndex >= 0) { "levelIndex must be >= 0" }
        if (exerciseIds == null || exerciseIds.isEmpty()) {
            require(allowedDifficulties.isNotEmpty()) { "allowedDifficulties required when no exerciseIds" }
        }
    }
}
