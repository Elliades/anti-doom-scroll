package app.antidoomscroll.web

import app.antidoomscroll.application.GetJourneyStepContentUseCase
import app.antidoomscroll.application.StartSessionUseCase
import app.antidoomscroll.application.port.JourneyPort
import app.antidoomscroll.domain.JourneyStepType
import app.antidoomscroll.web.dto.ChapterSeriesContentDto
import app.antidoomscroll.web.dto.JourneyDto
import app.antidoomscroll.web.dto.JourneyStepContentDto
import app.antidoomscroll.web.dto.JourneyStepDefDto
import app.antidoomscroll.web.dto.ReflectionContentDto
import app.antidoomscroll.web.dto.SessionResponseDto
import app.antidoomscroll.web.dto.SessionStepDto
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller: journey definition and step content. Scalable: add steps in config; navigate to any step by index.
 */
@RestController
@RequestMapping("/api/journey")
class JourneyController(
    private val journeyPort: JourneyPort,
    private val getJourneyStepContentUseCase: GetJourneyStepContentUseCase,
    private val exerciseDtoMapper: ExerciseDtoMapper
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getJourney(@RequestParam(required = false, defaultValue = "default") code: String): ResponseEntity<JourneyDto> {
        val fallback = fallbackDefaultJourney(code)
        val journey = journeyPort.getByCode(code)
        val dto = when {
            journey != null -> JourneyDto(
                code = journey.code,
                name = journey.name,
                steps = journey.steps.map { s ->
                    JourneyStepDefDto(
                        stepIndex = s.stepIndex,
                        type = s.type.name,
                        config = s.config
                    )
                }
            )
            fallback != null -> fallback
            else -> return ResponseEntity.notFound().build()
        }
        return ResponseEntity.ok(dto)
    }

    /** Fallback when config is missing so the app still loads for code=default. */
    private fun fallbackDefaultJourney(code: String): JourneyDto? =
        if (code == "default") JourneyDto(
            code = "default",
            name = "Default journey",
            steps = listOf(
                JourneyStepDefDto(0, "OPEN_APP", mapOf("exerciseCount" to 3)),
                JourneyStepDefDto(1, "REFLECTION", mapOf("contentKey" to "why-doom-scrolling")),
                JourneyStepDefDto(2, "CHAPTER_EXERCISES", mapOf("subjectCodes" to listOf("default", "B1")))
            )
        ) else null

    @GetMapping(
        "/steps/{stepIndex}/content",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getStepContent(
        @PathVariable stepIndex: Int,
        @RequestParam(required = false, defaultValue = "default") journeyCode: String,
        @RequestParam(required = false) profileId: String?,
        @RequestParam(required = false, defaultValue = "0") chapterIndex: Int
    ): ResponseEntity<JourneyStepContentDto> {
        val content = getJourneyStepContentUseCase.getStepContent(
            journeyCode = journeyCode,
            stepIndex = stepIndex,
            profileId = profileId,
            chapterIndex = chapterIndex
        ) ?: return ResponseEntity.notFound().build()
        val dto = when (content) {
            is GetJourneyStepContentUseCase.StepContent.OpenAppSession -> JourneyStepContentDto(
                stepIndex = stepIndex,
                type = JourneyStepType.OPEN_APP.name,
                session = toSessionResponseDto(content.profileId, content.steps, content.sessionDefaultSeconds, content.lowBatteryModeSeconds)
            )
            is GetJourneyStepContentUseCase.StepContent.Reflection -> JourneyStepContentDto(
                stepIndex = stepIndex,
                type = JourneyStepType.REFLECTION.name,
                reflection = ReflectionContentDto(title = content.title, body = content.body)
            )
            is GetJourneyStepContentUseCase.StepContent.ChapterSeries -> JourneyStepContentDto(
                stepIndex = stepIndex,
                type = JourneyStepType.CHAPTER_EXERCISES.name,
                chapterSeries = ChapterSeriesContentDto(
                    chapters = content.chapters,
                    currentChapterIndex = content.currentChapterIndex,
                    session = content.session?.let { r ->
                        toSessionResponseDto(r.profileId, r.steps, r.sessionDefaultSeconds, r.lowBatteryModeSeconds)
                    }
                )
            )
        }
        return ResponseEntity.ok(dto)
    }

    private fun toSessionResponseDto(
        profileId: String,
        steps: List<StartSessionUseCase.StepWithSubjectCode>,
        sessionDefaultSeconds: Int,
        lowBatteryModeSeconds: Int
    ): SessionResponseDto = SessionResponseDto(
        profileId = profileId,
        steps = steps.map { stepWithCode ->
            SessionStepDto(
                stepIndex = stepWithCode.step.stepIndex,
                difficulty = stepWithCode.step.difficulty.name,
                exercise = exerciseDtoMapper.toExerciseDto(stepWithCode.step.exercise, stepWithCode.subjectCode)
            )
        },
        sessionDefaultSeconds = sessionDefaultSeconds,
        lowBatteryModeSeconds = lowBatteryModeSeconds
    )

}
