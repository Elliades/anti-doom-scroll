package app.antidoomscroll.domain

/**
 * Exercise difficulty. Session flow: ULTRA_EASY -> EASY/MEDIUM -> optional HARD/VERY_HARD.
 */
enum class Difficulty {
    ULTRA_EASY,
    EASY,
    MEDIUM,
    HARD,
    VERY_HARD
}
