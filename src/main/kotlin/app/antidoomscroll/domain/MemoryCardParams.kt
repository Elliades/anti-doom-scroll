package app.antidoomscroll.domain

/**
 * Type-specific params for MEMORY_CARD_PAIRS exercises.
 * - pairCount: number of pairs (total cards = 2 * pairCount)
 * - symbols: one symbol per pair (e.g. emoji or letter); frontend duplicates each for the two cards and shuffles
 */
data class MemoryCardParams(
    val pairCount: Int,
    val symbols: List<String>
) {
    init {
        require(pairCount >= 2) { "pairCount must be >= 2" }
        require(symbols.size == pairCount) { "symbols size must equal pairCount" }
    }
}
