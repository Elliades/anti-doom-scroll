package app.antidoomscroll.domain

/**
 * Configuration for a score ladder.
 * Defines levels and advancement thresholds.
 */
data class LadderConfig(
    val code: String,
    val name: String?,
    val levels: List<LadderLevel>,
    val thresholds: LadderThresholds
) {
    init {
        require(levels.isNotEmpty()) { "levels must not be empty" }
        val indices = levels.map { it.levelIndex }.sorted()
        require(indices == List(indices.size) { it }) { "levelIndex must be 0, 1, 2, ..." }
    }

    fun levelAt(index: Int): LadderLevel? = levels.firstOrNull { it.levelIndex == index }

    /**
     * Returns the number of answers needed to evaluate advancement for a given level.
     * Rules:
     * - For n-back and pair ladders: always 1 exercise regardless of level
     * - For all other ladders: 1 exercise for levels 0-4, default threshold for level 5+
     */
    fun getAnswersNeededToAdvance(levelIndex: Int): Int {
        // Special ladders: always 1 exercise regardless of level
        if (code == "nback" || code == "pair") {
            return 1
        }
        // All other ladders: 1 exercise for first 5 levels (0-4)
        if (levelIndex < 5) {
            return 1
        }
        // Level 5+: use default threshold
        return thresholds.answersNeededToAdvance
    }
}
