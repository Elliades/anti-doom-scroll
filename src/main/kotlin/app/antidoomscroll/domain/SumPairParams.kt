package app.antidoomscroll.domain

/**
 * Type-specific params for SUM_PAIR exercises.
 * - staticNumbers: one or more static values K; user finds pairs (a, b) with a + K = b.
 *   Single element = one round; multiple = multiple rounds, one static displayed at a time.
 * - pairsPerRound: number of sum pairs per round (2 * pairsPerRound cards per round).
 * - minValue / maxValue: optional bounds for generated numbers (used by generator).
 */
data class SumPairParams(
    val staticNumbers: List<Int>,
    val pairsPerRound: Int,
    val minValue: Int = 1,
    val maxValue: Int = 99
) {
    init {
        require(staticNumbers.isNotEmpty()) { "staticNumbers must not be empty" }
        require(staticNumbers.all { it > 0 }) { "each static must be positive" }
        require(pairsPerRound >= 2) { "pairsPerRound must be >= 2" }
        require(minValue < maxValue) { "minValue must be < maxValue" }
    }

    val isMultiRound: Boolean get() = staticNumbers.size > 1
}
