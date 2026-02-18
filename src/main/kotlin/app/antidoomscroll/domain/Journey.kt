package app.antidoomscroll.domain

/**
 * A user journey: ordered list of steps. Easily scalable: add/remove/reorder steps by config.
 * Supports jumping to any step index for "go back to precise step".
 */
data class Journey(
    val code: String,
    val name: String?,
    val steps: List<JourneyStepDef>
) {
    fun stepAt(index: Int): JourneyStepDef? = steps.getOrNull(index)
    fun stepCount(): Int = steps.size
}
