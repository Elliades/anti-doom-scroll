package app.antidoomscroll.application.port

import app.antidoomscroll.domain.Difficulty
import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.ExerciseType
import java.util.UUID

/**
 * Port: load exercises (e.g. from DB or cache). Ultra-easy must be fast.
 * Exercises belong to subjects; query by subject for scaling.
 */
interface ExercisePort {

    fun findById(id: UUID): Exercise?

    /** Find exercises by IDs (for ladder levels with fixed exercise sets). */
    fun findByIds(ids: List<UUID>): List<Exercise>

    /**
     * Load one ultra-easy exercise for instant reopen. Should be cached.
     * @param subjectCode if null, returns first ultra-easy from any subject.
     */
    fun findOneUltraEasy(subjectCode: String? = null): Exercise?

    /**
     * Load one ultra-easy exercise of a specific type. Used for preferType in session.
     */
    fun findOneUltraEasyByType(type: ExerciseType): Exercise?

    /**
     * Exercises by subject and difficulty (for session step 2).
     */
    fun findBySubjectAndDifficulty(subjectId: UUID, difficulty: Difficulty, limit: Int): List<Exercise>

    /**
     * Random exercise from subject matching any of the given difficulties (for ladder levels).
     */
    fun findRandomBySubjectAndDifficulties(subjectId: UUID, difficulties: List<Difficulty>, limit: Int = 1): List<Exercise>

    /**
     * Random exercises from multiple subjects matching any of the given difficulties.
     * Used by ladder levels that combine exercises from different subjects.
     * If [subjectIds] is empty, returns exercises from ALL subjects (no subject filter).
     */
    fun findRandomBySubjectsAndDifficulties(subjectIds: List<UUID>, difficulties: List<Difficulty>, limit: Int = 20): List<Exercise>

    /**
     * Random ultra-easy or easy exercises from all subjects (for openapp session).
     * @param limit max number to return (e.g. 3).
     */
    fun findRandomUltraEasyOrEasy(limit: Int): List<Exercise>

    /**
     * N-back exercise by level (1 = 1-back, 2 = 2-back, 3 = 3-back). Returns null if none.
     */
    fun findNBackByLevel(level: Int): Exercise?

    /**
     * Raw exercise_params JSON for an exercise (by id). Used when entity has null params (e.g. H2/dialect).
     */
    fun findExerciseParamsById(id: UUID): Map<String, Any?>?

    /**
     * List exercises for a subject (for subject detail / dedicated play). Ordered by createdAt.
     * @param limit max count; if null, returns all (use with care).
     */
    fun findBySubjectId(subjectId: UUID, limit: Int? = 50): List<Exercise>

    /**
     * List exercises for a subject including parent subject's exercises (for hierarchy).
     * When subject has a parent, returns own exercises + parent's exercises.
     */
    fun findBySubjectIdIncludingParent(subjectId: UUID, parentSubjectId: UUID?, limit: Int? = 100): List<Exercise>
}
