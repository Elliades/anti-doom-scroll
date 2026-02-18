package app.antidoomscroll.domain

import java.time.Instant
import java.util.UUID

/**
 * A subject (e.g. A1 Flashcards, B1 N-back). Groups exercises and defines scoring.
 * Hierarchy: parentSubjectId for grouping (e.g. A → A1, A2).
 */
data class Subject(
    val id: UUID,
    val code: String,
    val name: String,
    val description: String?,
    val parentSubjectId: UUID?,
    val scoringConfig: SubjectScoringConfig,
    val createdAt: Instant
)
