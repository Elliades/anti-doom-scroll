package app.antidoomscroll.application

import app.antidoomscroll.domain.ImagePairParams
import org.springframework.stereotype.Component
import kotlin.random.Random

/** Background colors for image-pair cards; index 0 = no color, 1..n = palette. */
private val IMAGE_PAIR_BG_COLORS = listOf(
    null,  // backgroundId 0 = no color
    "#3b82f6",
    "#22c55e",
    "#f59e0b",
    "#8b5cf6",
    "#ec4899"
)

/** Pool of image codes (e.g. animal emojis) for generating pairs. */
private val IMAGE_POOL = listOf(
    "🐶", "🐱", "🐰", "🐻", "🦊", "🐼", "🐨", "🐯", "🦁", "🐮", "🐷", "🐸",
    "🐵", "🐔", "🐧", "🐦", "🐤", "🦆", "🦅", "🦉", "🐴", "🦄", "🐝", "🐛"
)

/**
 * Generates IMAGE_PAIR deck: pairs of cards that match by (background + image).
 * Only cards with the same background and same image form a valid pair.
 * Respects maxPairsPerBackground (at most that many pairs share one background).
 */
@Component
class ImagePairGenerator {

    /**
     * Produces a shuffled deck of 2 * pairCount cards. Each card has backgroundId, imageId, and optional backgroundColorHex.
     * Matching rule: same backgroundId and same imageId.
     */
    fun generate(params: ImagePairParams, random: Random = Random.Default): ImagePairResult {
        val n = params.pairCount
        val maxPerBg = params.maxPairsPerBackground
        val numBackgrounds = params.numBackgrounds

        // Assign each pair to a background so that no background has more than maxPerBg pairs
        val backgroundAssignments = assignBackgrounds(n, numBackgrounds, maxPerBg, random)

        // Pick n distinct images from pool
        require(IMAGE_POOL.size >= n) { "Image pool size ${IMAGE_POOL.size} < pairCount $n" }
        val chosenImages = IMAGE_POOL.shuffled(random).take(n)

        val deck = mutableListOf<ImagePairCard>()
        for (i in 0 until n) {
            val bgId = backgroundAssignments[i]
            val imageId = chosenImages[i]
            val colorHex = if (bgId == 0) null else IMAGE_PAIR_BG_COLORS.getOrNull(bgId) ?: "#888888"
            deck.add(ImagePairCard(backgroundId = bgId, imageId = imageId, backgroundColorHex = colorHex))
            deck.add(ImagePairCard(backgroundId = bgId, imageId = imageId, backgroundColorHex = colorHex))
        }
        return ImagePairResult(deck = deck.shuffled(random))
    }

    /**
     * Assigns each of [pairCount] pairs to a background in [0, numBackgrounds-1]
     * so that no background is used more than [maxPerBackground] times.
     */
    private fun assignBackgrounds(
        pairCount: Int,
        numBackgrounds: Int,
        maxPerBackground: Int,
        random: Random
    ): List<Int> {
        val result = MutableList(pairCount) { 0 }
        val usage = MutableList(numBackgrounds) { 0 }
        for (i in 0 until pairCount) {
            val available = (0 until numBackgrounds).filter { usage[it] < maxPerBackground }
            require(available.isNotEmpty()) { "Cannot assign background: all at cap" }
            val bg = available.random(random)
            usage[bg]++
            result[i] = bg
        }
        return result
    }
}

/** Single card: background id (0 = no color), image code, optional hex color for UI. */
data class ImagePairCard(
    val backgroundId: Int,
    val imageId: String,
    val backgroundColorHex: String?
)

/** Shuffled deck for IMAGE_PAIR. */
data class ImagePairResult(val deck: List<ImagePairCard>)
