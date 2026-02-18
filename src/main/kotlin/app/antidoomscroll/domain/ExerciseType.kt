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
    N_BACK
}
