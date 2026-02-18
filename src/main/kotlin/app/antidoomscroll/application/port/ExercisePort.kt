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
     * Random ultra-easy or easy exercises from all subjects (for openapp session).
     * @param limit max number to return (e.g. 3).
     */
    fun findRandomUltraEasyOrEasy(limit: Int): List<Exercise>
}
