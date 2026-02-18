package app.antidoomscroll.web.dto

/**
 * Content for one journey step. Type discriminator + payload.
 * OPEN_APP → session; REFLECTION → reflection; CHAPTER_EXERCISES → chapterSeries.
 */
data class JourneyStepContentDto(
    val stepIndex: Int,
    val type: String,
    val session: SessionResponseDto? = null,
    val reflection: ReflectionContentDto? = null,
    val chapterSeries: ChapterSeriesContentDto? = null
)

data class ReflectionContentDto(
    val title: String,
    val body: String
)

data class ChapterSeriesContentDto(
    val chapters: List<String>,
    val currentChapterIndex: Int,
    val session: SessionResponseDto? = null
)
