package app.antidoomscroll.web.dto

/**
 * Response for start-session: profile id, steps, session lengths.
 */
data class SessionResponseDto(
    val profileId: String,
    val steps: List<SessionStepDto>,
    val sessionDefaultSeconds: Int,
    val lowBatteryModeSeconds: Int
)
