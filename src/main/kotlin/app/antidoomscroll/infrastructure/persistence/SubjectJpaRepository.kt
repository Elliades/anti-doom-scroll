package app.antidoomscroll.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SubjectJpaRepository : JpaRepository<SubjectEntity, UUID> {

    fun findByCode(code: String): SubjectEntity?
}
