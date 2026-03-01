package app.antidoomscroll.application

import kotlin.random.Random

/**
 * Generates N-Back grid position sequences from parameters.
 * Each cell is a 0-based index in row-major order (0..gridSize²-1).
 * ~35% chance of a target match at each eligible position (i ≥ n).
 */
object NBackGridSequenceGenerator {

    /**
     * @param n N-back step count (≥ 1)
     * @param gridSize Grid dimension (2–5), e.g. 3 for a 3×3 grid
     * @param sequenceLength Number of stimuli (default 12)
     * @param seed Random seed (use exercise ID hash for reproducibility within a session)
     * @return Pair of (sequence of cell indices, match indices)
     */
    fun generate(
        n: Int,
        gridSize: Int,
        sequenceLength: Int = 12,
        seed: Int = 0
    ): Pair<List<Int>, List<Int>> {
        require(n >= 1) { "n must be >= 1" }
        require(gridSize in 2..5) { "gridSize must be 2..5" }
        require(sequenceLength >= n + 2) { "sequence must have at least n+2 items" }

        val cellCount = gridSize * gridSize
        val random = Random(seed)
        val sequence = mutableListOf<Int>()
        val matchIndices = mutableListOf<Int>()

        repeat(sequenceLength) { i ->
            if (i < n) {
                sequence.add(random.nextInt(cellCount))
            } else {
                val prevCell = sequence[i - n]
                if (random.nextFloat() < 0.35f) {
                    sequence.add(prevCell)
                    matchIndices.add(i)
                } else {
                    val candidates = (0 until cellCount).filter { it != prevCell }
                    sequence.add(
                        if (candidates.isNotEmpty()) candidates[random.nextInt(candidates.size)]
                        else random.nextInt(cellCount)
                    )
                }
            }
        }

        return sequence to matchIndices
    }
}
