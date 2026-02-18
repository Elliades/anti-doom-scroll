package app.antidoomscroll.domain

import java.time.Instant
import java.util.UUID

/**
 * Plan for a day: selected axes + queued exercise ids.
 */
data class DailyPlan(
    val id: UUID,
    val profileId: UUID,
    val planDate: Instant,
    val axes: List<String>,
    val queuedExerciseIds: List<UUID>
)
