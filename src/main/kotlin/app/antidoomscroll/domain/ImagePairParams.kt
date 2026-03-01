package app.antidoomscroll.domain

/**
 * Type-specific params for IMAGE_PAIR exercises.
 * - pairCount: number of pairs (total cards = 2 * pairCount).
 * - maxPairsPerBackground: at most this many pairs share the same background (duplicate cap).
 * - colorCount: 0 = no background color; 1 = no color + 1 color (2 backgrounds); etc. Number of background types = colorCount + 1.
 * Matching: two cards match iff they have the same background and the same image.
 */
data class ImagePairParams(
    val pairCount: Int,
    val maxPairsPerBackground: Int,
    val colorCount: Int
) {
    init {
        require(pairCount >= 2) { "pairCount must be >= 2" }
        require(maxPairsPerBackground >= 1) { "maxPairsPerBackground must be >= 1" }
        require(colorCount >= 0) { "colorCount must be >= 0" }
        val numBackgrounds = colorCount + 1
        require(numBackgrounds * maxPairsPerBackground >= pairCount) {
            "Need at least ceil($pairCount / $maxPairsPerBackground) backgrounds; have $numBackgrounds (colorCount+1)"
        }
    }

    val numBackgrounds: Int get() = colorCount + 1
}
