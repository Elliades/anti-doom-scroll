package app.antidoomscroll.application.port

/**
 * Port: resolve reflection content by key (e.g. "why-doom-scrolling").
 * Enables config- or CMS-driven content without code change.
 */
interface ReflectionContentPort {

    /**
     * Returns title and body for the content key, or null if not found.
     */
    fun getByKey(contentKey: String): ReflectionContent?

    data class ReflectionContent(val title: String, val body: String)
}
