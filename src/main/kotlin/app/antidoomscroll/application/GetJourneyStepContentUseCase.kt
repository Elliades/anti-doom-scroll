package app.antidoomscroll.application

import app.antidoomscroll.application.port.JourneyPort
import app.antidoomscroll.application.port.ReflectionContentPort
import app.antidoomscroll.domain.JourneyStepType
import org.springframework.stereotype.Service

/**
 * Resolves content for a given journey step. Enables scalable journey: add step types or steps without changing flow.
 * Caller can navigate to any step index (e.g. go back).
 */
@Service
class GetJourneyStepContentUseCase(
    private val journeyPort: JourneyPort,
    private val reflectionContentPort: ReflectionContentPort,
    private val startSessionUseCase: StartSessionUseCase
) {

    /**
     * Returns content for the given step, or null if journey/step not found.
     * For CHAPTER_EXERCISES, chapterIndex selects which chapter's session to return (0-based).
     */
    fun getStepContent(
        journeyCode: String,
        stepIndex: Int,
        profileId: String?,
        chapterIndex: Int = 0
    ): StepContent? {
        val journey = journeyPort.getByCode(journeyCode) ?: return null
        val stepDef = journey.stepAt(stepIndex) ?: return null
        return when (stepDef.type) {
            JourneyStepType.OPEN_APP -> {
                val result = startSessionUseCase.startOpenAppSession(profileId)
                StepContent.OpenAppSession(
                    profileId = result.profileId,
                    steps = result.steps,
                    sessionDefaultSeconds = result.sessionDefaultSeconds,
                    lowBatteryModeSeconds = result.lowBatteryModeSeconds
                )
            }
            JourneyStepType.REFLECTION -> {
                val contentKey = stepDef.getContentKey() ?: return null
                val content = reflectionContentPort.getByKey(contentKey) ?: return null
                StepContent.Reflection(title = content.title, body = content.body)
            }
            JourneyStepType.CHAPTER_EXERCISES -> {
                val subjectCodes = stepDef.getSubjectCodes()
                if (subjectCodes.isEmpty()) return StepContent.ChapterSeries(emptyList(), null, 0)
                val chapterIndexSafe = chapterIndex.coerceIn(0, subjectCodes.size - 1)
                val subjectCode = subjectCodes[chapterIndexSafe]
                val result = startSessionUseCase.startSessionForSubject(profileId, subjectCode)
                StepContent.ChapterSeries(
                    chapters = subjectCodes,
                    session = result,
                    currentChapterIndex = chapterIndexSafe
                )
            }
        }
    }

    sealed class StepContent {
        data class OpenAppSession(
            val profileId: String,
            val steps: List<StartSessionUseCase.StepWithSubjectCode>,
            val sessionDefaultSeconds: Int,
            val lowBatteryModeSeconds: Int
        ) : StepContent()

        data class Reflection(val title: String, val body: String) : StepContent()

        data class ChapterSeries(
            val chapters: List<String>,
            val session: StartSessionUseCase.SessionResult?,
            val currentChapterIndex: Int
        ) : StepContent()
    }
}
