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

    /**
     * Subject codes for CHAPTER_EXERCISES. YAML lists and JSON may arrive as a [List] or,
     * after map-shaped binding/serialization, as an indexed map `{"0":"a","1":"b"}`.
     */
    fun getSubjectCodes(): List<String> {
        val raw = config["subjectCodes"] ?: return emptyList()
        if (raw is List<*>) return raw.mapNotNull { it?.toString() }
        if (raw is Map<*, *>) {
            return raw.entries
                .sortedBy { (key) ->
                    when (key) {
                        is Number -> key.toInt()
                        is String -> key.toIntOrNull() ?: 0
                        else -> 0
                    }
                }
                .map { it.value?.toString() ?: "" }
                .filter { it.isNotEmpty() }
        }
        return emptyList()
    }
    fun getExerciseCount(): Int = (config["exerciseCount"] as? Number)?.toInt() ?: 3
}
