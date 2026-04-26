package app.antidoomscroll.domain

/**
 * Params for MATH_CHAIN exercise: a starting number followed by sequential operations.
 * The user sees operations one at a time, presses "Continue" between each, then types
 * the final result. Difficulty is controlled by step count and per-step complexity
 * (preferring more simple steps over fewer complex ones).
 */
data class MathChainParams(
    val startNumber: Int,
    val steps: List<MathChainStep>,
    val expectedAnswer: Int,
    val totalComplexity: Double
) {
    init {
        require(startNumber > 0) { "startNumber must be positive" }
        require(steps.isNotEmpty()) { "steps must not be empty" }
    }
}

data class MathChainStep(
    val operation: MathOperation,
    val operand: Int,
    val complexity: Double
)
