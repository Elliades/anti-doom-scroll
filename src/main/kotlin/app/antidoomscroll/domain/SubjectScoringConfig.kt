package app.antidoomscroll.domain

/**
 * Scoring rules for a subject (from .cursorrules: accuracy, speed, confidence, streak; no harsh punishment).
 * Stored per subject so new subjects can be added without code changes.
 */
data class SubjectScoringConfig(
    /** How accuracy is computed: binary (1/0), partial (e.g. cloze by slot), weighted (essentials count 2). */
    val accuracyType: AccuracyType = AccuracyType.BINARY,
    /** Target time in ms for speed bonus; speed = min(1, target/actual). */
    val speedTargetMs: Long? = null,
    /** Weight for confidence calibration (0 = ignore). */
    val confidenceWeight: Double = 0.0,
    /** Cap for streak bonus (small, never punish). */
    val streakBonusCap: Double = 0.1,
    /** For PARTIAL: score per correct slot or threshold. */
    val partialMatchThreshold: Double? = null
) {
    enum class AccuracyType { BINARY, PARTIAL, WEIGHTED }
}
