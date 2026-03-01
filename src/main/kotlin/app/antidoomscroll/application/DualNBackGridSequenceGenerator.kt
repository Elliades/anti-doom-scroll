package app.antidoomscroll.application

import kotlin.random.Random

/**
 * Generates dual N-Back grid sequences: position and color are tracked independently.
 * ~35% chance of a position match and ~35% chance of a color match at each eligible step.
 * The two channels are sampled independently — a step can match both, one, or neither.
 */
object DualNBackGridSequenceGenerator {

    private val DEFAULT_COLORS = listOf("#4285F4", "#EA4335", "#FBBC04", "#34A853", "#FF6D00", "#9C27B0")

    data class DualGridResult(
        val sequence: List<Map<String, Any>>,
        val matchPositionIndices: List<Int>,
        val matchColorIndices: List<Int>,
        val colors: List<String>
    )

    /**
     * @param n N-back step count (≥ 1)
     * @param gridSize Grid dimension (2–5)
     * @param colorCount Number of distinct colors to use (2–6, default 4)
     * @param sequenceLength Number of stimuli (default 12)
     * @param seed Random seed
     */
    fun generate(
        n: Int,
        gridSize: Int,
        colorCount: Int = 4,
        sequenceLength: Int = 12,
        seed: Int = 0
    ): DualGridResult {
        require(n >= 1) { "n must be >= 1" }
        require(gridSize in 2..5) { "gridSize must be 2..5" }
        require(colorCount in 2..DEFAULT_COLORS.size) { "colorCount must be 2..${DEFAULT_COLORS.size}" }
        require(sequenceLength >= n + 2) { "sequence must have at least n+2 items" }

        val cellCount = gridSize * gridSize
        val colors = DEFAULT_COLORS.take(colorCount)
        val random = Random(seed)

        val positions = mutableListOf<Int>()
        val colorSeq = mutableListOf<String>()
        val matchPositionIndices = mutableListOf<Int>()
        val matchColorIndices = mutableListOf<Int>()

        repeat(sequenceLength) { i ->
            if (i < n) {
                positions.add(random.nextInt(cellCount))
                colorSeq.add(colors[random.nextInt(colors.size)])
            } else {
                val prevPos = positions[i - n]
                val prevColor = colorSeq[i - n]

                if (random.nextFloat() < 0.35f) {
                    positions.add(prevPos)
                    matchPositionIndices.add(i)
                } else {
                    val candidates = (0 until cellCount).filter { it != prevPos }
                    positions.add(
                        if (candidates.isNotEmpty()) candidates[random.nextInt(candidates.size)]
                        else random.nextInt(cellCount)
                    )
                }

                if (random.nextFloat() < 0.35f) {
                    colorSeq.add(prevColor)
                    matchColorIndices.add(i)
                } else {
                    val candidates = colors.filter { it != prevColor }
                    colorSeq.add(
                        if (candidates.isNotEmpty()) candidates[random.nextInt(candidates.size)]
                        else colors[random.nextInt(colors.size)]
                    )
                }
            }
        }

        val sequence = positions.mapIndexed { i, pos ->
            mapOf("position" to pos, "color" to colorSeq[i])
        }

        return DualGridResult(
            sequence = sequence,
            matchPositionIndices = matchPositionIndices,
            matchColorIndices = matchColorIndices,
            colors = colors
        )
    }
}
