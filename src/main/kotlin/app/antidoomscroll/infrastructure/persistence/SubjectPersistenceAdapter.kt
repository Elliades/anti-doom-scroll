package app.antidoomscroll.infrastructure.persistence

import app.antidoomscroll.application.port.SubjectPort
import app.antidoomscroll.domain.Subject
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SubjectPersistenceAdapter(
    private val repository: SubjectJpaRepository
) : SubjectPort {

    override fun findByCode(code: String): Subject? =
        repository.findByCode(code)?.let(::toDomain)

    override fun findById(id: UUID): Subject? =
        repository.findById(id).orElse(null)?.let(::toDomain)

    override fun listAll(): List<Subject> =
        repository.findAll().map(::toDomain)

    private fun toDomain(e: SubjectEntity): Subject =
        Subject(
            id = e.id!!,
            code = e.code,
            name = e.name,
            description = e.description,
            parentSubjectId = e.parentSubjectId,
            scoringConfig = e.scoringConfig,
            createdAt = e.createdAt!!
        )
}
