package app.antidoomscroll.web

import app.antidoomscroll.application.StartSessionUseCase
import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.ExerciseType
import app.antidoomscroll.domain.SessionStep
import app.antidoomscroll.web.dto.ExerciseDto
import app.antidoomscroll.web.dto.SessionResponseDto
import app.antidoomscroll.web.dto.SessionStepDto
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller: start session (reopen flow). No business logic here.
 */
@RestController
@RequestMapping("/api/session")
class SessionController(
    private val startSessionUseCase: StartSessionUseCase
) {

    @GetMapping("/start", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun startSession(
        @RequestParam(required = false) profileId: String?,
        @RequestParam(required = false) preferType: String?,
        @RequestParam(required = false) mode: String?
    ): SessionResponseDto {
        val result = if (mode?.lowercase() == "openapp") {
            startSessionUseCase.startOpenAppSession(profileId)
        } else {
            val type = preferType?.takeIf { it.isNotBlank() }?.let { runCatching { ExerciseType.valueOf(it) }.getOrNull() }
            startSessionUseCase.startSession(profileId, type)
        }
        return SessionResponseDto(
            profileId = result.profileId,
            steps = result.steps.map { stepWithCode ->
                SessionStepDto(
                    stepIndex = stepWithCode.step.stepIndex,
                    difficulty = stepWithCode.step.difficulty.name,
                    exercise = toExerciseDto(stepWithCode.step.exercise, stepWithCode.subjectCode)
                )
            },
            sessionDefaultSeconds = result.sessionDefaultSeconds,
            lowBatteryModeSeconds = result.lowBatteryModeSeconds
        )
    }

    private fun toExerciseDto(ex: Exercise, subjectCode: String?): ExerciseDto =
        ExerciseDto(
            id = ex.id,
            subjectId = ex.subjectId,
            subjectCode = subjectCode,
            type = ex.type.name,
            difficulty = ex.difficulty.name,
            prompt = ex.prompt,
            expectedAnswers = ex.expectedAnswers,
            timeLimitSeconds = ex.timeLimitSeconds,
            nBackParams = ex.nBackParams()?.let { p ->
                app.antidoomscroll.web.dto.NBackParamsDto(
                    n = p.n,
                    sequence = p.sequence,
                    matchIndices = p.matchIndices
                )
            }
        )
}
