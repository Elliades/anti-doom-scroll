package app.antidoomscroll.infrastructure.persistence

import app.antidoomscroll.application.port.ExercisePort
import app.antidoomscroll.application.port.SubjectPort
import app.antidoomscroll.domain.Difficulty
import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.ExerciseType
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ExercisePersistenceAdapter(
    private val repository: ExerciseJpaRepository,
    private val subjectPort: SubjectPort,
    private val jdbcTemplate: JdbcTemplate
) : ExercisePort {

    override fun findById(id: UUID): Exercise? =
        repository.findById(id).orElse(null)?.let(::toDomain)

    override fun findByIds(ids: List<UUID>): List<Exercise> =
        if (ids.isEmpty()) emptyList()
        else repository.findAllById(ids).map(::toDomain)

    @Cacheable(
        value = ["ultraEasyExercise"],
        key = "#subjectCode != null ? #subjectCode : 'any'",
        unless = "#result == null || #result.type == T(app.antidoomscroll.domain.ExerciseType).N_BACK"
    )
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

    override fun findRandomBySubjectAndDifficulties(subjectId: UUID, difficulties: List<Difficulty>, limit: Int): List<Exercise> {
        if (difficulties.isEmpty()) return emptyList()
        val diffStrings = difficulties.map { it.name }
        val pageSize = (limit * 5).coerceAtLeast(20)
        val list = repository.findBySubjectIdAndDifficultyInOrderByCreatedAtAsc(
            subjectId,
            diffStrings,
            PageRequest.of(0, pageSize)
        )
        return list.shuffled().take(limit).map(::toDomain)
    }

    override fun findRandomUltraEasyOrEasy(limit: Int): List<Exercise> {
        val pageSize = (limit * 10).coerceAtLeast(50)
        val list = repository.findUltraEasyOrEasy(PageRequest.of(0, pageSize))
        return list.shuffled().take(limit).map(::toDomain)
    }

    @Cacheable(value = ["nBackByLevel"], key = "#level", unless = "#result == null")
    override fun findNBackByLevel(level: Int): Exercise? {
        if (level !in 1..3) return null
        return repository.findAllNBack()
            .map(::toDomain)
            .firstOrNull { it.nBackParams()?.n == level }
    }

    override fun findExerciseParamsById(id: UUID): Map<String, Any?>? {
        val list = jdbcTemplate.query(
            "SELECT exercise_params FROM exercise WHERE id = ?",
            { rs, _ ->
                rs.getString(1) ?: run {
                    val obj = rs.getObject(1) ?: return@query null
                    if (obj is String) obj else obj.toString()
                }
            },
            id
        )
        val json = list.firstOrNull() ?: return null
        return runCatching {
            ObjectMapper().readValue(json, object : TypeReference<Map<String, Any?>>() {})
        }.getOrNull()
    }

    override fun findBySubjectId(subjectId: UUID, limit: Int?): List<Exercise> {
        val pageable = if (limit != null) PageRequest.of(0, limit) else PageRequest.of(0, Int.MAX_VALUE)
        return repository.findBySubjectIdOrderByCreatedAtAsc(subjectId, pageable).map(::toDomain)
    }

    override fun findBySubjectIdIncludingParent(subjectId: UUID, parentSubjectId: UUID?, limit: Int?): List<Exercise> {
        val cap = limit ?: 100
        val own = repository.findBySubjectIdOrderByCreatedAtAsc(subjectId, PageRequest.of(0, cap)).map(::toDomain)
        if (parentSubjectId == null) return own
        val parentIds = own.map { it.id }.toSet()
        val parentExs = repository.findBySubjectIdOrderByCreatedAtAsc(parentSubjectId, PageRequest.of(0, cap))
            .map(::toDomain)
            .filter { it.id !in parentIds }
        return (own + parentExs).distinctBy { it.id }.take(cap)
    }

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
