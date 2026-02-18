package app.antidoomscroll.domain

import java.time.Instant
import java.util.UUID

/**
 * Spaced repetition card (SM-2 simplified). Part of a Deck.
 */
data class Card(
    val id: UUID,
    val deckId: UUID,
    val front: String,
    val back: String,
    val easeFactor: Double,
    val intervalDays: Int,
    val repetitions: Int,
    val nextReviewAt: Instant?
)
