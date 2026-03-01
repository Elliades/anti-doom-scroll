package app.antidoomscroll.domain

/**
 * Params for ESTIMATION exercise.
 * Scoring is logarithmic: score = max(0, 1 − |ln(answer/correct)| / ln(toleranceFactor)).
 * - toleranceFactor = 1.1 → tight (math: ±10% off = 0)
 * - toleranceFactor = 2.0 → moderate (within 2× = partial credit)
 * - toleranceFactor = 10.0 → wide (orders-of-magnitude, within 10× = partial credit)
 * Category: "math" | "geography" | "science" | "history".
 */
data class EstimationParams(
    val correctAnswer: Double,
    val unit: String,
    val toleranceFactor: Double,
    val category: String,
    val hint: String? = null
) {
    init {
        require(correctAnswer > 0) { "correctAnswer must be positive" }
        require(toleranceFactor > 1.0) { "toleranceFactor must be > 1" }
        require(category in setOf("math", "geography", "science", "history")) {
            "category must be one of: math, geography, science, history"
        }
    }
}
