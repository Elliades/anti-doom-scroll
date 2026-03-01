package app.antidoomscroll.domain

/**
 * Mutable ladder session state.
 * Carried by the client and sent back on each "next exercise" request.
 */
data class LadderState(
    val ladderCode: String,
    val currentLevelIndex: Int,
    val recentScores: List<Double>,
    val overallScoreSum: Double,
    val overallTotal: Int
) {
    /** Average of recent scores (current level), or null if empty. */
    fun currentLevelScorePercent(): Double? =
        if (recentScores.isEmpty()) null else recentScores.average()

    /** Overall score as fraction 0–1 (journey-wide average). */
    fun overallScorePercent(): Double =
        if (overallTotal == 0) 0.0 else overallScoreSum / overallTotal

    fun withScoreAdded(score: Double, maxRecent: Int): LadderState =
        copy(
            recentScores = (recentScores + score).takeLast(maxRecent),
            overallScoreSum = overallScoreSum + score,
            overallTotal = overallTotal + 1
        )

    fun withLevelChanged(newLevelIndex: Int): LadderState =
        copy(currentLevelIndex = newLevelIndex, recentScores = emptyList())
}
