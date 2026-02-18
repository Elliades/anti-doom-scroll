package app.antidoomscroll.infrastructure.persistence

import app.antidoomscroll.application.port.ProfilePort
import app.antidoomscroll.domain.UserProfile
import org.springframework.stereotype.Component
import java.time.ZoneId
import java.util.UUID

@Component
class ProfilePersistenceAdapter(
    private val repository: UserProfileJpaRepository
) : ProfilePort {

    override fun findById(id: UUID): UserProfile? =
        repository.findById(id).orElse(null)?.let(::toDomain)

    override fun getOrCreateAnonymousProfile(): UserProfile {
        val anonymousId = UUID.fromString("00000000-0000-0000-0000-000000000000")
        return repository.findById(anonymousId)
            .orElse(null)
            ?.let(::toDomain)
            ?: run {
                val e = UserProfileEntity().apply {
                    this.id = anonymousId
                    displayName = "Guest"
                    timezoneId = "UTC"
                    sessionDefaultSeconds = DEFAULT_SESSION_SECONDS
                    lowBatteryModeSeconds = LOW_BATTERY_SECONDS
                    anonymous = true
                }
                repository.save(e)
                toDomain(e)
            }
    }

    private fun toDomain(e: UserProfileEntity): UserProfile =
        UserProfile(
            id = e.id!!,
            displayName = e.displayName,
            timezone = ZoneId.of(e.timezoneId),
            dailyAxes = emptyList(),
            sessionDefaultSeconds = e.sessionDefaultSeconds,
            lowBatteryModeSeconds = e.lowBatteryModeSeconds,
            anonymous = e.anonymous
        )

    companion object {
        private const val DEFAULT_SESSION_SECONDS = 180
        private const val LOW_BATTERY_SECONDS = 45
    }
}
