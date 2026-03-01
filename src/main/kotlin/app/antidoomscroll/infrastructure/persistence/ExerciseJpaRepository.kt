package app.antidoomscroll.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ExerciseJpaRepository : JpaRepository<ExerciseEntity, UUID> {

    @Query("SELECT e FROM ExerciseEntity e WHERE e.difficulty = 'ULTRA_EASY' ORDER BY e.createdAt ASC")
    fun findAllUltraEasy(): List<ExerciseEntity>

    @Query("SELECT e FROM ExerciseEntity e WHERE e.difficulty = 'ULTRA_EASY' AND e.subjectId = :subjectId ORDER BY e.createdAt ASC")
    fun findAllUltraEasyBySubjectId(subjectId: UUID): List<ExerciseEntity>

    @Query("SELECT e FROM ExerciseEntity e WHERE e.difficulty = 'ULTRA_EASY' AND e.type = :type ORDER BY e.createdAt ASC")
    fun findAllUltraEasyByType(type: String): List<ExerciseEntity>

    fun findBySubjectIdAndDifficultyOrderByCreatedAtAsc(
        subjectId: UUID,
        difficulty: String,
        pageable: Pageable
    ): List<ExerciseEntity>

    fun findBySubjectIdAndDifficultyInOrderByCreatedAtAsc(
        subjectId: UUID,
        difficulties: List<String>,
        pageable: Pageable
    ): List<ExerciseEntity>

    /**
     * JPQL so that all entity attributes (including exercise_params JSON) are properly loaded.
     * Native query bypasses JdbcTypeCode and leaves exercise_params null for N_BACK.
     * Call with page size large enough to allow shuffling for random order.
     */
    @Query("SELECT e FROM ExerciseEntity e WHERE e.difficulty IN ('ULTRA_EASY', 'EASY') ORDER BY e.createdAt")
    fun findUltraEasyOrEasy(pageable: Pageable): List<ExerciseEntity>

    @Query("SELECT e FROM ExerciseEntity e WHERE e.type = 'N_BACK' ORDER BY e.createdAt")
    fun findAllNBack(): List<ExerciseEntity>

    fun findBySubjectIdOrderByCreatedAtAsc(subjectId: UUID, pageable: Pageable): List<ExerciseEntity>

    /** Multi-subject difficulty query for combo ladder levels. */
    fun findBySubjectIdInAndDifficultyInOrderByCreatedAtAsc(
        subjectIds: List<UUID>,
        difficulties: List<String>,
        pageable: Pageable
    ): List<ExerciseEntity>

    /** All exercises matching any of the given difficulties (no subject filter). */
    fun findByDifficultyInOrderByCreatedAtAsc(
        difficulties: List<String>,
        pageable: Pageable
    ): List<ExerciseEntity>
}
