package app.antidoomscroll.infrastructure.persistence

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "exercise")
class ExerciseEntity {

    @Id
    @Column(name = "id", updatable = false)
    var id: UUID? = null

    @Column(name = "subject_id", nullable = false)
    var subjectId: UUID? = null

    @Column(name = "type", nullable = false, length = 32)
    var type: String = ""

    @Column(name = "difficulty", nullable = false, length = 32)
    var difficulty: String = ""

    @Column(name = "prompt", nullable = false, columnDefinition = "TEXT")
    var prompt: String = ""

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "expected_answers", columnDefinition = "jsonb")
    var expectedAnswers: List<String>? = null

    @Column(name = "time_limit_seconds", nullable = false)
    var timeLimitSeconds: Int = 60

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "exercise_params", columnDefinition = "jsonb")
    var exerciseParams: Map<String, Any?>? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null

    @PrePersist
    fun prePersist() {
        if (createdAt == null) createdAt = Instant.now()
    }
}
