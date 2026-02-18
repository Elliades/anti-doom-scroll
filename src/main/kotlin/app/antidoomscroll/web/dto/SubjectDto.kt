package app.antidoomscroll.web.dto

import java.util.UUID

data class SubjectDto(
    val id: UUID,
    val code: String,
    val name: String,
    val description: String?,
    val parentSubjectId: UUID?,
    val scoringConfig: SubjectScoringConfigDto
)

data class SubjectScoringConfigDto(
    val accuracyType: String,
    val speedTargetMs: Long?,
    val confidenceWeight: Double,
    val streakBonusCap: Double,
    val partialMatchThreshold: Double?
)
