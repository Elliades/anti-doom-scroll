package app.antidoomscroll.application.port

import app.antidoomscroll.domain.Subject
import java.util.UUID

/**
 * Port: load subjects. Enables adding subjects and exercises without code change.
 */
interface SubjectPort {

    fun findByCode(code: String): Subject?

    fun findById(id: UUID): Subject?

    fun listAll(): List<Subject>
}
