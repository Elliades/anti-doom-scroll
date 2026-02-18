package app.antidoomscroll.infrastructure.persistence

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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

    @Query(
        value = "SELECT * FROM exercise WHERE difficulty IN ('ULTRA_EASY', 'EASY') ORDER BY RANDOM() LIMIT :limit",
        nativeQuery = true
    )
    fun findRandomUltraEasyOrEasy(@Param("limit") limit: Int): List<ExerciseEntity>
}
