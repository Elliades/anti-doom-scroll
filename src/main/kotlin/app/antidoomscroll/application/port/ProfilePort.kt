package app.antidoomscroll.application.port

import app.antidoomscroll.domain.UserProfile
import java.util.UUID

/**
 * Port: resolve current profile (anonymous or authenticated).
 */
interface ProfilePort {

    fun findById(id: UUID): UserProfile?

    /**
     * Get or create anonymous local profile for MVP.
     */
    fun getOrCreateAnonymousProfile(): UserProfile
}
