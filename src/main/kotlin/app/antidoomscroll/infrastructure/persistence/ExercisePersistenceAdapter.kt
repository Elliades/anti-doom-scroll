package app.antidoomscroll.infrastructure.persistence

import app.antidoomscroll.application.port.ExercisePort
import app.antidoomscroll.application.port.SubjectPort
import app.antidoomscroll.domain.Difficulty
import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.ExerciseType
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ExercisePersistenceAdapter(
    private val repository: ExerciseJpaRepository,
    private val subjectPort: SubjectPort
) : ExercisePort {

    override fun findById(id: UUID): Exercise? =
        repository.findById(id).orElse(null)?.let(::toDomain)

    @Cacheable(value = ["ultraEasyExercise"], key = "#subjectCode != null ? #subjectCode : 'any'", unless = "#result == null")
    override fun findOneUltraEasy(subjectCode: String?): Exercise? {
        val list = if (subjectCode != null) {
            subjectPort.findByCode(subjectCode)?.id?.let { repository.findAllUltraEasyBySubjectId(it) } ?: emptyList()
        } else {
            repository.findAllUltraEasy()
        }
        return list.firstOrNull()?.let(::toDomain)
    }

    @org.springframework.cache.annotation.Cacheable(
        value = ["ultraEasyExerciseByType"],
        key = "#type.name",
        unless = "#result == null"
    )
    override fun findOneUltraEasyByType(type: app.antidoomscroll.domain.ExerciseType): Exercise? =
        repository.findAllUltraEasyByType(type.name).firstOrNull()?.let(::toDomain)

    override fun findBySubjectAndDifficulty(subjectId: UUID, difficulty: Difficulty, limit: Int): List<Exercise> =
        repository.findBySubjectIdAndDifficultyOrderByCreatedAtAsc(
            subjectId,
            difficulty.name,
            PageRequest.of(0, limit)
        ).map(::toDomain)

    override fun findRandomUltraEasyOrEasy(limit: Int): List<Exercise> =
        repository.findRandomUltraEasyOrEasy(limit).map(::toDomain)

    private fun toDomain(e: ExerciseEntity): Exercise =
        Exercise(
            id = e.id!!,
            subjectId = e.subjectId!!,
            type = ExerciseType.valueOf(e.type),
            difficulty = Difficulty.valueOf(e.difficulty),
            prompt = e.prompt,
            expectedAnswers = e.expectedAnswers ?: emptyList(),
            timeLimitSeconds = e.timeLimitSeconds,
            exerciseParams = e.exerciseParams
        )
}
