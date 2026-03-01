package app.antidoomscroll.domain

/**
 * State for a ladder mix session: alternates exercises between multiple ladders.
 * Advancement requires BOTH ladders to pass their level (avg >= minScoreToAdvance).
 * Demotion occurs if either ladder fails (avg < minScoreToStay).
 */
data class LadderMixState(
    val mixCode: String,
    val ladderCodes: List<String>,
    val currentLevelIndex: Int,
    val perLadderStates: Map<String, PerLadderState>,
    /** Index of the ladder to pick the next exercise from (0-based). */
    val nextLadderIndex: Int
) {
    init {
        require(ladderCodes.size >= 2) { "ladderCodes must have at least 2 ladders" }
        require(perLadderStates.keys == ladderCodes.toSet()) {
            "perLadderStates must have an entry for each ladder in ladderCodes"
        }
        require(nextLadderIndex in 0 until ladderCodes.size) { "nextLadderIndex must be valid" }
    }

    fun nextLadderCode(): String = ladderCodes[nextLadderIndex]

    fun withScoreAdded(ladderCode: String, score: Double, maxRecent: Int): LadderMixState {
        val current = perLadderStates[ladderCode] ?: return this
        val updated = current.withScoreAdded(score, maxRecent)
        return copy(
            perLadderStates = perLadderStates + (ladderCode to updated)
        )
    }

    /** Advance to next ladder index (alternate). */
    fun withNextLadder(): LadderMixState =
        copy(nextLadderIndex = (nextLadderIndex + 1) % ladderCodes.size)

    /** Advance both ladders to next level; clear recent scores. */
    fun withLevelAdvanced(): LadderMixState =
        copy(
            currentLevelIndex = currentLevelIndex + 1,
            perLadderStates = perLadderStates.mapValues { (_, s) -> s.withRecentCleared() }
        )

    /** Demote both ladders; clear recent scores. */
    fun withLevelDemoted(): LadderMixState =
        copy(
            currentLevelIndex = (currentLevelIndex - 1).coerceAtLeast(0),
            perLadderStates = perLadderStates.mapValues { (_, s) -> s.withRecentCleared() }
        )

    data class PerLadderState(
        val recentScores: List<Double>,
        val overallScoreSum: Double,
        val overallTotal: Int
    ) {
        fun withScoreAdded(score: Double, maxRecent: Int): PerLadderState =
            copy(
                recentScores = (recentScores + score).takeLast(maxRecent),
                overallScoreSum = overallScoreSum + score,
                overallTotal = overallTotal + 1
            )

        fun withRecentCleared(): PerLadderState =
            copy(recentScores = emptyList())

        fun averageRecent(): Double? =
            if (recentScores.isEmpty()) null else recentScores.average()
    }
}
