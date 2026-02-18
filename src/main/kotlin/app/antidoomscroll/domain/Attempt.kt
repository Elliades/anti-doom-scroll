package app.antidoomscroll.domain

import java.time.Instant
import java.util.UUID

/**
 * A single attempt at an exercise. UTC timestamps.
 * Scoring: accuracy, speed, confidence; no harsh punishment.
 */
data class Attempt(
    val id: UUID,
    val exerciseId: UUID,
    val profileId: UUID,
    val response: String?,
    val correct: Boolean,
    val reactionTimeMs: Long?,
    val confidencePercent: Int?,
    val score: Double,
    val attemptedAt: Instant
)
