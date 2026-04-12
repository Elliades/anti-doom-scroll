package app.antidoomscroll.domain

/**
 * Working-memory digit span: memorize a sequence, recall in order, then 1–3 extra recall modes.
 */
enum class DigitSpanTaskKind {
    /** Same order as presented */
    FORWARD_ORDER,
    ASCENDING,
    DESCENDING,
    /** Even digits (ascending), then odd digits (ascending) */
    EVEN_THEN_ODD,
    /** Odd digits (ascending), then even digits (ascending) */
    ODD_THEN_EVEN,
    /** Positions 0, 2, 4, … in original order */
    EVERY_OTHER_FROM_FIRST,
    /** Positions 1, 3, 5, … in original order */
    EVERY_OTHER_FROM_SECOND
}

data class DigitSpanParams(
    val sequence: List<Int>,
    val displaySeconds: Int,
    val tasks: List<DigitSpanTaskKind>
)
