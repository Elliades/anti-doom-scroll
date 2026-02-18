package app.antidoomscroll.domain

/**
 * Definition of one step in a journey. Config is type-specific and extensible.
 * - OPEN_APP: config may contain "exerciseCount" (default 3).
 * - REFLECTION: config contains "contentKey" (e.g. "why-doom-scrolling").
 * - CHAPTER_EXERCISES: config contains "subjectCodes" (list of subject/chapter codes in order).
 */
data class JourneyStepDef(
    val stepIndex: Int,
    val type: JourneyStepType,
    val config: Map<String, Any?> = emptyMap()
) {
    fun getContentKey(): String? = config["contentKey"] as? String
    fun getSubjectCodes(): List<String> = (config["subjectCodes"] as? List<*>)
        ?.mapNotNull { it?.toString() }
        ?: emptyList()
    fun getExerciseCount(): Int = (config["exerciseCount"] as? Number)?.toInt() ?: 3
}
