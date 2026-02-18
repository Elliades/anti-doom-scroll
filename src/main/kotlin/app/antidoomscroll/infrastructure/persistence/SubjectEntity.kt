package app.antidoomscroll.infrastructure.persistence

import app.antidoomscroll.domain.SubjectScoringConfig
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "subject")
class SubjectEntity {

    @Id
    @Column(name = "id", updatable = false)
    var id: UUID? = null

    @Column(name = "code", nullable = false, unique = true, length = 64)
    var code: String = ""

    @Column(name = "name", nullable = false)
    var name: String = ""

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null

    @Column(name = "parent_subject_id")
    var parentSubjectId: UUID? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scoring_config", nullable = false, columnDefinition = "jsonb")
    var scoringConfig: SubjectScoringConfig = SubjectScoringConfig()

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null

    @PrePersist
    fun prePersist() {
        if (createdAt == null) createdAt = Instant.now()
    }
}
