package app.antidoomscroll.domain

/**
 * Params for grid N-back: stimuli are grid positions (0-based, row-major).
 * - n: match current position with position N steps back
 * - sequence: grid cell indices (0..gridSize²-1)
 * - matchIndices: positions where sequence[i] == sequence[i - n]
 */
data class NBackGridParams(
    val n: Int,
    val sequence: List<Int>,
    val matchIndices: List<Int>,
    val gridSize: Int = 3
) {
    init {
        require(n >= 1) { "n must be >= 1" }
        require(gridSize in 2..5) { "gridSize must be 2..5" }
        val maxPos = gridSize * gridSize - 1
        require(sequence.size >= n + 2) { "sequence must have at least n+2 items" }
        require(sequence.all { it in 0..maxPos }) { "sequence values must be 0..$maxPos" }
        require(matchIndices.all { it >= n && it < sequence.size }) { "matchIndices must be valid positions" }
    }

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

