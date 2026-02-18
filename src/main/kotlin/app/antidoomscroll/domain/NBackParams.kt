package app.antidoomscroll.domain

/**
 * Type-specific params for N-Back exercises.
 * - n: match current item with item N positions back (1-back = previous)
 * - sequence: stimuli to display one-by-one (letters or numbers)
 * - matchIndices: 0-based positions where current == item at (current - n)
 */
data class NBackParams(
    val n: Int,
    val sequence: List<String>,
    val matchIndices: List<Int>
) {
    init {
        require(n >= 1) { "n must be >= 1" }
        require(sequence.size >= n + 2) { "sequence must have at least n+2 items" }
        require(matchIndices.all { it >= n && it < sequence.size }) { "matchIndices must be valid positions" }
    }

    /**
     * Validates user responses (positions where user tapped "match").
     * Returns (hits, misses, falseAlarms) for scoring.
     * Never punishes harshly: use accuracy and small bonus for hits.
     */
    fun evaluate(userMatchPositions: Set<Int>): NBackScore {
        val matchSet = matchIndices.toSet()
        val hits = userMatchPositions.intersect(matchSet).size
        val misses = matchSet.size - hits
        val falseAlarms = userMatchPositions.minus(matchSet).size
        return NBackScore(
            hits = hits,
            misses = misses,
            falseAlarms = falseAlarms,
            totalTargets = matchSet.size,
            totalResponses = userMatchPositions.size
        )
    }
}

data class NBackScore(
    val hits: Int,
    val misses: Int,
    val falseAlarms: Int,
    val totalTargets: Int,
    val totalResponses: Int
) {
    /** MVP: accuracy = hits / totalTargets. Never negative. */
    fun accuracy(): Double =
        if (totalTargets == 0) 1.0 else (hits.toDouble() / totalTargets).coerceIn(0.0, 1.0)

    /** Simple score 0..1: reward hits, small penalty for false alarms. No harsh punishment. */
    fun normalizedScore(): Double {
        if (totalTargets == 0) return 1.0
        val hitBonus = hits.toDouble() / totalTargets
        val falseAlarmPenalty = (falseAlarms * 0.1).coerceAtMost(0.3) // cap penalty
        return (hitBonus - falseAlarmPenalty).coerceIn(0.0, 1.0)
    }
}
