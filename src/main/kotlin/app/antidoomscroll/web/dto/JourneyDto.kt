package app.antidoomscroll.web.dto

/**
 * Journey definition: ordered steps. Frontend can navigate to any stepIndex.
 */
data class JourneyDto(
    val code: String,
    val name: String?,
    val steps: List<JourneyStepDefDto>
)

data class JourneyStepDefDto(
    val stepIndex: Int,
    val type: String,
    val config: Map<String, Any?> = emptyMap()
)
