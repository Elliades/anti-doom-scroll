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
}
