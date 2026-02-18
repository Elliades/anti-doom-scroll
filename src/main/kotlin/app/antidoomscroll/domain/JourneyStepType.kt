package app.antidoomscroll.domain

/**
 * Type of a step in the user journey. Scalable: add new types without changing flow.
 */
enum class JourneyStepType {
    /** Open-app: 3 ultra-easy/easy exercises. */
    OPEN_APP,

    /** Reflection content (e.g. "Why am I doom scrolling?"). */
    REFLECTION,

    /** Exercise series by chapter (subject): one or more chapters in order. */
    CHAPTER_EXERCISES
}
