package app.antidoomscroll.domain

/**
 * Type of exercise (flashcard, cloze, QCM, blurting, etc.).
 */
enum class ExerciseType {
    FLASHCARD_QA,
    CLOZE,
    QCM,
    KEYWORD_BLURTING,
    MINI_PROBLEM,
    N_BACK,
    N_BACK_GRID,
    DUAL_NBACK_GRID,
    DUAL_NBACK_CARD,
    MEMORY_CARD_PAIRS,
    SUM_PAIR,
    IMAGE_PAIR,
    ANAGRAM,
    WORDLE,
    ESTIMATION,
    DIGIT_SPAN,
    MATH_CHAIN,
    REMEMBER_NUMBER
}
