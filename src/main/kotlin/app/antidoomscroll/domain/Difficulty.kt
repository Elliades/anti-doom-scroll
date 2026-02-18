package app.antidoomscroll.domain

/**
 * Exercise difficulty. Session flow: ULTRA_EASY -> EASY/MEDIUM -> optional HARD.
 */
enum class Difficulty {
    ULTRA_EASY,
    EASY,
    MEDIUM,
    HARD
}
