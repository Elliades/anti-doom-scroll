package app.antidoomscroll.infrastructure.persistence

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "user_profile")
class UserProfileEntity {

    @Id
    @Column(name = "id", updatable = false)
    var id: UUID? = null

    @Column(name = "display_name")
    var displayName: String? = null

    @Column(name = "timezone_id", nullable = false)
    var timezoneId: String = "UTC"

    @Column(name = "session_default_seconds", nullable = false)
    var sessionDefaultSeconds: Int = 180

    @Column(name = "low_battery_mode_seconds", nullable = false)
    var lowBatteryModeSeconds: Int = 45

    @Column(name = "anonymous", nullable = false)
    var anonymous: Boolean = true

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null

    @PrePersist
    fun prePersist() {
        if (createdAt == null) createdAt = Instant.now()
        if (updatedAt == null) updatedAt = Instant.now()
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }
}
