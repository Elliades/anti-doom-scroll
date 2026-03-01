package app.antidoomscroll.domain

/**
 * Params for dual N-back with cards: match suit (color) or rank (number).
 * - matchColorIndices: suit matches N back (e.g. both Clubs)
 * - matchNumberIndices: rank matches N back (e.g. both 2)
 */
data class DualNBackCardParams(
    val n: Int,
    val sequence: List<String>,
    val matchColorIndices: List<Int>,
    val matchNumberIndices: List<Int>
) {
    init {
        require(n >= 1) { "n must be >= 1" }
        require(sequence.size >= n + 2) { "sequence must have at least n+2 items" }
        val validRange = n until sequence.size
        require(matchColorIndices.all { it in validRange }) { "matchColorIndices must be valid" }
        require(matchNumberIndices.all { it in validRange }) { "matchNumberIndices must be valid" }
    }

    fun evaluate(userColorResponses: Set<Int>, userNumberResponses: Set<Int>): DualNBackScore {
        val colorSet = matchColorIndices.toSet()
        val numberSet = matchNumberIndices.toSet()
        val colorHits = userColorResponses.intersect(colorSet).size
        val colorMisses = colorSet.size - colorHits
        val colorFalseAlarms = userColorResponses.minus(colorSet).size
        val numberHits = userNumberResponses.intersect(numberSet).size
        val numberMisses = numberSet.size - numberHits
        val numberFalseAlarms = userNumberResponses.minus(numberSet).size
        return DualNBackScore(
            positionHits = numberHits,
            positionMisses = numberMisses,
            positionFalseAlarms = numberFalseAlarms,
            positionTargets = numberSet.size,
            colorHits = colorHits,
            colorMisses = colorMisses,
            colorFalseAlarms = colorFalseAlarms,
            colorTargets = colorSet.size
        )
    }
}
