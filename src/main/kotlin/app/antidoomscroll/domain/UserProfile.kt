package app.antidoomscroll.domain

import java.time.ZoneId
import java.util.UUID

/**
 * User or anonymous profile: goals, daily axes, risk windows, preferences.
 * MVP: anonymous local profile supported.
 */
data class UserProfile(
    val id: UUID,
    val displayName: String?,
    val timezone: ZoneId,
    val dailyAxes: List<String>,
    val sessionDefaultSeconds: Int,
    val lowBatteryModeSeconds: Int,
    val anonymous: Boolean
)
