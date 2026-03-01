package app.antidoomscroll.infrastructure.persistence

import app.antidoomscroll.domain.SubjectScoringConfig
import app.antidoomscroll.infrastructure.persistence.converter.SubjectScoringConfigConverter
import jakarta.persistence.*
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

    @Convert(converter = SubjectScoringConfigConverter::class)
    @Column(name = "scoring_config", nullable = false, length = 500)
    var scoringConfig: SubjectScoringConfig = SubjectScoringConfig()

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null

    @PrePersist
    fun prePersist() {
        if (createdAt == null) createdAt = Instant.now()
    }
}
