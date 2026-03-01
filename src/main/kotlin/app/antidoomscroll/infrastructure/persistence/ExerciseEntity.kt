package app.antidoomscroll.infrastructure.persistence

import app.antidoomscroll.infrastructure.persistence.converter.JsonListStringConverter
import app.antidoomscroll.infrastructure.persistence.converter.JsonMapConverter
import jakarta.persistence.*
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

    @Convert(converter = JsonListStringConverter::class)
    @Column(name = "expected_answers", columnDefinition = "jsonb")
    var expectedAnswers: List<String>? = null

    @Column(name = "time_limit_seconds", nullable = false)
    var timeLimitSeconds: Int = 60

    @Convert(converter = JsonMapConverter::class)
    @Column(name = "exercise_params", columnDefinition = "jsonb")
    var exerciseParams: Map<String, Any?>? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null

    @PrePersist
    fun prePersist() {
        if (createdAt == null) createdAt = Instant.now()
    }
}
